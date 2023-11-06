package at.wtioit.intellij.plugins.odoo.fixtures;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.*;
import org.jetbrains.annotations.NotNull;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;

@DefaultXpath(by = "MyDialog type", xpath = "//*[@title.key='untrusted.project.open.dialog.title']")
@FixtureName(name = "TrustProjectDialog")
public class TrustProjectDialogFixture extends ContainerFixture {
    public TrustProjectDialogFixture(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
        super(remoteRobot, remoteComponent);
    }

    public void trust() {
        this.find(JButtonFixture.class, byXpath("//div[@text.key='untrusted.project.dialog.trust.button']")).click();
    }


}
