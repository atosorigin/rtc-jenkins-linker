package net.atos.RtcJenkinsLinker;

import com.ibm.team.foundation.common.text.XMLString;
import com.ibm.team.links.common.IItemReference;
import com.ibm.team.links.common.IURIReference;
import com.ibm.team.links.common.factory.IReferenceFactory;
import com.ibm.team.links.common.registry.IEndPointDescriptor;
import com.ibm.team.links.common.registry.ILinkType;
import com.ibm.team.links.common.registry.ILinkTypeRegistry;
import com.ibm.team.repository.common.IContributorHandle;
import com.ibm.team.repository.common.IItemHandle;
import com.ibm.team.repository.common.IItemType;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.client.WorkItemOperation;
import com.ibm.team.workitem.client.WorkItemWorkingCopy;
import com.ibm.team.workitem.common.model.*;
import org.eclipse.core.runtime.IProgressMonitor;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class WorkItemReferencesModification extends WorkItemOperation {

    public String getLink() {
        return link;
    }

    private final String link;

    public WorkItemReferencesModification(String link) {
        super("Modifying Work Item References", IWorkItem.FULL_PROFILE);
        this.link = link;
    }

    @Override
    protected void execute(WorkItemWorkingCopy workingCopy, IProgressMonitor monitor) throws TeamRepositoryException {
        try {
            IURIReference reference = IReferenceFactory.INSTANCE.createReferenceFromURI(new URI(link));
            workingCopy.getReferences().add(ILinkTypeRegistry.INSTANCE.getLinkType(WorkItemLinkTypes.RELATED_ARTIFACT).getTargetEndPointDescriptor(), reference);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}