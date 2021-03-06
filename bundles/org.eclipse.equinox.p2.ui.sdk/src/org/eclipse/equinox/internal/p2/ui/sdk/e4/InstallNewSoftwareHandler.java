/*******************************************************************************
 *  Copyright (c) 2008, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.internal.p2.ui.sdk.e4;

import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.equinox.internal.p2.ui.ProvUI;
import org.eclipse.equinox.internal.p2.ui.sdk.ProvSDKMessages;
import org.eclipse.equinox.p2.ui.LoadMetadataRepositoryJob;
import org.eclipse.swt.widgets.Shell;

/**
 * InstallNewSoftwareHandler invokes the install wizard
 * 
 * @since 3.5
 */
public class InstallNewSoftwareHandler extends PreloadingRepositoryHandler {

	@Inject
	@Named(IServiceConstants.ACTIVE_SHELL)
	private Shell shell;

	@Inject
	private MApplication application;

	private IWorkbench e4Workbench;

	@Execute
	public Object execute() {
		ProvUI.setE4Workbench(getE4Workbench());
		ProvUI.setDefaultShell(getShell());
		return super.execute();
	}

	@Override
	protected Shell getShell() {
		return shell;
	}

	protected void doExecute(LoadMetadataRepositoryJob job) {
		getProvisioningUI().openInstallWizard(null, null, job);
	}

	@Override
	protected IWorkbench getE4Workbench() {
		if (e4Workbench == null && application != null)
			e4Workbench = (IWorkbench) application.getContext().get(IWorkbench.class.getName());
		return e4Workbench;
	}

	protected boolean waitForPreload() {
		// If the user cannot see repositories, then we may as well wait
		// for existing repos to load so that content is available.  
		// If the user can manipulate the repositories, then we don't wait, 
		// because we don't know which ones they want to work with.
		return !getProvisioningUI().getPolicy().getRepositoriesVisible();
	}

	protected void setLoadJobProperties(Job loadJob) {
		super.setLoadJobProperties(loadJob);
		// If we are doing a background load, we do not wish to authenticate, as the
		// user is unaware that loading was needed
		if (!waitForPreload()) {
			loadJob.setProperty(LoadMetadataRepositoryJob.SUPPRESS_AUTHENTICATION_JOB_MARKER, Boolean.toString(true));
			loadJob.setProperty(LoadMetadataRepositoryJob.SUPPRESS_REPOSITORY_EVENTS, Boolean.toString(true));
		}
	}

	@Override
	protected String getProgressTaskName() {
		return ProvSDKMessages.InstallNewSoftwareHandler_ProgressTaskName;
	}
}
