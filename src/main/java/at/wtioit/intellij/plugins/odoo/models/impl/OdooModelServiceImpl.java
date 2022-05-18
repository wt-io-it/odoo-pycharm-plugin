package at.wtioit.intellij.plugins.odoo.models.impl;

import at.wtioit.intellij.plugins.odoo.index.OdooIndex;
import at.wtioit.intellij.plugins.odoo.index.OdooIndexSubKeys;
import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.models.OdooModelService;
import at.wtioit.intellij.plugins.odoo.models.OdooModelUtil;
import at.wtioit.intellij.plugins.odoo.models.index.OdooModelDefinition;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.Nullable;

import java.util.stream.StreamSupport;

public class OdooModelServiceImpl implements OdooModelService {

    private final Project project;

    public OdooModelServiceImpl(Project project) {
        this.project = project;
    }

    @Override
    public Iterable<OdooModel> getModels() {
        // TODO check usages for wildcard scenarios
        FileBasedIndex index = FileBasedIndex.getInstance();
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        // TODO this seems inefficient
        // TODO check if this yields false positives (records names like models?)
        return OdooIndex.getAllKeys(OdooIndexSubKeys.ODOO_MODELS, project)
                .map(modelName -> Pair.create(modelName, index.getContainingFiles(OdooIndex.NAME, modelName, scope)))
                .filter(pair -> pair.second.size() > 0)
                .map(pair -> (OdooModel) new OdooModelImpl(pair.first, pair.second, project))
                ::iterator;
    }

    @Override
    public OdooModel getModel(String modelName) {
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        FileBasedIndex index = FileBasedIndex.getInstance();
        OdooModel model = OdooIndex.getValues(modelName, scope, OdooModelDefinition.class)
                .map(m -> new OdooModelImpl(m.getName(), index.getContainingFiles(OdooIndex.NAME, m.getName(), scope), project))
                .findFirst()
                .orElse(null);
        if (model != null) {
            return model;
        }
        return OdooIndex.getAllKeys(OdooIndexSubKeys.ODOO_MODELS, project)
                .filter(k -> OdooModelUtil.wildcardNameMatches(k, modelName))
                .map(k -> Pair.create(k, index.getContainingFiles(OdooIndex.NAME, k, scope)))
                .filter(pair -> pair.second.size() > 0)
                .findFirst()
                .map(pair -> new OdooModelImpl(pair.first, pair.second, project))
                .orElse(null);
    }

    @Override
    public Iterable<String> getModelNames() {
        // TODO check usages for wildcard scenarios
        return OdooIndex.getAllKeys(OdooIndexSubKeys.ODOO_MODELS, project)::iterator;
    }

    @Override
    public OdooModel getModelForElement(PsiElement psiElement) {
        return StreamSupport.stream(getModels().spliterator(), false)
                .filter(m -> m.getDefiningElement().equals(psiElement))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean hasModel(String name) {
        if (OdooIndex.getAllKeys(OdooIndexSubKeys.ODOO_MODELS, project).anyMatch(k -> k.equals(name))) {
            return true;
        }
        return matchedByWildcardName(name) != null;
    }

    @Nullable
    private String matchedByWildcardName(@Nullable  String name) {
        if (name == null) return null;
        return OdooIndex.getAllKeys(OdooIndexSubKeys.ODOO_MODELS, project)
                .filter(modelName -> OdooModelUtil.wildcardNameMatches(modelName, name))
                .findFirst()
                .orElse(null);
    }
}
