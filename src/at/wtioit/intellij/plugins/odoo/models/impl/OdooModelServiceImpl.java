package at.wtioit.intellij.plugins.odoo.models.impl;

import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.models.OdooModelService;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.jetbrains.python.psi.PyClass;
import com.intellij.openapi.diagnostic.Logger;

import java.util.*;

public class OdooModelServiceImpl implements OdooModelService {

    private final Project project;

    private final Logger logger = Logger.getInstance(OdooModuleService.class);

    private static final Set<String> ODOO_MODELS_DIRECTORY_NAMES = new HashSet<>(Arrays.asList("models", "model", "wizard"));
    //TODO add all base classes
    private static final Set<String> ODOO_MODEL_BASE_CLASS_NAMES = new HashSet<>(Arrays.asList("odoo.models.Model", "odoo.models.BaseModel"));

    public OdooModelServiceImpl(Project project) {
        this.project = project;
    }

    // TODO use a set?
    Collection<OdooModel> modelsCache;

    @Override
    public Iterable<OdooModel> getModels() {
        if (modelsCache == null) {
            OdooModuleService moduleService = ServiceManager.getService(project, OdooModuleService.class);
            ArrayList<OdooModel> models = new ArrayList<>();
            for (OdooModule module : moduleService.getModules()) {
                for (PsiElement child : module.getDirectory().getChildren()) {
                    if (child instanceof PsiFileSystemItem) {
                        String childName = ((PsiFileSystemItem) child).getName();
                        if (child instanceof PsiDirectory && ODOO_MODELS_DIRECTORY_NAMES.contains(childName)) {
                            logger.warn("Scanning " + ((PsiDirectory) child).getVirtualFile());
                            for (PsiElement file : child.getChildren()) {
                                if (file instanceof PsiFile) {
                                    ((PsiFile) file).getName();
                                    for (PsiElement pyline : file.getChildren()) {
                                        if (isOdooModelDefinition(pyline)) {
                                            logger.warn("Found " + pyline + " in " + ((PsiFile) file).getName());
                                            models.add(new OdooModelImpl(pyline, module));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            modelsCache = models;
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
