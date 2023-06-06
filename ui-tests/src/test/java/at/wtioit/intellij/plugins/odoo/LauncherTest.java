package at.wtioit.intellij.plugins.odoo;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.fixtures.JLabelFixture;
import com.intellij.remoterobot.launcher.Ide;
import com.intellij.remoterobot.launcher.IdeDownloader;
import com.intellij.remoterobot.launcher.IdeLauncher;
import okhttp3.OkHttpClient;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static com.intellij.remoterobot.utils.RepeatUtilsKt.waitFor;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(LauncherTest.IdeTestWatcher.class)
@Timeout(value = 5, unit = TimeUnit.MINUTES)
public class LauncherTest {

    private static RemoteRobot remoteRobot;
    private static Path tmpDir = null;
    private static Process ideaProcess;

    @BeforeAll
    public static void before() throws IOException {
        OkHttpClient client = new OkHttpClient();
        remoteRobot = new RemoteRobot("http://localhost:8082", client);
        IdeDownloader ideDownloader = new IdeDownloader(client);
        HashMap<String, Object> additionalProperties = new HashMap<>();
        additionalProperties.put("robot-server.port", 8082);
        tmpDir = Files.createTempDirectory("launcher");
        Path downloadDir = new File("build/launcherDownloads").getAbsoluteFile().toPath();
        if (!Files.exists(downloadDir)) {
            Files.createDirectories(downloadDir);
        }
        Path robotPluginFile = downloadDir.resolve("robot-server-plugin-0.11.18");
        if (!Files.exists(robotPluginFile)) {
            Path tmpRobotPluginFile = ideDownloader.downloadRobotPlugin(tmpDir);
            Files.move(tmpRobotPluginFile, robotPluginFile);
        }
        // TODO make sure the plugin is built
        Path pycharmPluginFile = new File("../build/libs/odoo_plugin-0.6.12-SNAPSHOT.jar").getAbsoluteFile().toPath();
        List<Path> plugins = Arrays.asList(robotPluginFile, pycharmPluginFile);
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
            Files.move(tmpIdePath, idePath);
        }
        ideaProcess = IdeLauncher.INSTANCE.launchIde(
                idePath,
                additionalProperties,
                Collections.emptyList(),
                plugins,
                tmpDir);
        waitFor(Duration.ofSeconds(90), Duration.ofSeconds(5), () -> isAvailable(remoteRobot));
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
            ideaProcess.destroy();
        }
        if (tmpDir != null) {
            FileUtils.cleanDirectory(tmpDir.toFile());
        }
    }

    @Test
    public void testIdeCanBeStarted() {
        JLabelFixture welcome = remoteRobot.find(JLabelFixture.class, byXpath("//div[@text.key='label.welcome.to.0']"));
        assertEquals("Welcome to PyCharm", welcome.getValue());
    }

    public void testPluginIsListedAsInstalled() {

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