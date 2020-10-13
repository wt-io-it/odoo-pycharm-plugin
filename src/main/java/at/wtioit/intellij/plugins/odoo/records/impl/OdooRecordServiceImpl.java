package at.wtioit.intellij.plugins.odoo.records.impl;

import at.wtioit.intellij.plugins.odoo.WithinProject;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import at.wtioit.intellij.plugins.odoo.records.OdooRecord;
import at.wtioit.intellij.plugins.odoo.records.OdooRecordService;
import at.wtioit.intellij.plugins.odoo.records.index.OdooRecordFileIndex;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class OdooRecordServiceImpl implements OdooRecordService {

    final Project project;

    public OdooRecordServiceImpl(Project project) {
        this.project = project;
    }

    @Override
    public String[] getXmlIds() {
        Collection<String> allKeys = FileBasedIndex.getInstance().getAllKeys(OdooRecordFileIndex.NAME, project);
        return allKeys.toArray(new String[allKeys.size()]);
    }

    @Override
    public OdooRecord getRecord(String xmlId) {
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        List<OdooRecord> records = FileBasedIndex.getInstance().getValues(OdooRecordFileIndex.NAME, xmlId, scope);
        if (records.size() == 1) {
            return records.get(0);
        }
        // TODO find base elements
        OdooModuleService moduleService = ServiceManager.getService(project, OdooModuleService.class);
        return WithinProject.call(project, () -> {
            for (OdooRecord record : records) {
                boolean dependsOnOtherRecords = false;
                OdooModule module = moduleService.getModule(getVirtualFileForPath(record.getPath()));
                if (module != null) {
                    for (OdooRecord otherRecord : records) {
                        OdooModule otherModule = moduleService.getModule(getVirtualFileForPath(otherRecord.getPath()));
                        if (module.dependsOn(otherModule)) {
                            dependsOnOtherRecords = true;
                            break;
                        }
                    }
                    if (!dependsOnOtherRecords) {
                        return record;
                    }
                }
            }
            return null;
        });
    }

    private VirtualFile getVirtualFileForPath(String path) {
        return VirtualFileManager.getInstance().findFileByUrl("file:///" + path);
    }
}
