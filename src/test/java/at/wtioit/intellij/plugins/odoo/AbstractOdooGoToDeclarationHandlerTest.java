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

    void doTestExpectNoResult() {
        doTest(null);
    }

    void doTest(String expectedClassName) {
        myFixture.configureByFile("goto/" + getTestName(true) + getFileExtension());
        PsiElement elementToClick = myFixture.getFile().findElementAt(myFixture.getCaretOffset());
        @Nullable PsiElement result = handler.getGotoDeclarationTarget(elementToClick, myFixture.getEditor());
        if (expectedClassName != null) {
            assertNotNull(result);
            assertTrue(result instanceof PyClassImpl);
            assertEquals(expectedClassName, ((PyClassImpl) result).getQualifiedName());
        } else {
            assertNull(result);
        }
    }
}
