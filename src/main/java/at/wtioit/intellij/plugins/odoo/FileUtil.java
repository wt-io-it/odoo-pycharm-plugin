package at.wtioit.intellij.plugins.odoo;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;

public class FileUtil {
    public static VirtualFile findFileByPath(String path) {
        VirtualFileManager fileManager = VirtualFileManager.getInstance();
        VirtualFile virtualFile = fileManager.findFileByUrl("file:///" + path);
        if (virtualFile == null) {
            // Mostly enable tests where files for the current tests are configured as temp files
            virtualFile = fileManager.findFileByUrl("temp:///" + path);
        }
        return virtualFile;
    }
}
