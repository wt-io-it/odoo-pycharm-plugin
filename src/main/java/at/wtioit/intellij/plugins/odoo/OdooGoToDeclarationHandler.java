package at.wtioit.intellij.plugins.odoo;

import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.models.OdooModelService;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import at.wtioit.intellij.plugins.odoo.records.OdooRecord;
import at.wtioit.intellij.plugins.odoo.records.OdooRecordService;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandlerBase;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlToken;
import com.jetbrains.python.psi.PyAssignmentStatement;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyStringElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static at.wtioit.intellij.plugins.odoo.PsiElementsUtil.findParent;

public class OdooGoToDeclarationHandler extends GotoDeclarationHandlerBase {

    private final ThreadLocal<Integer> currentOffset = new ThreadLocal<>();

    public PsiElement[] getGotoDeclarationTargets(@Nullable PsiElement sourceElement, int offset, Editor editor) {
        currentOffset.set(offset);
        try {
            return super.getGotoDeclarationTargets(sourceElement, offset, editor);
        } finally {
            currentOffset.set(null);
        }
    }

    @Override
    public @Nullable PsiElement getGotoDeclarationTarget(@Nullable PsiElement psiElement, Editor editor) {
        if (OdooModelPsiElementMatcherUtil.isOdooModelPsiElement(psiElement)) {
            if (psiElement instanceof PyStringElement) {
                PyStringElement pyStringElement = (PyStringElement) psiElement;
                PyClass inClass = PsiElementsUtil.findParent(pyStringElement, PyClass.class);
                PsiElement odooModelElement = getOdooModel(pyStringElement);
                if (inClass == odooModelElement) {
                    PyAssignmentStatement assignmentStatement = findParent(pyStringElement, PyAssignmentStatement.class, 4);
                    if (assignmentStatement != null) {
                        String variableName = assignmentStatement.getFirstChild().getText();
                        if ("_name".equals(variableName)) {
                            // avoid resolving itself for _name
                            return null;
                        }
                    }
                }
                return odooModelElement;
            } else if (psiElement instanceof XmlToken) {
                return getOdooModel(psiElement.getProject(), psiElement.getText());
            }
        } else if (OdooModulePsiElementMatcherUtil.isOdooModulePsiElement(psiElement)) {
            if (psiElement instanceof PyStringElement) {
                return getOdooModule((PyStringElement) psiElement);
            }
        } else if (OdooRecordPsiElementMatcherUtil.isOdooRecordPsiElement(psiElement)) {
            if (psiElement instanceof XmlToken) {
                return getOdooRecord((XmlToken) psiElement);
            } else if (psiElement instanceof PyStringElement) {
                return getOdooRecord((PyStringElement) psiElement);
            }
        } else if (OdooRecordPsiElementMatcherUtil.holdsOdooRecordReference(psiElement)) {
            if (psiElement instanceof XmlToken) {
                return getOdooRecord((XmlToken) psiElement);
            }
        }
        return null;
    }

    private PsiElement getOdooRecord(XmlToken psiElement) {
        String refName;
        XmlAttribute parent = findParent(psiElement, XmlAttribute.class, 2);
        if (parent != null && "eval".equals(parent.getName())) {
            String text = psiElement.getText();
            int positionInText = currentOffset.get() - psiElement.getTextOffset();
            String beforeCursor = psiElement.getText().substring(0, positionInText);
            int indexOfRef = beforeCursor.lastIndexOf("ref(");
            char quote = beforeCursor.charAt(indexOfRef + 4);
            int indexEndRef = text.indexOf(quote, positionInText);
            refName = text.substring(indexOfRef + 5, indexEndRef);
        } else {
            refName = psiElement.getText();
        }
        return getOdooRecord(psiElement.getProject(), psiElement.getContainingFile(), refName);
    }

    private PsiElement getOdooRecord(@NotNull PyStringElement psiElement) {
        String refName = getPyStringValue(psiElement);
        return getOdooRecord(psiElement.getProject(), psiElement.getContainingFile(), refName);
    }

    private PsiElement getOdooRecord(Project project, PsiFile file, String refName) {
        OdooRecordService recordService = project.getService(OdooRecordService.class);
        refName = recordService.ensureFullXmlId(file, refName);
        OdooRecord record = recordService.getRecord(refName);
        if (record != null) {
            return WithinProject.call(project, record::getDefiningElement);
        }
        return null;
    }

    private PsiElement getOdooModule(PyStringElement pyString) {
        String value = getPyStringValue(pyString);
        OdooModuleService moduleService = pyString.getProject().getService(OdooModuleService.class);
        OdooModule module = moduleService.getModule(value);
        if (module != null) {
            return WithinProject.call(pyString.getProject(), module::getNavigationItem);
        }
        return null;
    }

    @Nullable
    private PsiElement getOdooModel(@NotNull PyStringElement pyString) {
        String value = getPyStringValue(pyString);
        return getOdooModel(pyString.getContainingFile().getProject(), value);
    }

    @NotNull
    private String getPyStringValue(@NotNull PyStringElement pyString) {
        TextRange range = pyString.getContentRange();
        String value = pyString.getText().substring(range.getStartOffset(), range.getEndOffset());
        return value;
    }

    @Nullable
    private  PsiElement getOdooModel(@NotNull Project project, @NotNull String value) {
        OdooModelService modelService = project.getService(OdooModelService.class);
        OdooModel model = modelService.getModel(value);
        if (model != null) {
            return model.getDefiningElement();
        }
        return null;
    }


}
