/*******************************************************************************
 *  Copyright (c) 2007, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.internal.p2.engine.phases;

import java.util.Map;
import org.eclipse.core.runtime.*;
import org.eclipse.equinox.internal.p2.engine.InstallableUnitPhase;
import org.eclipse.equinox.internal.p2.engine.Messages;
import org.eclipse.equinox.p2.engine.*;
import org.eclipse.equinox.p2.engine.spi.ProvisioningAction;
import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.query.FragmentQuery;

public class Unconfigure extends InstallableUnitPhase {

	public Unconfigure(int weight, boolean forced) {
		super(IPhaseSet.PHASE_UNCONFIGURE, weight, forced);
	}

	public Unconfigure(int weight) {
		this(weight, false);
	}

	protected boolean isApplicable(InstallableUnitOperand op) {
		return (op.first() != null);
	}

	protected ProvisioningAction[] getActions(InstallableUnitOperand currentOperand) {
		//TODO: monitor.subTask(NLS.bind(Messages.Engine_Unconfiguring_IU, unit.getId()));

		IInstallableUnit unit = currentOperand.first();
		if (FragmentQuery.isFragment(unit))
			return null;

		return getActions(unit, phaseId);
	}

	protected String getProblemMessage() {
		return Messages.Phase_Unconfigure_Error;
	}

	protected IStatus initializeOperand(IProfile profile, InstallableUnitOperand operand, Map parameters, IProgressMonitor monitor) {
		IInstallableUnit iu = operand.first();
		parameters.put(PARM_IU, iu);

		IArtifactKey[] artifacts = iu.getArtifacts();
		if (artifacts != null && artifacts.length > 0)
			parameters.put(PARM_ARTIFACT, artifacts[0]);

		return Status.OK_STATUS;
	}
}