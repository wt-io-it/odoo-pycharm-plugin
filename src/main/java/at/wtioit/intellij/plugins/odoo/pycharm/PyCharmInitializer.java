package at.wtioit.intellij.plugins.odoo.pycharm;

import at.wtioit.intellij.plugins.odoo.OdooGoToDeclarationHandler;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.extensions.ExtensionPoint;
import com.intellij.openapi.extensions.Extensions;
import com.jetbrains.python.psi.impl.PyImportResolver;

public class PyCharmInitializer extends ApplicationComponent.Adapter implements Disposable {

    @Override
    public void initComponent() {
        try {
            registerImportResolver();
            registerGoToDeclarationHandler();
        } catch (NoClassDefFoundError e) {
            // TODO are we running in intellij without the python plugin?
        }
    }

    private void registerGoToDeclarationHandler() {
        ExtensionPoint<GotoDeclarationHandler> ep = Extensions.getRootArea().getExtensionPoint(GotoDeclarationHandler.EP_NAME);
        ep.registerExtension(new OdooGoToDeclarationHandler(), this);
    }

    private void registerImportResolver() {
        ExtensionPoint<PyImportResolver> ep = Extensions.getRootArea().getExtensionPoint(PyImportResolver.EP_NAME);
        ep.registerExtension(new PyCharmOdooAddonsImportResolver(), this);

    }

    @Override
    public void dispose() {
        //TODO what do i need to do here? (implements Disposable)
    }
}
