package at.wtioit.intellij.plugins.odoo.models.impl;

import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.models.OdooModelService;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import com.intellij.openapi.components.ServiceManager;
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
    private static final Set<String> ODOO_MODEL_BASE_CLASS_NAMES = new HashSet<>(Arrays.asList("odoo.models.Model",
            "odoo.models.BaseModel", "odoo.models.TransientModel"));

    public OdooModelServiceImpl(Project project) {
        this.project = project;
    }

    Collection<OdooModel> modelsCache = new CopyOnWriteArraySet<>();
    Map<String, OdooModel> modelsCacheByName = Collections.emptyMap();
    Map<PsiElement, OdooModel> modelsCacheByElement = Collections.emptyMap();
    Collection<VirtualFile> scannedFiles = new CopyOnWriteArraySet<>();
    private boolean scanFinished = false;

    @Override
    public Iterable<OdooModel> getModels() {
        if (!scanFinished) {
            OdooModuleService moduleService = ServiceManager.getService(project, OdooModuleService.class);
            for (OdooModule module : moduleService.getModules()) {
                for (PsiElement child : module.getDirectory().getChildren()) {
                    if (child instanceof PsiFileSystemItem) {
                        String childName = ((PsiFileSystemItem) child).getName();
                        if (child instanceof PsiDirectory && ODOO_MODELS_DIRECTORY_NAMES.contains(childName)) {
                            VirtualFile virtualFile = ((PsiDirectory) child).getVirtualFile();
                            if (!scannedFiles.contains(virtualFile)) {
                                logger.debug("Scanning " + virtualFile);
                                ArrayList<OdooModel> models = new ArrayList<>();
                                HashMap<String, OdooModel> modelsByName = new HashMap<>(modelsCacheByName);
                                HashMap<PsiElement, OdooModel> modelsByElement = new HashMap<>(modelsCacheByElement);
                                for (PsiElement file : child.getChildren()) {
                                    if (file instanceof PsiFile) {
                                        for (PsiElement pyline : file.getChildren()) {
                                            if (isOdooModelDefinition(pyline)) {
                                                logger.debug("Found " + pyline + " in " + ((PsiFile) file).getName());
                                                OdooModelImpl model = new OdooModelImpl(pyline, module);
                                                ArrayList<OdooModel> moduleModels = new ArrayList<>(module.getModels());
                                                moduleModels.add(model);
                                                module.setModels(Collections.unmodifiableList(moduleModels));
                                                modelsByElement.put(pyline, model);
                                                if (!dependencyHasModel(module, model.getName())) {
                                                    // only add the model if none of our dependencies has already defined it
                                                    logger.debug("Adding model: " + model.getName() + " from " + module.getName());
                                                    models.add(model);
                                                    modelsByName.put(model.getName(), model);
                                                } else {
                                                    List<OdooModule> modelModules = new ArrayList<>(model.getModules());
                                                    modelModules.add(module);
                                                    model.setModules(modelModules);
                                                }
                                            }
                                        }
                                    }
                                }
                                modelsCache.addAll(models);
                                modelsCacheByName = Collections.unmodifiableMap(modelsByName);
                                modelsCacheByElement = Collections.unmodifiableMap(modelsByElement);
                                scannedFiles.add(virtualFile);
                            }
                        }
                    }
                }
            }
            scanFinished = true;
        }
        if (modelsCache == null) {
            return Collections.emptyList();
        } else {
            return modelsCache;
        }
    }

    private boolean dependencyHasModel(OdooModule module, String modelName) {
        if (modelName == null) {
            return false;
        }
        for (OdooModule dependency : module.getDependencies()) {
            logger.debug("Checking " + dependency.getName() + "  for " + modelName);
            for (OdooModel modelInDependency : dependency.getModels()) {
                // TODO replace by getModels() by map
                if (modelName.equals(modelInDependency.getName())) {
                    return true;
                }
            }
            if (dependencyHasModel(dependency, modelName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public OdooModel getModel(String modelName) {
        if (!scanFinished) {
            getModels();
        }
        if (modelsCacheByName.containsKey(modelName)) {
            return modelsCacheByName.get(modelName);
        }
        return null;
    }

    @Override
    public Iterable<String> getModelNames() {
        if (!scanFinished) {
            getModels();
        }
        return modelsCacheByName.keySet();
    }

    @Override
    public OdooModel getModelForElement(PsiElement psiElement) {
        if (!scanFinished) {
            getModels();
        }
        return modelsCacheByElement.get(psiElement);
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
