package at.wtioit.intellij.plugins.odoo;


import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;

public class DefaultCharset {
    @NotNull private static final Charset UTF_8 = Charset.availableCharsets().get("UTF-8");
    @NotNull public static final Charset DEFAULT = UTF_8;
}
