/*******************************************************************************
 *  Copyright (c) 2008, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *     IBM Corporation - ongoing development
 *     Cloudsmith Inc - ongoing development
 *******************************************************************************/
package org.eclipse.equinox.internal.p2.touchpoint.natives.actions;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import org.eclipse.core.runtime.*;
import org.eclipse.equinox.internal.p2.touchpoint.natives.Messages;
import org.eclipse.equinox.internal.p2.touchpoint.natives.Util;
import org.eclipse.equinox.p2.engine.spi.ProvisioningAction;
import org.eclipse.osgi.util.NLS;

public class ChmodAction extends ProvisioningAction {
	private static final String ACTION_CHMOD = "chmod"; //$NON-NLS-1$

	public IStatus execute(Map<String, Object> parameters) {
		String targetDir = (String) parameters.get(ActionConstants.PARM_TARGET_DIR);
		if (targetDir == null)
			return Util.createError(NLS.bind(Messages.param_not_set, ActionConstants.PARM_TARGET_DIR, ACTION_CHMOD));
		String targetFile = (String) parameters.get(ActionConstants.PARM_TARGET_FILE);
		if (targetFile == null)
			return Util.createError(NLS.bind(Messages.param_not_set, ActionConstants.PARM_TARGET_FILE, ACTION_CHMOD));
		String permissions = (String) parameters.get(ActionConstants.PARM_PERMISSIONS);
		if (permissions == null)
			return Util.createError(NLS.bind(Messages.param_not_set, ActionConstants.PARM_PERMISSIONS, ACTION_CHMOD));
		String optionsString = (String) parameters.get(ActionConstants.PARM_OPTIONS);

		// Check that file exist
		File probe = new File(targetDir + IPath.SEPARATOR + targetFile);
		if (!probe.exists())
			return Util.createError(NLS.bind(Messages.action_0_failed_file_1_doesNotExist, ACTION_CHMOD, probe.toString()));

		String options[] = null;
		if (optionsString != null) {
			ArrayList<String> collect = new ArrayList<String>();
			String r = optionsString.trim();
			while (r.length() > 0) {
				int spaceIdx = r.indexOf(' ');
				if (spaceIdx < 0) {
					collect.add(r);
					r = ""; //$NON-NLS-1$
				} else {
					collect.add(r.substring(0, spaceIdx));
					r = r.substring(spaceIdx + 1);
					r = r.trim();
				}
			}
			if (collect.size() > 0) {
				options = new String[collect.size()];
				collect.toArray(options);
			}
		}

		chmod(targetDir, targetFile, permissions, options);
		return Status.OK_STATUS;
	}

	public IStatus undo(Map<String, Object> parameters) {
		//TODO: implement undo ??
		return Status.OK_STATUS;
	}

	public void chmod(String targetDir, String targetFile, String perms, String[] options) {
		Runtime r = Runtime.getRuntime();
		try {
			// Note: 3 is from chmod, permissions, and target
			String[] args = new String[3 + (options == null ? 0 : options.length)];
			int i = 0;
			args[i++] = "chmod"; //$NON-NLS-1$
			if (options != null) {
				for (int j = 0; j < options.length; j++)
					args[i++] = options[j];
			}
			args[i++] = perms;
			args[i] = targetDir + IPath.SEPARATOR + targetFile;
			Process process = r.exec(args);
			readOffStream(process.getErrorStream());
			readOffStream(process.getInputStream());
			try {
				process.waitFor();
			} catch (InterruptedException e) {
				// mark thread interrupted and continue
				Thread.currentThread().interrupt();
			}
		} catch (IOException e) {
			// ignore
		}
	}

	private void readOffStream(InputStream inputStream) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		try {
			while (reader.readLine() != null) {
				// do nothing
			}
		} catch (IOException e) {
			// ignore
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}
}