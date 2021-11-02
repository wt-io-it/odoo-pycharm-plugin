package at.wtioit.intellij.plugins.odoo.records.index;

import at.wtioit.intellij.plugins.odoo.records.AbstractOdooRecord;
import at.wtioit.intellij.plugins.odoo.records.OdooRecord;
import com.intellij.psi.PsiElement;
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

    @Nullable
    public static OdooRecord getFromXml(XmlTag tag, @NotNull String path) {
        String id = tag.getAttributeValue("id");
        String modelName = tag.getAttributeValue("model");
        if (modelName == null) {
            modelName = MODELS_FOR_TAG_NAMES.get(tag.getName());
        }
        if (modelName != null && id != null) {
            if (!id.contains(".")) {
                String xmlId = null;
                // TODO maybe make a fast guess for the module name from path
                return new OdooRecordImpl(id, xmlId, modelName, path, tag);
            } else {
                return new OdooRecordImpl(id, id, modelName, path, tag);
            }
        }
        return null;
    }

    @Nullable
    public static OdooRecord getFromXmlTemplate(XmlTag templateTag, @NotNull String path) {
        String id = templateTag.getAttributeValue("t-name");
        String modelName = "ir.ui.view"; //TODO this is not really an ui view but a javascript template
        if (id != null) {
            if (!id.contains(".")) {
                String xmlId = null;
                // TODO maybe make a fast guess for the module name from path
                return new OdooRecordImpl(id, xmlId, modelName, path, templateTag);
            } else {
                return new OdooRecordImpl(id, id, modelName, path, templateTag);
            }
        }
        return null;
    }

    public static OdooRecord getFromCsvLine(@NotNull String modelName, @NotNull String[] columns, String[] line, String path, PsiElement definingElement) {
        String id = null;
        for (int i = 0; i < columns.length; i++) {
            if ("id".equals(columns[i])) {
                if (line.length > i) {
                    id = line[i];
                }
                break;
            }
        }
        if (id != null) {
            String xmlId = id;
            if (!xmlId.contains(".")) {
                xmlId = null;
            }
            // TODO maybe make a fast guess for the module name from path
            return new OdooRecordImpl(id, xmlId, modelName, path, definingElement);
        }
        return null;
    }

    @Override
    public PsiElement getDefiningElement() {
        return definingElement;
    }
}
