package at.wtioit.intellij.plugins.odoo;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

public class OdooAddonsCompletionContributor extends CompletionContributor {
    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        PsiElement dot = parameters.getPosition().getPrevSibling();
        if (dot != null
                && dot.getText().equals(".")
                && dot.getParent().getText().substring(0, dot.getStartOffsetInParent()).equals("odoo.addons")) {
            Project project = parameters.getOriginalFile().getProject();
            GlobalSearchScope scope = GlobalSearchScope.allScope(project);
            for (PsiFile file : FilenameIndex.getFilesByName(project, "__manifest__.py", scope)) {
                PsiDirectory moduleDir = file.getParent();
                ItemPresentation presentation = moduleDir.getPresentation();
                LookupElementBuilder element = LookupElementBuilder
                        .createWithSmartPointer(moduleDir.getName(), moduleDir)
                        .withIcon(moduleDir.getIcon(0))
                        .withTailText(" " + presentation.getLocationString(), true);
                        // TODO add insert handler if used in code (not import statement)?
                result.addElement(element);
            }
        }
    }
}
