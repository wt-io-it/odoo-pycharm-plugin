package at.wtioit.intellij.plugins.odoo;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Stack;

public abstract class BaseOdooPluginTest extends BasePlatformTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.configureByFiles(getTestFiles("odoo"));
    }

    @NotNull
    private String getBuildDir() {
        try {
            URL testClassDirectoryUrl = this.getClass().getClassLoader().getResource(".");
            assertNotNull("Directory for the class of the current directory shouldn't be null", testClassDirectoryUrl);
            return testClassDirectoryUrl.toURI().getPath().replaceAll("/classes/.*", "");
        } catch (URISyntaxException e) {
            throw new AssertionError("Cannot get resources directory", e);
        }
    }

    @Override
    protected String getTestDataPath() {
        return getBuildDir() + "/resources/test";
    }

    @NotNull
    private String[] getTestFiles(String dir) {
        Path testDataPath = new File(getTestDataPath()).toPath();

        try {
            Stack<Path> toHandle = new Stack<>();
            ArrayList<String> files = new ArrayList<>();
            Files.list(testDataPath.resolve(dir)).forEach(toHandle::push);
            while (!toHandle.empty()) {
                Path path = toHandle.pop();
                if (path.toFile().isDirectory()) {
                    Files.list(path).forEach(toHandle::push);
                } else {
                    files.add(testDataPath.relativize(path).toString());
                }
            }
            return files.toArray(new String[0]);
        } catch (IOException e) {
            throw new AssertionError("Cannot list " + testDataPath, e);
        }
    }

}
