package at.wtioit.intellij.plugins.odoo.errorHandling;

import at.wtioit.intellij.plugins.odoo.OdooBundle;
import com.intellij.diagnostic.IdeaReportingEvent;
import com.intellij.diagnostic.LogMessage;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.diagnostic.Attachment;
import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.util.NlsActions;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class PluginErrorHandler extends ErrorReportSubmitter {

    /**
     * Number of characters where body becomes so long that the issue on github is not able to be created via direkt
     * link
     */
    public static final int ISSUE_BODY_LIMIT = 3000;

    public static final String NOT_SO_IMPORTANT_PACKAGES = "(?:java|kotlinx?|com.jetbrains|com.intellij|org.intellij)";
    public static final String ELLIPSIS = "...";

    @Override
    public @NlsActions.ActionText
    @NotNull String getReportActionText() {
        return OdooBundle.message("PLUGIN.ERROR.HANDLER.report.action.text");
    }

    @Override
    public boolean submit(@NotNull IdeaLoggingEvent[] events, @Nullable String additionalInfo, @NotNull Component parentComponent, @NotNull Consumer<? super SubmittedReportInfo> consumer) {
        String versions = Arrays.stream(events)
                .map((event) -> ((IdeaReportingEvent) event).getPlugin()).filter(Objects::nonNull)
                .filter((plugin) -> "at.wtioit.intellij.plugins.odoo".equals(plugin.getPluginId().getIdString()))
                .map(PluginDescriptor::getVersion)
                .collect(Collectors.joining(","));
        String issueTitle = OdooBundle.message("PLUGIN.ERROR.HANDLER.report.new.issue.title");
        String shortenedIssueText = createIssueText(events, additionalInfo, versions);
        shortenedIssueText = shortenIssueText(shortenedIssueText);
        try {
            // for github issue parameters see https://docs.github.com/en/enterprise-server@3.1/issues/tracking-your-work-with-issues/creating-an-issue#creating-an-issue-from-a-url-query
            URI uri = new URI(OdooBundle.message("PLUGIN.ERROR.HANDLER.report.new.issue.url")
                    + "?title=" + URLEncoder.encode(issueTitle, "UTF-8")
                    + "&labels=" + URLEncoder.encode(versions, "UTF-8")
                    + "&body=" + URLEncoder.encode(shortenedIssueText, "UTF-8")
            );
            BrowserUtil.browse(uri);
        } catch (URISyntaxException | UnsupportedEncodingException exception) {
            throw new AssertionError("issue url could not be generated correctly.", exception);
        }
        consumer.consume(new SubmittedReportInfo(SubmittedReportInfo.SubmissionStatus.NEW_ISSUE));
        return true;
    }

    @NotNull
    private String shortenIssueText(String issueText) {
        if (issueText.length() > ISSUE_BODY_LIMIT) {
            // shorten groups of calls with the same package name
            issueText = issueText.replaceAll("\t(at " + NOT_SO_IMPORTANT_PACKAGES + "\\.)([^\n]+)(?:\n\t\\1[^\n]+)+\n\t(\\1)", "\t$1$2\n\t" + ELLIPSIS + "\n\t$3");
        }
        if (issueText.length() > ISSUE_BODY_LIMIT) {
            // shorten as much calls as possible event if packages change in between
            issueText = issueText.replaceAll("\t(at " + NOT_SO_IMPORTANT_PACKAGES + "[^\n]+)(?:\n\t(?:at " + NOT_SO_IMPORTANT_PACKAGES + "[^\n]+|" + ELLIPSIS + "))+\n\t(at " + NOT_SO_IMPORTANT_PACKAGES + "[^\n]+)", "\t$1\n\t" + ELLIPSIS + "\n\t$2");
        }
        if (issueText.length() > ISSUE_BODY_LIMIT) {
            // shorten package names of not so important packages
            issueText = issueText.replaceAll("\t(at " + NOT_SO_IMPORTANT_PACKAGES + ")\\.(?:[^.\n(]+\\.)*", "\t->$1...");
        }
        if (issueText.length() > ISSUE_BODY_LIMIT) {
            // when all above methods fail we hint to the user that he should copy the message manually
            issueText = issueText.replaceAll("</summary>\n\n```\n(?:.*\n)+?```\n", "</summary>\n\n```\n" + OdooBundle.message("PLUGIN.ERROR.HANDLER.report.new.issue.stacktrace.to.long")+ "\n```\n");
        }
        return issueText;
    }

    @NotNull
    private String createIssueText(@NotNull IdeaLoggingEvent[] events, @Nullable String additionalInfo, String versions) {
        StringBuilder issueText = new StringBuilder();
        if (additionalInfo != null) {
            issueText.append(additionalInfo);
            issueText.append("\n\n");
        }
        if (versions != null) {
            issueText.append("Plugin Version: " + versions + "\n");
            issueText.append("IntelliJ Version: " + ApplicationInfo.getInstance().getFullApplicationName() + "\n");
            issueText.append("IntelliJ Build: " + ApplicationInfo.getInstance().getBuild() + "\n");
            issueText.append("\n\n");
        }
        for (IdeaLoggingEvent event : events) {
            Object data = event.getData();
            String[] stackTrace = event.getThrowableText().split("\n");
            String summary;
            if (data instanceof LogMessage && !((LogMessage) data).getMessage().isEmpty()) {
                summary = ((LogMessage) data).getMessage();
            } else {
                summary = stackTrace[0];
            }
            issueText.append("<details><summary>");
            issueText.append(summary);
            issueText.append("</summary>\n\n");
            issueText.append("```\n");
            issueText.append(String.join("\n", stackTrace));
            issueText.append("\n```\n");
            issueText.append("</details>\n\n");
            if (data instanceof LogMessage) {
                for (Attachment attachment : ((LogMessage) data).getAllAttachments()) {
                    if (attachment.isIncluded() && "induced.txt".equals(attachment.getName())) {
                        issueText.append("<details><summary>");
                        issueText.append(attachment.getName());
                        issueText.append("</summary>\n\n");
                        issueText.append("```\n");
                        issueText.append(attachment.getDisplayText());
                        issueText.append("\n```\n");
                        issueText.append("</details>\n\n");
                    }
                }
            }
        }

        return issueText.toString();
    }
}
