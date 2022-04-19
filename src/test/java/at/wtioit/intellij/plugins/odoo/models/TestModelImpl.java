package at.wtioit.intellij.plugins.odoo.models;

import at.wtioit.intellij.plugins.odoo.BaseOdooPluginTest;
import at.wtioit.intellij.plugins.odoo.models.impl.OdooModelImpl;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.PsiElement;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TestModelImpl extends BaseOdooPluginTest {

    public void testFindingModels() {
        OdooModelService modelService = ServiceManager.getService(getProject(), OdooModelService.class);

        assertContainsElements(StreamSupport.stream(modelService.getModelNames().spliterator(), false).collect(Collectors.toList()),
                "model_a",
                "model_b",
                "model_c",
                "model_d",
                "model_e");
    }

    public void testInheritedDefiningElement() {
        OdooModelService modelService = ServiceManager.getService(getProject(), OdooModelService.class);
        OdooModel inherited = modelService.getModel("inherited");
        assertNotNull(inherited);

        PsiElement definingElement = inherited.getDefiningElement();
        assertEquals("PyClass: InheritedBase", definingElement.toString());
        assertEquals("/src/odoo/addons/addon3/models/inherited.py", definingElement.getContainingFile().getVirtualFile().getPath());
    }

    public void testInheritedDefiningElements() {
        OdooModelService modelService = ServiceManager.getService(getProject(), OdooModelService.class);
        OdooModel inherited = modelService.getModel("inherited");
        assertNotNull(inherited);

        assertTrue(inherited instanceof OdooModelImpl);
        OdooModelImpl inheritedImpl = (OdooModelImpl) inherited;
        List<String> definingElements = inheritedImpl.getDefiningElements().stream().map(element -> element.toString() + "; " + element.getContainingFile().getVirtualFile().getPath()).collect(Collectors.toList());
        assertSameElements(definingElements,
                "PyClass: Inherited; /src/odoo/addons/addon1/models/inherited.py",
                "PyClass: InheritedBase; /src/odoo/addons/addon3/models/inherited.py",
                "PyClass: InheritedInherited; /src/odoo/addons/addon3/models/inherited.py",
                "PyClass: InheritedInherited2; /src/odoo/addons/addon3/models/inherited.py",
                "PyClass: InheritedInherited; /src/odoo/addons/addon3/other_dir/inherited.py");
    }

}
