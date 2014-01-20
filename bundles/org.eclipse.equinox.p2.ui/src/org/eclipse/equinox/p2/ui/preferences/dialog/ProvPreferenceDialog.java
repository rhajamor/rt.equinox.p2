package org.eclipse.equinox.p2.ui.preferences.dialog;

import org.eclipse.core.runtime.Assert;
import org.eclipse.equinox.internal.p2.ui.ProvUI;
import org.eclipse.equinox.internal.p2.ui.ProvUIActivator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Preference dialog for the provisioning wizard including the ability to load/save
 * preferences.
 */
public class ProvPreferenceDialog extends FilteredPreferenceDialog {
	/**
	 * There can only ever be one instance of the workbench's preference dialog.
	 * This keeps a handle on this instance, so that attempts to create a second
	 * dialog should just fail (or return the original instance).
	 * 
	 * @since 3.1
	 */
	private static ProvPreferenceDialog instance = null;

	/**
	 * The bounds of this dialog will be persisted in the dialog settings.
	 * This is defined at the most concrete level of the hierarchy so that
	 * different concrete implementations don't necessarily store their bounds 
	 * in the same settings.
	 * 
	 * @since 3.2
	 */
	private static final String DIALOG_SETTINGS_SECTION = "ProvPreferenceDialogSettings"; //$NON-NLS-1$

	private String initialPageId;

	private Object pageData;

	/**
	 * Creates a workbench preference dialog to a particular preference page. It
	 * is the responsibility of the caller to then call <code>open()</code>.
	 * The call to <code>open()</code> will not return until the dialog
	 * closes, so this is the last chance to manipulate the dialog.
	 * 
	 * @param shell
	 * 			The Shell to parent the dialog off of if it is not
	 * 			already created. May be <code>null</code>
	 * 			in which case the active workbench window will be used
	 * 			if available.
	 * @param preferencePageId
	 *            The identifier of the preference page to open; may be
	 *            <code>null</code>. If it is <code>null</code>, then the
	 *            preference page is not selected or modified in any way.
	 * @return The selected preference page.
	 * @since 3.1
	 */
	public static final ProvPreferenceDialog createDialogOn(Shell shell, final String preferencePageId) {
		final ProvPreferenceDialog dialog;

		if (instance == null) {
			/*
			 * There is no existing preference dialog, so open a new one with
			 * the given selected page.
			 */

			Shell parentShell = shell;
			if (parentShell == null) {
				parentShell = ProvUI.getDefaultParentShell();
			}

			// Create the dialog
			final PreferenceManager preferenceManager = ProvUI.getPreferenceManager();
			dialog = new ProvPreferenceDialog(parentShell, preferenceManager);
			if (preferencePageId != null) {
				dialog.setSelectedNode(preferencePageId);
				dialog.setInitialPage(preferencePageId);
			}
			dialog.create();
		} else {
			/*
			 * There is an existing preference dialog, so let's just select the
			 * given preference page.
			 */
			dialog = instance;
			if (preferencePageId != null) {
				dialog.setSelectedNode(preferencePageId);
				dialog.setInitialPage(preferencePageId);
			}

		}

		// Get the selected node, and return it.
		return dialog;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferenceDialog#createPage(org.eclipse.jface.preference.IPreferenceNode)
	 */
	protected void createPage(IPreferenceNode node) {
		super.createPage(node);
		if (this.pageData == null) {
			return;
		}
		// Apply the data if it has been set.
		IPreferencePage page = node.getPage();
		if (page instanceof PreferencePage) {
			((PreferencePage) page).applyData(this.pageData);
		}

	}

	/**
	 * @return the pageData
	 */
	public Object getPageData() {
		return pageData;
	}

	/**
	 * @param pageData the pageData to set
	 */
	public void setPageData(Object pageData) {
		this.pageData = pageData;
	}

	/**
	 * Creates a new preference dialog under the control of the given preference
	 * manager.
	 * 
	 * @param parentShell
	 *            the parent shell
	 * @param manager
	 *            the preference manager
	 */
	public ProvPreferenceDialog(Shell parentShell, PreferenceManager manager) {
		super(parentShell, manager);
		Assert.isTrue((instance == null), "There cannot be two preference dialogs at once in the workbench."); //$NON-NLS-1$
		instance = this;

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceDialog#getCurrentPage()
	 */
	@Override
	public IPreferencePage getCurrentPage() {
		return super.getCurrentPage();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
		instance = null;
		return super.close();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		super.okPressed();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Dialog#getDialogBoundsSettings()
	 * 
	 * @since 3.2
	 */
	protected IDialogSettings getDialogBoundsSettings() {
		IDialogSettings settings = ProvUIActivator.getDefault().getDialogSettings();
		IDialogSettings section = settings.getSection(DIALOG_SETTINGS_SECTION);
		if (section == null) {
			section = settings.addNewSection(DIALOG_SETTINGS_SECTION);
		}
		return section;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Dialog#getDialogBoundsStrategy()
	 * 
	 * Overridden to persist only the location, not the size, since the current
	 * page dictates the most appropriate size for the dialog.
	 * @since 3.2
	 */
	protected int getDialogBoundsStrategy() {
		return DIALOG_PERSISTLOCATION;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceDialog#open()
	 * Overrides to set focus to the specific page if it a specific page was requested. 
	 * @since 3.5
	 */
	public int open() {
		IPreferencePage selectedPage = getCurrentPage();
		if ((initialPageId != null) && (selectedPage != null)) {
			Shell shell = getShell();
			if ((shell != null) && (!shell.isDisposed())) {
				shell.open(); // make the dialog visible to properly set the focus
				Control control = selectedPage.getControl();
				if (!ProvUI.isFocusAncestor(control))
					control.setFocus();
			}
		}
		return super.open();
	}

	/**
	 * Remembers the initial page ID
	 * @param pageId ID of the initial page to display
	 * @since 3.5
	 */
	public void setInitialPage(String pageId) {
		initialPageId = pageId;
	}

}
