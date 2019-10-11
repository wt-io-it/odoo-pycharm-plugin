package at.wtioit.intellij.plugins.odoo;

import at.wtioit.intellij.plugins.odoo.models.OdooModelService;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Stack;

public class TestModuleServiceImpl extends BasePlatformTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.configureByFiles(getTestFiles("odoo"));
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

    @Override
    protected String getTestDataPath() {
        return getBuildDir() + "/resources/test";
    }

    @NotNull
    private String getBuildDir() {
        try {
            return this.getClass().getClassLoader().getResource(".").toURI().getPath().replaceAll("/classes/.*", "");
        } catch (URISyntaxException e) {
            throw new AssertionError("Cannot get resources directory", e);
        }
    }

    public void testFindingModels() {
        OdooModelService modelService = ServiceManager.getService(getProject(), OdooModelService.class);

        assertNull("Expected to get null for a not existing model", modelService.getModel("notExisting"));
        assertNotNull("Expected to get a model for an existing model", modelService.getModel("existing"));
        assertEquals(modelService.getModel("existing").getName(), "existing");
    }

    public void testFindingModules() {
        OdooModuleService moduleService = ServiceManager.getService(getProject(), OdooModuleService.class);

        assertNull("Expected to get null for a not existing model", moduleService.getModule("notExisting"));
        assertNotNull("Expected to get a model for an existing model", moduleService.getModule("addon1"));
    }
}
