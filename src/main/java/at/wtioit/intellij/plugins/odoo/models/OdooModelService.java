package at.wtioit.intellij.plugins.odoo.models;

import com.intellij.psi.PsiElement;

public interface OdooModelService {

    Iterable<OdooModel> getModels();

    OdooModel getModel(String modelName);

    Iterable<String> getModelNames();

    OdooModel getModelForElement(PsiElement psiElement);

    boolean hasModel(String name);
}
