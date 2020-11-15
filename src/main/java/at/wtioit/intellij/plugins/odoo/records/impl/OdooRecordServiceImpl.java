package at.wtioit.intellij.plugins.odoo.records.impl;

import at.wtioit.intellij.plugins.odoo.OdooModelPsiElementMatcherUtil;
import at.wtioit.intellij.plugins.odoo.WithinProject;
import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.models.OdooModelService;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import at.wtioit.intellij.plugins.odoo.records.OdooRecord;
import at.wtioit.intellij.plugins.odoo.records.OdooRecordService;
import at.wtioit.intellij.plugins.odoo.records.index.OdooRecordFileIndex;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        List<OdooRecord> records = findOdooRecords(xmlId);
        if (records.size() == 1) {
            return records.get(0);
        }
        // TODO find base elements
        OdooModuleService moduleService = ServiceManager.getService(project, OdooModuleService.class);
        OdooRecord baseRecord = WithinProject.call(project, () -> {
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
        if (baseRecord != null) return baseRecord;
        return getOdooModelRecord(xmlId);
    }

    @Nullable
    private OdooModelRecord getOdooModelRecord(String xmlId) {
        String[] xmlIdParts = xmlId.split("\\.");
        if (xmlIdParts[1].startsWith("model_")) {
            OdooModuleService moduleService = ServiceManager.getService(project, OdooModuleService.class);
            String moduleName = xmlIdParts[0];
            String modelNameId = xmlIdParts[1].substring(6);
            OdooModule module = moduleService.getModule(moduleName);
            if (module != null) {
                return WithinProject.call(project, () -> {
                    for (OdooModel model : module.getModels()) {
                        String name = model.getName();
                        if (name != null) {
                            if (name.replace('.', '_').equals(modelNameId)) {
                                return new OdooModelRecord(model);
                            }
                        }
                    }
                    return null;
                });
            }
        }
        return null;
    }

    @Override
    public boolean hasRecord(String xmlId) {
        Collection<String> allKeys = FileBasedIndex.getInstance().getAllKeys(OdooRecordFileIndex.NAME, project);
        if (allKeys.contains(xmlId)) {
            return true;
        } else {
            String undetectedXmlId = xmlId.replaceFirst(".*(?=\\.)", OdooModelPsiElementMatcherUtil.NULL_XML_ID_KEY);
            GlobalSearchScope scope = GlobalSearchScope.allScope(project);
            @NotNull List<OdooRecord> records = FileBasedIndex.getInstance().getValues(OdooRecordFileIndex.NAME, undetectedXmlId, scope);
            OdooModuleService moduleService = ServiceManager.getService(project, OdooModuleService.class);
            return WithinProject.call(project, () -> records.stream()
                    .map(record -> Pair.create(record.getDefiningElement(), record))
                    .filter(pair -> pair.first != null)
                    .map(pair -> Pair.create(pair.first.getContainingFile(), pair.second))
                    .map(pair -> Pair.create(pair.first.getVirtualFile(), pair.second))
                    .map(pair -> Pair.create(moduleService.getModule(pair.first), pair.second))
                    .filter(pair -> pair.first != null)
                    .anyMatch(pair -> xmlId.equals(pair.first.getName() + "." + pair.second.getId())))
                    || getOdooModelRecord(xmlId) != null;
        }
    }

    @Override
    public String ensureFullXmlId(PsiFile file, String refName) {
        if (!refName.contains(".")) {
            OdooModuleService moduleService = ServiceManager.getService(project, OdooModuleService.class);
            OdooModule currentModule = moduleService.getModule(file.getVirtualFile());
            if (currentModule != null) {
                refName = currentModule.getName() + "." + refName;
            }
        }
        return refName;
    }

    @NotNull
    private List<OdooRecord> findOdooRecords(String xmlId) {
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        List<OdooRecord> records = FileBasedIndex.getInstance().getValues(OdooRecordFileIndex.NAME, xmlId, scope);
        if (records.size() == 0) {
            String undetectedXmlId = xmlId.replaceFirst(".*(?=\\.)", OdooModelPsiElementMatcherUtil.NULL_XML_ID_KEY);
            records = FileBasedIndex.getInstance().getValues(OdooRecordFileIndex.NAME, undetectedXmlId, scope);
        }
        return records;
    }

    private VirtualFile getVirtualFileForPath(String path) {
        return VirtualFileManager.getInstance().findFileByUrl("file:///" + path);
    }
}
