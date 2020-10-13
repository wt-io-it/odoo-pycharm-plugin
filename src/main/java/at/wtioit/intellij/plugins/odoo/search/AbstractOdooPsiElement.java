package at.wtioit.intellij.plugins.odoo.search;

import at.wtioit.intellij.plugins.odoo.WithinProject;
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

import java.util.function.Supplier;

public abstract class AbstractOdooPsiElement implements OdooSEResult {
    final Supplier<PsiElement> psiElementSupplier;
    final Project project;

    protected AbstractOdooPsiElement(@NotNull Project project, Supplier<PsiElement> psiElementSupplier) {
        this.psiElementSupplier = psiElementSupplier;
        this.project = project;
    }

    @Override
    public @NotNull Language getLanguage() {
        return psiElementSupplier.get().getLanguage();
    }

    @Override
    public PsiManager getManager() {
        return psiElementSupplier.get().getManager();
    }

    @Override
    public @NotNull PsiElement[] getChildren() {
        return psiElementSupplier.get().getChildren();
    }

    @Override
    public PsiElement getParent() {
        return psiElementSupplier.get().getParent();
    }

    @Override
    public PsiElement getFirstChild() {
        return psiElementSupplier.get().getParent();
    }

    @Override
    public PsiElement getLastChild() {
        return psiElementSupplier.get().getLastChild();
    }

    @Override
    public PsiElement getNextSibling() {
        return psiElementSupplier.get().getNextSibling();
    }

    @Override
    public PsiElement getPrevSibling() {
        return psiElementSupplier.get().getPrevSibling();
    }

    @Override
    public PsiFile getContainingFile() throws PsiInvalidElementAccessException {
        return WithinProject.call(project, () -> psiElementSupplier.get().getContainingFile());
    }

    @Override
    public TextRange getTextRange() {
        return psiElementSupplier.get().getTextRange();
    }

    @Override
    public int getStartOffsetInParent() {
        return psiElementSupplier.get().getStartOffsetInParent();
    }

    @Override
    public int getTextLength() {
        return psiElementSupplier.get().getTextLength();
    }

    @Override
    public @Nullable PsiElement findElementAt(int offset) {
        return psiElementSupplier.get().findElementAt(offset);
    }

    @Override
    public @Nullable PsiReference findReferenceAt(int offset) {
        return psiElementSupplier.get().findReferenceAt(offset);
    }

    @Override
    public int getTextOffset() {
        return psiElementSupplier.get().getTextOffset();
    }

    @Override
    public String getText() {
        return psiElementSupplier.get().getText();
    }

    @Override
    public @NotNull char[] textToCharArray() {
        return psiElementSupplier.get().textToCharArray();
    }

    @Override
    public PsiElement getNavigationElement() {
        return psiElementSupplier.get().getNavigationElement();
    }

    @Override
    public PsiElement getOriginalElement() {
        return psiElementSupplier.get().getOriginalElement();
    }

    @Override
    public boolean textMatches(@NotNull CharSequence text) {
        return psiElementSupplier.get().textMatches(text);
    }

    @Override
    public boolean textMatches(@NotNull PsiElement element) {
        return psiElementSupplier.get().textMatches(element);
    }

    @Override
    public boolean textContains(char c) {
        return psiElementSupplier.get().textContains(c);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        psiElementSupplier.get().accept(visitor);
    }

    @Override
    public void acceptChildren(@NotNull PsiElementVisitor visitor) {
        psiElementSupplier.get().acceptChildren(visitor);
    }

    @Override
    public PsiElement copy() {
        return psiElementSupplier.get().copy();
    }

    @Override
    public PsiElement add(@NotNull PsiElement element) throws IncorrectOperationException {
        return psiElementSupplier.get().add(element);
    }

    @Override
    public PsiElement addBefore(@NotNull PsiElement element, @Nullable PsiElement anchor) throws IncorrectOperationException {
        return psiElementSupplier.get().addBefore(element, anchor);
    }

    @Override
    public PsiElement addAfter(@NotNull PsiElement element, @Nullable PsiElement anchor) throws IncorrectOperationException {
        return psiElementSupplier.get().addAfter(element, anchor);
    }

    @Override
    public void checkAdd(@NotNull PsiElement element) throws IncorrectOperationException {
        psiElementSupplier.get().checkAdd(element);
    }

    @Override
    public PsiElement addRange(PsiElement first, PsiElement last) throws IncorrectOperationException {
        return psiElementSupplier.get().addRange(first, last);
    }

    @Override
    public PsiElement addRangeBefore(@NotNull PsiElement first, @NotNull PsiElement last, PsiElement anchor) throws IncorrectOperationException {
        return psiElementSupplier.get().addRangeBefore(first, last, anchor);
    }

    @Override
    public PsiElement addRangeAfter(PsiElement first, PsiElement last, PsiElement anchor) throws IncorrectOperationException {
        return psiElementSupplier.get().addRangeAfter(first, last, anchor);
    }

    @Override
    public void delete() throws IncorrectOperationException {
        psiElementSupplier.get().delete();
    }

    @Override
    public void checkDelete() throws IncorrectOperationException {
        psiElementSupplier.get().checkDelete();
    }

    @Override
    public void deleteChildRange(PsiElement first, PsiElement last) throws IncorrectOperationException {
        psiElementSupplier.get().deleteChildRange(first, last);
    }

    @Override
    public PsiElement replace(@NotNull PsiElement newElement) throws IncorrectOperationException {
        return psiElementSupplier.get().replace(newElement);
    }

    @Override
    public boolean isValid() {
        return WithinProject.call(project, () -> {
            return psiElementSupplier.get().isValid();
        });
    }

    @Override
    public boolean isWritable() {
        return psiElementSupplier.get().isWritable();
    }

    @Override
    public @Nullable PsiReference getReference() {
        return psiElementSupplier.get().getReference();
    }

    @Override
    public @NotNull PsiReference[] getReferences() {
        return psiElementSupplier.get().getReferences();
    }

    @Override
    public <T> @Nullable T getCopyableUserData(Key<T> key) {
        return psiElementSupplier.get().getCopyableUserData(key);
    }

    @Override
    public <T> void putCopyableUserData(Key<T> key, @Nullable T value) {
        psiElementSupplier.get().putCopyableUserData(key, value);
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, @Nullable PsiElement lastParent, @NotNull PsiElement place) {
        return psiElementSupplier.get().processDeclarations(processor, state, lastParent, place);
    }

    @Override
    public @Nullable PsiElement getContext() {
        return psiElementSupplier.get().getContext();
    }

    @Override
    public boolean isPhysical() {
        return psiElementSupplier.get().isPhysical();
    }

    @Override
    public @NotNull GlobalSearchScope getResolveScope() {
        return psiElementSupplier.get().getResolveScope();
    }

    @Override
    public @NotNull SearchScope getUseScope() {
        return psiElementSupplier.get().getUseScope();
    }

    @Override
    public ASTNode getNode() {
        return psiElementSupplier.get().getNode();
    }

    @Override
    public boolean isEquivalentTo(PsiElement another) {
        return psiElementSupplier.get().isEquivalentTo(another);
    }

    @Override
    public <T> @Nullable T getUserData(@NotNull Key<T> key) {
        return psiElementSupplier.get().getUserData(key);
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {
        psiElementSupplier.get().putUserData(key, value);
    }

    @Override
    public @NotNull Project getProject() throws PsiInvalidElementAccessException {
        return project;
    }
}
