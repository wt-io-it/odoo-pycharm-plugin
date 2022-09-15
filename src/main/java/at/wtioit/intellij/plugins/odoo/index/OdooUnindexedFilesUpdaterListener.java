package at.wtioit.intellij.plugins.odoo.index;

import com.intellij.openapi.project.Project;
import com.intellij.util.indexing.UnindexedFilesUpdaterListener;
import org.jetbrains.annotations.NotNull;

public class OdooUnindexedFilesUpdaterListener implements UnindexedFilesUpdaterListener {

    @Override
    public void updateStarted(@NotNull Project project) {
        IndexWatcher.updateStarted(project);
    }

    @Override
    public void updateFinished(@NotNull Project project) {
        IndexWatcher.updateFinished(project);
    }

}
