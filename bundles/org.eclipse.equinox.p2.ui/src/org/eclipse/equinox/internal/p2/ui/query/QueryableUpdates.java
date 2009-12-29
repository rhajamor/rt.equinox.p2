/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     EclipseSource - ongoing development
 *******************************************************************************/
package org.eclipse.equinox.internal.p2.ui.query;

import java.util.ArrayList;
import org.eclipse.core.runtime.*;
import org.eclipse.equinox.internal.p2.ui.ProvUIMessages;
import org.eclipse.equinox.internal.provisional.p2.director.IPlanner;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.Collector;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.IQueryable;
import org.eclipse.equinox.p2.engine.ProvisioningContext;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.query.IQuery;
import org.eclipse.equinox.p2.metadata.query.IQueryResult;
import org.eclipse.equinox.p2.ui.ProvisioningUI;

/**
 * An object that implements a query for available updates
 */
public class QueryableUpdates implements IQueryable<IInstallableUnit> {

	private IInstallableUnit[] iusToUpdate;
	ProvisioningUI ui;

	public QueryableUpdates(ProvisioningUI ui, IInstallableUnit[] iusToUpdate) {
		this.ui = ui;
		this.iusToUpdate = iusToUpdate;
	}

	public IQueryResult<IInstallableUnit> query(IQuery<IInstallableUnit> query, IProgressMonitor monitor) {
		if (monitor == null)
			monitor = new NullProgressMonitor();
		int totalWork = 2000;
		monitor.beginTask(ProvUIMessages.QueryableUpdates_UpdateListProgress, totalWork);
		IPlanner planner = ui.getSession().getPlanner();
		try {
			ArrayList<IInstallableUnit> allUpdates = new ArrayList<IInstallableUnit>();
			for (int i = 0; i < iusToUpdate.length; i++) {
				if (monitor.isCanceled())
					return Collector.emptyCollector();
				IInstallableUnit[] updates = planner.updatesFor(iusToUpdate[i], new ProvisioningContext(), new SubProgressMonitor(monitor, totalWork / 2 / iusToUpdate.length));
				for (int j = 0; j < updates.length; j++)
					allUpdates.add(updates[j]);
			}
			return query.perform(allUpdates.iterator());
		} catch (OperationCanceledException e) {
			// Nothing more to do, return result
			return Collector.emptyCollector();
		} finally {
			monitor.done();
		}
	}
}
