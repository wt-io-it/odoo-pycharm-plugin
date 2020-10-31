package at.wtioit.intellij.plugins.odoo;

import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;

public interface OdooRecordPsiElementMatcherUtil {
    static boolean isOdooRecordPsiElement(PsiElement psiElement) {
        XmlAttribute attribute = PsiElementsUtil.findParent(psiElement, XmlAttribute.class, 2);
        if (attribute != null && "ref".equals(attribute.getName())) {
            return true;
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
