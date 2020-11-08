package at.wtioit.intellij.plugins.odoo;

import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.impl.PyClassImpl;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractOdooGoToDeclarationHandlerTest extends BaseOdooPluginTest {

    private OdooGoToDeclarationHandler handler;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        handler =  new OdooGoToDeclarationHandler();
    }

    abstract String getFileExtension();

    Class<? extends PsiElement> getExpectedClass() {
        return PyClassImpl.class;
    }

    String resultToString(PsiElement result) {
        return ((PyClassImpl) result).getQualifiedName();
    }

    void doTestExpectNoResult() {
        doTest(null);
    }

    void doTest(String expectedResultName) {
        myFixture.configureByFile("goto/" + getTestName(true) + getFileExtension());
        PsiElement elementToClick = myFixture.getFile().findElementAt(myFixture.getCaretOffset());
        @Nullable PsiElement[] results = handler.getGotoDeclarationTargets(elementToClick, myFixture.getCaretOffset(), myFixture.getEditor());
        if (expectedResultName != null) {
            assertNotNull(results);
            assertTrue(results.length >= 1);
            PsiElement result = results[0];
            assertNotNull(result);
            assertTrue(getExpectedClass().isAssignableFrom(result.getClass()));
            assertEquals(expectedResultName, resultToString(result));
        } else {
            assertNull(results);
        }
    }
}
