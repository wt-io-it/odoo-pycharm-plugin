package at.wtioit.intellij.plugins.odoo.records.index;

import at.wtioit.intellij.plugins.odoo.FileUtil;
import at.wtioit.intellij.plugins.odoo.records.AbstractOdooRecord;
import at.wtioit.intellij.plugins.odoo.records.OdooRecord;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

    private static final List<String> KNOWN_ODOO_XML_DIRECTORIES = Arrays.asList("/views/", "/data/", "/static/src/xml/");

    private final PsiElement definingElement;

    private OdooRecordImpl(@NotNull String id, @NotNull String modelName, @NotNull String path) {
        this(id, null, modelName, path, null);
    }

    private OdooRecordImpl(@NotNull String id, @Nullable String xmlId, @NotNull String modelName, @NotNull String path, PsiElement definingElement) {
        super(id, xmlId, modelName, path);
        this.definingElement = definingElement;
    }

    public static OdooRecord getFromData(@NotNull String id, @Nullable String xmlId, @NotNull String modelName, @NotNull String path, PsiElement definingElement) {
        return new OdooRecordImpl(id, xmlId, modelName, path, definingElement);
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
                return new OdooRecordImpl(id, guessFromPath(id, path), modelName, path, tag);
            } else {
                return new OdooRecordImpl(id, id, modelName, path, tag);
            }
        }
        return null;
    }

    public static String guessFromPath(String id, String path) {
        if (path.endsWith(".xml")) {
            for (String directory : KNOWN_ODOO_XML_DIRECTORIES) {
                int endOfAddonName = path.indexOf(directory);
                if (endOfAddonName != -1) {
                    String[] segments = path.substring(0, endOfAddonName).split("/");
                    return segments[segments.length - 1] + "." + id;
                }
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
                return new OdooRecordImpl(id, guessFromPath(id, path), modelName, path, templateTag);
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

    @Override
    public @Nullable VirtualFile findVirtualFile() {
        PsiFile containingFile = getDefiningElement().getContainingFile();
        VirtualFile virtualFile = containingFile.getVirtualFile();
        if (virtualFile == null) {
            virtualFile = FileUtil.findFileByPath(getPath());
        }
        return virtualFile;
    }
}
