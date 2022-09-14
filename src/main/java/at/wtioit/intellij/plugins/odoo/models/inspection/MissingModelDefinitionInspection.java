package at.wtioit.intellij.plugins.odoo.models.inspection;

import at.wtioit.intellij.plugins.odoo.OdooBundle;
import at.wtioit.intellij.plugins.odoo.OdooModelPsiElementMatcherUtil;
import at.wtioit.intellij.plugins.odoo.PsiElementsUtil;
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

import static at.wtioit.intellij.plugins.odoo.OdooModelPsiElementMatcherUtil.*;
import static at.wtioit.intellij.plugins.odoo.PsiElementsUtil.findParent;

public class MissingModelDefinitionInspection extends LocalInspectionTool {


    @Nls(capitalization = Nls.Capitalization.Sentence)
    @Override
    public @NotNull String getDisplayName() {
        return OdooBundle.message("INSP.NAME.missing.model.definition");
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getGroupDisplayName() {
        return OdooBundle.message("INSP.GROUP.odoo");
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
                    if (isOdooModelPsiElement(element) && !isPartOfExpression(element) && !isUnresolvableOdooModelNameDefinitionPsiElement(element)) {
                        PyStringLiteralExpression expression = findParent(element, PyStringLiteralExpression.class,1);
                        String modelName;
                        if (expression != null) {
                            /* prefer getStringValue from parent as it also covers multiline strings like
                             * _name = "module." \
                             *         "model"
                             */
                            modelName = expression.getStringValue();
                        } else {
                            TextRange contentRange = ((PyStringElement) element).getContentRange();
                            modelName = element.getText().substring(contentRange.getStartOffset(), contentRange.getEndOffset());
                        }
                        if (!modelService.hasModel(modelName)) {
                            holder.registerProblem(element, OdooBundle.message("INSP.NAME.missing.model.definition.for.$0", modelName), ProblemHighlightType.ERROR);
                            // TODO add possible quick fixes
                        }
                    }
                }
            }

            @Override
            public void visitPyStringLiteralExpression(PyStringLiteralExpression element) {
                // TODO this might be covered by the method above
                super.visitPyStringLiteralExpression(element);
                if (isOdooModelPsiElement(element) && !isPartOfExpression(element) && !isUnresolvableOdooModelNameDefinitionPsiElement(element)) {
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
