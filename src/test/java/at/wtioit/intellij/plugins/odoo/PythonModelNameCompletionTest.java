package at.wtioit.intellij.plugins.odoo;

public class PythonModelNameCompletionTest extends BaseOdooPluginTest {

    public void testModelNameInName() {
        doTest();
    }

    public void testModelNameInInherit() {
        doTest();
    }

    public void testModelNameInEnv() {
        doTest();
    }

    public void testModelNameInVariableEnv() {
        doTest();
    }

    public void testModelNameInMultipleInherit() {
        // TODO not yet implemented
        //doTest();
    }

    public void testModelNameInInheritWildcard() {
        doTest();
    }

    public void testModelNameInOne2manyField() {
        doTest();
    }

    public void testModelNameInOne2manyFieldNamedArgs() {
        doTest();
    }

    public void testModelNameInOne2manyFieldNamedArgsHelpNoAutocomplete() {
        doTest();
    }

    public void testModelNameInOne2manyFieldSecondArgNoAutocomplete() {
        doTest();
    }

    public void testModelNameInMany2oneField() {
        doTest();
    }

    public void testModelNameInMany2manyField() {
        doTest();
    }

    private void doTest() {
        myFixture.configureByFile("completion/pythonModelName/" + getTestName(true) + ".py");
        myFixture.completeBasic();
        myFixture.checkResultByFile("completion/pythonModelName/" + getTestName(true) + ".after.py");
    }


}
