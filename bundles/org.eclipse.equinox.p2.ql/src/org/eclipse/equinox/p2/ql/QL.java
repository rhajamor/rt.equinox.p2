/*******************************************************************************
 * Copyright (c) 2009 Cloudsmith Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Cloudsmith Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.p2.ql;

import java.util.Iterator;
import org.eclipse.equinox.internal.p2.ql.QueryContext;
import org.eclipse.equinox.internal.p2.ql.expression.ExpressionFactory;
import org.eclipse.equinox.internal.p2.ql.parser.ExpressionParser;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.IQueryable;

/**
 * The public access point to all QL functionality.
 */
public abstract class QL {
	/**
	 * Returns the default expression factory.
	 * @return the default expression factory.
	 */
	public static IExpressionFactory getFactory() {
		return ExpressionFactory.INSTANCE;
	}

	/**
	 * Creates a new instance of the default expression parser.
	 * @return the new parser.
	 */
	public static IExpressionParser newParser() {
		return new ExpressionParser(getFactory());
	}

	/**
	 * Creates a query context based on the given queryable
	 * @param queryable The queryable to use for the creation of the context
	 * @return A new context
	 */
	public static IQueryContext newQueryContext(IQueryable queryable) {
		return new QueryContext(queryable);
	}

	/**
	 * Creates a query context based on the given iterator
	 * @param iterator The iterator to use for the creation of the context
	 * @return A new context
	 */
	public static IQueryContext newQueryContext(Iterator iterator) {
		return new QueryContext(iterator);
	}
}