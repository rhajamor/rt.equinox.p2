/*******************************************************************************
 *  Copyright (c) 2005, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.p2.tests.planner;

import java.io.File;
import java.util.ArrayList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.internal.p2.engine.SimpleProfileRegistry;
import org.eclipse.equinox.internal.provisional.p2.core.Version;
import org.eclipse.equinox.internal.provisional.p2.core.VersionRange;
import org.eclipse.equinox.internal.provisional.p2.director.*;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfile;
import org.eclipse.equinox.internal.provisional.p2.engine.ProvisioningContext;
import org.eclipse.equinox.internal.provisional.p2.metadata.*;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.InstallableUnitQuery;
import org.eclipse.equinox.internal.provisional.p2.query.Collector;
import org.eclipse.equinox.p2.tests.AbstractProvisioningTest;

public class SDKPatchingTest2 extends AbstractProvisioningTest {
	IProfile profile = null;
	ArrayList newIUs = new ArrayList();
	IInstallableUnit patchInstallingJDTLaunching = null;
	IInstallableUnit patchInstallingDebugUI = null;

	protected void setUp() throws Exception {
		super.setUp();
		File reporegistry1 = getTestData("test data for sdkpatching test", "testData/sdkpatchingtest/p2/org.eclipse.equinox.p2.engine/profileRegistry");
		File tempFolder = getTempFolder();
		copy("0.2", reporegistry1, tempFolder);
		SimpleProfileRegistry registry = new SimpleProfileRegistry(tempFolder, null, false);
		profile = registry.getProfile("SDKPatchingTest");
		assertNotNull(profile);

		//create a patch to install a new version of jdt.launching
		MetadataFactory.InstallableUnitDescription newJDTLaunching = createIUDescriptor((IInstallableUnit) profile.query(new InstallableUnitQuery("org.eclipse.jdt.launching"), new Collector(), new NullProgressMonitor()).iterator().next());
		Version newJDTLaunchingVersion = new Version(3, 5, 0, "zeNewVersion");
		changeVersion(newJDTLaunching, newJDTLaunchingVersion);
		newIUs.add(MetadataFactory.createInstallableUnit(newJDTLaunching));

		IRequirementChange change = MetadataFactory.createRequirementChange(MetadataFactory.createRequiredCapability(IInstallableUnit.NAMESPACE_IU_ID, "org.eclipse.jdt.launching", VersionRange.emptyRange, null, false, false, false), MetadataFactory.createRequiredCapability(IInstallableUnit.NAMESPACE_IU_ID, "org.eclipse.jdt.launching", new VersionRange(newJDTLaunchingVersion, true, newJDTLaunchingVersion, true), null, false, false, true));
		IRequiredCapability lifeCycle = MetadataFactory.createRequiredCapability(IInstallableUnit.NAMESPACE_IU_ID, "org.eclipse.jdt.feature.group", new VersionRange("[3.5.0.v20081202-0800-7p83FGDFHmHuj2mNpJBSKZe, 3.5.0.v20081202-0800-7p83FGDFHmHuj2mNpJBSKZe]"), null, false, false, true);
		patchInstallingJDTLaunching = createIUPatch("P", new Version("1.0.0"), true, new IRequirementChange[] {change}, new IRequiredCapability[0][0], lifeCycle);

		newIUs.add(patchInstallingJDTLaunching);

		//create a patch to install a new version of jdt.debug.ui
		MetadataFactory.InstallableUnitDescription newDebugUI = createIUDescriptor((IInstallableUnit) profile.query(new InstallableUnitQuery("org.eclipse.jdt.debug.ui"), new Collector(), new NullProgressMonitor()).iterator().next());
		Version newDebugVersion = new Version(3, 3, 0, "zeNewVersion");
		changeVersion(newDebugUI, newDebugVersion);
		newIUs.add(MetadataFactory.createInstallableUnit(newDebugUI));

		IRequirementChange change2 = MetadataFactory.createRequirementChange(MetadataFactory.createRequiredCapability(IInstallableUnit.NAMESPACE_IU_ID, "org.eclipse.jdt.debug.ui", VersionRange.emptyRange, null, false, false, false), MetadataFactory.createRequiredCapability(IInstallableUnit.NAMESPACE_IU_ID, "org.eclipse.jdt.debug.ui", new VersionRange(newDebugVersion, true, newDebugVersion, true), null, false, false, true));
		IRequiredCapability lifeCycle2 = MetadataFactory.createRequiredCapability(IInstallableUnit.NAMESPACE_IU_ID, "org.eclipse.jdt.feature.group", new VersionRange("[3.5.0.v20081202-0800-7p83FGDFHmHuj2mNpJBSKZe, 3.5.0.v20081202-0800-7p83FGDFHmHuj2mNpJBSKZe]"), null, false, false, true);
		patchInstallingDebugUI = createIUPatch("P2", new Version("1.0.0"), true, new IRequirementChange[] {change2}, new IRequiredCapability[0][0], lifeCycle2);

		newIUs.add(patchInstallingDebugUI);

	}

	public void testInstallFeaturePatch() {
		ProvisioningContext ctx = new ProvisioningContext();
		ctx.setExtraIUs(newIUs);
		ProfileChangeRequest request = new ProfileChangeRequest(profile);
		request.addInstallableUnits(new IInstallableUnit[] {patchInstallingJDTLaunching, patchInstallingDebugUI});
		request.setInstallableUnitInclusionRules(patchInstallingJDTLaunching, PlannerHelper.createOptionalInclusionRule(patchInstallingJDTLaunching));
		request.setInstallableUnitInclusionRules(patchInstallingDebugUI, PlannerHelper.createOptionalInclusionRule(patchInstallingDebugUI));
		IPlanner planner = createPlanner();
		ProvisioningPlan plan = planner.getProvisioningPlan(request, ctx, new NullProgressMonitor());
		assertOK("Installation plan", plan.getStatus());
		assertEquals(8, plan.getOperands().length);
	}
}
