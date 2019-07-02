package at.wtioit.intellij.plugins.odoo;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionUtilCore;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.SystemIndependent;

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
            Project project = parameters.getOriginalFile().getProject();
            GlobalSearchScope scope = GlobalSearchScope.allScope(project);
            for (PsiFile file : FilenameIndex.getFilesByName(project, "__manifest__.py", scope)) {
                PsiDirectory moduleDir = file.getParent();
                if (moduleDir != null
                        && !"base".equals(moduleDir.getName())
                        && moduleDir.getName().startsWith(addonNameStart)
                        // TODO probably this exclude should be done via scope
                        && !moduleDir.toString().contains("/remote_sources/")) {
                    ItemPresentation presentation = moduleDir.getPresentation();
                    LookupElementBuilder element = LookupElementBuilder
                            .createWithSmartPointer(moduleDir.getName(), moduleDir)
                            .withIcon(moduleDir.getIcon(0))
                            .withTailText(" " + getRelativeLocationString(project, presentation), true);
                    // TODO add insert handler if used in code (not import statement)?
                    result.addElement(element);
                }
            }
        }
    }

    private String getRelativeLocationString(Project project, ItemPresentation presentation) {
        String locationString = presentation.getLocationString();
        @SystemIndependent String basePath = project.getBasePath();
        if (locationString.startsWith(basePath)) {
            return locationString.substring(basePath.length() + 1);
        }
        return locationString;
    }
}
