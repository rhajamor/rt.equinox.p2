package org.eclipse.equinox.p2.ui.preferences.dialog;

import org.eclipse.swt.graphics.Font;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

/**
 * This PreferenceBoldLabelProvider will bold those elements which really match
 * the search contents
 */
public class PreferenceBoldLabelProvider extends PreferenceLabelProviderWithTooltip {

	private FilteredTree filterTree;

	private PatternFilter filterForBoldElements;

	PreferenceBoldLabelProvider(FilteredTree filterTree) {
		this.filterTree = filterTree;
		this.filterForBoldElements = filterTree.getPatternFilter();
	}

	public Font getFont(Object element) {
		return FilteredTree.getBoldFont(element, filterTree, filterForBoldElements);
	}

}
