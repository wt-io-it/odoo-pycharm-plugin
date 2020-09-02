package at.wtioit.intellij.plugins.odoo.models.search;

import at.wtioit.intellij.plugins.odoo.icons.OdooPluginIcons;
import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.search.OdooSEResult;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.ui.LayeredIcon;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.python.psi.PyClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Set;

public class OdooModelPsiElement implements OdooModel, PsiElement, OdooSEResult {

    private final OdooModel model;
    private final Project project;
    private Icon icon;

    public OdooModelPsiElement(OdooModel forModel, Project forProject) {
        model = forModel;
        project = forProject;
    }

    @Override
    public @Nullable String getName() {
        return model.getName();
    }

    @Override
    public String getLocationString() {
        ItemPresentation presentation = getContainingFile().getPresentation();
        if (presentation != null) {
            PsiElement definingElement = getDefiningElement();
            String locationString = presentation.getLocationString();
            if (definingElement instanceof PyClass && locationString != null) {
                return locationString.replace("(", "(" + ((PyClass) definingElement).getName() + " in ");
            }
            return locationString;
        }
        return null;
    }

    @Override
    public Set<OdooModule> getModules() {
        return model.getModules();
    }

    @NotNull
    @Override
    public PsiElement getDefiningElement() {
        return model.getDefiningElement();
    }

    @Override
    public OdooModule getBaseModule() {
        return model.getBaseModule();
    }

    @Override
    public @NotNull Project getProject() throws PsiInvalidElementAccessException {
        return model.getDefiningElement().getProject();
    }

    @Override
    public @NotNull Language getLanguage() {
        return model.getDefiningElement().getLanguage();
    }

    @Override
    public PsiManager getManager() {
        return model.getDefiningElement().getManager();
    }

    @Override
    public @NotNull PsiElement[] getChildren() {
        return model.getDefiningElement().getChildren();
    }

    @Override
    public PsiElement getParent() {
        return model.getDefiningElement().getParent();
    }

    @Override
    public PsiElement getFirstChild() {
        return model.getDefiningElement().getFirstChild();
    }

    @Override
    public PsiElement getLastChild() {
        return model.getDefiningElement().getLastChild();
    }

    @Override
    public PsiElement getNextSibling() {
        return model.getDefiningElement().getNextSibling();
    }

    @Override
    public PsiElement getPrevSibling() {
        return model.getDefiningElement().getPrevSibling();
    }

    @Override
    public PsiFile getContainingFile() throws PsiInvalidElementAccessException {
        return model.getDefiningElement().getContainingFile();
    }

    @Override
    public TextRange getTextRange() {
        return model.getDefiningElement().getTextRange();
    }

    @Override
    public int getStartOffsetInParent() {
        return model.getDefiningElement().getStartOffsetInParent();
    }

    @Override
    public int getTextLength() {
        return model.getDefiningElement().getTextLength();
    }

    @Override
    public @Nullable PsiElement findElementAt(int offset) {
        return model.getDefiningElement().findElementAt(offset);
    }

    @Override
    public @Nullable PsiReference findReferenceAt(int offset) {
        return model.getDefiningElement().findReferenceAt(offset);
    }

    @Override
    public int getTextOffset() {
        return model.getDefiningElement().getTextOffset();
    }

    @Override
    public String getText() {
        return model.getDefiningElement().getText();
    }

    @Override
    public @NotNull char[] textToCharArray() {
        return model.getDefiningElement().textToCharArray();
    }

    @Override
    public PsiElement getNavigationElement() {
        return model.getDefiningElement().getNavigationElement();
    }

    @Override
    public PsiElement getOriginalElement() {
        return model.getDefiningElement().getOriginalElement();
    }

    @Override
    public boolean textMatches(@NotNull CharSequence text) {
        return model.getDefiningElement().textMatches(text);
    }

    @Override
    public boolean textMatches(@NotNull PsiElement element) {
        return model.getDefiningElement().textMatches(element);
    }

    @Override
    public boolean textContains(char c) {
        return model.getDefiningElement().textContains(c);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        model.getDefiningElement().accept(visitor);
    }

    @Override
    public void acceptChildren(@NotNull PsiElementVisitor visitor) {
        model.getDefiningElement().acceptChildren(visitor);
    }

    @Override
    public PsiElement copy() {
        return model.getDefiningElement().copy();
    }

    @Override
    public PsiElement add(@NotNull PsiElement element) throws IncorrectOperationException {
        return model.getDefiningElement().add(element);
    }

    @Override
    public PsiElement addBefore(@NotNull PsiElement element, @Nullable PsiElement anchor) throws IncorrectOperationException {
        return model.getDefiningElement().addBefore(element, anchor);
    }

    @Override
    public PsiElement addAfter(@NotNull PsiElement element, @Nullable PsiElement anchor) throws IncorrectOperationException {
        return model.getDefiningElement().addAfter(element, anchor);
    }

    @Override
    public void checkAdd(@NotNull PsiElement element) throws IncorrectOperationException {
        model.getDefiningElement().checkAdd(element);
    }

    @Override
    public PsiElement addRange(PsiElement first, PsiElement last) throws IncorrectOperationException {
        return model.getDefiningElement().addRange(first, last);
    }

    @Override
    public PsiElement addRangeBefore(@NotNull PsiElement first, @NotNull PsiElement last, PsiElement anchor) throws IncorrectOperationException {
        return model.getDefiningElement().addRangeBefore(first, last, anchor);
    }

    @Override
    public PsiElement addRangeAfter(PsiElement first, PsiElement last, PsiElement anchor) throws IncorrectOperationException {
        return model.getDefiningElement().addRangeAfter(first, last, anchor);
    }

    @Override
    public void delete() throws IncorrectOperationException {
        model.getDefiningElement().delete();
    }

    @Override
    public void checkDelete() throws IncorrectOperationException {
        model.getDefiningElement().checkDelete();
    }

    @Override
    public void deleteChildRange(PsiElement first, PsiElement last) throws IncorrectOperationException {
        model.getDefiningElement().deleteChildRange(first, last);
    }

    @Override
    public PsiElement replace(@NotNull PsiElement newElement) throws IncorrectOperationException {
        return model.getDefiningElement().replace(newElement);
    }

    @Override
    public boolean isValid() {
        return model.getDefiningElement().isValid();
    }

    @Override
    public boolean isWritable() {
        return model.getDefiningElement().isWritable();
    }

    @Override
    public @Nullable PsiReference getReference() {
        return model.getDefiningElement().getReference();
    }

    @Override
    public @NotNull PsiReference[] getReferences() {
        return model.getDefiningElement().getReferences();
    }

    @Override
    public <T> @Nullable T getCopyableUserData(Key<T> key) {
        return model.getDefiningElement().getCopyableUserData(key);
    }

    @Override
    public <T> void putCopyableUserData(Key<T> key, @Nullable T value) {
        model.getDefiningElement().putCopyableUserData(key, value);

    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, @Nullable PsiElement lastParent, @NotNull PsiElement place) {
        return model.getDefiningElement().processDeclarations(processor, state, lastParent, place);
    }

    @Override
    public @Nullable PsiElement getContext() {
        return model.getDefiningElement().getContext();
    }

    @Override
    public boolean isPhysical() {
        return model.getDefiningElement().isPhysical();
    }

    @Override
    public @NotNull GlobalSearchScope getResolveScope() {
        return model.getDefiningElement().getResolveScope();
    }

    @Override
    public @NotNull SearchScope getUseScope() {
        return model.getDefiningElement().getUseScope();
    }

    @Override
    public ASTNode getNode() {
        return model.getDefiningElement().getNode();
    }

    @Override
    public boolean isEquivalentTo(PsiElement another) {
        return model.getDefiningElement().isEquivalentTo(another);
    }

    @Override
    public Icon getIcon(int flags) {
        if (icon == null) {
            Icon baseIcon = model.getDefiningElement().getIcon(flags);
            icon = new LayeredIcon(baseIcon, OdooPluginIcons.ODOO_OVERLAY_ICON);
        }
        return icon;
    }

    @Override
    public <T> @Nullable T getUserData(@NotNull Key<T> key) {
        return model.getDefiningElement().getUserData(key);
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {
        model.getDefiningElement().putUserData(key, value);
    }
}
