package at.wtioit.intellij.plugins.odoo;

import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.jetbrains.python.psi.*;

import java.util.Arrays;
import java.util.List;

import static at.wtioit.intellij.plugins.odoo.PsiElementsUtil.findParent;

public interface OdooModelPsiElementMatcherUtil {

    static List<String> ODOO_MODEL_XML_ATTRIBUTE_NAMES = Arrays.asList("model", "data-oe-model");
    static List<String> ODOO_MODEL_XML_FIELD_ATTRIBUTE_NAMES = Arrays.asList("model", "res_model", "src_model");

    /**
     * Checks if {@link PsiElement} is supposed to contain an Odoo model name e.g. `_name = 'ir.ui.view'`
     * @param element - element to check
     * @return `true` if element is supposed to contain an Odoo model name
     */
    static boolean isOdooModelPsiElement(PsiElement element) {
        PyCallExpression pyCallExpression = findParent(element, PyCallExpression.class, 3);
        if (pyCallExpression != null && pyCallExpression.getCallee() != null) {
            String fieldType = pyCallExpression.getCallee().getText();
            if (OdooModel.ODOO_MODEL_NAME_FIELD_NAMES.contains(fieldType)) {
                PyArgumentList arguments = findParent(element, PyArgumentList.class, 2);
                if (arguments != null) {
                    PsiElement firstArgument = arguments.getChildren()[0];
                    if (firstArgument == element.getParent()) {
                        // fields.{N}2{M}(..., ) first argument
                        return true;
                    }
                }
            }
        }

        PyKeywordArgument pyKeywordArgument = findParent(element, PyKeywordArgument.class, 2);
        pyCallExpression = findParent(element, PyCallExpression.class, 4);
        if (pyKeywordArgument != null && pyCallExpression != null && pyCallExpression.getCallee() != null) {
            String fieldType = pyCallExpression.getCallee().getText();
            if (OdooModel.ODOO_MODEL_NAME_FIELD_NAMES.contains(fieldType)) {
                String keyword = pyKeywordArgument.getKeyword();
                if (OdooModel.ODOO_MODEL_NAME_FIELD_KEYWORD_ARGUMENTS.contains(keyword)) {
                    // fields.{N}2{M}(comodel_name=..., )
                    return true;
                }
            }
        }

        PyAssignmentStatement assignmentStatement = findParent(element, PyAssignmentStatement.class, 4);
        if (assignmentStatement != null && element.getParent() instanceof PyStringLiteralExpression) {
            String variableName = assignmentStatement.getFirstChild().getText();
            if (OdooModel.ODOO_MODEL_NAME_VARIABLE_NAME.contains(variableName)) {
                // _name and _inherit fields
                return true;
            } else if (OdooModel.ODOO_MODEL_NAME_VARIABLE_NAME_IN_DICT_KEY.contains(variableName)
                    && findParent(element, PyKeyValueExpression.class, 2) != null) {
                // keys for _inherits fields
                return true;
            }
        }

        PySubscriptionExpression pySubscriptionExpression = findParent(element, PySubscriptionExpression.class, 2);
        if (pySubscriptionExpression != null && "env".equals(pySubscriptionExpression.getRootOperand().getName())) {
            // handle self.env[...] and request.env[...]
            return true;
        }

        XmlAttribute xmlAttribute = findParent(element, XmlAttribute.class, 2);
        if (xmlAttribute != null && ODOO_MODEL_XML_ATTRIBUTE_NAMES.contains(xmlAttribute.getName())) {
            // xml attributes model="...", data-oe-model="..."
            return true;
        }
        XmlTag xmlTag = findParent(element, XmlTag.class, 2);
        if (xmlTag != null && "field".equals(xmlTag.getName()) && ODOO_MODEL_XML_FIELD_ATTRIBUTE_NAMES.contains(xmlTag.getAttributeValue("name"))) {
            // xml <field name="model">...</field> (also res_model and src_model)
            return true;
        }

        return false;
    }
}
