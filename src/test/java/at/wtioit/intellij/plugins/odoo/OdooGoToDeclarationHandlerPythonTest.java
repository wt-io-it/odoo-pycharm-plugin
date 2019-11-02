package at.wtioit.intellij.plugins.odoo;

public class OdooGoToDeclarationHandlerPythonTest extends AbstractOdooGoToDeclarationHandlerTest {

    @Override
    String getFileExtension() {
        return ".py";
    }

    public void testModelName() {
        doTest("odoo.addons.addon3.models.inherited.Inherited");
    }

    public void testModelNameEnv() {
        doTest("odoo.addons.addon1.models.existing.Existing");
    }

    public void testModelNameInherit() {
        doTest("odoo.addons.addon1.models.existing.Existing");
    }

    public void testModelNameM2NField() {
        doTest("odoo.addons.addon1.models.existing.Existing");
    }

    public void testModelNameM2NFieldKeywordArgs() {
        doTest("odoo.addons.addon1.models.existing.Existing");
    }

    public void testModelNameNotFound() {
        doTestExpectNoResult();
    }
}
