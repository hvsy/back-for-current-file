package com.sumenghua.BackForCurrentFile;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.ex.FileEditorWithProvider;
import com.intellij.openapi.fileEditor.ex.IdeDocumentHistory;
import com.intellij.openapi.fileEditor.impl.IdeDocumentHistoryImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

public class ProjectHelper {
    protected Project myProject;
    ProjectHelper(Project project){
        myProject = project;
    }

    public @Nullable IdeDocumentHistoryImpl.PlaceInfo getCurrentPlaceInfo() {
        FileEditorWithProvider selectedEditorWithProvider = getSelectedEditor();
        if (selectedEditorWithProvider == null) {
            return null;
        }
        return createPlaceInfo(selectedEditorWithProvider.getFileEditor(), selectedEditorWithProvider.getProvider());
    }

    @SuppressWarnings("WeakerAccess")
    protected IdeDocumentHistoryImpl.PlaceInfo createPlaceInfo(@NotNull FileEditor fileEditor, FileEditorProvider fileProvider) {
        if (!fileEditor.isValid()) {
            return null;
        }

        FileEditorManagerEx editorManager = getFileEditorManager();
        final VirtualFile file = editorManager.getFile(fileEditor);
        FileEditorState state = fileEditor.getState(FileEditorStateLevel.NAVIGATION);

        return new IdeDocumentHistoryImpl.PlaceInfo(file, state, fileProvider.getEditorTypeId(), editorManager.getCurrentWindow(), getCaretPosition(fileEditor),
                System.currentTimeMillis());
    }

    private static @Nullable RangeMarker getCaretPosition(@NotNull FileEditor fileEditor) {
        if (!(fileEditor instanceof TextEditor)) {
            return null;
        }

        Editor editor = ((TextEditor)fileEditor).getEditor();
        int offset = editor.getCaretModel().getOffset();

        return editor.getDocument().createRangeMarker(offset, offset);
    }

    protected FileEditorManagerEx getFileEditorManager() {
        return FileEditorManagerEx.getInstanceEx(myProject);
    }

    /**
     * @return currently selected FileEditor or null.
     */
    protected @Nullable FileEditorWithProvider getSelectedEditor() {
        FileEditorManagerEx editorManager = getFileEditorManager();
        VirtualFile file = editorManager != null ? editorManager.getCurrentFile() : null;
        return file == null ? null : editorManager.getSelectedEditorWithProvider(file);
    }
    private static final Logger LOG = Logger.getInstance(ProjectHelper.class);

    public void printPlaces(String label, List<IdeDocumentHistoryImpl.PlaceInfo> places, List<IdeDocumentHistoryImpl.PlaceInfo> forwards) {
        List<String> locations = ContainerUtil.map(places, placeInfo -> {
            return placeInfo.toString();
        });
        LOG.info(label + ":\n" + StringUtil.join(locations,"\n") + "\ncurrent:\n" +getCurrentPlaceInfo().toString()+  "\nforwards:\n" +
                StringUtil.join(ContainerUtil.map(forwards, forward->{
            return forward.toString();
        }),"\n"));
    }
    public IdeDocumentHistoryImpl getHistory(){
        return (IdeDocumentHistoryImpl) IdeDocumentHistoryImpl.getInstance(myProject);
    }
    public void historyLock(String name, Runnable runnable ){
        IdeDocumentHistory history = getHistory();
        try {
            Field field = history.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.setBoolean(history,true);
            try{
                runnable.run();
            }finally {
                field.setBoolean(history,false);
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return;
        }
    }
    public LinkedList<IdeDocumentHistoryImpl.PlaceInfo> getForwardPlaces(){
        IdeDocumentHistoryImpl history = getHistory();
        Class<? extends IdeDocumentHistoryImpl> klass = history.getClass();
        Field myForwardPlaces = null;
        try {
            myForwardPlaces = klass.getDeclaredField("myForwardPlaces");
            myForwardPlaces.setAccessible(true);
            LinkedList<IdeDocumentHistoryImpl.PlaceInfo> value = (LinkedList<IdeDocumentHistoryImpl.PlaceInfo>) myForwardPlaces.get(history);
            return value;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public  void addToForward(IdeDocumentHistoryImpl.PlaceInfo info){
        if(info == null) return;
        IdeDocumentHistoryImpl history = getHistory();
        Class<? extends IdeDocumentHistoryImpl> klass = history.getClass();
        Field myForwardPlaces = null;
        try {
            myForwardPlaces = klass.getDeclaredField("myForwardPlaces");
            myForwardPlaces.setAccessible(true);
            LinkedList<IdeDocumentHistoryImpl.PlaceInfo> value = (LinkedList<IdeDocumentHistoryImpl.PlaceInfo>) myForwardPlaces.get(history);
            value.add(info);
            myForwardPlaces.set(getHistory(),value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

    }
}
