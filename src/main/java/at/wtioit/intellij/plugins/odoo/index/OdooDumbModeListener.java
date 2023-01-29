package at.wtioit.intellij.plugins.odoo.index;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;

public class OdooDumbModeListener implements DumbService.DumbModeListener {

    private final Project project;

    public OdooDumbModeListener(Project project) {
        this.project = project;
    }

    @Override
    public void enteredDumbMode() {
        // This is never really called (in PyCharm <2022.3.2) because the listener is only initialised when we are
        // in dumb mode
    }

    @Override
    public void exitDumbMode() {
        IndexWatcher.dumbModeLeft(project);
    }
}
