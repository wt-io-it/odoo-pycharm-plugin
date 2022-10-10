package at.wtioit.intellij.plugins.odoo;

import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlToken;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.intellij.psi.xml.XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN;

public interface OdooRecordPsiElementMatcherUtil {

    List<String> ODOO_RECORD_REF_ATTRIBUTES = Collections.unmodifiableList(Arrays.asList(
            "ref",
            "inherit_id",
            "groups", // TODO handle comma seperated groups
            "action",
            "t-call",
            "t-call-assets",
            "t-extend"));

    static boolean isOdooRecordPsiElement(PsiElement psiElement) {
        if (psiElement instanceof XmlToken && ((XmlToken) psiElement).getTokenType() == XML_ATTRIBUTE_VALUE_TOKEN) {
            XmlAttribute attribute = PsiElementsUtil.findParent(psiElement, XmlAttribute.class, 2);
            if (attribute != null && ODOO_RECORD_REF_ATTRIBUTES.contains(attribute.getName())) {
                return true;
            }
        }
        return false;
    }

    static boolean holdsOdooRecordReference(PsiElement psiElement) {
        XmlAttribute attribute = PsiElementsUtil.findParent(psiElement, XmlAttribute.class, 2);
        if (attribute != null && "eval".equals(attribute.getName())) {
            if (psiElement.getText().contains("ref(")) {
                return true;
            }
        }
        return false;
    }
}
