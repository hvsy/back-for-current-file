package com.sumenghua.BackForCurrentFile;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.ex.IdeDocumentHistory;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;


public class ClearNavigationHistoryAction extends com.intellij.openapi.actionSystem.AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = getEventProject(e);
        if (project == null) {
            return;
        }
        IdeDocumentHistory history = IdeDocumentHistory.getInstance(project);
        history.clearHistory();

    }

}
