package at.wtioit.intellij.plugins.odoo;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionUtilCore;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyFromImportStatement;
import com.jetbrains.python.psi.PyStringElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static at.wtioit.intellij.plugins.odoo.PsiElementsUtil.findParent;

public class OdooAddonsCompletionContributor extends AbstractOdooAddonsCompletionContributor {

    private final Logger logger = Logger.getInstance(OdooAddonsCompletionContributor.class);

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        String expressionWithDummy = parameters.getPosition().getParent().getText();
        String fqdn = expressionWithDummy.substring(0, expressionWithDummy.length() - CompletionUtilCore.DUMMY_IDENTIFIER.length() + 1);
        if (fqdn.startsWith("odoo.addons.")) {
            PsiElement dot = parameters.getPosition().getPrevSibling();
            if (dot != null) {
                PsiElement packageName = dot.getPrevSibling();
                String addonNameStart = "";
                if (packageName.getText().equals("odoo.addons")) {
                    if (dot.getText().equals(".")) {
                        addonNameStart = fqdn.substring(dot.getStartOffsetInParent() + 1);
                    }
                    suggestModuleName(parameters, result, addonNameStart);
                }
            }
        } else {
            PyFromImportStatement parentImportStatement = findParent(parameters.getPosition(), PyFromImportStatement.class);
            if (!fqdn.contains(".") && parentImportStatement != null
                    && parentImportStatement.getText().startsWith("from odoo.addons import ")) {
                suggestModuleName(parameters, result, fqdn);
            } else if (OdooModelPsiElementMatcherUtil.isOdooModelPsiElement(parameters.getPosition())) {
                // suggest model names
                String value = getStringValue(parameters, expressionWithDummy);
                if (value != null) {
                    suggestModelName(parameters, result, value);
                }
            }
        }

    }

    @Nullable
    private String getStringValue(@NotNull CompletionParameters parameters, String expressionWithDummy) {
        PsiElement position = parameters.getPosition();
        if (position instanceof PyStringElement) {
            TextRange contentRange = ((PyStringElement) position).getContentRange();
            String content = expressionWithDummy.substring(contentRange.getStartOffset(), contentRange.getEndOffset());
            return content.replace(CompletionUtilCore.DUMMY_IDENTIFIER, "")
                    .replace(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED, "");
        }
        return null;
    }
}
