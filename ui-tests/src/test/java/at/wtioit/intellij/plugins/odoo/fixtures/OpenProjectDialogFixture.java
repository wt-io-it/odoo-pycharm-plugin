package at.wtioit.intellij.plugins.odoo.fixtures;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.*;
import org.jetbrains.annotations.NotNull;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;

@DefaultXpath(by = "MyDialog type", xpath = "//*[@title.key='title.open.file.or.project']")
@FixtureName(name = "OpenProjectDialog")
public class OpenProjectDialogFixture extends ContainerFixture {
    public OpenProjectDialogFixture(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
        super(remoteRobot, remoteComponent);
    }

    public void open(String path) {
        while (!this.find(JTextFieldFixture.class, byXpath("//div[@class='BorderlessTextField']")).getText().equals(path)) {
            this.find(JTextFieldFixture.class, byXpath("//div[@class='BorderlessTextField']")).setText(path);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new AssertionError(e);
            }
        }
        this.find(JButtonFixture.class, byXpath("//div[@text.key='button.ok']")).click();
    }
}
