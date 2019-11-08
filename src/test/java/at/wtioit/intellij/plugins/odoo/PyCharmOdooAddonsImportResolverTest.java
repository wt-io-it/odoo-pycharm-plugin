package at.wtioit.intellij.plugins.odoo;

import at.wtioit.intellij.plugins.odoo.pycharm.PyCharmOdooAddonsImportResolver;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.QualifiedName;
import com.jetbrains.python.psi.impl.PyImportResolver;
import com.jetbrains.python.psi.resolve.PyQualifiedNameResolveContext;
import com.jetbrains.python.psi.resolve.PyQualifiedNameResolveContextImpl;
import org.jetbrains.annotations.Nullable;

public class PyCharmOdooAddonsImportResolverTest extends BaseOdooPluginTest {

    private PyImportResolver importResolver;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        forceRescan();
        importResolver = getImportResolver();
    }

    public void testExtensionPointRegistered() {
        assertNotNull(importResolver);
    }

    private PyImportResolver getImportResolver() {
        return PyImportResolver.EP_NAME.getExtensionList().stream().filter(extension -> PyCharmOdooAddonsImportResolver.class.isInstance(extension)).findFirst().orElse(null);
    }

    public void testAddonImport() {
        doTest("odoo.addons.my_other_addon", "PsiDirectory:/src/odoo/my_addons/my_other_addon");
    }

    public void testAddonImportModelDirectory() {
        doTest("odoo.addons.my_other_addon.models", "PsiDirectory:/src/odoo/my_addons/my_other_addon/models");
    }

    public void testAddonImportModelFile() {
        doTest("odoo.addons.my_other_addon.models.my_other_model", "PyFile:my_other_model.py");
    }

    public void testFromAddonsImport() {
        doTest("my_other_addon", "PsiDirectory:/src/odoo/my_addons/my_other_addon");
    }

    public void testNonExistingAddon() {
        doTest("not_existing_addon", null);
    }

    public void testNonExistingAddonFullyQualified() {
        doTest("odoo.addons.not_existing_addon", null);
    }

    public void testNonExistingDirectory() {
        doTest("odoo.addons.my_other_addon.setup", null);
    }

    public void testNonExistingFile() {
        doTest("odoo.addons.my_other_addon.models.not_existing_file", null);
    }

    private void doTest(String addonName, String expected) {
        QualifiedName name = QualifiedName.fromDottedString(addonName);
        PyQualifiedNameResolveContext context = new PyQualifiedNameResolveContextImpl(
                myFixture.getPsiManager(),
                null,
                null,
                null,
                0,
                true,
                false,
                false,
                false,
                false);
        @Nullable PsiElement resolved = importResolver.resolveImportReference(name, context, true);
        if (expected != null) {
            assertNotNull(resolved);
            assertEquals(expected, resolved.toString());
        } else {
            assertNull(resolved);
        }
    }
}
