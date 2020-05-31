package at.wtioit.intellij.plugins.odoo.pycharm;

import at.wtioit.intellij.plugins.odoo.WithinProject;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import com.google.common.collect.Lists;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.util.QualifiedName;
import com.jetbrains.python.psi.impl.PyImportResolver;
import com.jetbrains.python.psi.resolve.PyQualifiedNameResolveContext;
import org.jetbrains.annotations.NotNull;
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
        OdooModuleService moduleService = ServiceManager.getService(context.getProject(), OdooModuleService.class);
        if (fqn.startsWith("odoo.addons.")) {
            if (fqn.indexOf('.', 12) == -1) {
                // we resolve the addon directly, this is triggered when using 'import odoo.addons.addonname' or 'from odoo.addons.addonname import models'
                String addonName = fqn.substring(12);
                OdooModule module = moduleService.getModule(addonName);
                if (module != null) {
                    return WithinProject.call(context.getProject(), module::getDirectory);
                }
            } else {
                // import from an addons subdirs are resolved here, like 'from odoo.addons.addonname.models.modelfile import ModelClass'
                PsiDirectory addon = (PsiDirectory) resolveImportReference(name.removeTail(name.getComponentCount() - 3), context, withRoots);
                if (addon != null) {
                    return resolveOdooAddonImportReference(addon, fqn.substring(fqn.indexOf('.', 12) + 1));
                }
            }
        } else if (!fqn.contains(".")) {
            // this is used when using 'from odoo.addons import addonname'
            OdooModule module = moduleService.getModule(fqn);
            if (module != null) {
                return WithinProject.call(context.getProject(), module::getDirectory);
            }
        }

        return null;
    }

    private PsiElement resolveOdooAddonImportReference(@NotNull PsiDirectory addon, String name) {
        String[] path = name.split("\\.");
        PsiFileSystemItem element = addon;
        for (String segment : path) {
            @NotNull PsiElement[] children = element.getChildren();
            element = null;
            for (PsiElement child : children) {
                List<String> searchFor = Lists.asList(segment, segment + ".py", new String[0]);
                if (child instanceof PsiFileSystemItem && searchFor.contains(((PsiFileSystemItem) child).getName())) {
                    element = (PsiFileSystemItem) child;
                    break;
                }
            }
            if (element == null) {
                // no child matches the current segment we are searching for
                return null;
            }
        }
        return element;
    }

}
