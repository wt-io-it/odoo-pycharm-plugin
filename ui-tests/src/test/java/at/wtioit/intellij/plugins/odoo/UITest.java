package at.wtioit.intellij.plugins.odoo;

import at.wtioit.intellij.plugins.odoo.fixtures.WelcomeFrameFixture;
import com.intellij.openapi.util.Pair;
import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.fixtures.JLabelFixture;
import com.intellij.remoterobot.launcher.Ide;
import com.intellij.remoterobot.launcher.IdeDownloader;
import com.intellij.remoterobot.launcher.IdeLauncher;
import com.jetbrains.qodana.sarif.model.Run;
import okhttp3.OkHttpClient;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static com.intellij.remoterobot.utils.RepeatUtilsKt.waitFor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(UITest.IdeTestWatcher.class)
@Timeout(value = 5, unit = TimeUnit.MINUTES)
public class UITest {

    private static final Logger LOGGER = Logger.getLogger(UITest.class.getName());

    private static RemoteRobot remoteRobot;
    private static Path tmpDir = null;
    private static Process ideaProcess;

    @BeforeAll
    public static void before() throws IOException, InterruptedException {
        OkHttpClient client = new OkHttpClient();
        remoteRobot = new RemoteRobot("http://localhost:8082", client);
        HashMap<String, Object> additionalProperties = new HashMap<>();
        additionalProperties.put("robot-server.port", 8082);
        tmpDir = Files.createTempDirectory("launcher");
        Pair<Path, List<Path>> files = prepareIdeAndPluginFiles(client);
        ideaProcess = IdeLauncher.INSTANCE.launchIde(
                files.first,
                additionalProperties,
                Collections.emptyList(),
                files.second,
                tmpDir);
        LOGGER.info("Waiting for startup (info)");
        waitFor(Duration.ofSeconds(90), Duration.ofSeconds(5), () -> isAvailable(remoteRobot));
    }

    private static Pair<Path, List<Path>> prepareIdeAndPluginFiles(OkHttpClient client) throws IOException, InterruptedException {
        IdeDownloader ideDownloader = new IdeDownloader(client);
        Path downloadDir = new File("build/launcherDownloads").getAbsoluteFile().toPath();
        if (!Files.exists(downloadDir)) {
            Files.createDirectories(downloadDir);
        }

        Path robotPluginFile = downloadDir.resolve("robot-server-plugin-0.11.18");
        if (!Files.exists(robotPluginFile)) {
            Path tmpRobotPluginFile = ideDownloader.downloadRobotPlugin(tmpDir);
            LOGGER.info("Moving robot plugin from tmpPath (" + tmpRobotPluginFile + ") to idePath (" + robotPluginFile + ")");
            Files.move(tmpRobotPluginFile, robotPluginFile);
        }

        String ideFileName;
        if (getIde().equals(Ide.PYCHARM)) {
            ideFileName = "pycharm";
        } else if (getIde().equals(Ide.IDEA_COMMUNITY)) {
            ideFileName = "ideaIC";
        } else if (getIde().equals(Ide.IDEA_ULTIMATE)) {
            ideFileName = "ideaIU";
        } else {
            throw new AssertionError("Unknown ideFileName for " + getIde());
        }
        Path idePath = downloadDir.resolve(ideFileName + "-" + getVersion());
        if (!Files.exists(idePath)) {
            Path tmpIdePath = ideDownloader.downloadAndExtract(getIde(), tmpDir, Ide.BuildType.RELEASE, getVersion());
            LOGGER.info("Moving IDE from tmpPath (" + tmpIdePath + ") to idePath (" + idePath + ")");
            try {
                Files.move(tmpIdePath, idePath);
            } catch (DirectoryNotEmptyException e) {
                // File.move might not succeed for directories that cannot be "moved"
                // so we move it with mv instead
                Runtime.getRuntime().exec("mv " + tmpIdePath + " " + idePath).waitFor();
            }
        }

        Path pycharmPluginFile = new File("../build/libs/odoo_plugin-0.6.12-SNAPSHOT.jar").getAbsoluteFile().toPath();
        List<Path> plugins = Arrays.asList(robotPluginFile, pycharmPluginFile);

        return Pair.create(idePath, plugins);
    }

    @NotNull
    private static Ide getIde() {
        String testType = System.getenv("AT_WTIOIT_PYCHARM_PLUGIN_testType");
        if (testType == null) {
            testType = getGradleProperty("platformType");
        }
        if (testType.equals("PC")) {
            // Ide Enum does not have a PyCharm Community (yet?)
            return Ide.PYCHARM;
        }
        final String finalTestType = testType;
        return Arrays.stream(Ide.values())
                .filter(ide -> ide.getCode().equals(finalTestType))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Cannot find Ide for " + finalTestType));
    }

    @Nullable
    private static String getVersion() {
        String version = System.getenv("AT_WTIOIT_PYCHARM_PLUGIN_testVersion");
        if (version == null) {
            return getGradleProperty("platformVersion");
        }
        return version;
    }

    private static String getGradleProperty(String propertyName) {
        Properties properties = new Properties();
        try {
            // use gradle properties from root project
            properties.load(new FileInputStream("../gradle.properties"));
            return properties.getProperty(propertyName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Boolean isAvailable(RemoteRobot remoteRobot) {
        try {
            return remoteRobot.callJs("true");
        } catch (Throwable t) {
            return Boolean.FALSE;
        }
    }

    @AfterAll
    public static void after() throws IOException {
        if (ideaProcess != null) {
            Long[] pids = Stream.concat(
                    Stream.of(ideaProcess.pid()),
                    ideaProcess.children().map(ProcessHandle::pid)
            ).toArray(Long[]::new);
            if (ideaProcess.isAlive()) {
                ideaProcess.destroy();
            }
            ideaProcess.children().filter(ProcessHandle::isAlive).forEach(ProcessHandle::destroy);
            if (ideaProcess.isAlive()) {
                ideaProcess.destroyForcibly();
            }
            for (Long pid : pids) {
                // check all pids again and if they are still alive kill them
                if (new File("/proc/" + pid).exists()) {
                    Runtime.getRuntime().exec("kill " + pid);
                }
            }

        }
        if (tmpDir != null) {
            // wait until no processes are using the tmpDir anymore
            waitFor(Duration.ofSeconds(90), Duration.ofSeconds(1), () -> {
                try {
                    Process lsof = Runtime.getRuntime().exec("lsof " + tmpDir.toAbsolutePath());
                    lsof.waitFor();
                    return lsof.exitValue() != 0;
                } catch (IOException e) {
                    // cannot check if any processes are accessing the tmp directory (with lsof)
                    // we continue and try to delete the directory anyway
                    return true;
                } catch (InterruptedException e) {
                    // we got interrupted while waiting for lsof to finish
                    return true;
                }
            });
            FileUtils.cleanDirectory(tmpDir.toFile());
        }
    }

    @Test
    public void testIdeCanBeStarted() {
        JLabelFixture welcome = remoteRobot.find(WelcomeFrameFixture.class).findWelcomeLabel();
        // TODO also check for IDEA
        assertEquals("Welcome to PyCharm", welcome.getValue());
    }

    @Test
    public void testPluginIsListedAsInstalled() {
        remoteRobot.find(WelcomeFrameFixture.class).findLeftPanelText("Plugins").click();

        // Select the "installed" plugins tab
        JLabelFixture installedPluginsTab = remoteRobot.find(JLabelFixture.class, byXpath("//div[contains(@text.key, 'plugin.manager.tab.installed')]"));
        installedPluginsTab.click();

        // Verify that Odoo Autocompletion Plugin is listed in installed tab
        JLabelFixture odooAutocompletionSupportPlugin = remoteRobot.find(JLabelFixture.class, byXpath("//div[@text='Odoo Autocompletion Support']"));
        assertNotNull(odooAutocompletionSupportPlugin);
    }

    public static class IdeTestWatcher implements TestWatcher {
        @Override
        public void testFailed(ExtensionContext context, Throwable cause) {
            TestWatcher.super.testFailed(context, cause);
            try {
                ImageIO.write(remoteRobot.getScreenshot(), "png", new File("build/reports", context.getDisplayName().replaceAll("[()]", "") + ".png"));
            } catch (IOException ex) {
                throw new AssertionError(ex);
            }
        }
    }
}