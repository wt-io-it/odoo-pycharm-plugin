package at.wtioit.intellij.plugins.odoo.modules.search;

import at.wtioit.intellij.plugins.odoo.WithinProject;
import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.search.OdooSEResult;
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

public class OdooModulePsiElement implements OdooModule, PsiElement, OdooSEResult {

    private final OdooModule module;
    private final Project project;

    public OdooModulePsiElement(OdooModule forModule, Project forProject) {
        module = forModule;
        project = forProject;
    }

    @Override
    public @NotNull String getName() {
        return module.getName();
    }

    @Override
    public String getLocationString() {
        return getRelativeLocationString();
    }

    @Override
    public @Nullable PsiElement getDirectory() {
        return WithinProject.call(this.project, module::getDirectory);
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
    public boolean dependsOn(OdooModule module) {
        return module.dependsOn(module);
    }

    @Override
    public PsiFile getManifestFile() {
        return module.getManifestFile();
    }

    @Override
    public @NotNull Project getProject() throws PsiInvalidElementAccessException {
        return getDirectory().getProject();
    }

    @Override
    public @NotNull Language getLanguage() {
        return getDirectory().getLanguage();
    }

    @Override
    public PsiManager getManager() {
        return getDirectory().getManager();
    }

    @Override
    public @NotNull PsiElement[] getChildren() {
        return getDirectory().getChildren();
    }

    @Override
    public PsiElement getParent() {
        return getDirectory().getParent();
    }

    @Override
    public PsiElement getFirstChild() {
        return getDirectory().getParent();
    }

    @Override
    public PsiElement getLastChild() {
        return getDirectory().getLastChild();
    }

    @Override
    public PsiElement getNextSibling() {
        return getDirectory().getNextSibling();
    }

    @Override
    public PsiElement getPrevSibling() {
        return getDirectory().getPrevSibling();
    }

    @Override
    public PsiFile getContainingFile() throws PsiInvalidElementAccessException {
        return getDirectory().getContainingFile();
    }

    @Override
    public TextRange getTextRange() {
        return getDirectory().getTextRange();
    }

    @Override
    public int getStartOffsetInParent() {
        return getDirectory().getStartOffsetInParent();
    }

    @Override
    public int getTextLength() {
        return getDirectory().getTextLength();
    }

    @Override
    public @Nullable PsiElement findElementAt(int offset) {
        return getDirectory().findElementAt(offset);
    }

    @Override
    public @Nullable PsiReference findReferenceAt(int offset) {
        return getDirectory().findReferenceAt(offset);
    }

    @Override
    public int getTextOffset() {
        return getDirectory().getTextOffset();
    }

    @Override
    public String getText() {
        return getDirectory().getText();
    }

    @Override
    public @NotNull char[] textToCharArray() {
        return getDirectory().textToCharArray();
    }

    @Override
    public PsiElement getNavigationElement() {
        return getDirectory().getNavigationElement();
    }

    @Override
    public PsiElement getOriginalElement() {
        return getDirectory().getOriginalElement();
    }

    @Override
    public boolean textMatches(@NotNull CharSequence text) {
        return getDirectory().textMatches(text);
    }

    @Override
    public boolean textMatches(@NotNull PsiElement element) {
        return getDirectory().textMatches(element);
    }

    @Override
    public boolean textContains(char c) {
        return getDirectory().textContains(c);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        getDirectory().accept(visitor);
    }

    @Override
    public void acceptChildren(@NotNull PsiElementVisitor visitor) {
        getDirectory().acceptChildren(visitor);
    }

    @Override
    public PsiElement copy() {
        return getDirectory().copy();
    }

    @Override
    public PsiElement add(@NotNull PsiElement element) throws IncorrectOperationException {
        return getDirectory().add(element);
    }

    @Override
    public PsiElement addBefore(@NotNull PsiElement element, @Nullable PsiElement anchor) throws IncorrectOperationException {
        return getDirectory().addBefore(element, anchor);
    }

    @Override
    public PsiElement addAfter(@NotNull PsiElement element, @Nullable PsiElement anchor) throws IncorrectOperationException {
        return getDirectory().addAfter(element, anchor);
    }

    @Override
    public void checkAdd(@NotNull PsiElement element) throws IncorrectOperationException {
        getDirectory().checkAdd(element);
    }

    @Override
    public PsiElement addRange(PsiElement first, PsiElement last) throws IncorrectOperationException {
        return getDirectory().addRange(first, last);
    }

    @Override
    public PsiElement addRangeBefore(@NotNull PsiElement first, @NotNull PsiElement last, PsiElement anchor) throws IncorrectOperationException {
        return getDirectory().addRangeBefore(first, last, anchor);
    }

    @Override
    public PsiElement addRangeAfter(PsiElement first, PsiElement last, PsiElement anchor) throws IncorrectOperationException {
        return getDirectory().addRangeAfter(first, last, anchor);
    }

    @Override
    public void delete() throws IncorrectOperationException {
        getDirectory().delete();
    }

    @Override
    public void checkDelete() throws IncorrectOperationException {
        getDirectory().checkDelete();
    }

    @Override
    public void deleteChildRange(PsiElement first, PsiElement last) throws IncorrectOperationException {
        getDirectory().deleteChildRange(first, last);
    }

    @Override
    public PsiElement replace(@NotNull PsiElement newElement) throws IncorrectOperationException {
        return getDirectory().replace(newElement);
    }

    @Override
    public boolean isValid() {
        return getDirectory().isValid();
    }

    @Override
    public boolean isWritable() {
        return getDirectory().isWritable();
    }

    @Override
    public @Nullable PsiReference getReference() {
        return getDirectory().getReference();
    }

    @Override
    public @NotNull PsiReference[] getReferences() {
        return getDirectory().getReferences();
    }

    @Override
    public <T> @Nullable T getCopyableUserData(Key<T> key) {
        return getDirectory().getCopyableUserData(key);
    }

    @Override
    public <T> void putCopyableUserData(Key<T> key, @Nullable T value) {
        getDirectory().putCopyableUserData(key, value);
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
