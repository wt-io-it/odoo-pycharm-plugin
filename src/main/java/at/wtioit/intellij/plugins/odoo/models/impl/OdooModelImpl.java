package at.wtioit.intellij.plugins.odoo.models.impl;

import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.models.OdooModelService;
import at.wtioit.intellij.plugins.odoo.models.index.OdooModelDefinition;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.python.psi.PyClass;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class OdooModelImpl implements OdooModel {
    private final String name;
    private final Collection<VirtualFile> definingFiles;
    private final Project project;
    private Set<OdooModule> modules;
    private PsiElement element;

    public OdooModelImpl(String modelName, Collection<VirtualFile> files, Project project) {
        name = modelName;
        definingFiles = files;
        this.project = project;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<OdooModule> getModules() {
        return modules;
    }

    @NotNull
    @Override
    public PsiElement getDefiningElement() {
        if (definingFiles.size() == 1) {
            if (element == null) {
                PsiFile psiFile = PsiManager.getInstance(project).findFile(definingFiles.iterator().next());
                for (PsiElement pyline : psiFile.getChildren()) {
                    if (OdooModelService.isOdooModelDefinition(pyline)) {
                        OdooModelDefinition model = new OdooModelDefinition((PyClass) pyline);
                        if (model.getName().equals(name)) {
                            element = pyline;
                            return pyline;
                        }
                    }
                }
            }
            return element;
        }
        throw new RuntimeException("TODO");
    }

    public void setModules(Set<OdooModule> modules) {
        this.modules = Collections.unmodifiableSet(modules);
    }

    @Override
    public OdooModule getBaseModule() {
        if (modules.size() == 1) {
            return modules.iterator().next();
        } else {
            for (OdooModule module : modules) {
                for (OdooModel moduleModel : module.getModels()) {
                    if (moduleModel.getDefiningElement() == getDefiningElement()) {
                        return module;
                    }
                }
            }
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof OdooModelImpl) {
            if (((OdooModelImpl) o).getDefiningElement().equals(getDefiningElement())) {
                String oName = ((OdooModelImpl) o).getName();
                if (oName != null) {
                    return oName.equals(getName());
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDefiningElement(), getName());
    }
}
