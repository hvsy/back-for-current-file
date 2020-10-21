package com.sumenghua.BackForCurrentFile;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.ex.IdeDocumentHistory;
import com.intellij.openapi.fileEditor.impl.IdeDocumentHistoryImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class BackAction extends com.intellij.openapi.actionSystem.AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = getEventProject(e);
        if (project == null) {
            return;
        }
        IdeDocumentHistory history = IdeDocumentHistory.getInstance(project);
        List<IdeDocumentHistoryImpl.PlaceInfo> places = history.getBackPlaces();

        ProjectHelper helper = new ProjectHelper(project);
        IdeDocumentHistoryImpl.PlaceInfo currentPlaceInfo = helper.getCurrentPlaceInfo();
        VirtualFile currentFile= currentPlaceInfo.getFile();
        if(currentPlaceInfo == null){
            return;
        }
        List<IdeDocumentHistoryImpl.PlaceInfo> filePlaces= ContainerUtil.filter(places, placeInfo -> {
            return placeInfo.getFile().equals(currentFile);
        });
        if(filePlaces.size() != 0){
            helper.historyLock("myBackInProgress",()->{
                final IdeDocumentHistoryImpl.PlaceInfo info = ContainerUtil.getLastItem(filePlaces);
                history.removeBackPlace(info);
                history.gotoPlaceInfo(info,true);
                helper.addToForward(currentPlaceInfo);
            });
        }
    }




}
