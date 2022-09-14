package at.wtioit.intellij.plugins.odoo.compatibility;

import at.wtioit.intellij.plugins.odoo.WithinProject;
import at.wtioit.intellij.plugins.odoo.index.IndexWatcher;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class CompatibleFileIndex {
    /**
     * Depending on availability in the current JVM runs FilenameIndex#getVirtualFilesByName(String, GlobalSearchScope)
     * for newer versions (>2021.2) or runs FilenameIndex#getFilesByName(Project, String, GlobalSearchScope) and
     * converts the result to a collection of VirtualFiles
     * -
     * Holds a workaround for https://youtrack.jetbrains.com/issue/IDEA-291382/Assertion-failed-at-VirtualDirectoryImpldoFindChildById
     * where getVirtualFilesByName might provoke an error when indexing is still to be done
     * @see FilenameIndex#getVirtualFilesByName(String, GlobalSearchScope)
     * @see FilenameIndex#getFilesByName(Project, String, GlobalSearchScope)
     * @param name - name to search
     * @param scope - scope to use for the search
     * @return collection of virtual files matching the name
     */
    public static Collection<VirtualFile> getVirtualFilesByName(String name, GlobalSearchScope scope) {
        Project project = WithinProject.INSTANCE.get();
        if (!IndexWatcher.isFullyIndexed(project)) {
            // When project is not fully indexed calling getVirtualFilesByName or getFilesByName might produce an error
            // with VirtualDirectoryImpl#doFindChildById so until we can be "sure" that our call doesn't provoke anybody
            // to slip an error into our plugin error reporter.
            // https://github.com/wt-io-it/odoo-pycharm-plugin/issues/33
            // https://youtrack.jetbrains.com/issue/IDEA-291382/Assertion-failed-at-VirtualDirectoryImpldoFindChildById
            // https://youtrack.jetbrains.com/issue/IDEA-289822
            // https://github.com/jonathanlermitage/intellij-extra-icons-plugin/issues/106
            return Collections.emptyList();
        }

        try {
            Method getVirtualFilesByName = FilenameIndex.class.getDeclaredMethod("getVirtualFilesByName", String.class, GlobalSearchScope.class);
            return (Collection<VirtualFile>) getVirtualFilesByName.invoke(FilenameIndex.class, name, scope);
        } catch (NoSuchMethodException e) {
            // if getVirtualFilesByName doesn't exist yet we use getFilesByName bellow
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        try {
            Method getFilesByName = FilenameIndex.class.getDeclaredMethod("getFilesByName", Project.class, String.class, GlobalSearchScope.class);
            PsiFile[] files = (PsiFile[]) getFilesByName.invoke(FilenameIndex.class, project, name, scope);
            return Arrays.stream(files).map(PsiFile::getVirtualFile).collect(Collectors.toList());
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e2) {
            throw new RuntimeException(e2);
        }
    }
}