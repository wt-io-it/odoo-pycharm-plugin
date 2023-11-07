package at.wtioit.intellij.plugins.odoo.fixtures;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.*;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static com.intellij.remoterobot.utils.RepeatUtilsKt.waitFor;
import static com.intellij.remoterobot.utils.RepeatUtilsKt.waitForIgnoringError;

@DefaultXpath(by = "IdeStatusBarImpl type", xpath = "//div[@class='IdeStatusBarImpl']")
@FixtureName(name = "ProgressBar")
public class ProgressBarFixture extends ContainerFixture {
    public ProgressBarFixture(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
        super(remoteRobot, remoteComponent);
    }

    public void waitForIndexing() {
        waitFor(Duration.ofMinutes(5), Duration.ofMillis(500), () -> !indexJobRunning());
    }

    public boolean indexJobRunning() {
        return !this.findAll(JTextFieldFixture.class, byXpath("(//div[@text.key='progress.indexing']|//div[@text.key='progress.indexing.scanning']|//div[@text.key='indexable.files.provider.indexing.module.name'])")).isEmpty();
    }

    public boolean backgroundProgressRunning(){
        return !this.findAll(ComponentFixture.class, byXpath("//div[@class='StatusBarPanel']//div[@class='InlineProgressPanel']//div[@class='TextPanel']")).isEmpty();
    }

    public void waitUntilReady() {
        // Wait for progress indicator to be shown
        waitForIgnoringError(Duration.ofMinutes(1), Duration.ofMillis(500), () -> backgroundProgressRunning() || indexJobRunning());
        if (backgroundProgressRunning() || indexJobRunning()) {
            // Wait for progress indicator to vanish
            waitFor(Duration.ofMinutes(5), Duration.ofMillis(500), () -> !backgroundProgressRunning() && !indexJobRunning());
        }
    }
}
