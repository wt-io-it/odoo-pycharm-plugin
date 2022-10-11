package at.wtioit.intellij.plugins.odoo;

import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;

public class OdooRecordGoToDeclarationHandlerPythonTest extends AbstractOdooGoToDeclarationHandlerTest {

    public void testRecordNameInRefMethod() {
        doTest("records.xml record:id=record1 model=existing ");
    }

    public void testRecordNameInClsRefMethod() {
        doTest("records.xml record:id=record1 model=existing ");
    }

    public void testRecordNameInRequestRefMethod() {
        doTest("records.xml record:id=record1 model=existing ");
    }

    public void testRecordNameInSudoRefMethod() {
        doTest("records.xml record:id=record1 model=existing ");
    }

    public void testRecordNameInCustomEnvRefMethod() {
        doTest("records.xml record:id=record1 model=existing ");
    }

    @Override
    String getFileExtension() {
        return ".py";
    }

    @Override
    Class<? extends PsiElement> getExpectedClass() {
        return XmlTag.class;
    }

    @Override
    String resultToString(PsiElement result) {
        StringBuilder resultString = new StringBuilder();
        resultString.append(result.getContainingFile().getName());
        resultString.append(' ');
        resultString.append(((XmlTag) result).getLocalName());
        resultString.append(':');
        for (XmlAttribute attribute : ((XmlTag) result).getAttributes()) {
            resultString.append(attribute.getLocalName());
            resultString.append('=');
            resultString.append(attribute.getValue());
            resultString.append(' ');
        }
        return  resultString.toString();
    }
}
