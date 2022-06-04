package at.wtioit.intellij.plugins.odoo;

import com.intellij.util.io.CompactDataInput;
import com.intellij.util.io.CompactDataOutput;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;

import java.io.*;

public class DataSerializerTest extends TestCase {

    public void testStringIO() {
        assertStringIOEquals("myBasicString");
        assertStringIOEquals("myStringWithUmlautsÖÄÜ");
    }

    protected void assertStringIOEquals(String value) {
        AbstractDataExternalizer<String> abstractDataExternalizer = new AbstractDataExternalizer<String>() {

            @Override
            public void save(@NotNull DataOutput out, String value) throws IOException {
                saveString(value, out);
            }

            @Override
            public String read(@NotNull DataInput in) throws IOException {
                return readString(in);
            }
        };
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        String externalizedValue;
        String externalizedValue2;
        try {
            // we write the string 2 times to test if the logic also works when multiple datasets are written
            abstractDataExternalizer.save(new CompactDataOutput(byteArrayOutputStream), value);
            abstractDataExternalizer.save(new CompactDataOutput(byteArrayOutputStream), value);
            CompactDataInput dataInput = new CompactDataInput(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
            externalizedValue = abstractDataExternalizer.readString(dataInput);
            externalizedValue2 = abstractDataExternalizer.readString(dataInput);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assertEquals(value, externalizedValue);
        assertEquals(value, externalizedValue2);


    }
}
