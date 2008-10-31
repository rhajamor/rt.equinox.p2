/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.equinox.p2.tests.touchpoint.eclipse;

import java.util.*;
import org.eclipse.equinox.internal.p2.touchpoint.eclipse.EclipseTouchpoint;
import org.eclipse.equinox.internal.p2.touchpoint.eclipse.actions.ActionConstants;
import org.eclipse.equinox.internal.p2.touchpoint.eclipse.actions.SetProgramPropertyAction;
import org.eclipse.equinox.internal.provisional.frameworkadmin.Manipulator;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfile;
import org.eclipse.equinox.internal.provisional.p2.engine.InstallableUnitOperand;
import org.eclipse.equinox.p2.tests.AbstractProvisioningTest;

public class SetProgramPropertyActionTest extends AbstractProvisioningTest {

	public SetProgramPropertyActionTest(String name) {
		super(name);
	}

	public SetProgramPropertyActionTest() {
		super("");
	}

	public void testExecuteUndo() {
		Map parameters = new HashMap();
		EclipseTouchpoint touchpoint = new EclipseTouchpoint();
		IProfile profile = createProfile("test");
		InstallableUnitOperand operand = new InstallableUnitOperand(null, createIU("test"));
		touchpoint.initializePhase(null, profile, "test", parameters);
		parameters.put("iu", operand.second());
		touchpoint.initializeOperand(profile, operand, parameters);
		Manipulator manipulator = (Manipulator) parameters.get(EclipseTouchpoint.PARM_MANIPULATOR);
		assertNotNull(manipulator);

		String frameworkDependentPropertyName = "test";
		String frameworkDependentPropertyValue = "true";
		assertFalse(manipulator.getConfigData().getFwDependentProps().containsKey(frameworkDependentPropertyName));
		parameters.put(ActionConstants.PARM_PROP_NAME, frameworkDependentPropertyName);
		parameters.put(ActionConstants.PARM_PROP_VALUE, frameworkDependentPropertyValue);
		parameters = Collections.unmodifiableMap(parameters);

		SetProgramPropertyAction action = new SetProgramPropertyAction();
		action.execute(parameters);
		assertEquals("true", manipulator.getConfigData().getFwDependentProp(frameworkDependentPropertyName));
		action.undo(parameters);
		assertFalse(manipulator.getConfigData().getFwDependentProps().containsKey(frameworkDependentPropertyName));
	}

}