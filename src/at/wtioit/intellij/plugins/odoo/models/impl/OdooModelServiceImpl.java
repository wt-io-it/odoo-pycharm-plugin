package at.wtioit.intellij.plugins.odoo.models.impl;

import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.models.OdooModelService;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.jetbrains.python.psi.PyClass;
import com.intellij.openapi.diagnostic.Logger;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class OdooModelServiceImpl implements OdooModelService {

    private final Project project;

    private final Logger logger = Logger.getInstance(OdooModuleService.class);

    private static final Set<String> ODOO_MODELS_DIRECTORY_NAMES = new HashSet<>(Arrays.asList("models", "model", "wizard", "report"));
    //TODO add all base classes
    private static final Set<String> ODOO_MODEL_BASE_CLASS_NAMES = new HashSet<>(Arrays.asList("odoo.models.Model", "odoo.models.BaseModel"));

    public OdooModelServiceImpl(Project project) {
        this.project = project;
    }

    Collection<OdooModel> modelsCache = new CopyOnWriteArraySet<>();
    Collection<VirtualFile> scannedFiles = new CopyOnWriteArraySet<>();
    private boolean scanFinished = false;

    @Override
    public Iterable<OdooModel> getModels() {
        if (!scanFinished) {
            ProgressIndicator progressIndicator = ProgressIndicatorProvider.getInstance().getProgressIndicator();
            OdooModuleService moduleService = ServiceManager.getService(project, OdooModuleService.class);
            List<OdooModule> modules = moduleService.getModules();
            if (progressIndicator != null) {
                progressIndicator.setIndeterminate(false);
                progressIndicator.setText("Scanning odoo addons for models");
            }
            for (OdooModule module : modules) {
                if (progressIndicator != null) progressIndicator.setFraction(modules.size() * 1.0d / modules.indexOf(module));
                for (PsiElement child : module.getDirectory().getChildren()) {
                    if (child instanceof PsiFileSystemItem) {
                        String childName = ((PsiFileSystemItem) child).getName();
                        if (child instanceof PsiDirectory && ODOO_MODELS_DIRECTORY_NAMES.contains(childName)) {
                            VirtualFile virtualFile = ((PsiDirectory) child).getVirtualFile();
                            if (!scannedFiles.contains(virtualFile)) {
                                logger.debug("Scanning " + virtualFile);
                                ArrayList<OdooModel> models = new ArrayList<>();
                                for (PsiElement file : child.getChildren()) {
                                    if (file instanceof PsiFile) {
                                        ((PsiFile) file).getName();
                                        for (PsiElement pyline : file.getChildren()) {
                                            if (isOdooModelDefinition(pyline)) {
                                                logger.debug("Found " + pyline + " in " + ((PsiFile) file).getName());
                                                models.add(new OdooModelImpl(pyline, module));
                                            }
                                        }
                                    }
                                }
                                modelsCache.addAll(models);
                                scannedFiles.add(virtualFile);
                            }
                        }
                    }
                }
            }
            scanFinished = true;
            logger.debug("Finished scanning Odoo Modules");
            if (progressIndicator != null) progressIndicator.setFraction(1.0d);
        }
        if (modelsCache == null) {
            return Collections.emptyList();
        } else {
            return modelsCache;
        }
    }

    private boolean isOdooModelDefinition(PsiElement pyline) {
        if (pyline instanceof PyClass) {
            for (PyClass superClass : ((PyClass) pyline).getSuperClasses(null)) {
                if (ODOO_MODEL_BASE_CLASS_NAMES.contains(superClass.getQualifiedName())) {
                    return true;
                } else {
                    continue;
                }
            }
        }
        return false;
    }
}
