package at.wtioit.intellij.plugins.odoo;

import com.intellij.codeInsight.lookup.LookupElement;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ImportCompletionTest extends BaseOdooPluginTest {


    public void testFromOdooAddonsImport() {
        doTest("PsiDirectory:/src/odoo/addons/autocomplete");
    }

    public void testFromOdooAddonsImportOtherDirectory() {
        // this should be unique so no multiple result handling needed
        doTest(null);
    }

    public void testFromOdooAddonsAddon1ImportModels() {
        doTest("file{temp:///src/odoo/addons/addon1/models/__init__.py, Python}");
    }

    public void testFromOdooAddonsMyOtherAddonImportModels() {
        doTest("file{temp:///src/odoo/my_addons/my_other_addon/models/__init__.py, Python}");
    }

    public void testFromOdooAddonsNoDependencies() {
        doTest("PsiDirectory:/src/odoo/addons/no_dependencies");
    }

    public void testFromOdooAddonsMyOtherAddon() {
        // this should be unique so no multiple result handling needed
        doTest(null);
    }

    private void doTest(String expectedObject) {
        myFixture.configureByFile("completion/import/" + getTestName(true) + ".py");
        LookupElement[] lookupElements = myFixture.completeBasic();
        if (lookupElements == null || lookupElements.length == 1) {
            myFixture.checkResultByFile("completion/import/" + getTestName(true) + ".after.py");
        } else {
            myFixture.checkResultByFile("completion/import/" + getTestName(true) + ".after.multiple.py");
            assertContainsElements(Arrays.stream(lookupElements)
                    .map(lookupElement -> lookupElement.getObject().toString())
                    // in 2019.2 the toString of python files is file{..., Language: Python} instead of {...., Python}
                    .map(lookupElement -> lookupElement.replace(", Language: Python}", ", Python}"))
                    .collect(Collectors.toSet()), expectedObject);
        }
    }


}
