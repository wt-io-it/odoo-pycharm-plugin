package at.wtioit.intellij.plugins.odoo;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public class OdooBundle extends DynamicBundle {

    public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, @NotNull Object... params) {
        return INSTANCE.getMessage(key, params);
    }

    public static final String BUNDLE = "at.wtioit.intellij.plugins.OdooBundle";
    private static final OdooBundle INSTANCE = new OdooBundle();

    private OdooBundle() {
        super(BUNDLE);
    }
}
