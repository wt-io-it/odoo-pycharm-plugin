package at.wtioit.intellij.plugins.odoo;

import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;

public class OdooRecordGoToDeclarationHandlerXMLTest extends AbstractOdooGoToDeclarationHandlerTest {

    public void testRecordNameInRefAttribute() {
        doTest("records.xml record:id=record1 model=existing ");
    }
    public void testRecordNameInEvalAttribute() {
        doTest("records.xml record:id=addon1.record2 model=existing ");
    }

    public void testRecordNameInEvalAttributeCursorAtRef() {
        doTest(null);
    }

    public void testRecordNameInEvalAttributeCursorBeforeRef() {
        doTest(null);
    }

    public void testRecordNameInEvalAttributeCursorAtQuote() {
        doTest("records.xml record:id=addon1.record2 model=existing ");
    }

    public void testIncompleteRefExpressionInEvalAttribute() {
        doTest(null);
    }

    @Override
    String getFileExtension() {
        return ".xml";
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
