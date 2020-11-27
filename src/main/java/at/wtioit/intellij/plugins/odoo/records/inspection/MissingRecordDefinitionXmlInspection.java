package at.wtioit.intellij.plugins.odoo.records.inspection;

import at.wtioit.intellij.plugins.odoo.OdooBundle;
import at.wtioit.intellij.plugins.odoo.OdooRecordPsiElementMatcherUtil;
import at.wtioit.intellij.plugins.odoo.PsiElementsUtil;
import at.wtioit.intellij.plugins.odoo.records.OdooRecordService;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.impl.source.xml.XmlTagImpl;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

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
                    String id = element.getText();
                    String xmlId = recordService.ensureFullXmlId(element.getContainingFile(), id);
                    if (!recordService.hasRecord(xmlId)) {
                        XmlTagImpl templateTag = PsiElementsUtil.findParent(element, XmlTagImpl.class, (tag) -> {
                            return "templates".equals(tag.getName());
                        });
                        if (templateTag != null) {
                            HashMap<String, XmlTag> localTemplates = new HashMap<>();
                            for (PsiElement child : templateTag.getChildren()) {
                                if (child instanceof XmlTag) {
                                    if ("t".equals(((XmlTag) child).getName())) {
                                        XmlAttribute nameAttribute = ((XmlTag) child).getAttribute("t-name");
                                        if (nameAttribute != null) {
                                            String templateName = nameAttribute.getValue();
                                            if (id.equals(templateName) || xmlId.equals(templateName)) {
                                                // We found the template
                                                return;
                                            }
                                            localTemplates.put(templateName, (XmlTag) child);
                                        }
                                    }
                                }
                            }
                            // TODO add index for those local names
                        }

                        holder.registerProblem(element, OdooBundle.message("INSP.NAME.missing.record.definition.for.$0", xmlId), ProblemHighlightType.WARNING);
                        // TODO add possible quick fixes
                    }
                }
            }
        };
    }
}
