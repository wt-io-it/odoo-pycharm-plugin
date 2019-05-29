package at.wtioit.intellij.plugins.odoo;

import com.google.common.collect.Lists;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.QualifiedName;
import com.jetbrains.python.psi.impl.PyImportResolver;
import com.jetbrains.python.psi.resolve.PyQualifiedNameResolveContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractOdooAddonsImportResolver implements PyImportResolver {

    private final PyImportResolver parent;

    AbstractOdooAddonsImportResolver(PyImportResolver parent) {
        this.parent = parent;
    }

    protected abstract QualifiedName getAddonQualifiedName(QualifiedName name);

    @Override
    @Nullable
    public PsiElement resolveImportReference(QualifiedName name, PyQualifiedNameResolveContext context, boolean withRoots) {
        // TODO add caches?
        String fqn = name.toString();
        if (fqn.equals("odoo.addons.base") || fqn.startsWith("odoo.addons.base.")) {
            // base addon is always in odoo/addons/base and resolves fine by default
            return null;
        }

        // resolve odoo.addons as a virtual directory containing all addons
        if (fqn.equals("odoo.addons")) {
            GlobalSearchScope scope = GlobalSearchScope.allScope(context.getProject());
            for (PsiFile file : FilenameIndex.getFilesByName(context.getProject(), "__init__.py", scope)) {
                if (file.getParent().getName().equals("addons") && file.getParent().getParent().getName().equals("odoo")) {
                    // TODO this should give us a list of modules when we type "import odoo.addons."
                    return new OdooAddonsDirectory((PsiManagerImpl) context.getPsiManager(), file.getParent().getVirtualFile());
                }
            }
        }

        // resolve addons by their directory
        if (fqn.startsWith("odoo.addons.")){
            if (fqn.indexOf('.', 12) == -1) {
                String addonName = fqn.substring(12);
                GlobalSearchScope scope = GlobalSearchScope.allScope(context.getProject());
                for (PsiFile file : FilenameIndex.getFilesByName(context.getProject(), "__manifest__.py", scope)) {
                    if (file.getParent().getName().equals(addonName)) {
                        return file.getParent();
                    }
                }
            } else {
                PsiDirectory addon = (PsiDirectory) resolveImportReference(name.removeTail(name.getComponentCount() - 3), context, withRoots);
                return resolveOdooAddonImportReference(addon, fqn.substring(fqn.indexOf('.', 12) + 1));
            }
        }

        return null;
    }

    private PsiElement resolveOdooAddonImportReference(PsiDirectory addon, String name) {
        String[] path = name.split("\\.");
        PsiFileSystemItem element = addon;
        for (String segment : path) {
            for (PsiElement child : element.getChildren()) {
                element = null;
                List<String> searchFor = Lists.asList(segment, segment + ".py", new String[0]);
                if (child instanceof PsiFileSystemItem && searchFor.contains(((PsiFileSystemItem) child).getName())) {
                    element = (PsiFileSystemItem) child;
                    break;
                }
            }
            if (element == null) {
                return null;
            }
        }
        return element;
    }

}
