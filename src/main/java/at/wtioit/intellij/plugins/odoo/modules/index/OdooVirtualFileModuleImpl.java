package at.wtioit.intellij.plugins.odoo.modules.index;

import at.wtioit.intellij.plugins.odoo.modules.index.OdooDeserializedModuleImpl;
import com.intellij.openapi.vfs.VirtualFile;

public class OdooVirtualFileModuleImpl extends OdooDeserializedModuleImpl {

    OdooVirtualFileModuleImpl(VirtualFile manifestFile) {
        super(manifestFile.getParent().getName(), manifestFile.getParent().getPath());
    }

}
