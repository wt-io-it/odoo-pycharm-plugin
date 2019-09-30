package at.wtioit.intellij.plugins.odoo.models.impl;

import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.python.psi.PyCallExpression;
import com.jetbrains.python.psi.PyListLiteralExpression;
import com.jetbrains.python.psi.impl.PyStringLiteralExpressionImpl;

import java.util.Collections;
import java.util.List;

import com.intellij.openapi.diagnostic.Logger;

public class OdooModelImpl implements OdooModel {
    private final PsiElement pyline;
    private String name;
    private boolean nameDetected = false;
    private final Logger logger = Logger.getInstance(OdooModelImpl.class);
    private final List<OdooModule> modules;

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
                    } else if (valueChild instanceof PyCallExpression) {
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
}
