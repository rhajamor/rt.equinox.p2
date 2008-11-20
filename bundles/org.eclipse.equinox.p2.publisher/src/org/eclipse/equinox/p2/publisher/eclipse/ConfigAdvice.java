/*******************************************************************************
 * Copyright (c) 2008 Code 9 and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Code 9 - initial API and implementation
 ******************************************************************************/
package org.eclipse.equinox.p2.publisher.eclipse;

import java.util.Properties;
import org.eclipse.equinox.internal.provisional.frameworkadmin.BundleInfo;
import org.eclipse.equinox.internal.provisional.frameworkadmin.ConfigData;
import org.eclipse.equinox.p2.publisher.AbstractAdvice;

public class ConfigAdvice extends AbstractAdvice implements IConfigAdvice {

	private ConfigData data;
	private String configSpec;

	public ConfigAdvice(ConfigData data, String configSpec) {
		this.data = data;
		this.configSpec = configSpec;
	}

	public BundleInfo[] getBundles() {
		return data.getBundles();
	}

	protected String getConfigSpec() {
		return configSpec;
	}

	public Properties getProperties() {
		Properties result = new Properties();
		result.putAll(data.getProperties());
		return result;
	}

}
