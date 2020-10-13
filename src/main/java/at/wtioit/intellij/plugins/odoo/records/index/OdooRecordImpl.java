package at.wtioit.intellij.plugins.odoo.records.index;

import at.wtioit.intellij.plugins.odoo.records.AbstractOdooRecord;
import at.wtioit.intellij.plugins.odoo.records.OdooRecord;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class OdooRecordImpl extends AbstractOdooRecord {

    private static final Map<String, String> MODELS_FOR_TAG_NAMES = new HashMap<>();
    static {
        // from tools/convert.py
        MODELS_FOR_TAG_NAMES.put("template", "ir.ui.view"); // todo inside themes this is theme.ir.ui.view
        MODELS_FOR_TAG_NAMES.put("menuitem", "ir.ui.menu");
        MODELS_FOR_TAG_NAMES.put("report", "ir.actions.report");
        MODELS_FOR_TAG_NAMES.put("act_window", "ir.actions.act_window");

    }

    private final PsiElement definingElement;

    private OdooRecordImpl(@NotNull String id, @NotNull String modelName, @NotNull String path) {
        this(id, null, modelName, path, null);
    }

    private OdooRecordImpl(@NotNull String id, @Nullable String xmlId, @NotNull String modelName, @NotNull String path, PsiElement definingElement) {
        super(id, xmlId, modelName, path);
        this.definingElement = definingElement;
    }

    public static OdooRecord getFromXml(XmlTag tag, @NotNull String path) {
        String id = tag.getAttributeValue("id");
        String modelName = tag.getAttributeValue("model");
        if (modelName == null) {
            modelName = MODELS_FOR_TAG_NAMES.get(tag.getName());
        }
        if (!id.contains(".")) {
            String xmlId = null;
            // TODO maybe make a fast guess for the module name from path
            return new OdooRecordImpl(id, xmlId, modelName, path, tag);
        } else {
            return new OdooRecordImpl(id, id, modelName, path, tag);
        }
    }

    @Override
    public PsiElement getDefiningElement() {
        return definingElement;
    }
}
