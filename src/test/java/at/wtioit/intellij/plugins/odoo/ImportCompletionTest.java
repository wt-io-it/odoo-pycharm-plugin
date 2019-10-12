package at.wtioit.intellij.plugins.odoo;

import org.junit.Ignore;

public class ImportCompletionTest extends BaseOdooPluginTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        forceRescan();
    }

    public void testFromOdooAddonsImport() {
        doTest();
    }

    public void testFromOdooAddonsImportOtherDirectory() {
        doTest();
    }

    // TODO we probably need to use the ImportResolver to (@see PyCharmInitializer)
    /*public void testFromOdooAddonsAddon1ImportModels() {
        doTest();
    }*/

    // TODO we probably need to use the ImportResolver to (@see PyCharmInitializer)
    /*public void testFromOdooAddonsMyOtherAddonImportModels() {
        doTest();
    }*/

    public void testFromOdooAddonsNoDependencies() {
        doTest();
    }

    public void testFromOdooAddonsMyOtherAddon() {
        doTest();
    }

    private void doTest() {
        myFixture.configureByFile("completion/import/" + getTestName(true) + ".py");
        myFixture.completeBasic();
        myFixture.checkResultByFile("completion/import/" + getTestName(true) + ".after.py");
    }


}
