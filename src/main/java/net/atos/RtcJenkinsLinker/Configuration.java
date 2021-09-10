package net.atos.RtcJenkinsLinker;

import com.ibm.team.repository.client.TeamPlatform;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.init.Terminator;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.util.regex.Pattern;

@Extension
@Symbol("rjl")
public class Configuration extends GlobalConfiguration {
    public static Configuration get() {
        return ExtensionList.lookupSingleton(Configuration.class);
    }

    private String rtcLink = "https://your.company.com/rtc";

    @DataBoundConstructor
    public Configuration() {
        load();
    }

    public String getRtcLink() {
        return rtcLink;
    }

    @DataBoundSetter
    public void setRtcLink(String rtcLink) {
        this.rtcLink = rtcLink;
        save();
    }

    @Initializer(after = InitMilestone.EXTENSIONS_AUGMENTED)
    public void start() {
        TeamPlatform.startup();
    }

    @Terminator
    public void stop() {
        TeamPlatform.shutdown();
    }
}
