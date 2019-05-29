package at.wtioit.intellij.plugins.odoo;

import com.intellij.psi.util.QualifiedName;
import com.jetbrains.python.psi.impl.PyImportResolver;
import org.jetbrains.annotations.NotNull;


public class OdooAddonsRepositoryImportResolver extends AbstractOdooAddonsImportResolver {

    OdooAddonsRepositoryImportResolver(PyImportResolver parent) {
        super(parent);
    }

    @Override
    protected QualifiedName getAddonQualifiedName(QualifiedName name) {
        return name.removeHead(2);
    }
}
