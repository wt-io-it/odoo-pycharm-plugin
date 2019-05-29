package at.wtioit.intellij.plugins.odoo;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.extensions.ExtensionPoint;
import com.intellij.openapi.extensions.Extensions;
import com.jetbrains.python.psi.impl.PyImportResolver;
import com.jetbrains.python.psi.impl.PyJavaImportResolver;

public class PyImportResolverInitializer extends ApplicationComponent.Adapter implements Disposable {

    @Override
    public void initComponent() {
        registerImportResolver();
    }

    private void registerImportResolver() {
        ExtensionPoint<PyImportResolver> ep = Extensions.getRootArea().getExtensionPoint(PyImportResolver.EP_NAME);
        ep.registerExtension(new OdooAddonsImportResolver(new PyJavaImportResolver()), this);
    }

    @Override
    public void dispose() {
        //TODO what do i need to do here? (implements Disposable)
    }
}
