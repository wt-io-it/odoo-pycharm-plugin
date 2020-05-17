package at.wtioit.intellij.plugins.odoo.modules.search;

import at.wtioit.intellij.plugins.odoo.WithinProject;
import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.List;

public class OdooModulePsiElement implements OdooModule, PsiElement {

    private final OdooModule module;
    private final Project project;

    OdooModulePsiElement(OdooModule forModule, Project forProject) {
        module = forModule;
        project = forProject;
    }

    @Override
    public @NotNull String getName() {
        return module.getName();
    }

    @Override
    public @Nullable PsiElement getDirectory() {
        return module.getDirectory();
    }

    @Override
    public @NotNull String getPath() {
        return module.getPath();
    }

    @Override
    public @Nullable Icon getIcon() {
        return module.getIcon();
    }

    @Override
    public @Nullable String getRelativeLocationString() {
        return module.getRelativeLocationString();
    }

    @Override
    public @NotNull Collection<OdooModule> getDependencies() {
        return module.getDependencies();
    }

    @Override
    public @NotNull List<OdooModel> getModels() {
        return module.getModels();
    }

    @Override
    public void setModels(List<OdooModel> models) {
        module.setModels(models);
    }

    @Override
    public boolean dependsOn(OdooModule module) {
        return module.dependsOn(module);
    }

    @Override
    public @NotNull Project getProject() throws PsiInvalidElementAccessException {
        return module.getDirectory().getProject();
    }

    @Override
    public @NotNull Language getLanguage() {
        return module.getDirectory().getLanguage();
    }

    @Override
    public PsiManager getManager() {
        return module.getDirectory().getManager();
    }

    @Override
    public @NotNull PsiElement[] getChildren() {
        return module.getDirectory().getChildren();
    }

    @Override
    public PsiElement getParent() {
        return module.getDirectory().getParent();
    }

    @Override
    public PsiElement getFirstChild() {
        return module.getDirectory().getParent();
    }

    @Override
    public PsiElement getLastChild() {
        return module.getDirectory().getLastChild();
    }

    @Override
    public PsiElement getNextSibling() {
        return module.getDirectory().getNextSibling();
    }

    @Override
    public PsiElement getPrevSibling() {
        return module.getDirectory().getPrevSibling();
    }

    @Override
    public PsiFile getContainingFile() throws PsiInvalidElementAccessException {
        try {
            WithinProject.INSTANCE.set(project);
            return module.getDirectory().getContainingFile();
        } finally {
            WithinProject.INSTANCE.remove();
        }
    }

    @Override
    public TextRange getTextRange() {
        return module.getDirectory().getTextRange();
    }

    @Override
    public int getStartOffsetInParent() {
        return module.getDirectory().getStartOffsetInParent();
    }

    @Override
    public int getTextLength() {
        return module.getDirectory().getTextLength();
    }

    @Override
    public @Nullable PsiElement findElementAt(int offset) {
        return module.getDirectory().findElementAt(offset);
    }

    @Override
    public @Nullable PsiReference findReferenceAt(int offset) {
        return module.getDirectory().findReferenceAt(offset);
    }

    @Override
    public int getTextOffset() {
        return module.getDirectory().getTextOffset();
    }

    @Override
    public String getText() {
        return module.getDirectory().getText();
    }

    @Override
    public @NotNull char[] textToCharArray() {
        return module.getDirectory().textToCharArray();
    }

    @Override
    public PsiElement getNavigationElement() {
        return module.getDirectory().getNavigationElement();
    }

    @Override
    public PsiElement getOriginalElement() {
        return module.getDirectory().getOriginalElement();
    }

    @Override
    public boolean textMatches(@NotNull CharSequence text) {
        return module.getDirectory().textMatches(text);
    }

    @Override
    public boolean textMatches(@NotNull PsiElement element) {
        return module.getDirectory().textMatches(element);
    }

    @Override
    public boolean textContains(char c) {
        return module.getDirectory().textContains(c);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        module.getDirectory().accept(visitor);
    }

    @Override
    public void acceptChildren(@NotNull PsiElementVisitor visitor) {
        module.getDirectory().acceptChildren(visitor);
    }

    @Override
    public PsiElement copy() {
        return module.getDirectory().copy();
    }

    @Override
    public PsiElement add(@NotNull PsiElement element) throws IncorrectOperationException {
        return module.getDirectory().add(element);
    }

    @Override
    public PsiElement addBefore(@NotNull PsiElement element, @Nullable PsiElement anchor) throws IncorrectOperationException {
        return module.getDirectory().addBefore(element, anchor);
    }

    @Override
    public PsiElement addAfter(@NotNull PsiElement element, @Nullable PsiElement anchor) throws IncorrectOperationException {
        return module.getDirectory().addAfter(element, anchor);
    }

    @Override
    public void checkAdd(@NotNull PsiElement element) throws IncorrectOperationException {
        module.getDirectory().checkAdd(element);
    }

    @Override
    public PsiElement addRange(PsiElement first, PsiElement last) throws IncorrectOperationException {
        return module.getDirectory().addRange(first, last);
    }

    @Override
    public PsiElement addRangeBefore(@NotNull PsiElement first, @NotNull PsiElement last, PsiElement anchor) throws IncorrectOperationException {
        return module.getDirectory().addRangeBefore(first, last, anchor);
    }

    @Override
    public PsiElement addRangeAfter(PsiElement first, PsiElement last, PsiElement anchor) throws IncorrectOperationException {
        return module.getDirectory().addRangeAfter(first, last, anchor);
    }

    @Override
    public void delete() throws IncorrectOperationException {
        module.getDirectory().delete();
    }

    @Override
    public void checkDelete() throws IncorrectOperationException {
        module.getDirectory().checkDelete();
    }

    @Override
    public void deleteChildRange(PsiElement first, PsiElement last) throws IncorrectOperationException {
        module.getDirectory().deleteChildRange(first, last);
    }

    @Override
    public PsiElement replace(@NotNull PsiElement newElement) throws IncorrectOperationException {
        return module.getDirectory().replace(newElement);
    }

    @Override
    public boolean isValid() {
        return module.getDirectory().isValid();
    }

    @Override
    public boolean isWritable() {
        return module.getDirectory().isWritable();
    }

    @Override
    public @Nullable PsiReference getReference() {
        return module.getDirectory().getReference();
    }

    @Override
    public @NotNull PsiReference[] getReferences() {
        return module.getDirectory().getReferences();
    }

    @Override
    public <T> @Nullable T getCopyableUserData(Key<T> key) {
        return module.getDirectory().getCopyableUserData(key);
    }

    @Override
    public <T> void putCopyableUserData(Key<T> key, @Nullable T value) {
        module.getDirectory().putCopyableUserData(key, value);
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, @Nullable PsiElement lastParent, @NotNull PsiElement place) {
        return module.getDirectory().processDeclarations(processor, state, lastParent, place);
    }

    @Override
    public @Nullable PsiElement getContext() {
        return module.getDirectory().getContext();
    }

    @Override
    public boolean isPhysical() {
        return module.getDirectory().isPhysical();
    }

    @Override
    public @NotNull GlobalSearchScope getResolveScope() {
        return module.getDirectory().getResolveScope();
    }

    @Override
    public @NotNull SearchScope getUseScope() {
        return module.getDirectory().getUseScope();
    }

    @Override
    public ASTNode getNode() {
        return module.getDirectory().getNode();
    }

    @Override
    public boolean isEquivalentTo(PsiElement another) {
        return module.getDirectory().isEquivalentTo(another);
    }

    @Override
    public Icon getIcon(int flags) {
        return module.getIcon();
    }

    @Override
    public <T> @Nullable T getUserData(@NotNull Key<T> key) {
        return module.getDirectory().getUserData(key);
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {
        module.getDirectory().putUserData(key, value);
    }
}
