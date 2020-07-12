package at.wtioit.intellij.plugins.odoo.models.impl;

import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.models.OdooModelUtil;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import com.jetbrains.python.psi.types.TypeEvalContext;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class OdooModelImpl implements OdooModel {
    private final PsiElement pyline;
    private String name;
    private boolean nameDetected = false;
    private final Logger logger = Logger.getInstance(OdooModelImpl.class);
    private Set<OdooModule> modules;
    private PyResolveContext resolveContext;

    public OdooModelImpl(PsiElement pyline, OdooModule module) {
        this.pyline = pyline;
        modules = Collections.singleton(module);
    }

    @Override
    public String getName() {
        if (!nameDetected) {
            name = OdooModelUtil.detectName(pyline, this::getResolveContext);
            nameDetected = true;
        }
        if (name == null) {
            logger.debug("Name not detected for: " + pyline + " in " + pyline.getContainingFile().getVirtualFile());
        }
        return name;
    }

    private PyResolveContext getResolveContext() {
        if (resolveContext == null) {
            TypeEvalContext evalContext = TypeEvalContext.codeAnalysis(pyline.getContainingFile().getProject(), pyline.getContainingFile());
            resolveContext = PyResolveContext.defaultContext().withTypeEvalContext(evalContext);
        }
        return resolveContext;
    }

    @Override
    public Set<OdooModule> getModules() {
        return modules;
    }

    @Override
    public PsiElement getDefiningElement() {
        return pyline;
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
