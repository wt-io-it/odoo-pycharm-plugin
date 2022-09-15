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
import com.jetbrains.python.PythonLanguage;
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
        return project;
    }

    @Override
    public @NotNull Language getLanguage() {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().getLanguage();
        }
        return PythonLanguage.getInstance();
    }

    @Override
    @Nullable
    public PsiManager getManager() {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().getManager();
        }
        return null;
    }

    @Override
    public @NotNull PsiElement[] getChildren() {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().getChildren();
        }
        return new PsiElement[0];
    }

    @Override
    public PsiElement getParent() {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().getParent();
        }
        return null;
    }

    @Override
    public PsiElement getFirstChild() {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().getParent();
        }
        return null;
    }

    @Override
    public PsiElement getLastChild() {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().getLastChild();
        }
        return null;
    }

    @Override
    public PsiElement getNextSibling() {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().getNextSibling();
        }
        return null;
    }

    @Override
    @Nullable
    public PsiElement getPrevSibling() {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().getPrevSibling();
        }
        return null;
    }

    @Override
    @Nullable
    public PsiFile getContainingFile() throws PsiInvalidElementAccessException {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return directory.getContainingFile();
        }
        return null;
    }

    @Override
    @Nullable
    public TextRange getTextRange() {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return directory.getTextRange();
        }
        return null;
    }

    @Override
    public int getStartOffsetInParent() {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return directory.getStartOffsetInParent();
        }
        return 0;
    }

    @Override
    public int getTextLength() {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().getTextLength();
        }
        return 0;
    }

    @Override
    public @Nullable PsiElement findElementAt(int offset) {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().findElementAt(offset);
        }
        return null;
    }

    @Override
    public @Nullable PsiReference findReferenceAt(int offset) {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().findReferenceAt(offset);
        }
        return null;
    }

    @Override
    public int getTextOffset() {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().getTextOffset();
        }
        return 0;
    }

    @Override
    @Nullable
    public String getText() {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().getText();
        }
        return null;
    }

    @Override
    public @NotNull char[] textToCharArray() {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().textToCharArray();
        }
        return new char[0];
    }

    @Override
    @Nullable
    public PsiElement getNavigationElement() {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().getNavigationElement();
        }
        return null;
    }

    @Override
    @Nullable
    public PsiElement getOriginalElement() {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().getOriginalElement();
        }
        return null;
    }

    @Override
    public boolean textMatches(@NotNull CharSequence text) {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().textMatches(text);
        }
        return text.length() == 0;
    }

    @Override
    public boolean textMatches(@NotNull PsiElement element) {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().textMatches(element);
        }
        return false;
    }

    @Override
    public boolean textContains(char c) {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().textContains(c);
        }
        return false;
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        PsiElement directory = getDirectory();
        if (directory != null) {
            getDirectory().accept(visitor);
        }
    }

    @Override
    public void acceptChildren(@NotNull PsiElementVisitor visitor) {
        PsiElement directory = getDirectory();
        if (directory != null) {
            getDirectory().acceptChildren(visitor);
        }
    }

    @Override
    @Nullable
    public PsiElement copy() {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().copy();
        }
        return null;
    }

    @Override
    @Nullable
    public PsiElement add(@NotNull PsiElement element) throws IncorrectOperationException {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().add(element);
        }
        return null;
    }

    @Override
    @Nullable
    public PsiElement addBefore(@NotNull PsiElement element, @Nullable PsiElement anchor) throws IncorrectOperationException {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().addBefore(element, anchor);
        }
        return null;
    }

    @Override
    @Nullable
    public PsiElement addAfter(@NotNull PsiElement element, @Nullable PsiElement anchor) throws IncorrectOperationException {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().addAfter(element, anchor);
        }
        return null;
    }

    @Override
    public void checkAdd(@NotNull PsiElement element) throws IncorrectOperationException {
        PsiElement directory = getDirectory();
        if (directory != null) {
            getDirectory().checkAdd(element);
        }
    }

    @Override
    @Nullable
    public PsiElement addRange(PsiElement first, PsiElement last) throws IncorrectOperationException {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().addRange(first, last);
        }
        return null;
    }

    @Override
    @Nullable
    public PsiElement addRangeBefore(@NotNull PsiElement first, @NotNull PsiElement last, PsiElement anchor) throws IncorrectOperationException {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().addRangeBefore(first, last, anchor);
        }
        return null;
    }

    @Override
    @Nullable
    public PsiElement addRangeAfter(PsiElement first, PsiElement last, PsiElement anchor) throws IncorrectOperationException {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().addRangeAfter(first, last, anchor);
        }
        return null;
    }

    @Override
    public void delete() throws IncorrectOperationException {
        PsiElement directory = getDirectory();
        if (directory != null) {
            getDirectory().delete();
        }
        throw new IncorrectOperationException("No directory found");
    }

    @Override
    public void checkDelete() throws IncorrectOperationException {
        PsiElement directory = getDirectory();
        if (directory != null) {
            getDirectory().checkDelete();
        }
        throw new IncorrectOperationException("No directory found");
    }

    @Override
    public void deleteChildRange(PsiElement first, PsiElement last) throws IncorrectOperationException {
        PsiElement directory = getDirectory();
        if (directory != null) {
            getDirectory().deleteChildRange(first, last);
        }
    }

    @Override
    @Nullable
    public PsiElement replace(@NotNull PsiElement newElement) throws IncorrectOperationException {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().replace(newElement);
        }
        return null;
    }

    @Override
    public boolean isValid() {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().isValid();
        }
        return false;
    }

    @Override
    public boolean isWritable() {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().isWritable();
        }
        return false;
    }

    @Override
    public @Nullable PsiReference getReference() {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().getReference();
        }
        return null;
    }

    @Override
    public @NotNull PsiReference[] getReferences() {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().getReferences();
        }
        return new PsiReference[0];
    }

    @Override
    public <T> @Nullable T getCopyableUserData(@NotNull Key<T> key) {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().getCopyableUserData(key);
        }
        return null;
    }

    @Override
    public <T> void putCopyableUserData(Key<T> key, @Nullable T value) {
        PsiElement directory = getDirectory();
        if (directory != null) {
            getDirectory().putCopyableUserData(key, value);
        }
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, @Nullable PsiElement lastParent, @NotNull PsiElement place) {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().processDeclarations(processor, state, lastParent, place);
        }
        return false;
    }

    @Override
    public @Nullable PsiElement getContext() {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().getContext();
        }
        return null;
    }

    @Override
    public boolean isPhysical() {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().isPhysical();
        }
        return false;
    }

    @Override
    public @NotNull GlobalSearchScope getResolveScope() {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().getResolveScope();
        }
        return GlobalSearchScope.allScope(project);
    }

    @Override
    public @NotNull SearchScope getUseScope() {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().getUseScope();
        }
        return GlobalSearchScope.allScope(project);
    }

    @Override
    @Nullable
    public ASTNode getNode() {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().getNode();
        }
        return null;
    }

    @Override
    public boolean isEquivalentTo(PsiElement another) {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().isEquivalentTo(another);
        }
        return false;
    }

    @Override
    public Icon getIcon(int flags) {
        return module.getIcon();
    }

    @Override
    public <T> @Nullable T getUserData(@NotNull Key<T> key) {
        PsiElement directory = getDirectory();
        if (directory != null) {
            return getDirectory().getUserData(key);
        }
        return null;
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {
        PsiElement directory = getDirectory();
        if (directory != null) {
            getDirectory().putUserData(key, value);
        }
    }
}
