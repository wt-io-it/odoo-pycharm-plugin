package at.wtioit.intellij.plugins.odoo;

import at.wtioit.intellij.plugins.odoo.index.IndexWatcher;
import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.records.OdooRecord;
import at.wtioit.intellij.plugins.odoo.records.index.OdooRecordImpl;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPlainText;
import com.intellij.psi.xml.*;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static at.wtioit.intellij.plugins.odoo.PsiElementsUtil.findParent;

public interface OdooModelPsiElementMatcherUtil {

    Set<String> ODOO_MODEL_BASE_CLASS_NAMES = new HashSet<>(Arrays.asList(
            "odoo.models.Model",
            "odoo.models.BaseModel",
            "odoo.models.TransientModel",
            "odoo.models.AbstractModel"));
    Set<String> ODOO_TEST_BASE_CLASS_NAMES = new HashSet<>(Arrays.asList(
            "odoo.tests.BaseCase",
            "odoo.tests.TransactionCase",
            "odoo.tests.SingleTransactionCase",
            "odoo.tests.HttpCase",
            "odoo.tests.common.BaseCase",
            "odoo.tests.common.TransactionCase",
            "odoo.tests.common.SingleTransactionCase",
            "odoo.tests.common.HttpCase"));
    Set<String> ODOO_CONTROLLER_BASE_CLASS_NAMES = new HashSet<>(Arrays.asList(
            "odoo.http.Controller"
    ));
    List<String> ODOO_MODEL_XML_ATTRIBUTE_NAMES = Arrays.asList("model", "data-oe-model");
    List<String> ODOO_MODEL_XML_FIELD_ATTRIBUTE_NAMES = Arrays.asList("model", "res_model", "src_model");
    List<String> ODOO_XML_RECORD_TYPES = Arrays.asList("record", "template", "menuitem", "act_window", "report");
    String NULL_XML_ID_KEY = ":UNDETECTED_XML_ID:";


    /**
     * @param element element to check
     * @return true if element is part of a definition for an odoo model name
     */
    static boolean isOdooModelNameDefinitionPsiElement(PsiElement element) {
        PyAssignmentStatement assignmentStatement = findParent(element, PyAssignmentStatement.class, 4);
        if (assignmentStatement != null && element.getParent() instanceof PyStringLiteralExpression) {
            String variableName = assignmentStatement.getFirstChild().getText();
            // _name and _inherit fields
            return OdooModel.ODOO_MODEL_NAME_VARIABLE_NAME.contains(variableName) && isField(element);
        }
        return false;
    }

    /**
     * @param element element to check
     * @return true if element is part of an unresolvable definition for an odoo model name
     */
    static boolean isUnresolvableOdooModelNameDefinitionPsiElement(PsiElement element) {
        if (isOdooModelNameDefinitionPsiElement(element)) {
            // We cannot resolve subscription expections in model names (e.g. _name = model_data['name']) yet
            return PsiElementsUtil.findParent(element, PySubscriptionExpression.class, 2) != null;
        }
        return false;
    }

    /**
     * Checks if {@link PsiElement} is supposed to contain an Odoo model name e.g. `_name = 'ir.ui.view'`
     * @param element - element to check
     * @return `true` if element is supposed to contain an Odoo model name
     */
    static boolean isOdooModelPsiElement(PsiElement element) {
        if (isOdooModelNameDefinitionPsiElement(element)) {
            return true;
        }
        PyCallExpression pyCallExpression = findParent(element, PyCallExpression.class, 3);
        if (pyCallExpression != null && pyCallExpression.getCallee() != null) {
            String fieldType = pyCallExpression.getCallee().getText();
            if (OdooModel.ODOO_MODEL_NAME_FIELD_NAMES.contains(fieldType)) {
                PyArgumentList arguments = findParent(element, PyArgumentList.class, 2);
                if (arguments != null && arguments.getChildren().length > 0) {
                    PsiElement firstArgument = arguments.getChildren()[0];
                    if (!(firstArgument instanceof PyKeywordArgument) && firstArgument == element.getParent()) {
                        // fields.{N}2{M}(..., ) first argument
                        return true;
                    }
                }
            }
        }

        PyKeywordArgument pyKeywordArgument = findParent(element, PyKeywordArgument.class, 2);
        pyCallExpression = findParent(element, PyCallExpression.class, 4);
        if (pyKeywordArgument != null && pyCallExpression != null && pyCallExpression.getCallee() != null) {
            String fieldType = pyCallExpression.getCallee().getText();
            if (OdooModel.ODOO_MODEL_NAME_FIELD_NAMES.contains(fieldType)) {
                String keyword = pyKeywordArgument.getKeyword();
                if (OdooModel.ODOO_MODEL_NAME_FIELD_KEYWORD_ARGUMENTS.contains(keyword)) {
                    // fields.{N}2{M}(comodel_name=..., )
                    return true;
                }
            }
        }

        PyAssignmentStatement assignmentStatement = findParent(element, PyAssignmentStatement.class, 4);
        if (assignmentStatement != null && element.getParent() instanceof PyStringLiteralExpression) {
            String variableName = assignmentStatement.getFirstChild().getText();
            if (OdooModel.ODOO_MODEL_NAME_VARIABLE_NAME_IN_DICT_KEY.contains(variableName)) {
                PyKeyValueExpression valueExpression = findParent(element, PyKeyValueExpression.class, 2);
                if (valueExpression != null && valueExpression.getValue() != element.getParent()) {
                    // keys (not values) for _inherits fields
                    return true;
                }
            }
        }

        PySubscriptionExpression pySubscriptionExpression = findParent(element, PySubscriptionExpression.class, 2);
        if (pySubscriptionExpression != null) {
            String name = pySubscriptionExpression.getRootOperand().getName();
            String text = pySubscriptionExpression.getRootOperand().getText();
            PyClass pyClass = PsiElementsUtil.findParent(element, PyClass.class);
            PyFunction function = PsiElementsUtil.findParent(element, PyFunction.class);
            if ("self.env".equals(text) && (isOdooModelDefinition(pyClass) || isOdooTest(pyClass) || isOdooApiFunction(function))) {
                // handle self.env[...]
                return true;
            } else if ("request.env".equals(text) && isOdooControllerDefinition(pyClass) && hasImport(pyClass,"odoo.http.request", "request")) {
                // handle request.env[...]
                // TODO this is also possible in Models if http.request is imported
                return true;
            } else if ("http.request.env".equals(text) && isOdooControllerDefinition(pyClass) && hasImport(pyClass,"odoo.http.request", "http.request")) {
                // handle http.request.env[...]
                // TODO this is also possible in Models if http.request is imported
                return true;
            }
        }

        XmlAttribute xmlAttribute = findParent(element, XmlAttribute.class, 2);
        if (xmlAttribute != null && ODOO_MODEL_XML_ATTRIBUTE_NAMES.contains(xmlAttribute.getName())) {
            // xml attributes model="...", data-oe-model="..."
            return true;
        }
        XmlTag xmlTag = findParent(element, XmlTag.class, 2);
        if (xmlTag != null && "field".equals(xmlTag.getName()) && ODOO_MODEL_XML_FIELD_ATTRIBUTE_NAMES.contains(xmlTag.getAttributeValue("name"))) {
            // xml <field name="model">...</field> (also res_model and src_model)
            return true;
        }

        return false;
    }

    static boolean hasImport(PyClass pyClass, String importName, String importAlias) {
        PyFile file = findParent(pyClass, PyFile.class);
        if (file != null) {
            for (PyFromImportStatement fromImport : file.getFromImports()) {
                if (definedByImport(fromImport, Collections.singleton(importName), importAlias)) {
                    return true;
                }
            }
        }
        return false;
    }

    static boolean isField(PsiElement element) {
        // we test if a statement defines a field by checking if the next parent class is lower than the next parent function
        // if the function is closer than the class we are inside a function (and therefore it is not a field)
        return PsiElementsUtil.findParent(element, PyClass.class, 5) != null && PsiElementsUtil.findParent(element, PyFunction.class, 4) == null;
    }

    static boolean isOdooApiFunction(@Nullable PyFunction function) {
        if (function == null) return false;
        PsiElement firstChild = function.getFirstChild();
        if (firstChild instanceof PyDecoratorList) {
            PyDecorator modelDecorator = ((PyDecoratorList) firstChild).findDecorator("api.model");
            return modelDecorator != null;
        }
        return false;
    }

    static boolean isOdooControllerDefinition(PyClass pyClass) {
        if (pyClass == null) return false;
        @NotNull PyClass[] superClasses = new PyClass[0];
        try {
            TypeEvalContext typeEvalContext = TypeEvalContext.codeAnalysis(pyClass.getContainingFile().getProject(), pyClass.getContainingFile());
            superClasses = pyClass.getSuperClasses(typeEvalContext);
            for (PyClass superClass : superClasses) {
                if (isOdooControllerClassName(superClass.getQualifiedName(), pyClass)) {
                    return true;
                }
            }
        } catch (IndexNotReadyException e) {
            superClasses = new PyClass[0];
        }
        // try resolving superclasses by expressions
        // TODO this could be needed elsewhere as well
        for (PyExpression superClassExpression : pyClass.getSuperClassExpressions()) {
            String text = ((PyReferenceExpression) superClassExpression).getText();
            if (isOdooControllerClassName(text, pyClass)) {
                return true;
            }
        }
        return false;
    }

    static boolean isOdooTest(PyClass pyClass) {
        if (pyClass == null) return false;
        @NotNull PyClass[] superClasses = new PyClass[0];
        if (!DumbService.isDumb(pyClass.getProject())) {
            try {
                TypeEvalContext typeEvalContext = TypeEvalContext.codeAnalysis(pyClass.getContainingFile().getProject(), pyClass.getContainingFile());
                superClasses = pyClass.getSuperClasses(typeEvalContext);
                for (PyClass superClass : superClasses) {
                    if (isOdooTestClassName(superClass.getQualifiedName(), pyClass)) {
                        return true;
                    }
                }
            } catch (IndexNotReadyException e) {
                superClasses = new PyClass[0];
            }
        }
        if (superClasses.length == 0) {
            // when we cannot resolve super classes we resort to string matching
            for (@NotNull PsiElement superClassElement : pyClass.getChildren()[0].getChildren()) {
                if (superClassElement instanceof PyReferenceExpression) {
                    if (isOdooTestClassName(superClassElement.getText(), pyClass)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    static boolean isOdooModelDefinition(PsiElement pyline) {
        if (pyline instanceof PyClass) {
            TypeEvalContext typeEvalContext = TypeEvalContext.codeAnalysis(pyline.getContainingFile().getProject(), pyline.getContainingFile());
            PyClass pyClass = (PyClass) pyline;
            @NotNull PyClass[] superClasses = new PyClass[0];
            if (!(IndexWatcher.isCalledInIndexJob()) && !DumbService.isDumb(pyline.getProject())) {
                /*
                We use a version switch here because starting with 2021 releases JetBrains shows an error to the user if
                an indexing job opens a file it did not expect it to. Inspecting the super classes may lead to such an
                error because it may load other python files. So we fall back to our own parsing.
                */
                try {
                    superClasses = pyClass.getSuperClasses(typeEvalContext);
                    for (PyClass superClass : superClasses) {
                        if (isOdooModelClassName(superClass.getQualifiedName(), pyClass)) {
                            return true;
                        }
                    }
                } catch (IndexNotReadyException e) {
                    superClasses = new PyClass[0];
                }
            }
            if (superClasses.length == 0) {
                // when we cannot resolve super classes we resort to string matching
                for (@NotNull PsiElement superClassElement : pyClass.getChildren()[0].getChildren()) {
                    if (superClassElement instanceof PyReferenceExpression) {
                        // TODO check classes defined in same file (make website.published.multi.mixin work)
                        if (isOdooModelClassName(superClassElement.getText(), pyClass)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    static boolean isOdooModelClassName(String name, PyClass pyClass) {
        return isOdooClassName(ODOO_MODEL_BASE_CLASS_NAMES, name, pyClass);
    }

    static boolean isOdooTestClassName(String name, PyClass pyClass) {
       return isOdooClassName(ODOO_TEST_BASE_CLASS_NAMES, name, pyClass);
    }

    static boolean isOdooControllerClassName(String name, PyClass pyClass) {
        return isOdooClassName(ODOO_CONTROLLER_BASE_CLASS_NAMES, name, pyClass);
    }

    static boolean isOdooClassName(Set<String> possibleNames, String name, PyClass pyClass) {
        if (possibleNames.contains(name)) {
            return true;
        }
        PsiFile file = pyClass.getContainingFile();
        if (file instanceof PyFile) {
            for (PyFromImportStatement fromImport : ((PyFile) pyClass.getContainingFile()).getFromImports()) {
                if (definedByImport(fromImport, possibleNames, name)) {
                    return true;
                }
            }
        }
        return false;
    }

    static boolean definedByImport(PyFromImportStatement fromImport, Set<String> possibleQualifiedNames, String currentName) {
        PyReferenceExpression importSource = fromImport.getImportSource();
        if (importSource != null) {
            int currentNameParts = currentName.split("\\.").length - 1;
            List<String> uniquePackages = possibleQualifiedNames.stream()
                    .map(possibleImport -> possibleImport.replaceFirst("(\\.[^.]*){" + currentNameParts + "}$", ""))
                    .distinct()
                    .collect(Collectors.toList());
            for (String packageName : uniquePackages) {
                StringBuilder checkIfImport = new StringBuilder();
                for (String part : packageName.split("\\.")) {
                    if (checkIfImport.length() != 0) {
                        if (importSource.getText().equals(checkIfImport.toString())) {
                            checkIfImport.append('.');
                            if (PsiElementsUtil.findChildrenByClassAndName(fromImport, PyImportElement.class, part) != null
                                && possibleQualifiedNames.contains(checkIfImport.toString() + currentName)) {
                                return true;
                            }
                        } else {
                            checkIfImport.append('.');
                        }
                    }
                    checkIfImport.append(part);
                }
            }
        }
        return false;
    }

    static boolean isPartOfExpression(PsiElement element) {
        return PsiElementsUtil.findParent(element, PyBinaryExpression.class, 2) != null;
    }

    static HashMap<String, OdooRecord> getRecordsFromFile(PsiFile file) {
        return getRecordsFromFile(file, (record) -> true, Integer.MAX_VALUE, () -> file.getVirtualFile().getPath());
    }

    static HashMap<String, OdooRecord> getRecordsFromFile(PsiFile file, String path) {
        return getRecordsFromFile(file, (record) -> true, Integer.MAX_VALUE, () -> path);
    }

    static HashMap<String, OdooRecord> getRecordsFromFile(PsiFile file, Function<OdooRecord, Boolean> function, int limit) {
        return getRecordsFromFile(file, function, limit, () -> file.getVirtualFile().getPath());
    }

    static HashMap<String, OdooRecord> getRecordsFromFile(PsiFile file, Function<OdooRecord, Boolean> matches, int limit, Supplier<String> pathSupplier) {
        HashMap<String, OdooRecord> records = new HashMap<>();
        if (file instanceof XmlFile) {
            PsiElementsUtil.walkTree(file, (element) -> {
                if (element instanceof XmlTag) {
                    XmlTag tag = (XmlTag) element;
                    if (tag.getNamespace().contains("http://relaxng.com/ns/")) {
                        // skip investigating relaxng schemas for odoo models
                        return true;
                    } else if ("odoo".equals(tag.getName()) || "openerp".equals(tag.getName())) {
                        records.putAll(getRecordsFromOdooTag(tag, pathSupplier.get(), matches, limit));
                        return true;
                    } else if ("templates".equals(tag.getName()) || "template".equals(tag.getName())) {
                        // TODO templates should go to a different index (they have no real xmlid)
                        records.putAll(getRecordsFromTemplateTag(tag, pathSupplier.get(), matches, limit));
                    }
                    // investigate children
                    return false;
                } else if (element instanceof XmlDocument) {
                    // investigate children
                    return false;
                }
                // skip investigating children
                return true;
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
            if ("data".equals(name)) return records.size() >= limit;
            // function needs no further investigation (cannot hold records / templates)
            if ("function".equals(name)) return true;
            if (ODOO_XML_RECORD_TYPES.contains(name)) {
                OdooRecord record = OdooRecordImpl.getFromXml(tag, path);
                if (applyRecord(function, records, record)) return true;
            }
            return records.size() >= limit;
        }, XmlTag.class, 2);
        return records;
    }

    static Map<String, OdooRecord> getRecordsFromTemplateTag(XmlTag recordsTag, @NotNull String path, Function<OdooRecord, Boolean> function, int limit) {
        HashMap<String, OdooRecord> records = new HashMap<>();
        PsiElementsUtil.walkTree(recordsTag, (tag)-> {
            String name = tag.getAttributeValue("t-name");
            if (name != null) {
                OdooRecord record = OdooRecordImpl.getFromXmlTemplate(tag, path);
                if (applyRecord(function, records, record)) return true;
            }
            return records.size() >= limit;
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
