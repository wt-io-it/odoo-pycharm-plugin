package at.wtioit.intellij.plugins.odoo;

public class PythonRecordXmlIdCompletionTest extends BaseOdooPluginTest {

    public void testRecordXmlIdInRef() {
        doTest();
    }

    public void testRecordXmlIdInClsRef() {
        doTest();
    }

    public void testRecordXmlIdInRequestRef() {
        doTest();
    }

    public void testRecordXmlIdInSudoRef() {
        doTest();
    }

    public void testRecordXmlIdInRefWithSpace() {
        doTest();
    }

    public void testRecordXmlIdInCustomEnvRef() {
        doTest();
    }

    private void doTest() {
        myFixture.configureByFile("completion/pythonRecordXmlId/" + getTestName(true) + ".py");
        myFixture.completeBasic();
        myFixture.checkResultByFile("completion/pythonRecordXmlId/" + getTestName(true) + ".after.py");
    }


}
