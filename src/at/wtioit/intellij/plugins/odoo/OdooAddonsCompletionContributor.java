package at.wtioit.intellij.plugins.odoo;

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
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.python.psi.PyAssignmentStatement;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.SystemIndependent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class OdooAddonsCompletionContributor extends CompletionContributor {

    private static final Set<String> ODOO_MODEL_NAME_VARIABLE_NAME = new HashSet<>(Arrays.asList("_inherit", "_name"));

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        OdooModuleService moduleService = ServiceManager.getService(parameters.getOriginalFile().getProject(), OdooModuleService.class);
        String expressionWithDummy = parameters.getPosition().getParent().getText();
        String fqdn = expressionWithDummy.substring(0, expressionWithDummy.length() - CompletionUtilCore.DUMMY_IDENTIFIER.length() + 1);
        if (fqdn.startsWith("odoo.addons.")) {
            PsiElement dot = parameters.getPosition().getPrevSibling();
            String addonNameStart = "";
            if (dot != null && dot.getText().equals(".")) {
                addonNameStart = fqdn.substring(dot.getStartOffsetInParent() + 1);
            }
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
            if (ODOO_MODEL_NAME_VARIABLE_NAME.contains(variableName)) {
                for (OdooModule module : moduleService.getModules()) {
                    // TODO return models not modules
                    if (true) {
                        LookupElementBuilder element = LookupElementBuilder
                                .createWithSmartPointer("lookupString", module.getDirectory())
                                .withIcon(module.getIcon())
                                .withTailText(" " + module.getRelativeLocationString(), true);
                        result.addElement(element);
                    }
                }
            }
        }
    }


}
