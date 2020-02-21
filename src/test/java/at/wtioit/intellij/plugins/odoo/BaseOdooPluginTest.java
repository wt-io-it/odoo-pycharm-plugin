package at.wtioit.intellij.plugins.odoo;

import at.wtioit.intellij.plugins.odoo.models.OdooModelService;
import at.wtioit.intellij.plugins.odoo.models.impl.OdooModelServiceImpl;
import at.wtioit.intellij.plugins.odoo.modules.OdooModuleService;
import at.wtioit.intellij.plugins.odoo.modules.impl.OdooModuleServiceImpl;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
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

    /**
     * Force model and module service to rescan their contents.
     * TODO this should be removed once we implemented proper indexing:
     * http://www.jetbrains.org/intellij/sdk/docs/basics/indexing_and_psi_stubs.html
     */
    protected void forceRescan() {
        try {

            //OdooModuleService moduleService = ServiceManager.getService(getProject(), OdooModuleService.class);
            //forceSetField(moduleService, "moduleCache", OdooModuleServiceImpl.class, null);
            //forceSetField(moduleService, "moduleCacheByName", OdooModuleServiceImpl.class, null);

            OdooModelService modelService = ServiceManager.getService(getProject(), OdooModelService.class);
            forceSetField(modelService, "scanFinished", OdooModelServiceImpl.class, false);
            forceSetField(modelService, "modelsCacheByName", OdooModelServiceImpl.class, Collections.emptyMap());
            forceSetField(modelService, "modelsCacheByElement", OdooModelServiceImpl.class, Collections.emptyMap());

            modelService.getModels();
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new AssertionError("Cannot force model rescan", e);
        }
    }

    private <T, R extends T> void forceSetField(T moduleService, String fieldName, Class<R> clazz, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(moduleService, value);
    }
}
