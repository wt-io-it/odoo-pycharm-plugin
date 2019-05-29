package at.wtioit.intellij.plugins.odoo;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.QualifiedName;
import com.jetbrains.python.psi.resolve.PyQualifiedNameResolveContext;
import com.jetbrains.python.psi.impl.PyImportResolver;
import org.jetbrains.annotations.Nullable;


public class OdooAddonsImportResolver implements PyImportResolver {

    private final PyImportResolver parent;

    public OdooAddonsImportResolver(PyImportResolver parent) {
        this.parent = parent;
    }

    @Override
    @Nullable
    public PsiElement resolveImportReference(QualifiedName name, PyQualifiedNameResolveContext context, boolean withRoots) {
        String fqn = name.toString();
        if (fqn.equals("odoo.addons.base") || fqn.startsWith("odoo.addons.base.")) {
            // base addon is always in odoo/addons/base and resolves fine
            return null;
        }
        if (fqn.startsWith("odoo.addons.")) {
            // TODO call original resolver also with '*'
            QualifiedName addon_name = name.removeHead(1);
            return parent.resolveImportReference(addon_name, context, withRoots);
        } else if (fqn.equals("odoo.addons")) {
            // TODO call original resolver also with '*'
            QualifiedName addon_name = name.removeHead(1);
            return parent.resolveImportReference(addon_name, context, withRoots);
        }
        return null;
    }
}
