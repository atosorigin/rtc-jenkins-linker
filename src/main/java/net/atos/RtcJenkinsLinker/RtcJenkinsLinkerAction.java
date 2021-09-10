package net.atos.RtcJenkinsLinker;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.client.TeamPlatform;
import com.ibm.team.repository.common.Location;
import com.ibm.team.workitem.common.IWorkItemCommon;
import com.ibm.team.workitem.common.model.IWorkItem;
import hudson.Extension;
import hudson.Util;
import hudson.model.*;
import hudson.security.csrf.CrumbExclusion;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.*;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RtcJenkinsLinkerAction extends CrumbExclusion implements Action, StaplerProxy, Describable<RtcJenkinsLinkerAction> {

    private final Run run;
    private final transient DescriptorImpl dings = new DescriptorImpl();

    private static transient Logger logger = Logger.getLogger("rjl");

    public RtcJenkinsLinkerAction(Run run) {
        this.run = run;
    }

    @Override
    public String getIconFileName() {
        return this.run.hasPermission(Item.CONFIGURE) ? "plugin/rjl/images/24x24/RTC_24.png" : null;
    }

    @Override
    public String getDisplayName() {
        return "RTC Jenkins Linker";
    }

    @Override
    public String getUrlName() {
        return "rtc-jenkins-linker";
    }

    @Override
    public Object getTarget() {
        this.run.checkPermission(Item.CONFIGURE);
        return this;
    }

    public Run getRun() {
        return run;
    }

    public String getTicketId() {
        for (Object badgeAction : run.getBadgeActions()) {
            if (badgeAction.getClass() == BadgeAction.class) {
                return ((BadgeAction) badgeAction).getText().substring(11);
            }
        }
        return null;
    }

    public String getTicketLink() {
        for (Object badgeAction : run.getBadgeActions()) {
            if (badgeAction.getClass() == BadgeAction.class) {
                return ((BadgeAction) badgeAction).getLink();
            }
        }
        return null;
    }

    public BadgeAction[] getAllRtcBadges() {
        ArrayList<BadgeAction> res = new ArrayList<>();
        run.getBadgeActions().stream().filter((badge -> badge.getClass() == BadgeAction.class)).forEach(badge -> res.add((BadgeAction) badge));
        return res.toArray(new BadgeAction[]{});
    }

    public String getBuildNumber() {
        return String.valueOf(run.number);
    }

    @Override
    public boolean process(
            final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        final String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.startsWith("/" + getUrlName() + "/")) {
            chain.doFilter(request, response);
            return true;
        }
        return false;
    }

    public HttpResponse doLink(final StaplerRequest request) {
        return (staplerRequest, staplerResponse, o) -> {
            run.checkPermission(Item.CONFIGURE);

            if (!staplerRequest.hasParameter("id")) {
                staplerResponse.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
                return;
            }

            final String idPar = staplerRequest.getParameter("id");

            try {
                Long.parseLong(idPar);
            } catch (NumberFormatException e) {
                staplerResponse.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
                return;
            }

            try {
                DescriptorImpl.doLinkTicket(idPar, run);
            } catch (IOException e) {
                e.printStackTrace();
            }

            staplerResponse.setStatus(HttpServletResponse.SC_CREATED);
        };
    }

    @Override
    public Descriptor<RtcJenkinsLinkerAction> getDescriptor() {
        return dings;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<RtcJenkinsLinkerAction> {

        public static FormValidation doCheckRTCId(@QueryParameter String value) {
            try {
                Long.parseLong(value);
                return FormValidation.ok();
            } catch (NumberFormatException e) {
                return FormValidation.error("Not a number");
            }
        }

        public static FormValidation doDeleteLink(@QueryParameter("badgeHash") final String badgeHash, @AncestorInPath Run run) {
            if (run == null) {
                return FormValidation.error("Couldn't find build");
            }

            try {
                final BadgeAction toRemove = (BadgeAction) run.getBadgeActions().stream().filter(
                        badge -> badge.getClass() == BadgeAction.class
                                && ((BadgeAction) badge).getId().equals(badgeHash)).toArray()[0];

                boolean noChange = false;
                try {
                    String current = run.getDescription();
                    assert current != null;
                    String regx = "\n<a href=.*" + toRemove.getLink() + ".*</a>";
                    logger.info(regx);
                    String newD = current.replaceAll(regx, "");
                    if (current.equals(newD)) {
                        noChange = true;
                    }
                    logger.fine(current);
                    logger.fine(newD);
                    run.setDescription(newD);
                } catch (IOException e) {
                    logger.severe(e.getMessage());
                    e.printStackTrace();
                }

                run.removeAction(toRemove);
                run.save();
                String responseMsg = "Linked badge removed.";
                if (noChange) {
                    responseMsg += " Could not detect Link in Description. Delete manually.";
                }
                return FormValidation.ok(responseMsg);
            } catch (Exception e) {
                return FormValidation.error("Couldn't find linked badge");
            }
        }

        public static FormValidation doLinkTicket(@QueryParameter("RTCId") final String RTCId,
                                                  @AncestorInPath Run run) throws IOException {
            if (doCheckRTCId(RTCId).kind == FormValidation.Kind.ERROR) {
                return FormValidation.error("RTC ID has to be a number!");
            }

            if (run == null) {
                return FormValidation.error("Couldn't find build");
            }
            run.checkPermission(Item.CONFIGURE);

            logger.log(Level.FINE, RTCId);

            LoggerProgressMonitor monitor = new LoggerProgressMonitor();

            String rootUrl = Jenkins.get().getRootUrl();
            if (rootUrl == null) return FormValidation.error("Root URL isn't configured yet. Cannot compute absolute URL.");

            String workItemSummary;
            String workItemLink;

            try {

                StandardUsernamePasswordCredentials cred = CredentialsProvider.findCredentialById(
                        "RTCCREDENTIALS",
                        StandardUsernamePasswordCredentials.class,
                        run,
                        URIRequirementBuilder.fromUri(rootUrl).build()
                );

                if (cred == null) return FormValidation.error("No credentials were found");

                monitor.subTask("username: " + cred.getUsername());

                ITeamRepository repository = TeamPlatform.getTeamRepositoryService().getTeamRepository(Configuration.get().getRtcLink());
                repository.registerLoginHandler((ITeamRepository.ILoginHandler) repository1 -> new ITeamRepository.ILoginHandler.ILoginInfo() {
                    public String getUserId() {
                        return cred.getUsername();
                    }

                    public String getPassword() {
                        return cred.getPassword().getPlainText();
                    }
                });
                monitor.subTask("Contacting " + repository.getRepositoryURI() + "...");
                repository.login(monitor);
                monitor.subTask("Connected");

                IWorkItemCommon workItemCommon = (IWorkItemCommon) repository.getClientLibrary(IWorkItemCommon.class);
                int id = Integer.parseInt(RTCId);
                IWorkItem workItem = workItemCommon.findWorkItemById(id,
                        IWorkItem.SMALL_PROFILE, monitor);
                if (workItem == null) return FormValidation.error("WorkItem was not found on Server");

                workItemSummary = workItem.getHTMLSummary().getPlainText();

                workItemLink = Location.namedLocation(workItem, Configuration.get().getRtcLink()).toAbsoluteUri().toString();
                monitor.subTask("workItem info: " + workItem.getId() + " | " + workItem.getHTMLSummary() + " | " + workItemLink);

                WorkItemReferencesModification operation = new WorkItemReferencesModification(Util.encode(rootUrl + run.getUrl()));
                monitor.subTask("link to be inserted: " + operation.getLink());

                operation.run(workItem, monitor);
                monitor.subTask("Ran successfully!");
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.toString());
                return FormValidation.error(e.getMessage());
            }

            String current = run.getDescription();
            run.setDescription(current + "\n<a href=\"" + workItemLink + "\" target=\"_blank\">RTC Ticket '" + workItemSummary + "'</a>");
            BadgeAction action = new BadgeAction(rootUrl + "plugin/rjl/images/24x24/RTC_24.png", "RTC Ticket " + RTCId, workItemLink);
            run.addAction(action);
            run.save();

            return FormValidation.ok("Ticket linked ");
        }
    }

}
