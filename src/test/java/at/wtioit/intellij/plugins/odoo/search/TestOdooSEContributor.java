package at.wtioit.intellij.plugins.odoo.search;

import at.wtioit.intellij.plugins.odoo.BaseOdooPluginTest;
import com.intellij.mock.MockProgressIndicator;

import java.util.ArrayList;
import java.util.Collections;

public class TestOdooSEContributor extends BaseOdooPluginTest {
    public void testFetch() {
        OdooSEContributor contributor = new OdooSEContributor(getProject());
        ArrayList<String> resultsMy = new ArrayList<>();
        contributor.fetchElements("my", new MockProgressIndicator(), (result) -> resultsMy.add(result.getName() + ":" + result.getLocationString()));
        assertOrderedEquals(resultsMy, Collections.singletonList("my_other_addon:/src/odoo/my_addons/my_other_addon"));

        ArrayList<String> resultsModel = new ArrayList<>();
        contributor.fetchElements("model", new MockProgressIndicator(), (result) -> resultsModel.add(result.getName() + ":" + result.getLocationString()));
        assertOrderedEquals(resultsModel,
                "model_names:/src/odoo/addons/model_names",
                "model_d:(ModelD in odoo.addons.model_names.models)",
                "model_b:(ModelB in odoo.addons.model_names.models)",
                "model_e:(ModelE in odoo.addons.model_names.models)",
                "model_c:(ModelC in odoo.addons.model_names.models)",
                "model_a:(ModelA in odoo.addons.model_names.models)");
    }
}
