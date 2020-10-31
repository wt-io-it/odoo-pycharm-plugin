package at.wtioit.intellij.plugins.odoo.records.inspection;

import at.wtioit.intellij.plugins.odoo.OdooBundle;
import at.wtioit.intellij.plugins.odoo.OdooRecordPsiElementMatcherUtil;
import at.wtioit.intellij.plugins.odoo.records.OdooRecordService;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.xml.XmlToken;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class MissingRecordDefinitionXmlInspection extends LocalInspectionTool {


    @Nls(capitalization = Nls.Capitalization.Sentence)
    @Override
    public @NotNull String getDisplayName() {
        return OdooBundle.message("INSP.NAME.missing.record.definition");
    }

    @NotNull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.ERROR;
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        final OdooRecordService recordService = ServiceManager.getService(holder.getProject(), OdooRecordService.class);
        return new XmlElementVisitor() {

            @Override
            public void visitXmlToken(XmlToken element) {
                super.visitXmlToken(element);
                if (OdooRecordPsiElementMatcherUtil.isOdooRecordPsiElement(element)) {
                    String xmlId = recordService.ensureFullXmlId(element.getContainingFile(), element.getText());
                    if (!recordService.hasRecord(xmlId)) {
                        holder.registerProblem(element, OdooBundle.message("INSP.NAME.missing.record.definition.for.$0", xmlId), ProblemHighlightType.WARNING);
                        // TODO add possible quick fixes
                    }
                }
            }
        };
    }
}
