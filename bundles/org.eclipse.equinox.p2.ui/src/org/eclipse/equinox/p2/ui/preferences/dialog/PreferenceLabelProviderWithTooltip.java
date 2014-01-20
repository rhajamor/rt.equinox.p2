package org.eclipse.equinox.p2.ui.preferences.dialog;

import org.eclipse.equinox.p2.ui.decorators.ContributingPluginDecorator;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;

/**
 * @since 3.5
 *
 */
public class PreferenceLabelProviderWithTooltip extends ColumnLabelProvider {

	ILabelDecorator decorator = new ContributingPluginDecorator();

	protected void initialize(ColumnViewer viewer, ViewerColumn column) {
		super.initialize(viewer, column);
		if (decorator != null) {
			ColumnViewerToolTipSupport.enableFor(viewer);
		}
	}

	/**
	 * copied from org.eclipse.jface.preference.PreferenceLabelProvider
	 * 
	 * @param element
	 *            must be an instance of <code>IPreferenceNode</code>.
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		return ((IPreferenceNode) element).getLabelText();
	}

	/**
	 * copied from org.eclipse.jface.preference.PreferenceLabelProvider
	 * 
	 * @param element
	 *            must be an instance of <code>IPreferenceNode</code>.
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		return ((IPreferenceNode) element).getLabelImage();
	}

	public String getToolTipText(Object element) {
		if (decorator == null) {
			return null;
		}
		if (element instanceof PreferenceNode) {
			PreferenceNode node = (PreferenceNode) element;
			return decorator.decorateText(getText(element), node);
		}
		return null;
	}

}
