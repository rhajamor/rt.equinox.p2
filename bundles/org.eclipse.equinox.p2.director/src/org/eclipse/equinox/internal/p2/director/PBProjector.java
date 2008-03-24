/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.equinox.internal.p2.director;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import org.eclipse.core.runtime.*;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.metadata.RequiredCapability;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.InvalidSyntaxException;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.SolverFactory;
import org.sat4j.pb.core.PBSolverCP;
import org.sat4j.pb.orders.VarOrderHeapObjective;
import org.sat4j.pb.reader.OPBEclipseReader2007;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.specs.*;

/**
 * This class is the interface between SAT4J and the planner. It produces a
 * boolean satisfiability problem, invokes the solver, and converts the solver result
 * back into information understandable by the planner.
 */
public class PBProjector {
	private static boolean DEBUG = false;
	private Picker picker;

	private Map variables; //key IU, value corresponding variable in the problem
	private List toConsider; //IUs to add to the slice
	private TwoTierMap slice; //The IUs that have been considered to be part of the problem
	private ArrayList nonGreedyRequirements; //List of non greedy requirements. They are processed separately once the slicing has been done since they don't bring in new IU 

	private Dictionary selectionContext;

	private final static int shift = 1;

	private ArrayList constraints;
	private ArrayList dependencies;
	private ArrayList tautologies;
	private String objective;

	private Collection solution;
	private MultiStatus result;

	private File problemFile;

	public PBProjector(Picker p, Dictionary context) {
		picker = p;
		variables = new HashMap();
		slice = new TwoTierMap();
		constraints = new ArrayList();
		tautologies = new ArrayList();
		dependencies = new ArrayList();
		selectionContext = context;
		nonGreedyRequirements = new ArrayList();
		result = new MultiStatus(DirectorActivator.PI_DIRECTOR, IStatus.OK, "Problems resolving provisioning plan.", null);
	}

	public void encode(IInstallableUnit[] ius, IProgressMonitor monitor) {
		try {
			long start = 0;
			if (DEBUG) {
				start = System.currentTimeMillis();
				System.out.println("Start slicing: " + start); //$NON-NLS-1$
			}

			ius = validateInput(ius);

			toConsider = new ArrayList();
			toConsider.addAll(Arrays.asList(ius));
			for (int i = 0; i < toConsider.size(); i++) {
				IInstallableUnit current = (IInstallableUnit) toConsider.get(i);
				processIU(current);
			}
			processNonGreedyRequirements();
			createConstraintsForSingleton();
			for (int i = 0; i < ius.length; i++) {
				createMustHaves(ius[i]);
			}
			createOptimizationFunction();
			persist();
			if (DEBUG) {
				long stop = System.currentTimeMillis();
				System.out.println("Slicing complete: " + (stop - start)); //$NON-NLS-1$
			}
		} catch (IllegalStateException e) {
			result.add(new Status(IStatus.ERROR, DirectorActivator.PI_DIRECTOR, e.getMessage(), e));
		}
	}

	/**
	 * Filter out IUs that are not applicable to the environment, and create warnings
	 * for each non-applicable IU.
	 */
	private IInstallableUnit[] validateInput(IInstallableUnit[] ius) {
		Map versions = new HashMap();
		List toRemove = new ArrayList();
		ArrayList applicable = new ArrayList(ius.length);
		for (int i = 0; i < ius.length; i++) {
			IInstallableUnit iu = ius[i];
			if (isApplicable(iu.getFilter())) {
				applicable.add(iu);
				// if we have a singleton bundle, only try to install the highest version
				if (iu.isSingleton()) {
					String id = iu.getId();
					IInstallableUnit oldIU = (IInstallableUnit) versions.get(id);
					if (oldIU == null) {
						versions.put(id, iu);
					} else {
						IInstallableUnit removed = iu;
						if (iu.getVersion().compareTo(oldIU.getVersion()) > 0) {
							versions.put(id, iu);
							removed = oldIU;
						}
						toRemove.add(removed);
						String msg = NLS.bind("{0} version {1} cannot be installed because it is marked as a singleton and a higher version has been found.", id, removed.getVersion()); //$NON-NLS-1$
						result.add(new Status(IStatus.WARNING, DirectorActivator.PI_DIRECTOR, msg, null));
					}
				}
			} else {
				String msg = NLS.bind("{0} cannot be installed because its filter is not satisfied in this environment.", ius[i].getId()); //$NON-NLS-1$
				result.add(new Status(IStatus.WARNING, DirectorActivator.PI_DIRECTOR, msg, null));
			}
		}
		applicable.removeAll(toRemove);
		return (IInstallableUnit[]) applicable.toArray(new IInstallableUnit[applicable.size()]);
	}

	private void processNonGreedyRequirements() {
		if (nonGreedyRequirements.size() == 0)
			return;
		IInstallableUnit[] toPickFrom = (IInstallableUnit[]) slice.values().toArray(new IInstallableUnit[slice.values().size()]);
		Picker p = new Picker(toPickFrom, null);
		for (Iterator iterator = nonGreedyRequirements.iterator(); iterator.hasNext();) {
			Object[] req = (Object[]) iterator.next();
			getVariable((IInstallableUnit) req[0]);
			expandRequirement((IInstallableUnit) req[0], (RequiredCapability) req[1], p);
		}

	}

	//Create an optimization function favoring the highest version of each IU  
	private void createOptimizationFunction() {
		objective = "min:"; //$NON-NLS-1$
		Set s = slice.entrySet();
		for (Iterator iterator = s.iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			HashMap conflictingEntries = (HashMap) entry.getValue();
			if (conflictingEntries.size() <= 1) {
				objective += " 1 " + getVariable((IInstallableUnit) conflictingEntries.values().iterator().next()); //$NON-NLS-1$
				continue;
			}
			List toSort = new ArrayList(conflictingEntries.values());
			Collections.sort(toSort);
			int weight = toSort.size();
			for (Iterator iterator2 = toSort.iterator(); iterator2.hasNext();) {
				objective += " " + weight-- + " " + getVariable((IInstallableUnit) iterator2.next()); //$NON-NLS-1$//$NON-NLS-2$
			}
		}
		objective += " ;"; //$NON-NLS-1$
	}

	private void createMustHaves(IInstallableUnit iu) {
		tautologies.add(" +1 " + getVariable(iu) + " = 1;"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void createNegation(IInstallableUnit iu) {
		tautologies.add(" +1" + getVariable(iu) + " = 0;"); //$NON-NLS-1$//$NON-NLS-2$
	}

	//Write the problem generated into a temporary file
	private void persist() {
		try {
			problemFile = File.createTempFile("p2Encoding", ".opb"); //$NON-NLS-1$//$NON-NLS-2$
			BufferedWriter w = new BufferedWriter(new FileWriter(problemFile));
			int clauseCount = tautologies.size() + dependencies.size() + constraints.size();

			int variableCount = variables.size();
			w.write("* #variable= " + variableCount + " #constraint= " + clauseCount + "  "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			w.newLine();
			w.write("*"); //$NON-NLS-1$
			w.newLine();

			if (variableCount == 0 && clauseCount == 0) {
				w.close();
				return;
			}
			w.write(objective);
			w.newLine();
			w.newLine();

			w.write(explanation + " ;"); //$NON-NLS-1$
			w.newLine();
			w.newLine();

			for (Iterator iterator = dependencies.iterator(); iterator.hasNext();) {
				w.write((String) iterator.next());
				w.newLine();
			}
			for (Iterator iterator = constraints.iterator(); iterator.hasNext();) {
				w.write((String) iterator.next());
				w.newLine();
			}
			for (Iterator iterator = tautologies.iterator(); iterator.hasNext();) {
				w.write((String) iterator.next());
				w.newLine();
			}
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean isApplicable(String filter) {
		if (filter == null)
			return true;
		try {
			return DirectorActivator.context.createFilter(filter).match(selectionContext);
		} catch (InvalidSyntaxException e) {
			return false;
		}
	}

	String explanation = "explain: ";

	private void processIU(IInstallableUnit iu) {
		slice.put(iu.getId(), iu.getVersion(), iu);
		explanation += " " + getVariable(iu);
		if (!isApplicable(iu.getFilter())) {
			createNegation(iu);
			return;
		}

		RequiredCapability[] reqs = iu.getRequiredCapabilities();
		if (reqs.length == 0) {
			return;
		}
		for (int i = 0; i < reqs.length; i++) {
			if (!isApplicable(reqs[i].getFilter()) || reqs[i].isOptional())
				continue;

			if (!reqs[i].isGreedy()) {
				nonGreedyRequirements.add(new Object[] {iu, reqs[i]});
				continue;
			}
			try {
				expandRequirement(iu, reqs[i], picker);
			} catch (IllegalStateException ise) {
				createNegation(iu);
				return;
			}
		}
	}

	private void expandRequirement(IInstallableUnit iu, RequiredCapability req, Picker toPickFrom) {
		String expression = "-1 " + getVariable(iu); //$NON-NLS-1$
		Collection[] found = toPickFrom.findInstallableUnit(null, null, req);

		int countMatches = 0;
		for (int j = 0; j < found.length; j++) {
			for (Iterator iterator = found[j].iterator(); iterator.hasNext();) {
				IInstallableUnit match = (IInstallableUnit) iterator.next();
				if (!isApplicable(match.getFilter()))
					continue;
				countMatches++;
				expression += " +1 " + getVariable(match); //$NON-NLS-1$
				if (!slice.containsKey(match.getId(), match.getVersion()))
					if (!toConsider.contains(match))
						toConsider.add(match);
			}
		}
		if (countMatches > 0) {
			dependencies.add(expression + (countMatches == 1 ? " >= 0;" : " >= 0;")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			if (!req.isGreedy())
				return;

			if (req.isOptional()) {
				if (DEBUG)
					System.out.println("No IU found to satisfy optional dependency of " + iu + " req " + req); //$NON-NLS-1$//$NON-NLS-2$
			} else {
				throw new IllegalStateException("No IU found to satisfy dependency of " + iu + " req " + req); //$NON-NLS-1$//$NON-NLS-2$
			}
		}
	}

	//Create constraints to deal with singleton
	//When there is a mix of singleton and non singleton, several constraints are generated 
	private void createConstraintsForSingleton() {
		Set s = slice.entrySet();
		for (Iterator iterator = s.iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			HashMap conflictingEntries = (HashMap) entry.getValue();
			if (conflictingEntries.size() < 2)
				continue;

			Collection conflictingVersions = conflictingEntries.values();
			String singletonRule = ""; //$NON-NLS-1$
			ArrayList nonSingleton = new ArrayList();
			int countSingleton = 0;
			for (Iterator conflictIterator = conflictingVersions.iterator(); conflictIterator.hasNext();) {
				IInstallableUnit conflictElt = (IInstallableUnit) conflictIterator.next();
				if (conflictElt.isSingleton()) {
					singletonRule += " -1 " + getVariable(conflictElt); //$NON-NLS-1$
					countSingleton++;
				} else {
					nonSingleton.add(conflictElt);
				}
			}
			if (countSingleton == 0)
				continue;

			for (Iterator iterator2 = nonSingleton.iterator(); iterator2.hasNext();) {
				constraints.add(singletonRule + " -1 " + getVariable((IInstallableUnit) iterator2.next()) + " >= -1;"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			singletonRule += " >= -1;"; //$NON-NLS-1$
			constraints.add(singletonRule);
		}
	}

	//Return the corresponding variable 
	private String getVariable(IInstallableUnit iu) {
		String v = (String) variables.get(iu);
		if (v == null) {
			//			v = new String("x" + (variables.size() + shift) + iu.toString()); //$NON-NLS-1$
			v = new String("x" + (variables.size() + shift)); //$NON-NLS-1$
			variables.put(iu, v);
		}
		return v;
	}

	public IStatus invokeSolver(IProgressMonitor monitor) {
		if (result.getSeverity() == IStatus.ERROR)
			return result;
		IPBSolver solver = SolverFactory.newEclipseP2();
		solver.setTimeout(60);
		OPBEclipseReader2007 reader = new OPBEclipseReader2007(solver);
		// CNF filename is given on the command line 
		long start = System.currentTimeMillis();
		if (DEBUG)
			System.out.println("Invoking solver: " + start); //$NON-NLS-1$
		FileReader fr = null;
		try {
			fr = new FileReader(problemFile);
			PBSolverCP problem = (PBSolverCP) reader.parseInstance(fr);
			if (problem.getOrder() instanceof VarOrderHeapObjective) {
				((VarOrderHeapObjective) problem.getOrder()).setObjectiveFunction(reader.getObjectiveFunction());
			}
			if (problem.isSatisfiable()) {
				if (DEBUG) {
					System.out.println("Satisfiable !"); //$NON-NLS-1$
					System.out.println(reader.decode(problem.model()));
				}
				backToIU(problem);
				long stop = System.currentTimeMillis();
				if (DEBUG)
					System.out.println("Solver solution found: " + (stop - start)); //$NON-NLS-1$
			} else {
				if (DEBUG)
					System.out.println("Unsatisfiable !"); //$NON-NLS-1$
				result.merge(new Status(IStatus.ERROR, DirectorActivator.PI_DIRECTOR, 1, "No solution found", null));
			}
		} catch (FileNotFoundException e) {
			//Ignore we are producing the input file
			if (DEBUG)
				e.printStackTrace();
		} catch (ParseFormatException e) {
			//Ignore we are producing the input file
			if (DEBUG)
				e.printStackTrace();
		} catch (ContradictionException e) {
			result.merge(new Status(IStatus.ERROR, DirectorActivator.PI_DIRECTOR, 1, "No solution found because of a trivial contradiction", e));
		} catch (TimeoutException e) {
			result.merge(new Status(IStatus.ERROR, DirectorActivator.PI_DIRECTOR, 1, "No solution found.", e));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (fr != null)
					fr.close();
			} catch (IOException e) {
				//ignore
			}
			problemFile.delete();
		}
		return result;
	}

	private void backToIU(IProblem problem) {
		solution = new ArrayList();
		for (Iterator allIUs = variables.entrySet().iterator(); allIUs.hasNext();) {
			Entry entry = (Entry) allIUs.next();
			int match = Integer.parseInt(((String) entry.getValue()).substring(1));
			if (problem.model(match))
				solution.add(entry.getKey());
		}
	}

	private void printSolution(Collection state) {
		ArrayList l = new ArrayList(state);
		Collections.sort(l);
		System.out.println("Numbers of IUs selected:" + l.size()); //$NON-NLS-1$
		for (Iterator iterator = l.iterator(); iterator.hasNext();) {
			System.out.println(iterator.next());
		}
	}

	public Collection extractSolution() {
		if (DEBUG)
			printSolution(solution);
		return solution;
	}

}
