package at.wtioit.intellij.plugins.odoo.models.hierarchy;

import at.wtioit.intellij.plugins.odoo.BaseOdooPluginTest;
import at.wtioit.intellij.plugins.odoo.PsiElementsUtil;
import com.intellij.ide.hierarchy.*;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.PythonLanguage;
import com.jetbrains.python.psi.PyClass;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class OdooModelHierarchyProviderTest extends BaseOdooPluginTest {

    private static String getHierarchyType(@NotNull String methodName, @NotNull String fieldName, @NotNull String defaultValue) {
        // Starting with 2021.3 the static fields we used are no longer available but the getters are used for creating the type strings
        try {
            Method method = TypeHierarchyBrowserBase.class.getMethod(methodName);
            return (String) method.invoke(TypeHierarchyBrowserBase.class);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | ClassCastException e) {
            Field field = null;
            try {
                field = TypeHierarchyBrowserBase.class.getField(fieldName);
                return (String) field.get(TypeHierarchyBrowserBase.class);
            } catch (NoSuchFieldException | IllegalAccessException | ClassCastException ex) {
                return defaultValue;
            }
        }

    }

    public static final String TYPE_HIERARCHY_TYPE = getHierarchyType("getTypeHierarchyType", "TYPE_HIERARCHY_TYPE", "Class {0}");
    public static final String SUBTYPES_HIERARCHY_TYPE = getHierarchyType("getSubtypesHierarchyType", "SUBTYPES_HIERARCHY_TYPE","Subtypes of {0}");

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
        if (File.separator.equals("\\")) {
            expectedTree = expectedTree.replaceAll("/", "\\\\");
        }
        assertEquals(expectedTree, tree);
    }

    private String computeTree(HierarchyTreeStructure hierarchyTreeStructure, HierarchyNodeDescriptor element) {
        StringBuilder result = new StringBuilder();
        result.append(
                element.toString()).append(",")
                .append(element.getPsiElement()).append(",")
                .append(element.getContainingFile()).append(",")
                .append(element.getContainingFile().getContainingDirectory()).append("\n");
        Object[] childElements = hierarchyTreeStructure.getChildElements(element);
        List<HierarchyNodeDescriptor> sortedChildElements = Arrays.stream(childElements)
                .filter((o) -> o instanceof HierarchyNodeDescriptor)
                .map(o -> (HierarchyNodeDescriptor) o)
                .sorted(Comparator.comparing(o -> o.getContainingFile().getContainingDirectory().toString()))
                .collect(Collectors.toList());
        assertEquals("Expected all children to be a HierarchyNodeDescriptor", childElements.length, sortedChildElements.size());
        for (HierarchyNodeDescriptor descriptorObj : sortedChildElements) {
            result.append(computeTree(hierarchyTreeStructure, descriptorObj));
        }
        return result.toString();
    }

    private HierarchyProvider getHierarchyProvider() {
        return LanguageTypeHierarchy.INSTANCE.allForLanguage(PythonLanguage.INSTANCE).stream()
                .filter(languageExtensionPoint -> languageExtensionPoint instanceof OdooModelHierarchyProvider)
                .findFirst().orElse(null);
    }

}
