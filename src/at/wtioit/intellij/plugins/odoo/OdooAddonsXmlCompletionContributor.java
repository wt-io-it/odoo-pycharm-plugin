package at.wtioit.intellij.plugins.odoo;

import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.models.OdooModelService;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionUtilCore;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttributeValue;
import com.jetbrains.python.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooAddonsXmlCompletionContributor extends AbstractOdooAddonsCompletionContributor {

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        if (parameters.getPosition().getParent() instanceof XmlAttributeValue) {
            String value = parameters.getPosition().getText().replace(CompletionUtilCore.DUMMY_IDENTIFIER, "");
            suggestModelName(parameters, result, value);
        }
    }

    @NotNull
    private String getStringValue(@NotNull CompletionParameters parameters, String expressionWithDummy) {
        TextRange contentRange = ((PyStringElement) parameters.getPosition()).getContentRange();
        String content = expressionWithDummy.substring(contentRange.getStartOffset(), contentRange.getEndOffset());
        return content.replace(CompletionUtilCore.DUMMY_IDENTIFIER, "");
    }
}
