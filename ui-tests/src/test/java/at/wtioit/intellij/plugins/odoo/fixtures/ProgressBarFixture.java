package at.wtioit.intellij.plugins.odoo.fixtures;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.*;
import org.jetbrains.annotations.NotNull;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;

@DefaultXpath(by = "IdeStatusBarImpl type", xpath = "//div[@class='IdeStatusBarImpl']")
@FixtureName(name = "ProgressBar")
public class ProgressBarFixture extends ContainerFixture {
    public ProgressBarFixture(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
        super(remoteRobot, remoteComponent);
    }

    public void waitForIndexing() {
        while (!this.findAll(JTextFieldFixture.class, byXpath("(//div[@text.key='progress.indexing']|//div[@text.key='progress.indexing.scanning']|//div[@text.key='indexable.files.provider.indexing.module.name'])")).isEmpty()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new AssertionError(e);
            }
        }
    }

    public void waitUntilReady() {
        while (!this.findAll(ComponentFixture.class, byXpath("//div[@class='StatusBarPanel']//div[@class='InlineProgressPanel']//div[@class='TextPanel']")).isEmpty()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new AssertionError(e);
            }
        }
        this.waitForIndexing();
    }
}
