package at.wtioit.intellij.plugins.odoo;

public class OdooModelGoToDeclarationHandlerPythonTest extends AbstractOdooGoToDeclarationHandlerTest {

    @Override
    String getFileExtension() {
        return ".py";
    }

    public void testModelName() {
        doTest("odoo.addons.addon3.models.inherited.InheritedBase");
    }

    public void testModelNameWithSingleClassImport() {
        doTest("odoo.addons.addon3.models.inherited.InheritedBase");
    }

    public void testModelNameEnv() {
        doTest("odoo.addons.addon1.models.existing.Existing");
    }

    public void testModelNameEnvInTest() {
        doTest("odoo.addons.addon1.models.existing.Existing");
    }

    public void testModelNameEnvInTest2() {
        doTest("odoo.addons.addon1.models.existing.Existing");
    }

    public void testModelNameEnvInTest3() {
        doTest("odoo.addons.addon1.models.existing.Existing");
    }

    public void testModelNameEnvInController() {
        doTest("odoo.addons.addon1.models.existing.Existing");
    }

    public void testModelNameEnvInControllerWithHttpImport() {
        doTest("odoo.addons.addon1.models.existing.Existing");
    }

    public void testModelNameEnvInSelectionFunction() {
        doTest("odoo.addons.addon1.models.existing.Existing");
    }

    public void testModelNameEnvInNormalFunctionNoResult() {
        doTestExpectNoResult();
    }

    public void testModelNameEnvWildcard() {
        doTest("odoo.addons.addon1.models.existing.Wildcard");
    }

    public void testModelNameEnvWildcardFallback() {
        doTest("odoo.addons.addon1.models.existing.WildcardDefault");
    }

    public void testModelNameEnvWildcardPreferExplicit() {
        doTest("odoo.addons.addon1.models.existing.NonWildcardDefault");
    }

    public void testModelNameEnvWildcardFallbackFormat() {
        doTest("odoo.addons.addon1.models.existing.WildcardFormatString");
    }

    public void testModelNameEnvWildcardPreferExplicitFormat() {
        doTest("odoo.addons.addon1.models.existing.NonWildcardFormatString");
    }

    public void testModelNameInherit() {
        doTest("odoo.addons.addon1.models.existing.Existing");
    }

    public void testModelNameM2NField() {
        doTest("odoo.addons.addon1.models.existing.Existing");
    }

    public void testModelNameM2NFieldNoNameYet() {
        doTestExpectNoResult();
    }

    public void testModelNameM2NFieldKeywordArgs() {
        doTest("odoo.addons.addon1.models.existing.Existing");
    }

    public void testModelNameM2NFieldKeywordArgsRelatedNoResult() {
        doTestExpectNoResult();
    }

    public void testModelNameNotFound() {
        doTestExpectNoResult();
    }

    // TODO test class names in dumb mode
}
