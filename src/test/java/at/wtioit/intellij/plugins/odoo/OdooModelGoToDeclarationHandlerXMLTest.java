package at.wtioit.intellij.plugins.odoo;

import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.impl.PyClassImpl;
import org.jetbrains.annotations.Nullable;

public class OdooModelGoToDeclarationHandlerXMLTest extends AbstractOdooGoToDeclarationHandlerTest {

    public void testModelNameInModelAttribute() {
        doTest("odoo.addons.addon1.models.existing.Existing");
    }

    public void testModelNameInDataOeModelAttribute() {
        doTest("odoo.addons.addon1.models.existing.Existing");
    }

    public void testModelNameInRecordField() {
        doTest("odoo.addons.addon3.models.inherited.InheritedBase");
    }

    public void testModelNameInRefAttribute() {
        doTest("odoo.addons.addon1.models.existing.Existing");
    }

    @Override
    String getFileExtension() {
        return ".xml";
    }

}
