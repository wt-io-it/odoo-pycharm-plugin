package at.wtioit.intellij.plugins.odoo;

import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyFile;

public class OdooModuleGoToDeclarationHandlerXMLTest extends AbstractOdooGoToDeclarationHandlerTest {

    public void testModuleNameInRefAttribute() {
        doTest("/src/odoo/addons/addon1/__manifest__.py");
    }

    @Override
    String getFileExtension() {
        return ".xml";
    }

    @Override
    Class<? extends PsiElement> getExpectedClass() {
        return PyFile.class;
    }

    @Override
    String resultToString(PsiElement result) {
        return ((PyFile) result).getVirtualFile().getPath();
    }
}
