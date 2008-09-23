/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.internal.frameworkadmin.equinox.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import org.eclipse.equinox.internal.frameworkadmin.equinox.EquinoxConstants;
import org.eclipse.equinox.internal.provisional.frameworkadmin.LauncherData;
import org.eclipse.equinox.internal.provisional.frameworkadmin.Manipulator;
import org.osgi.framework.Version;

public class FileUtils {

	private static String FILE_PROTOCOL = "file:"; //$NON-NLS-1$
	private static String REFERENCE_PROTOCOL = "reference:"; //$NON-NLS-1$
	private static String INITIAL_PREFIX = "initial@"; //$NON-NLS-1$

	/**
	 * locations that are URLs are returned as is.  Otherwise, resolve the location against
	 * the given Manipulator
	 * 
	 * @param manipulator
	 * @param location
	 * @return a URL string for the actual location, or null
	 */
	// based on org.eclipse.core.runtime.adaptor.EclipseStarter#searchForBundle
	public static String getEclipseRealLocation(final Manipulator manipulator, String location) {
		//if this is some form of URL just return it
		try {
			new URL(location);
			return location;
		} catch (MalformedURLException e) {
			//expected
		}

		File base = new File(location);
		if (!base.isAbsolute()) {
			String pluginsDir = getSyspath(manipulator);
			if (pluginsDir == null)
				return null;
			base = new File(pluginsDir, location);
		}

		return getEclipsePluginFullLocation(base.getName(), base.getParentFile());
	}

	private static String getSyspath(final Manipulator manipulator) {
		Properties properties = manipulator.getConfigData().getFwDependentProps();
		String path = (String) properties.get(EquinoxConstants.PROP_OSGI_SYSPATH);
		if (path != null)
			return path;
		path = (String) properties.get(EquinoxConstants.PROP_OSGI_FW);
		if (path != null) {
			if (path.startsWith(FILE_PROTOCOL))
				path = path.substring(FILE_PROTOCOL.length());
			File file = new File(path);
			return file.getParentFile().getAbsolutePath();
		}

		LauncherData launcherData = manipulator.getLauncherData();
		File home = launcherData.getHome();
		File pluginsDir = null;
		if (home != null)
			pluginsDir = new File(home, EquinoxConstants.PLUGINS_DIR);
		else if (launcherData.getLauncher() != null)
			pluginsDir = new File(launcherData.getLauncher().getParentFile(), EquinoxConstants.PLUGINS_DIR);
		else if (launcherData.getFwJar() != null)
			pluginsDir = launcherData.getFwJar().getParentFile();

		if (pluginsDir != null)
			return pluginsDir.getAbsolutePath();
		return null;
	}

	public static String getRealLocation(Manipulator manipulator, final String location, boolean useEclipse) {
		if (location == null)
			return null;
		String ret = location;
		if (location.startsWith(REFERENCE_PROTOCOL))
			ret = location.substring(REFERENCE_PROTOCOL.length());
		else if (location.startsWith(INITIAL_PREFIX))
			ret = location.substring(INITIAL_PREFIX.length());

		if (!useEclipse)
			return ret;

		return FileUtils.getEclipseRealLocation(manipulator, ret);
	}

	/**
	 * If a bundle of the specified location is in the Eclipse plugin format (either plugin-name_version.jar 
	 * or as a folder named plugin-name_version ), return version string.Otherwise, return null;
	 * 
	 * @param url
	 * @param pluginName
	 * @return version string. If invalid format, return null. 
	 */
	private static Version getVersion(String version) {
		if (version.length() == 0)
			return Version.emptyVersion;

		if (version.endsWith(".jar")) //$NON-NLS-1$
			version = version.substring(0, version.length() - 4);

		try {
			return new Version(version);
		} catch (IllegalArgumentException e) {
			// bad format
			return null;
		}
	}

	/**
	 * Find the named plugin in the given bundlesDir
	 * @param pluginName
	 * @param bundlesDir
	 * @return a URL string for the found plugin, or null
	 */
	// Based on org.eclipse.core.runtime.adaptor.EclipseStarter#searchFor
	public static String getEclipsePluginFullLocation(String pluginName, File bundlesDir) {
		if (bundlesDir == null)
			return null;
		File[] candidates = bundlesDir.listFiles();
		if (candidates == null)
			return null;

		File result = null;
		Version maxVersion = null;

		for (int i = 0; i < candidates.length; i++) {
			String candidateName = candidates[i].getName();
			if (!candidateName.startsWith(pluginName))
				continue;

			if (candidateName.length() > pluginName.length() && candidateName.charAt(pluginName.length()) != '_') {
				// allow jar file with no _version tacked on the end
				if (!candidates[i].isFile() || (candidateName.length() != 4 + pluginName.length()) || !candidateName.endsWith(".jar")) //$NON-NLS-1$
					continue;
			}

			String candidateVersion = ""; //$NON-NLS-1$
			if (candidateName.length() > pluginName.length() + 1 && candidateName.charAt(pluginName.length()) == '_')
				candidateVersion = candidateName.substring(pluginName.length() + 1);

			Version currentVersion = getVersion(candidateVersion);
			if (currentVersion == null)
				continue;

			if (maxVersion == null || maxVersion.compareTo(currentVersion) < 0) {
				maxVersion = currentVersion;
				result = candidates[i];
			}
		}
		try {
			return result != null ? result.getAbsoluteFile().toURL().toExternalForm() : null;
		} catch (MalformedURLException e) {
			return null;
		}
	}
}
