package at.wtioit.intellij.plugins.odoo.search;

import at.wtioit.intellij.plugins.odoo.BaseOdooPluginTest;
import com.intellij.mock.MockProgressIndicator;

import java.util.ArrayList;
import java.util.Comparator;

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
                "model_with_name_in_multiple_lines:(ModelWithMultilineName in odoo.addons.addon1.models)",
                "model_a:(ModelA in odoo.addons.model_names.models)");
    }

    public void testFetchModulesMy() {
        OdooSEContributor contributor = new OdooSEContributor(getProject());
        ArrayList<String> resultsMy = new ArrayList<>();
        contributor.fetchElements("my", new MockProgressIndicator(), (result) -> resultsMy.add(result.getName() + ":" + result.getLocationString()));
        assertSameElements(resultsMy,
                "my_other_addon:/src/odoo/my_addons/my_other_addon");

    }

    public void testFetchModulesAddon() {
        OdooSEContributor contributor = new OdooSEContributor(getProject());
        ArrayList<String> resultsAddons = new ArrayList<>();
        contributor.fetchElements("addon1", new MockProgressIndicator(), (result) -> resultsAddons.add(result.getName() + ":" + result.getLocationString()));
        resultsAddons.sort(Comparator.naturalOrder());
        assertSameElements(resultsAddons,
                "addon1.Board1:/src/odoo/addons/addon1/static/src/xml/js_plugin.xml",
                "addon1.autocomplete_target_record:/src/odoo/addons/addon1/data/records.xml",
                "addon1.assets_addon1:/src/odoo/addons/addon1/__manifest__.py",
                "addon1.existing_kanban_view:/src/odoo/addons/addon1/views/existing_view.xml",
                "addon1.existing_tree_view:/src/odoo/addons/addon1/views/existing_view.xml",
                "addon1.inherited:/src/odoo/addons/addon1/data/records2.xml",
                "addon1.my_form_template:/src/odoo/addons/addon1/views/existing_view.xml",
                "addon1.my_not_unique_record_name:/src/odoo/addons/addon1/data/records.xml",
                "addon1.open_existing_dashboard_kanban:/src/odoo/addons/addon1/views/existing_view.xml",
                "addon1.openerp_record:/src/odoo/addons/addon1/data/openerp_records.xml",
                "addon1.record1:/src/odoo/addons/addon1/data/records.xml",
                "addon1.record2:/src/odoo/addons/addon1/data/records.xml",
                "addon1.record3:/src/odoo/addons/addon1/data/records2.xml",
                "addon1.record4:/src/odoo/addons/addon1/data/records2.xml",
                "addon1:/src/odoo/addons/addon1",
                "addon1_extension.my_not_unique_record_name:/src/odoo/addons/addon1_extension/data/records.xml",
                "addon1_extension:/src/odoo/addons/addon1_extension"
        );
    }

    public void testFetchModulesAddonRecords() {
        OdooSEContributor contributor = new OdooSEContributor(getProject());
        ArrayList<String> resultsAddons = new ArrayList<>();
        contributor.fetchElements("addon1.record", new MockProgressIndicator(), (result) -> resultsAddons.add(result.getName() + ":" + result.getLocationString()));
        assertSameElements(resultsAddons,
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
                "addon1.record4:/src/odoo/addons/addon1/data/records2.xml");
    }

    public void testFetchModulesAddonRecordsWithModulePrefixOnly() {
        OdooSEContributor contributor = new OdooSEContributor(getProject());
        ArrayList<String> resultsAddons = new ArrayList<>();
        contributor.fetchElements("addon1.", new MockProgressIndicator(), (result) -> resultsAddons.add(result.getName() + ":" + result.getLocationString()));
        resultsAddons.sort(Comparator.naturalOrder());
        assertSameElements(resultsAddons,
                "addon1.Board1:/src/odoo/addons/addon1/static/src/xml/js_plugin.xml",
                "addon1.access_existing_system:/src/odoo/addons/addon1/security/windows_newline.csv",
                "addon1.assets_addon1:/src/odoo/addons/addon1/__manifest__.py",
                "addon1.autocomplete_target_record:/src/odoo/addons/addon1/data/records.xml",
                "addon1.existing_kanban_view:/src/odoo/addons/addon1/views/existing_view.xml",
                "addon1.existing_tree_view:/src/odoo/addons/addon1/views/existing_view.xml",
                "addon1.inherited:/src/odoo/addons/addon1/data/records2.xml",
                "addon1.my_form_template:/src/odoo/addons/addon1/views/existing_view.xml",
                "addon1.my_not_unique_record_name:/src/odoo/addons/addon1/data/records.xml",
                "addon1.open_existing_dashboard_kanban:/src/odoo/addons/addon1/views/existing_view.xml",
                "addon1.openerp_record:/src/odoo/addons/addon1/data/openerp_records.xml",
                "addon1.record10:/src/odoo/addons/addon1/data/inherited.csv",
                "addon1.record11:/src/odoo/addons/addon1/data/inherited2.csv",
                "addon1.record14:/src/odoo/addons/addon1/data/inherited2.csv",
                "addon1.record15:/src/odoo/addons/addon1/data/inherited3.csv",
                "addon1.record16:/src/odoo/addons/addon1/data/inherited3.csv",
                "addon1.record1:/src/odoo/addons/addon1/data/records.xml",
                "addon1.record2:/src/odoo/addons/addon1/data/records.xml",
                "addon1.record3:/src/odoo/addons/addon1/data/records2.xml",
                "addon1.record4:/src/odoo/addons/addon1/data/records2.xml",
                "addon1.record5:/src/odoo/addons/addon1/data/existing.csv",
                "addon1.record6:/src/odoo/addons/addon1/data/existing.csv",
                "addon1.record7:/src/odoo/addons/addon1/data/inherited.csv",
                "addon1.record8:/src/odoo/addons/addon1/data/inherited.csv",
                "addon1.record9:/src/odoo/addons/addon1/data/inherited.csv"
        );
    }

    public void testFetchModelRecords() {
        // TODO .model_ should be prefixed with the base module of the model
        OdooSEContributor contributor = new OdooSEContributor(getProject());
        ArrayList<String> resultsAddons = new ArrayList<>();
        contributor.fetchElements("base.model_", new MockProgressIndicator(), (result) -> resultsAddons.add(result.getName() + ":" + result.getLocationString()));
        // sort results by strings to make errors easier to read (in pycharm comparison view)
        resultsAddons.sort(Comparator.naturalOrder());
        // TODO ignore for now, as it should be addon1.model_inherited anyway, with dependency added in addon3 => addon1
        resultsAddons.remove("base.model_inherited:/src/odoo/addons/addon3/models/inherited.py");
        resultsAddons.remove("base.model_inherited:/src/odoo/addons/addon3/other_dir/inherited.py");
        assertSameElements(resultsAddons,
                "base.model_9000_1:/src/odoo/addons/addon1/models/numbers.py",
                "base.model_addon_with_weird_subdirs_my_model:/src/odoo/my_addons/addon_with_weird_subdirs/models/my_model.py",
                "base.model_addon_with_weird_subdirs_my_test_only_model:/src/odoo/my_addons/addon_with_weird_subdirs/tests/models/my_test_only_model.py",
                "base.model_existing:/src/odoo/addons/addon1/models/existing.py",
                "base.model_inherited2:/src/odoo/addons/addon1/models/inherited.py",
                "base.model_inherited3:/src/odoo/addons/addon1/models/inherited.py",
                // TODO ignore for now, as it should be addon1.model_inherited anyway
                //"base.model_inherited:/src/odoo/addons/addon3/models/inherited.py",
                "base.model_mixed_wildcard_default_:ANYTHING::/src/odoo/addons/addon1/models/existing.py",
                "base.model_mixed_wildcard_default_explicit:/src/odoo/addons/addon1/models/existing.py",
                "base.model_mixed_wildcard_format_:ANYTHING::/src/odoo/addons/addon1/models/existing.py",
                "base.model_mixed_wildcard_format_explicit:/src/odoo/addons/addon1/models/existing.py",
                "base.model_model_a:/src/odoo/addons/model_names/models/model_a.py",
                "base.model_model_b:/src/odoo/addons/model_names/models/model_b.py",
                "base.model_model_c:/src/odoo/addons/model_names/models/model_c.py",
                "base.model_model_d:/src/odoo/addons/model_names/models/model_d.py",
                "base.model_model_e:/src/odoo/addons/model_names/models/model_e.py",
                "base.model_model_with_name_in_multiple_lines:/src/odoo/addons/addon1/models/multiline.py",
                "base.model_odoo_äöü:/src/odoo/addons/addon1/models/umlauts.py",
                "base.model_outer_model:/src/odoo/my_addons/my_other_addon/models/my_models_with_dynamic_name.py",
                "base.model_single_wildcard_:ANYTHING::/src/odoo/addons/addon1/models/existing.py",
                "base.model_strangely_inherited:/src/odoo/addons/addon3/models/incomplete.py",
                "base.model_testing_conditional_expressions_false:/src/odoo/addons/addon1/models/conditional.py",
                "base.model_testing_conditional_expressions_true:/src/odoo/addons/addon1/models/conditional.py",
                "base.model_well_described:/src/odoo/addons/addon1/models/existing.py"
                );
    }
}
