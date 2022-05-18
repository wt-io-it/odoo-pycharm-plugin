package at.wtioit.intellij.plugins.odoo.records.impl;

import at.wtioit.intellij.plugins.odoo.FileUtil;
import at.wtioit.intellij.plugins.odoo.OdooModelPsiElementMatcherUtil;
import at.wtioit.intellij.plugins.odoo.PsiElementsUtil;
import at.wtioit.intellij.plugins.odoo.WithinProject;
import at.wtioit.intellij.plugins.odoo.index.OdooIndex;
import at.wtioit.intellij.plugins.odoo.index.OdooIndexSubKeys;
import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.modules.OdooModule;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import at.wtioit.intellij.plugins.odoo.records.OdooRecord;
import at.wtioit.intellij.plugins.odoo.records.OdooRecordService;
import at.wtioit.intellij.plugins.odoo.records.index.OdooRecordImpl;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.xml.XmlTagImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OdooRecordServiceImpl implements OdooRecordService {

    final Project project;

    public OdooRecordServiceImpl(Project project) {
        this.project = project;
    }

    @Override
    public String[] getXmlIds() {
        return OdooIndex.getAllKeys(OdooIndexSubKeys.ODOO_RECORDS, project).toArray(String[]::new);
    }

    @Override
    public OdooRecord getRecord(String xmlId) {
        List<OdooRecord> records = findOdooRecords(xmlId);
        if (records.size() == 1) {
            return recordWithCorrectXmlId(records.get(0), xmlId);
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
                        if (otherModule != null && module.dependsOn(otherModule)) {
                            dependsOnOtherRecords = true;
                            break;
                        }
                    }
                    if (!dependsOnOtherRecords) {
                        return recordWithCorrectXmlId(record, xmlId);
                    }
                }
            }
            return null;
        });
        if (baseRecord != null) return baseRecord;
        return getOdooModelRecord(xmlId);
    }

    private OdooRecord recordWithCorrectXmlId(@NotNull OdooRecord record, @NotNull String xmlId) {
        if (xmlId.equals(record.getXmlId()) || xmlId.startsWith(OdooModelPsiElementMatcherUtil.NULL_XML_ID_KEY + ".")) {
            return record;
        }
        return OdooRecordImpl.getFromData(record.getId(), xmlId, record.getModelName(), record.getPath(), record.getDefiningElement());
    }

    @Nullable
    private OdooModelRecord getOdooModelRecord(String xmlId) {
        String[] xmlIdParts = xmlId.split("\\.");
        if (xmlIdParts.length == 2 && xmlIdParts[1].startsWith("model_")) {
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
        // TODO this seems very slow (could use it's own index ;-) )
        //if (OdooIndex.getAllKeys(OdooIndexSubKeys.ODOO_RECORDS, project).anyMatch(k -> k.equals(xmlId))) {
        if (OdooIndex.getValues(xmlId, GlobalSearchScope.allScope(project), OdooRecord.class).anyMatch(r -> Boolean.TRUE)) {
            return true;
        } else {
            String undetectedXmlId;
            if (xmlId.contains(".")) {
                undetectedXmlId = xmlId.replaceFirst(".*(?=\\.)", OdooModelPsiElementMatcherUtil.NULL_XML_ID_KEY);
            } else {
                undetectedXmlId = OdooModelPsiElementMatcherUtil.NULL_XML_ID_KEY + "." + xmlId;
            }
            GlobalSearchScope scope = GlobalSearchScope.allScope(project);
            Stream<OdooRecord> records = OdooIndex.getValues(undetectedXmlId, scope, OdooRecord.class);
            OdooModuleService moduleService = ServiceManager.getService(project, OdooModuleService.class);
            return WithinProject.call(project, () -> records
                    .map(record -> Pair.create(record.findVirtualFile(), record))
                    .map(pair -> Pair.create(moduleService.getModule(pair.first), pair.second))
                    .anyMatch(pair ->
                            (xmlId.contains(".") && pair.first != null && xmlId.equals(pair.first.getName() + "." + pair.second.getId()))
                                    // TODO the or part should be before resolving the defining elements for resolving the module
                                    || (!xmlId.contains(".") && pair.second.getXmlId() == null && pair.second.getId().equals(xmlId))))
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

    @Override
    public boolean hasLocalTemplate(PsiElement element, String id, String xmlId) {
        // TODO enable goto handler for those
        XmlTagImpl templateTag = PsiElementsUtil.findParent(element, XmlTagImpl.class, (tag) -> "templates".equals(tag.getName()) || "template".equals(tag.getName()));
        if (templateTag != null) {
            HashMap<String, XmlTag> localTemplates = new HashMap<>();
            for (PsiElement child : templateTag.getChildren()) {
                if (child instanceof XmlTag) {
                    if ("t".equals(((XmlTag) child).getName())) {
                        XmlAttribute nameAttribute = ((XmlTag) child).getAttribute("t-name");
                        if (nameAttribute != null) {
                            String templateName = nameAttribute.getValue();
                            if (id.equals(templateName) || xmlId.equals(templateName)) {
                                // We found the template
                                return true;
                            }
                            localTemplates.put(templateName, (XmlTag) child);
                        }
                    }
                }
            }
            // TODO add index for those local names
        }
        return false;
    }

    @NotNull
    private List<OdooRecord> findOdooRecords(String xmlId) {
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        List<OdooRecord> records = OdooIndex.getValues(xmlId, scope, OdooRecord.class).collect(Collectors.toList());
        if (records.size() == 0) {
            String undetectedXmlId = xmlId.replaceFirst(".*(?=\\.)", OdooModelPsiElementMatcherUtil.NULL_XML_ID_KEY);
            records = OdooIndex.getValues(undetectedXmlId, scope, OdooRecord.class).collect(Collectors.toList());
        }
        return records;
    }

    private VirtualFile getVirtualFileForPath(String path) {
        return FileUtil.findFileByPath(path);
    }
}
