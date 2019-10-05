package at.wtioit.intellij.plugins.odoo.models.impl;

import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.python.psi.PyBinaryExpression;
import com.jetbrains.python.psi.PyCallExpression;
import com.jetbrains.python.psi.PyListLiteralExpression;
import com.jetbrains.python.psi.impl.PyStringLiteralExpressionImpl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.intellij.openapi.diagnostic.Logger;

public class OdooModelImpl implements OdooModel {
    private final PsiElement pyline;
    private String name;
    private boolean nameDetected = false;
    private final Logger logger = Logger.getInstance(OdooModelImpl.class);
    private List<OdooModule> modules;

    public OdooModelImpl(PsiElement pyline, OdooModule module) {
        this.pyline = pyline;
        // TODO link other modules inheriting the same model too
        modules = Collections.singletonList(module);
    }

    @Override
    public String getName() {
        if (!nameDetected) {
            for (PsiElement statement : pyline.getChildren()[1].getChildren()) {
                // TODO _name can overwrite _inherit (i think)
                if (OdooModel.ODOO_MODEL_NAME_VARIABLE_NAME.contains(statement.getFirstChild().getText())) {
                    PsiElement valueChild = statement.getLastChild();
                    while (valueChild instanceof PsiComment || valueChild instanceof PsiWhiteSpace ) {
                        valueChild = valueChild.getPrevSibling();
                    }
                    if (valueChild instanceof PyStringLiteralExpressionImpl) {
                        name = ((PyStringLiteralExpressionImpl) valueChild).getStringValue();
                        break;
                    } else if (valueChild instanceof PyListLiteralExpression) {
                        //firstChild() somehow returns the bracket
                        PsiElement firstChild = valueChild.getChildren()[0];
                        if (firstChild instanceof PyStringLiteralExpressionImpl) {
                            name = ((PyStringLiteralExpressionImpl) firstChild).getStringValue();
                            break;
                        } else {
                            logger.error("Unknown string value class: " + valueChild.getClass());
                        }
                    } else if (valueChild instanceof PyCallExpression || valueChild instanceof PyBinaryExpression) {
                        logger.debug("Cannot detect string value for class: " + valueChild.getClass());
                    } else {
                        logger.error("Unknown string value class: " + valueChild.getClass());
                    }
                }
            }
            nameDetected = true;
        }
        if (name == null) {
            logger.debug("Name not detected for: " + pyline + " in " + pyline.getContainingFile().getVirtualFile());
        }
        return name;
    }

    @Override
    public List<OdooModule> getModules() {
        return modules;
    }

    @Override
    public PsiElement getDefiningElement() {
        return pyline;
    }

    public void setModules(List<OdooModule> modules) {
        this.modules = Collections.unmodifiableList(modules);
    }

    @Override
    public OdooModule getBaseModule() {
        if (modules.size() == 1) {
            return modules.get(0);
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
