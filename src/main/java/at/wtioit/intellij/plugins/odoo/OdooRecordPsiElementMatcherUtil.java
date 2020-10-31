package at.wtioit.intellij.plugins.odoo;

import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlToken;

import static com.intellij.psi.xml.XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN;

public interface OdooRecordPsiElementMatcherUtil {
    static boolean isOdooRecordPsiElement(PsiElement psiElement) {
        if (psiElement instanceof XmlToken && ((XmlToken) psiElement).getTokenType() == XML_ATTRIBUTE_VALUE_TOKEN) {
            XmlAttribute attribute = PsiElementsUtil.findParent(psiElement, XmlAttribute.class, 2);
            if (attribute != null && "ref".equals(attribute.getName())) {
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
