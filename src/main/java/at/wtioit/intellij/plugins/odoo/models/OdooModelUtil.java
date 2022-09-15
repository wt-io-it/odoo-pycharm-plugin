package at.wtioit.intellij.plugins.odoo.models;

import at.wtioit.intellij.plugins.odoo.PsiElementsUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import com.jetbrains.python.psi.types.TypeEvalContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class OdooModelUtil {

    private static final Logger logger = Logger.getInstance(OdooModelUtil.class);
    public static final String NAME_WILDCARD_MARKER = ":ANYTHING:";
    private static final Map<String, Pattern> regexCache = new ConcurrentHashMap<>();

    public static String detectName(PsiElement pyline) {
        return detectName(pyline, () -> PyResolveContext.defaultContext().withTypeEvalContext(TypeEvalContext.codeAnalysis(pyline.getContainingFile().getProject(), pyline.getContainingFile())));
    }

    public static String detectName(PsiElement pyline, Supplier<PyResolveContext> contextSupplier) {
        String name = null;
        for (PsiElement statement : pyline.getChildren()[1].getChildren()) {
            PsiElement statementFirstChild = statement.getFirstChild();
            if (statementFirstChild != null) {
                String variableName = statementFirstChild.getText();
                if (OdooModel.ODOO_MODEL_NAME_VARIABLE_NAME.contains(variableName)) {
                    PsiElement valueChild = statement.getLastChild();
                    while (valueChild instanceof PsiComment || valueChild instanceof PsiWhiteSpace) {
                        valueChild = valueChild.getPrevSibling();
                    }
                    String stringValueForChild = PsiElementsUtil.getStringValueForValueChild(valueChild, contextSupplier);
                    if (stringValueForChild != null) {
                        name = stringValueForChild;
                    }
                    // if we get the name from _name it takes precedence over _inherit
                    if ("_name".equals(variableName)) break;
                }
            }
        }
        if (name == null) logger.debug("Cannot detect name for " + pyline + " in " + pyline.getContainingFile());
        return name;
    }

    public static boolean wildcardNameMatches(String nameWithWildcard, String name) {
        if (!nameWithWildcard.contains(NAME_WILDCARD_MARKER))  {
            return false;
        }
        if (!regexCache.containsKey(nameWithWildcard)) {
            String rePattern = nameWithWildcard.replaceAll("\\.", "\\\\.").replace(NAME_WILDCARD_MARKER, ".*");
            regexCache.put(nameWithWildcard, Pattern.compile(rePattern));
        }
        Pattern pattern = regexCache.get(nameWithWildcard);
        return pattern.matcher(name).matches();
    }

    public static String removeWildcards(String modelName) {
        return modelName.replaceAll(NAME_WILDCARD_MARKER, "");
    }
}
