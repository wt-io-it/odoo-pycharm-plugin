package at.wtioit.intellij.plugins.odoo;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.rt.execution.junit.FileComparisonFailure;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ImportCompletionTest extends BaseOdooPluginTest {

    public void testFromOdooAddonsImport() {
        // this completion fails because there is two completion contributors suggesting odoo.addons.autocomplete
        // in the normal auto-completion the user would decide for one of them but in the tests it just is empty
        LookupElement[] result = doTest(FileComparisonFailure.class);
        String lookupStrings = Arrays.stream(result)
                .filter((b) -> b.getUserDataString().contains("=at.wtioit.intellij.plugins.odoo.OdooAddonsCompletionContributor@"))
                .map(LookupElement::getLookupString)
                .findAny()
                .orElseThrow(() -> new AssertionError("Expected completion contribution from at.wtioit.intellij.plugins.odoo.OdooAddonsCompletionContributor"));
        assertEquals("autocomplete", lookupStrings);
    }

    public void testFromOdooAddonsImportOtherDirectory() {
        doTest();
    }

    public void testFromOdooAddonsAddon1ImportModels() {
        // this completion fails because there is two completion contributors suggesting __manifest__ and models
        // in the normal auto-completion the user would decide for one of them but in the tests it just is empty
        LookupElement[] result = doTest(FileComparisonFailure.class);
        String lookupStrings = Arrays.stream(result)
                .map(LookupElement::getLookupString)
                .sorted()
                .collect(Collectors.joining(","));
        assertEquals("__manifest__,models", lookupStrings);
    }

    public void testFromOdooAddonsMyOtherAddonImportModels() {
        // this completion fails because there is two completion contributors suggesting __manifest__ and models
        // in the normal auto-completion the user would decide for one of them but in the tests it just is empty
        LookupElement[] result = doTest(FileComparisonFailure.class);
        String lookupStrings = Arrays.stream(result)
                .map(LookupElement::getLookupString)
                .sorted()
                .collect(Collectors.joining(","));
        assertEquals("__manifest__,models", lookupStrings);
    }

    public void testFromOdooAddonsMyOtherAddonImportFromSpecificModel() {
        LookupElement[] result = doTest(FileComparisonFailure.class);
        String lookupStrings = Arrays.stream(result)
                .map(LookupElement::getLookupString)
                .sorted()
                .collect(Collectors.joining(","));
        assertEquals("my_models_with_dynamic_name,my_other_model", lookupStrings);
    }

    public void testFromOdooAddonsNoDependencies() {
        // this completion fails because there is two completion contributors suggesting odoo.addons.no_dependencies
        // in the normal auto-completion the user would decide for one of them but in the tests it just is empty
        LookupElement[] result = doTest(FileComparisonFailure.class);
        String lookupStrings = Arrays.stream(result)
                .filter((b) -> b.getUserDataString().contains("=at.wtioit.intellij.plugins.odoo.OdooAddonsCompletionContributor@"))
                .map(LookupElement::getLookupString)
                .findAny()
                .orElseThrow(() -> new AssertionError("Expected completion contribution from at.wtioit.intellij.plugins.odoo.OdooAddonsCompletionContributor"));
        assertEquals("no_dependencies", lookupStrings);
    }

    public void testFromOdooAddonsMyOtherAddon() {
        doTest();
    }

    private void doTest() {
        doTest(null);
    }

    private <T> LookupElement[] doTest(Class<T> acceptableFailure) {
        myFixture.configureByFile("completion/import/" + getTestName(true) + ".py");
        LookupElement[] result = myFixture.completeBasic();
        try {
            myFixture.checkResultByFile("completion/import/" + getTestName(true) + ".after.py");
        } catch (Throwable t) {
            if (acceptableFailure == null || !acceptableFailure.isAssignableFrom(t.getClass())) {
                throw t;
            }
        }
        return result;
    }


}
