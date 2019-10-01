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
import com.jetbrains.python.psi.*;
import org.jetbrains.annotations.NotNull;

public class OdooAddonsCompletionContributor extends CompletionContributor {

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        String expressionWithDummy = parameters.getPosition().getParent().getText();
        String fqdn = expressionWithDummy.substring(0, expressionWithDummy.length() - CompletionUtilCore.DUMMY_IDENTIFIER.length() + 1);
        if (fqdn.startsWith("odoo.addons.")) {
            PsiElement dot = parameters.getPosition().getPrevSibling();
            String addonNameStart = "";
            if (dot != null && dot.getText().equals(".")) {
                addonNameStart = fqdn.substring(dot.getStartOffsetInParent() + 1);
            }
            OdooModuleService moduleService = ServiceManager.getService(parameters.getOriginalFile().getProject(), OdooModuleService.class);
            for (OdooModule module : moduleService.getModules()) {
                if (module.getName().startsWith(addonNameStart)) {
                    LookupElementBuilder element = LookupElementBuilder
                            .createWithSmartPointer(module.getName(), module.getDirectory())
                            .withIcon(module.getIcon())
                            .withTailText(" " + module.getRelativeLocationString(), true);
                    // TODO add insert handler if used in code (not import statement)?
                    result.addElement(element);
                }
            }
        } else if(parameters.getPosition().getParent() instanceof PyStringLiteralExpression
                && parameters.getPosition().getParent().getParent() instanceof PyAssignmentStatement) {
            String variableName = parameters.getPosition().getParent().getParent().getFirstChild().getText();
            if (OdooModel.ODOO_MODEL_NAME_VARIABLE_NAME.contains(variableName)) {
                String value = getStringValue(parameters, expressionWithDummy);
                suggestModelName(parameters, result, value);
            }
        } else if (parameters.getPosition().getParent() instanceof PyStringLiteralExpression
                && parameters.getPosition().getParent().getParent() instanceof PyArgumentList
                && parameters.getPosition().getParent().getParent().getParent() instanceof PyCallExpression) {
            String callExpressionName = ((PyCallExpression) parameters.getPosition().getParent().getParent().getParent()).getCallee().getText();
            if (OdooModel.ODOO_MODEL_NAME_FIELD_NAMES.contains(callExpressionName)) {
                //firstChild() returns the bracket
                PsiElement firstChild = parameters.getPosition().getParent().getParent().getChildren()[0];
                if (firstChild == parameters.getPosition().getParent()) {
                    String value = getStringValue(parameters, expressionWithDummy);
                    suggestModelName(parameters, result, value);
                }
            }
        } else if (parameters.getPosition().getParent() instanceof PyStringLiteralExpression
                && parameters.getPosition().getParent().getParent() instanceof PyKeywordArgument
                && "comodel_name".equals(((PyKeywordArgument) parameters.getPosition().getParent().getParent()).getKeyword())) {
            String callExpressionName = ((PyCallExpression) parameters.getPosition().getParent().getParent().getParent().getParent()).getCallee().getText();
            if (OdooModel.ODOO_MODEL_NAME_FIELD_NAMES.contains(callExpressionName)) {
                String value = getStringValue(parameters, expressionWithDummy);
                suggestModelName(parameters, result, value);
            }
        }

    }

    private void suggestModelName(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result, String value) {
        OdooModelService modelService = ServiceManager.getService(parameters.getOriginalFile().getProject(), OdooModelService.class);
        for (OdooModel model : modelService.getModels()) {
            // TODO return models not modules
            if (model.getName() != null && model.getName().startsWith(value)) {
                for (OdooModule module : model.getModules()) {
                    // TODO customize path for model definition
                    LookupElementBuilder element = LookupElementBuilder
                            .createWithSmartPointer(model.getName(), module.getDirectory())
                            .withIcon(module.getIcon())
                            .withTailText(" " + module.getRelativeLocationString(), true);
                    result.addElement(element);
                }
            }
        }
    }

    @NotNull
    private String getStringValue(@NotNull CompletionParameters parameters, String expressionWithDummy) {
        TextRange contentRange = ((PyStringElement) parameters.getPosition()).getContentRange();
        String content = expressionWithDummy.substring(contentRange.getStartOffset(), contentRange.getEndOffset());
        return content.replace(CompletionUtilCore.DUMMY_IDENTIFIER, "");
    }
}
