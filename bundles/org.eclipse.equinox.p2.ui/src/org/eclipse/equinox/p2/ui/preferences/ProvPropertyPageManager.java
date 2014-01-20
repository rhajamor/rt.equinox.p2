package org.eclipse.equinox.p2.ui.preferences;

import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;

/**
 * This class is created to avoid mentioning preferences
 * in this context. Ideally, JFace preference classes should be
 * renamed into something more generic (for example,
 * 'TreeNavigationDialog').
 */

public class ProvPropertyPageManager extends PreferenceManager {
	public static char PREFERENCE_PAGE_CATEGORY_SEPARATOR = '/';

	/**
	 * The constructor.
	 */
	public ProvPropertyPageManager() {
		super(PREFERENCE_PAGE_CATEGORY_SEPARATOR, new PreferenceNode("")); //$NON-NLS-1$
	}

}
