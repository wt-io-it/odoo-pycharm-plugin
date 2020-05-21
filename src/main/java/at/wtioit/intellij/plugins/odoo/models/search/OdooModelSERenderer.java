package at.wtioit.intellij.plugins.odoo.models.search;

import com.intellij.ide.util.PsiElementListCellRenderer;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyClass;
import org.jetbrains.annotations.Nullable;

public class OdooModelSERenderer extends PsiElementListCellRenderer<OdooModelPsiElement> {

    @Override
    public String getElementText(OdooModelPsiElement element) {
        return element.getName();
    }

    @Override
    protected @Nullable String getContainerText(OdooModelPsiElement element, String name) {
        // TODO shorten if necessary
        ItemPresentation presentation = element.getContainingFile().getPresentation();
        if (presentation != null) {
            PsiElement definingElement = element.getDefiningElement();
            if (definingElement instanceof PyClass) {
                return presentation.getLocationString().replace("(", "(" + ((PyClass) definingElement).getName() + " in ");
            }
            return presentation.getLocationString();
        }
        return null;
    }

    @Override
    protected int getIconFlags() {
        return 0;
    }
}
