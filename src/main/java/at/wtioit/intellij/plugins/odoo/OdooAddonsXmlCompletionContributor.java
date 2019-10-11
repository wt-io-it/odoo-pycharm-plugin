package at.wtioit.intellij.plugins.odoo;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionUtilCore;
import com.intellij.psi.xml.XmlAttributeValue;
import org.jetbrains.annotations.NotNull;

public class OdooAddonsXmlCompletionContributor extends AbstractOdooAddonsCompletionContributor {

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        if (parameters.getPosition().getParent() instanceof XmlAttributeValue
                && "model".equals(parameters.getPosition().getParent().getParent().getFirstChild().getText())) {
            String value = parameters.getPosition().getText().replace(CompletionUtilCore.DUMMY_IDENTIFIER, "");
            suggestModelName(parameters, result, value);
        }
    }
}
