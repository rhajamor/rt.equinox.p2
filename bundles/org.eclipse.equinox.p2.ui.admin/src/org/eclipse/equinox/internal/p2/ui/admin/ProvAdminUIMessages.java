/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.equinox.internal.p2.ui.admin;

import org.eclipse.osgi.util.NLS;

/**
 * Message class for provisioning UI messages.  
 * 
 * @since 3.4
 */
public class ProvAdminUIMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.equinox.internal.p2.ui.admin.messages"; //$NON-NLS-1$
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, ProvAdminUIMessages.class);
	}
	public static String AddArtifactRepositoryDialog_OperationLabel;
	public static String AddProfileDialog_Title;
	public static String AddRepositoryDialog_InvalidURL;
	public static String AddMetadataRepositoryDialog_OperationLabel;
	public static String MetadataRepositoriesView_AddRepositoryTooltip;
	public static String MetadataRepositoriesView_AddRepositoryLabel;
	public static String MetadataRepositoriesView_ChooseProfileDialogTitle;
	public static String MetadataRepositoriesView_RemoveRepositoryTooltip;
	public static String MetadataRepositoriesView_RemoveRepositoryOperationLabel;
	public static String ArtifactRepositoriesView_AddRepositoryTooltip;
	public static String ArtifactRepositoriesView_AddRepositoryLabel;
	public static String ArtifactRepositoriesView_RemoveRepositoryTooltip;
	public static String ArtifactRepositoriesView_RemoveRepositoryOperationLabel;
	public static String ProfilesView_0;
	public static String ProfilesView_AddProfileTooltip;
	public static String ProfilesView_AddProfileLabel;
	public static String ProfilesView_RemoveProfileLabel;
	public static String ProfilesView_RemoveProfileTooltip;
	public static String RepositoriesView_RemoveCommandLabel;
	// Preferences
	public static String InstalledIUPropertyPage_NoInfoAvailable;
	public static String ProvisioningPrefPage_HideInternalRepos;
	public static String ProvisioningPrefPage_ShowGroupsOnly;
	public static String ProvisioningPrefPage_ShowInstallRootsOnly;
	public static String ProvisioningPrefPage_CollapseIUVersions;
	public static String ProvisioningPrefPage_Description;
	public static String ProvisioningPrefPage_UseCategories;

	public static String Ops_RemoveProfileOperationLabel;
	public static String AddProfileDialog_OperationLabel;
	public static String AddProfileDialog_DuplicateProfileID;
	public static String UpdateAndInstallDialog_AvailableIUsPageLabel;
	public static String UpdateAndInstallDialog_InstalledIUsPageLabel;
	public static String UpdateAndInstallDialog_Title;

	public static String ProvView_RefreshCommandLabel;
	public static String ProvView_RefreshCommandTooltip;

	public static String ProfileRootPropertyName;

	public static String RepositoryGroup_NameColumnLabel;
	public static String RepositoryGroup_PropertiesLabel;
	public static String RepositoryGroup_ValueColumnLabel;

	public static String IUGroup_ID;
	public static String IUGroup_IU_ID_Required;
	public static String IUGroup_Namespace;
	public static String IUGroup_ProvidedCapabilities;
	public static String IUGroup_RequiredCapabilities;
	public static String IUGroup_TouchpointData;
	public static String IUGroup_TouchpointType;
	public static String IUGroup_Version;
	public static String IUProfilePropertiesGroup_InvalidProfileID;

	public static String No_Property_Item_Selected;

	public static String ProfileGroup_Browse;
	public static String ProfileGroup_Browse2;
	public static String ProfileGroup_Environments;
	public static String ProfileGroup_Flavor;
	public static String ProfileGroup_ID;
	public static String ProfileGroup_Cache;
	public static String ProfileGroup_InstallFolder;
	public static String ProfileGroup_Name;
	public static String ProfileGroup_NL;
	public static String ProfileGroup_SelectProfileMessage;
	public static String ProfileGroup_Description;
	public static String ProfileGroup_ProfileIDRequired;
	public static String ProfileGroup_ProfileInstallFolderRequired;
	public static String ProfileGroup_SelectBundlePoolCache;

}
