package at.wtioit.intellij.plugins.odoo.models.inspection;

import at.wtioit.intellij.plugins.odoo.OdooBundle;
import at.wtioit.intellij.plugins.odoo.OdooModelPsiElementMatcherUtil;
import at.wtioit.intellij.plugins.odoo.models.OdooModelService;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.python.psi.PyElementVisitor;
import com.jetbrains.python.psi.PyStringElement;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class MissingModelDefinitionInspection extends LocalInspectionTool {


    @Nls(capitalization = Nls.Capitalization.Sentence)
    @Override
    public @NotNull String getDisplayName() {
        return OdooBundle.message("INSP.NAME.missing.model.definition");
    }

    @NotNull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.ERROR;
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        final OdooModelService modelService = OdooModelService.getInstance(holder.getProject());
        return new PyElementVisitor() {

            @Override
            public void visitElement(PsiElement element) {
                super.visitElement(element);
                if (element instanceof PyStringElement) {
                    if (OdooModelPsiElementMatcherUtil.isOdooModelPsiElement(element)) {
                        TextRange contentRange = ((PyStringElement) element).getContentRange();
                        String modelName = element.getText().substring(contentRange.getStartOffset(), contentRange.getEndOffset());
                        if (!modelService.hasModel(modelName)) {
                            holder.registerProblem(element, OdooBundle.message("INSP.NAME.missing.model.definition.for.$0", modelName), ProblemHighlightType.ERROR);
                            // TODO add possible quick fixes
                        }
                    }
                }
            }

            @Override
            public void visitPyStringLiteralExpression(PyStringLiteralExpression element) {
                super.visitPyStringLiteralExpression(element);
                if (OdooModelPsiElementMatcherUtil.isOdooModelPsiElement(element)) {
                    String modelName = element.getStringValue();
                    if (!modelService.hasModel(modelName)) {
                        holder.registerProblem(element, OdooBundle.message("INSP.NAME.missing.model.definition.for.$0", modelName), ProblemHighlightType.ERROR);
                        // TODO add possible quick fixes
                    }
                }
            }

        };
    }
}
