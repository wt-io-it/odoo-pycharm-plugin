package at.wtioit.intellij.plugins.odoo;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionUtilCore;
import org.jetbrains.annotations.NotNull;

public class OdooXmlCompletionContributor extends AbstractOdooCompletionContributor {

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        if (OdooModelPsiElementMatcherUtil.isOdooModelPsiElement(parameters.getPosition())) {
            String value = parameters.getPosition().getText()
                    .replace(CompletionUtilCore.DUMMY_IDENTIFIER, "")
                    .replace(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED, "");
            suggestModelName(parameters, result, value);
        }
        if (OdooRecordPsiElementMatcherUtil.isOdooRecordPsiElement(parameters.getPosition())) {
            String value = parameters.getPosition().getText()
                    .replace(CompletionUtilCore.DUMMY_IDENTIFIER, "")
                    .replace(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED, "");
            suggestRecordXmlId(parameters, result, value);
        }
    }
}
