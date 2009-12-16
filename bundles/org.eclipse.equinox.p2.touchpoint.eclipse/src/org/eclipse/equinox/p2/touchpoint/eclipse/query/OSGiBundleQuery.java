/*******************************************************************************
 *  Copyright (c) 2008, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.p2.touchpoint.eclipse.query;

import org.eclipse.equinox.internal.provisional.p2.metadata.IProvidedCapability;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.MatchQuery;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;

/**
 * A query matching every {@link IInstallableUnit} that describes an OSGi bundle. 
 * @since 2.0
 */
public class OSGiBundleQuery extends MatchQuery {

	public boolean isMatch(Object candidate) {
		if (candidate instanceof IInstallableUnit) {
			return isOSGiBundle((IInstallableUnit) candidate);
		}
		return false;
	}

	/**
	 * Test if the {@link IInstallableUnit} describes an OSGi bundle. 
	 * @param iu the element being tested.
	 * @return <tt>true</tt> if the parameter describes an OSGi bundle.
	 */
	public static boolean isOSGiBundle(IInstallableUnit iu) {
		IProvidedCapability[] provided = iu.getProvidedCapabilities();
		for (int i = 0; i < provided.length; i++) {
			if (provided[i].getNamespace().equals("osgi.bundle")) { //$NON-NLS-1$
				return true;
			}
		}
		return false;
	}
}