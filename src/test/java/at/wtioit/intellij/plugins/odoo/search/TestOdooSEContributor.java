package at.wtioit.intellij.plugins.odoo.search;

import at.wtioit.intellij.plugins.odoo.BaseOdooPluginTest;
import com.intellij.mock.MockProgressIndicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class TestOdooSEContributor extends BaseOdooPluginTest {
    public void testFetchModels() {
        OdooSEContributor contributor = new OdooSEContributor(getProject());

        ArrayList<String> resultsModel = new ArrayList<>();
        contributor.fetchElements("model", new MockProgressIndicator(), (result) -> resultsModel.add(result.getName() + ":" + result.getLocationString()));
        assertSameElements(resultsModel,
                "model_names:/src/odoo/addons/model_names",
                "model_d:(ModelD in odoo.addons.model_names.models)",
                "model_b:(ModelB in odoo.addons.model_names.models)",
                "model_e:(ModelE in odoo.addons.model_names.models)",
                "model_c:(ModelC in odoo.addons.model_names.models)",
                "model_a:(ModelA in odoo.addons.model_names.models)");
    }

    public void testFetchModulesMy() {
        OdooSEContributor contributor = new OdooSEContributor(getProject());
        ArrayList<String> resultsMy = new ArrayList<>();
        contributor.fetchElements("my", new MockProgressIndicator(), (result) -> resultsMy.add(result.getName() + ":" + result.getLocationString()));
        assertOrderedEquals(resultsMy, Collections.singletonList("my_other_addon:/src/odoo/my_addons/my_other_addon"));

    }
    public void testFetchModulesAddon() {
        OdooSEContributor contributor = new OdooSEContributor(getProject());
        ArrayList<String> resultsAddons = new ArrayList<>();
        contributor.fetchElements("addon1", new MockProgressIndicator(), (result) -> resultsAddons.add(result.getName() + ":" + result.getLocationString()));
        assertOrderedEquals(resultsAddons, Arrays.asList(
                "addon1_extension:/src/odoo/addons/addon1_extension",
                "addon1:/src/odoo/addons/addon1",
                "addon1.record2:/src/odoo/addons/addon1/data/records.xml",
                "addon1.record4:/src/odoo/addons/addon1/data/records2.xml"
        ));
    }

    public void testFetchModulesAddonRecords() {
        OdooSEContributor contributor = new OdooSEContributor(getProject());
        ArrayList<String> resultsAddons = new ArrayList<>();
        contributor.fetchElements("addon1.record", new MockProgressIndicator(), (result) -> resultsAddons.add(result.getName() + ":" + result.getLocationString()));
        assertOrderedEquals(resultsAddons, Arrays.asList(
                "addon1.record16:/src/odoo/addons/addon1/data/inherited3.csv",
                "addon1.record15:/src/odoo/addons/addon1/data/inherited3.csv",
                "addon1.record2:/src/odoo/addons/addon1/data/records.xml",
                "addon1.record5:/src/odoo/addons/addon1/data/existing.csv",
                "addon1.record6:/src/odoo/addons/addon1/data/existing.csv",
                "addon1.record1:/src/odoo/addons/addon1/data/records.xml",
                "addon1.record14:/src/odoo/addons/addon1/data/inherited2.csv",
                "addon1.record11:/src/odoo/addons/addon1/data/inherited2.csv",
                "addon1.record8:/src/odoo/addons/addon1/data/inherited.csv",
                "addon1.record10:/src/odoo/addons/addon1/data/inherited.csv",
                "addon1.record9:/src/odoo/addons/addon1/data/inherited.csv",
                "addon1.record7:/src/odoo/addons/addon1/data/inherited.csv",
                "addon1.record3:/src/odoo/addons/addon1/data/records2.xml",
                "addon1.record4:/src/odoo/addons/addon1/data/records2.xml"
        ));
    }
    public void testFetchModulesAddonRecordsWithModulePrefixOnly() {
        OdooSEContributor contributor = new OdooSEContributor(getProject());
        ArrayList<String> resultsAddons = new ArrayList<>();
        contributor.fetchElements("addon1.", new MockProgressIndicator(), (result) -> resultsAddons.add(result.getName() + ":" + result.getLocationString()));
        assertOrderedEquals(resultsAddons, Arrays.asList(
                "addon1.record16:/src/odoo/addons/addon1/data/inherited3.csv",
                "addon1.record15:/src/odoo/addons/addon1/data/inherited3.csv",
                "addon1.record2:/src/odoo/addons/addon1/data/records.xml",
                "addon1.record5:/src/odoo/addons/addon1/data/existing.csv",
                "addon1.record6:/src/odoo/addons/addon1/data/existing.csv",
                "addon1.record1:/src/odoo/addons/addon1/data/records.xml",
                "addon1.record14:/src/odoo/addons/addon1/data/inherited2.csv",
                "addon1.record11:/src/odoo/addons/addon1/data/inherited2.csv",
                "addon1.record8:/src/odoo/addons/addon1/data/inherited.csv",
                "addon1.record10:/src/odoo/addons/addon1/data/inherited.csv",
                "addon1.record9:/src/odoo/addons/addon1/data/inherited.csv",
                "addon1.record7:/src/odoo/addons/addon1/data/inherited.csv",
                "addon1.record3:/src/odoo/addons/addon1/data/records2.xml",
                "addon1.record4:/src/odoo/addons/addon1/data/records2.xml"
        ));
    }

    // TODO add test for records
    // and make sure :UNDETECTED_XML_IDS:... is found as well (with full xml id)
}
