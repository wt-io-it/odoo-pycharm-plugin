package at.wtioit.intellij.plugins.odoo;

import com.intellij.AbstractBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public class OdooBundle extends AbstractBundle {
    // We cannot extend dynamic bundle here as long as we support 2019.2, 20219.3

    public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, @NotNull Object... params) {
        return INSTANCE.getMessage(key, params);
    }

    public static final String BUNDLE = "at.wtioit.intellij.plugins.OdooBundle";
    private static final OdooBundle INSTANCE = new OdooBundle();

    private OdooBundle() {
        super(BUNDLE);
    }
}
