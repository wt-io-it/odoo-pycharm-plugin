package at.wtioit.intellij.plugins.odoo.search;

import com.intellij.ide.actions.searcheverywhere.FoundItemDescriptor;

public class OdooFoundItemDescriptor extends FoundItemDescriptor<OdooSEResult> {

    private static final int MAGIC_FACTOR = 2;

    private OdooFoundItemDescriptor(OdooSEResult item, int weight, double mlWeight) {
        super(item, weight, mlWeight);
    }

    public static FoundItemDescriptor<OdooSEResult> weighted(String pattern, OdooSEResult odooSEResult) {
        String name = odooSEResult.getName();
        char[] chars = new char[pattern.length()];
        pattern.getChars(0, pattern.length(), chars, 0);
        int weight = 0;
        for(char character: chars) {
            if (name.indexOf(character) != -1) {
                weight += 1;
            }
        }
        return new OdooFoundItemDescriptor(odooSEResult, weight * MAGIC_FACTOR, 1.0);
    }
}
