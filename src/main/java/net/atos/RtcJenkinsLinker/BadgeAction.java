package net.atos.RtcJenkinsLinker;

import hudson.model.BuildBadgeAction;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

public class BadgeAction implements BuildBadgeAction {
    private final String iconPath;
    private final String text;
    private String link;

    public BadgeAction(String iconPath, String text, String link) {
        this.iconPath = iconPath;
        this.text = text;
        this.link = link;
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return null;
    }

    @Exported
    public String getIconPath() {
        // add the context path to the path variable if the image starts with /
        if (iconPath != null && iconPath.startsWith("/")) {
            StaplerRequest currentRequest = Stapler.getCurrentRequest();
            if (currentRequest != null && !iconPath.startsWith(currentRequest.getContextPath())) {
                return currentRequest.getContextPath() + iconPath;
            }
        }
        return iconPath;
    }

    @Exported
    public String getText() {
        return text;
    }

    @Exported
    public String getLink() {
        return link;
    }

    public String getId() {
        return String.valueOf(this.hashCode());
    }

    @Exported
    public String getFullIconPath() {
        Jenkins jenkins = Jenkins.getInstanceOrNull();
        if (jenkins == null) return null;
        return jenkins.getRootUrl() + "plugin/rtc-jenkins-linker/images/24x24/RTC_24.png";
    }
}
