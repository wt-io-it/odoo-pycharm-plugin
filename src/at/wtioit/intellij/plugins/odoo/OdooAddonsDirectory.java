package at.wtioit.intellij.plugins.odoo;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.impl.file.PsiDirectoryImpl;
import org.jetbrains.annotations.NotNull;

public class OdooAddonsDirectory extends PsiDirectoryImpl {
    public OdooAddonsDirectory(PsiManagerImpl manager, @NotNull VirtualFile file) {
        super(manager, file);
    }

    @NotNull
    @Override
    public PsiDirectory[] getSubdirectories() {
        return super.getSubdirectories();
    }

    @NotNull
    @Override
    public PsiElement[] getChildren() {
        return super.getChildren();
    }
}
