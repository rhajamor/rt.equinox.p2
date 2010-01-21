/*******************************************************************************
 *  Copyright (c) 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.p2.tests.planner;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.internal.provisional.p2.director.*;
import org.eclipse.equinox.p2.engine.*;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.tests.AbstractProvisioningTest;

public class SingletonOptionallyInstalled extends AbstractProvisioningTest {
	IInstallableUnit a1;
	IInstallableUnit a2;

	IPlanner planner;
	IProfile profile;
	private IEngine engine;

	protected void setUp() throws Exception {
		super.setUp();
		a1 = createIU("A", Version.create("1.0.0"), true);

		a2 = createIU("A", Version.create("2.0.0"), true);

		createTestMetdataRepository(new IInstallableUnit[] {a1, a2});

		profile = createProfile("TestProfile." + getName());
		planner = createPlanner();
		engine = createEngine();
	}

	public void test1() {
		ProfileChangeRequest req = new ProfileChangeRequest(profile);
		req.addInstallableUnits(new IInstallableUnit[] {a1});
		req.setInstallableUnitInclusionRules(a1, PlannerHelper.createOptionalInclusionRule(a1));
		IProvisioningPlan plan = planner.getProvisioningPlan(req, null, null);
		engine.perform(plan, new NullProgressMonitor());
		assertProfileContains("foo", profile, new IInstallableUnit[] {a1});

		ProfileChangeRequest req2 = new ProfileChangeRequest(profile);
		req2.addInstallableUnits(new IInstallableUnit[] {a2});
		req2.setInstallableUnitInclusionRules(a2, PlannerHelper.createOptionalInclusionRule(a2));
		IProvisioningPlan plan2 = planner.getProvisioningPlan(req2, null, null);
		engine.perform(plan2, new NullProgressMonitor());
		assertProfileContains("foo", profile, new IInstallableUnit[] {a2});

		ProfileChangeRequest req3 = new ProfileChangeRequest(profile);
		req3.addInstallableUnits(new IInstallableUnit[] {a1});
		req3.setInstallableUnitInclusionRules(a1, PlannerHelper.createOptionalInclusionRule(a1));
		IProvisioningPlan plan3 = planner.getProvisioningPlan(req3, null, null);
		engine.perform(plan3, new NullProgressMonitor());
		assertProfileContains("foo", profile, new IInstallableUnit[] {a2}); //Here we really expect a2

		ProfileChangeRequest req4 = new ProfileChangeRequest(profile);
		req4.addInstallableUnits(new IInstallableUnit[] {a2});
		req4.setInstallableUnitInclusionRules(a2, PlannerHelper.createOptionalInclusionRule(a2));
		IProvisioningPlan plan4 = planner.getProvisioningPlan(req4, null, null);
		engine.perform(plan4, new NullProgressMonitor());
		assertProfileContains("foo", profile, new IInstallableUnit[] {a2});

	}
}