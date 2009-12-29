/*******************************************************************************
 *  Copyright (c) 2007, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     WindRiver - https://bugs.eclipse.org/bugs/show_bug.cgi?id=227372
 *******************************************************************************/
package org.eclipse.equinox.internal.p2.engine.phases;

import java.util.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.internal.p2.engine.*;
import org.eclipse.equinox.internal.provisional.p2.metadata.ITouchpointType;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.engine.*;
import org.eclipse.equinox.p2.engine.spi.ProvisioningAction;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRequest;

/**
 * The goal of the collect phase is to ask the touchpoints if the artifacts associated with an IU need to be downloaded.
 */
public class Collect extends InstallableUnitPhase {
	private static final String PHASE_ID = IPhaseSet.PHASE_COLLECT;
	public static final String PARM_ARTIFACT_REQUESTS = "artifactRequests"; //$NON-NLS-1$

	public Collect(int weight) {
		super(PHASE_ID, weight);
		//re-balance work since postPerform will do almost all the time-consuming work
		prePerformWork = 0;
		mainPerformWork = 100;
		postPerformWork = 1000;
	}

	protected boolean isApplicable(InstallableUnitOperand op) {
		return (op.second() != null && !op.second().equals(op.first()));
	}

	protected List<ProvisioningAction> getActions(InstallableUnitOperand operand) {
		IInstallableUnit unit = operand.second();
		List<ProvisioningAction> parsedActions = getActions(unit, phaseId);
		if (parsedActions != null)
			return parsedActions;

		ITouchpointType type = unit.getTouchpointType();
		if (type == null || type == ITouchpointType.NONE)
			return null;

		String actionId = getActionManager().getTouchpointQualifiedActionId(phaseId, type);
		ProvisioningAction action = getActionManager().getAction(actionId, null);
		if (action == null) {
			return null;
		}
		return Collections.singletonList(action);
	}

	protected String getProblemMessage() {
		return Messages.Phase_Collect_Error;
	}

	protected IStatus completePhase(IProgressMonitor monitor, IProfile profile, Map<String, Object> parameters) {
		@SuppressWarnings("unchecked")
		List<IArtifactRequest[]> artifactRequests = (List<IArtifactRequest[]>) parameters.get(PARM_ARTIFACT_REQUESTS);
		ProvisioningContext context = (ProvisioningContext) parameters.get(PARM_CONTEXT);
		IProvisioningAgent agent = (IProvisioningAgent) parameters.get(PARM_AGENT);
		IArtifactRepositoryManager repositoryManager = (IArtifactRepositoryManager) agent.getService(IArtifactRepositoryManager.SERVICE_NAME);

		DownloadManager dm = new DownloadManager(context, repositoryManager);
		for (Iterator<IArtifactRequest[]> it = artifactRequests.iterator(); it.hasNext();) {
			IArtifactRequest[] requests = it.next();
			dm.add(requests);
		}
		return dm.start(monitor);
	}

	protected IStatus initializePhase(IProgressMonitor monitor, IProfile profile, Map<String, Object> parameters) {
		parameters.put(PARM_ARTIFACT_REQUESTS, new ArrayList<IArtifactRequest[]>());
		return null;
	}
}
