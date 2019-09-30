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
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.python.psi.PyAssignmentStatement;
import com.jetbrains.python.psi.PyStringElement;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.SystemIndependent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
                TextRange contentRange = ((PyStringElement) parameters.getPosition()).getContentRange();
                String content = expressionWithDummy.substring(contentRange.getStartOffset(), contentRange.getEndOffset());
                String value = content.replace(CompletionUtilCore.DUMMY_IDENTIFIER, "");
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
        }
    }


}
