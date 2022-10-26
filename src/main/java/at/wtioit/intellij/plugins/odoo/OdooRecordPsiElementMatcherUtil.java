package at.wtioit.intellij.plugins.odoo;

import at.wtioit.intellij.plugins.odoo.records.OdooRecord;
import at.wtioit.intellij.plugins.odoo.records.index.OdooRecordImpl;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPlainText;
import com.intellij.psi.xml.*;
import com.jetbrains.python.psi.PyArgumentList;
import com.jetbrains.python.psi.PyReferenceExpression;
import com.jetbrains.python.psi.PyStringElement;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static at.wtioit.intellij.plugins.odoo.PsiElementsUtil.findParent;
import static at.wtioit.intellij.plugins.odoo.PsiElementsUtil.getPrevSibling;
import static com.intellij.psi.xml.XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN;

public interface OdooRecordPsiElementMatcherUtil {

    List<String> ODOO_RECORD_REF_ATTRIBUTES = Collections.unmodifiableList(Arrays.asList(
            "ref",
            "inherit_id",
            "groups", // TODO handle comma seperated groups
            "action",
            "t-call",
            "t-call-assets",
            "t-extend"));

    Map<String, List<String>> ODOO_RECORD_REF_ATTRIBUTES_TAGS_FALSE_POSITIVES = Map.of(
            "action", Collections.singletonList("form")
    );

    List<String> ODOO_XML_RECORD_TYPES = Arrays.asList("record", "template", "menuitem", "act_window", "report");
    String NULL_XML_ID_KEY = ":UNDETECTED_XML_ID:";


    static boolean isOdooRecordPsiElement(PsiElement psiElement) {
        if (psiElement instanceof XmlToken && ((XmlToken) psiElement).getTokenType() == XML_ATTRIBUTE_VALUE_TOKEN) {
            XmlAttribute attribute = PsiElementsUtil.findParent(psiElement, XmlAttribute.class, 2);
            if (attribute != null) {
                String attributeName = attribute.getName();
                if (ODOO_RECORD_REF_ATTRIBUTES.contains(attributeName)) {
                    if (ODOO_RECORD_REF_ATTRIBUTES_TAGS_FALSE_POSITIVES.containsKey(attributeName)) {
                        String tagName = attribute.getParent().getName();
                        return !ODOO_RECORD_REF_ATTRIBUTES_TAGS_FALSE_POSITIVES.get(attributeName).contains(tagName);
                    }
                    return true;
                }
            }

        }
        if (psiElement instanceof PyStringElement) {
            PyArgumentList argumentList = findParent(psiElement, PyArgumentList.class, 2);
            if (argumentList != null) {
                PyReferenceExpression method = getPrevSibling(argumentList, PyReferenceExpression.class);
                if (method != null) {
                    // TODO use type of self.env (Environment) instead of text to check if method belongs to Environment
                    String methodName = method.getLastChild().getText();
                    return "ref".equals(methodName);
                }
            }
        }
        return false;
    }

    static boolean holdsOdooRecordReference(PsiElement psiElement) {
        XmlAttribute attribute = PsiElementsUtil.findParent(psiElement, XmlAttribute.class, 2);
        if (attribute != null && "eval".equals(attribute.getName())) {
            if (psiElement.getText().contains("ref(")) {
                return true;
            }
        }
        return false;
    }

    static HashMap<String, OdooRecord> getRecordsFromFile(@NotNull PsiFile file) {
        return getRecordsFromFile(file, (record) -> true, Integer.MAX_VALUE, () -> file.getVirtualFile().getPath());
    }

    static HashMap<String, OdooRecord> getRecordsFromFile(@NotNull PsiFile file, String path) {
        return getRecordsFromFile(file, (record) -> true, Integer.MAX_VALUE, () -> path);
    }

    static HashMap<String, OdooRecord> getRecordsFromFile(@NotNull PsiFile file, Function<OdooRecord, Boolean> function, int limit) {
        return getRecordsFromFile(file, function, limit, () -> file.getVirtualFile().getPath());
    }

    static HashMap<String, OdooRecord> getRecordsFromFile(@NotNull PsiFile file, Function<OdooRecord, Boolean> matches, int limit, Supplier<String> pathSupplier) {
        HashMap<String, OdooRecord> records = new HashMap<>();
        if (file instanceof XmlFile) {
            PsiElementsUtil.walkTree(file, (element) -> {
                if (element instanceof XmlTag) {
                    XmlTag tag = (XmlTag) element;
                    if (tag.getNamespace().contains("http://relaxng.com/ns/")) {
                        // skip investigating relaxng schemas for odoo models
                        return PsiElementsUtil.TREE_WALING_SIGNAL.SKIP_CHILDREN;
                    } else if ("odoo".equals(tag.getName()) || "openerp".equals(tag.getName())) {
                        records.putAll(getRecordsFromOdooTag(tag, pathSupplier.get(), matches, limit));
                        return PsiElementsUtil.TREE_WALING_SIGNAL.SKIP_CHILDREN;
                    } else if ("templates".equals(tag.getName()) || "template".equals(tag.getName())) {
                        // TODO templates should go to a different index (they have no real xmlid)
                        records.putAll(getRecordsFromTemplateTag(tag, pathSupplier.get(), matches, limit));
                    }
                    return PsiElementsUtil.TREE_WALING_SIGNAL.INVESTIGATE_CHILDREN;
                } else if (element instanceof XmlDocument) {
                    return PsiElementsUtil.TREE_WALING_SIGNAL.INVESTIGATE_CHILDREN;
                }
                return PsiElementsUtil.TREE_WALING_SIGNAL.SKIP_CHILDREN;
            }, XmlElement.class, 3);
        } else if ("csv".equals(file.getVirtualFile().getExtension())) {
            records.putAll(getRecordsFromCsvFile(file, pathSupplier.get(), matches, limit));
        }
        return records;
    }

    static HashMap<String, OdooRecord> getRecordsFromCsvFile(PsiFile file, String path) {
        return getRecordsFromCsvFile(file, path, (r) -> true, Integer.MAX_VALUE);
    }

    static HashMap<String, OdooRecord> getRecordsFromCsvFile(PsiFile file, String path, Function<OdooRecord, Boolean> matches, int limit) {
        String modelName = file.getName().replaceFirst(".csv$", "");
        HashMap<String, OdooRecord> result = new HashMap<>();
        PsiElement firstChild = file.getFirstChild();
        if (firstChild instanceof PsiPlainText) {
            // TODO replace with a proper csv parser
            String[] lines = firstChild.getText().split("\r?\n");
            if (lines.length > 1) {
                String[] columns = csvLine(lines[0]);
                for (int i = 1; i < lines.length && result.size() < limit; i++) {
                    String line = lines[i];
                    OdooRecord record = OdooRecordImpl.getFromCsvLine(modelName, columns, csvLine(line), path, firstChild);
                    // TODO check module name
                    if (record != null && matches.apply(record)) {
                        if (record.getXmlId() != null) {
                            result.put(record.getXmlId(), record);
                        } else {
                            result.put(NULL_XML_ID_KEY + "." + record.getId(), record);
                        }
                    }
                }
            }
        }
        return result;
    }

    static String[] csvLine(String line) {
        List<Character> quoteCharacters = Arrays.asList('"', '\'');
        ArrayList<String> elements = new ArrayList<>();
        char activeQuoteChar = 0;
        ArrayList<String> pendingElements = new ArrayList<>();
        for (String possibleElement : line.split(",")) {
            if (possibleElement.length() == 0) {
                if (activeQuoteChar != 0) {
                    pendingElements.add(possibleElement);
                } else {
                    elements.add(possibleElement);
                }
                continue;
            }
            char firstChar = possibleElement.charAt(0);
            char lastChar = possibleElement.charAt(possibleElement.length() - 1);
            if (activeQuoteChar != 0) {
                if (lastChar == activeQuoteChar) {
                    pendingElements.add(possibleElement.substring(0, possibleElement.length() - 1));
                    elements.add(String.join(",", pendingElements));
                    activeQuoteChar = 0;
                    pendingElements = new ArrayList<>();
                } else {
                    pendingElements.add(possibleElement);
                }
            } else if (quoteCharacters.contains(firstChar) && firstChar == lastChar && possibleElement.length() > 1) {
                elements.add(possibleElement.substring(1, possibleElement.length() - 1));
            } else if (quoteCharacters.contains(firstChar)){
                pendingElements.add(possibleElement.substring(1));
                activeQuoteChar = firstChar;
            } else {
                elements.add(possibleElement);
            }
        }
        return elements.toArray(new String[0]);
    }

    static Map<String, OdooRecord> getRecordsFromOdooTag(XmlTag odooTag, @NotNull String path, Function<OdooRecord, Boolean> function, int limit) {
        HashMap<String, OdooRecord> records = new HashMap<>();
        PsiElementsUtil.walkTree(odooTag, (tag)-> {
            String name = tag.getName();
            // data needs further investigation (can hold records / templates)
            if ("data".equals(name)) return PsiElementsUtil.TREE_WALING_SIGNAL.should_skip(records.size() >= limit);
            // function needs no further investigation (cannot hold records / templates)
            if ("function".equals(name)) return PsiElementsUtil.TREE_WALING_SIGNAL.SKIP_CHILDREN;
            if (ODOO_XML_RECORD_TYPES.contains(name)) {
                OdooRecord record = OdooRecordImpl.getFromXml(tag, path);
                if (applyRecord(function, records, record)) return PsiElementsUtil.TREE_WALING_SIGNAL.SKIP_CHILDREN;
            }
            // investigate children if we need more records
            return PsiElementsUtil.TREE_WALING_SIGNAL.investigate(records.size() < limit);
        }, XmlTag.class, 2);
        return records;
    }

    static Map<String, OdooRecord> getRecordsFromTemplateTag(XmlTag recordsTag, @NotNull String path, Function<OdooRecord, Boolean> function, int limit) {
        HashMap<String, OdooRecord> records = new HashMap<>();
        PsiElementsUtil.walkTree(recordsTag, (tag)-> {
            String name = tag.getAttributeValue("t-name");
            if (name != null) {
                OdooRecord record = OdooRecordImpl.getFromXmlTemplate(tag, path);
                if (applyRecord(function, records, record)) return PsiElementsUtil.TREE_WALING_SIGNAL.SKIP_CHILDREN;
            }
            // investigate children if we need more records
            return PsiElementsUtil.TREE_WALING_SIGNAL.investigate(records.size() < limit);
        }, XmlTag.class, 2);
        return records;
    }

    static boolean applyRecord(Function<OdooRecord, Boolean> function, HashMap<String, OdooRecord> records, OdooRecord record) {
        if (record == null) {
            return true;
        }
        if (function.apply(record)) {
            if (record.getXmlId() == null) {
                records.put(NULL_XML_ID_KEY + "." + record.getId(), record);
            } else {
                records.put(record.getXmlId(), record);
            }
            return true;
        }
        return false;
    }

}
