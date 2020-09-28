package at.wtioit.intellij.plugins.odoo.models.impl;

import at.wtioit.intellij.plugins.odoo.models.OdooModel;
import at.wtioit.intellij.plugins.odoo.models.OdooModelService;
import at.wtioit.intellij.plugins.odoo.models.index.OdooModelFileIndex;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;

import java.util.Objects;
import java.util.stream.StreamSupport;

public class OdooModelServiceImpl implements OdooModelService {

    private final Project project;

    public OdooModelServiceImpl(Project project) {
        this.project = project;
    }

    @Override
    public Iterable<OdooModel> getModels() {
        FileBasedIndex index = FileBasedIndex.getInstance();
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        return index.getAllKeys(OdooModelFileIndex.NAME, project).stream()
                .map(modelName -> Pair.create(modelName, index.getContainingFiles(OdooModelFileIndex.NAME, modelName, scope)))
                .filter(pair -> pair.second.size() > 0)
                .map(pair -> (OdooModel) new OdooModelImpl(pair.first, pair.second, project))
                ::iterator;
    }

    @Override
    public OdooModel getModel(String modelName) {
        return StreamSupport.stream(getModels().spliterator(), true)
                .filter(m -> Objects.equals(m.getName(), modelName))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Iterable<String> getModelNames() {
        return StreamSupport.stream(getModels().spliterator(), true).map(OdooModel::getName)::iterator;
    }

    @Override
    public OdooModel getModelForElement(PsiElement psiElement) {
        return StreamSupport.stream(getModels().spliterator(), true)
                .filter(m -> m.getDefiningElement().equals(psiElement))
                .findFirst()
                .orElse(null);
    }
}
