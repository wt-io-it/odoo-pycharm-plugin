package at.wtioit.intellij.plugins.odoo.fixtures;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.*;
import com.intellij.remoterobot.fixtures.dataExtractor.RemoteText;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static org.junit.jupiter.api.Assertions.assertEquals;

//From https://github.com/JetBrains/intellij-ui-test-robot?search=1
@DefaultXpath(by = "FlatWelcomeFrame type", xpath = "//div[@class='FlatWelcomeFrame']")
@FixtureName(name = "Welcome Frame")
public class WelcomeFrameFixture extends ContainerFixture {

    public WelcomeFrameFixture(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
        super(remoteRobot, remoteComponent);
    }

    public JLabelFixture findWelcomeLabel() {
        return find(JLabelFixture.class, byXpath("//div[@text.key='label.welcome.to.0']"));
    }

    // Create New Project
    public ComponentFixture createNewProjectLink() {
        return find(ComponentFixture.class, byXpath("//div[@text='Create New Project' and @class='ActionLink']"));
    }

    // Import Project
    public ComponentFixture importProjectLink() {
        return find(ComponentFixture.class, byXpath("//div[@text='Import Project' and @class='ActionLink']"));
    }

    public RemoteText findLeftPanelText(String text) {
        // JBList: 2021.2.4
        // Tree: 2023.1.2
        ComponentFixture leftPanel = find(ComponentFixture.class, byXpath("//div[@class='JBList']|//div[@class='Tree']"));
        List<RemoteText> leftPanelText = leftPanel.findAllText(text);
        assertEquals(1, leftPanelText.size(), "Expected to match only one Text (" + text + ")");
        return leftPanelText.get(0);
    }
}
