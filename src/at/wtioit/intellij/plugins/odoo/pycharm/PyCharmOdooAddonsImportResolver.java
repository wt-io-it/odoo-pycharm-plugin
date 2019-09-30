package at.wtioit.intellij.plugins.odoo.pycharm;

import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import com.google.common.collect.Lists;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.QualifiedName;
import com.jetbrains.python.psi.impl.PyImportResolver;
import com.jetbrains.python.psi.resolve.PyQualifiedNameResolveContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PyCharmOdooAddonsImportResolver implements PyImportResolver {

    @Override
    @Nullable
    public PsiElement resolveImportReference(QualifiedName name, PyQualifiedNameResolveContext context, boolean withRoots) {
        String fqn = name.toString();
        if (fqn.equals("odoo.addons.base") || fqn.startsWith("odoo.addons.base.")) {
            // base addon is always in odoo/addons/base and resolves fine by default
            return null;
        }

        // resolve addons by their directory
        if (fqn.startsWith("odoo.addons.")) {
            if (fqn.indexOf('.', 12) == -1) {
                // we resolve the addon directly
                String addonName = fqn.substring(12);
                OdooModuleService moduleService = ServiceManager.getService(context.getProject(), OdooModuleService.class);
                for (OdooModule module : moduleService.getModules()) {
                    if (module.getName().equals(addonName)) {
                        return module.getDirectory();
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
