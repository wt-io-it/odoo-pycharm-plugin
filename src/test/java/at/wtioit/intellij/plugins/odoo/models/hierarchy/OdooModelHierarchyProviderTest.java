package at.wtioit.intellij.plugins.odoo.models.hierarchy;

import at.wtioit.intellij.plugins.odoo.BaseOdooPluginTest;
import at.wtioit.intellij.plugins.odoo.PsiElementsUtil;
import com.intellij.ide.hierarchy.HierarchyBrowser;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.hierarchy.HierarchyProvider;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.lang.LanguageExtensionPoint;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyClass;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

import static com.intellij.ide.hierarchy.TypeHierarchyBrowserBase.SUBTYPES_HIERARCHY_TYPE;
import static com.intellij.ide.hierarchy.TypeHierarchyBrowserBase.TYPE_HIERARCHY_TYPE;

public class OdooModelHierarchyProviderTest extends BaseOdooPluginTest {

    HierarchyProvider hierarchyProvider;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        hierarchyProvider = getHierarchyProvider();
    }

    public void testOdooModelHierarchyProviderRegistered() {
        assertNotNull(hierarchyProvider);
    }

    public void testNonModelClass() {
        doTest();
    }

    public void testModelClass() {
        doTest();
        // TODO this should also display the Odoo Model Hierarchy (not just the python class hierarchy)
        // but currently it doesn't
    }

    public void testExistingModelClass() {
        doTest(() -> {
            PsiElement element = myFixture.configureByFile("odoo/addons/addon1/models/existing.py");
            return Arrays.stream(element.getChildren()).filter(child -> child instanceof PyClass).findFirst().orElseThrow(AssertionError::new);
        });
    }

    public void testInheritedModelClass() {
        doTest(() -> {
            PsiElement element = myFixture.configureByFile("odoo/addons/addon3/models/inherited.py");
            return Arrays.stream(element.getChildren()).filter(child -> child instanceof PyClass).findFirst().orElseThrow(AssertionError::new);
        });
    }

    public void testInheritedModelClassSubtype() {
        doTest(() -> {
            PsiElement element = myFixture.configureByFile("odoo/addons/addon3/models/inherited.py");
            return Arrays.stream(element.getChildren()).filter(child -> child instanceof PyClass).findFirst().orElseThrow(AssertionError::new);
        }, SUBTYPES_HIERARCHY_TYPE);
    }

    private void doTest() {
        doTest(() -> {
            myFixture.configureByFile("hierarchy/" + getTestName(true) + ".py");
            return myFixture.getFile().findElementAt(myFixture.getCaretOffset());
        });
    }

    private void doTest(Supplier<PsiElement> psiElement) {
        doTest(psiElement, TYPE_HIERARCHY_TYPE);
    }

    private void doTest(Supplier<PsiElement> psiElement, String hierarchyType) {
        PsiElement elementForHierarchy = psiElement.get();
        assertNotNull(elementForHierarchy);
        PyClass pyClass;
        if (elementForHierarchy instanceof PyClass) {
            pyClass = (PyClass) elementForHierarchy;
        } else {
            pyClass = PsiElementsUtil.findParent(elementForHierarchy, PyClass.class);
        }
        assertNotNull(pyClass);
        HierarchyBrowser hierarchyBrowser = hierarchyProvider.createHierarchyBrowser(pyClass);
        assertNotNull(hierarchyBrowser);
        HierarchyTreeStructure hierarchyTreeStructure = ((OdooModelTypesHierarchyBrowser) hierarchyBrowser).createHierarchyTreeStructure(hierarchyType, pyClass);
        assertNotNull(hierarchyTreeStructure);
        String tree = computeTree(hierarchyTreeStructure, (HierarchyNodeDescriptor) hierarchyTreeStructure.getRootElement());
        String expectedTree = myFixture.configureByFile("hierarchy/" + getTestName(true) + ".tree.txt").getText();
        assertEquals(expectedTree, tree);
    }

    private String computeTree(HierarchyTreeStructure hierarchyTreeStructure, HierarchyNodeDescriptor element) {
        StringBuilder result = new StringBuilder();
        result.append(
                element.toString()).append(",")
                .append(element.getPsiElement()).append(",")
                .append(element.getContainingFile()).append(",")
                .append(element.getContainingFile().getContainingDirectory()).append("\n");
        for (Object descriptorObj : hierarchyTreeStructure.getChildElements(element)) {
            if (descriptorObj instanceof HierarchyNodeDescriptor) {
                result.append(computeTree(hierarchyTreeStructure, (HierarchyNodeDescriptor) descriptorObj));
            } else {
                throw new AssertionError("Expected all children to be a HierarchyNodeDescriptor");
            }
        }
        return result.toString();
    }

    private HierarchyProvider getHierarchyProvider() {
        return ExtensionPointName.create("com.intellij.typeHierarchyProvider").getExtensionList().stream()
                .map(ep -> {if (ep instanceof LanguageExtensionPoint) { return (LanguageExtensionPoint) ep; } else return null; })
                .filter(Objects::nonNull)
                .filter(languageExtensionPoint -> languageExtensionPoint.implementationClass.equals(OdooModelHierarchyProvider.class.getName()))
                .map(languageExtensionPoint -> newInstance(languageExtensionPoint, HierarchyProvider.class))
                .findFirst().orElse(null);
    }

    private <T> T newInstance(LanguageExtensionPoint languageExtensionPoint, Class<T> providerClass) {
        try {
            Class<?> clazz = Class.forName(languageExtensionPoint.implementationClass);
            if (providerClass.isAssignableFrom(clazz)) {
                return (T) clazz.asSubclass(providerClass).newInstance();
            }
            throw new AssertionError("Cannot find provider Class " + providerClass + " in extension point");
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            throw new AssertionError("Cannot create instance", e);
        }
    }

}
