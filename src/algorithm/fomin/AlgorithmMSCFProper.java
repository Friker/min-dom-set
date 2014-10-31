package algorithm.fomin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;

import datastructure.graph.DirectedGraph;
import datastructure.graph.Edge;
import datastructure.graph.Graph;
import main.Utils;
import algorithm.AbstractMSCAlgorithm;
import algorithm.RepresentedSet;

public class AlgorithmMSCFProper implements AbstractMSCAlgorithm {

	private LinkedHashSet<Integer> polyMSC(ArrayList<RepresentedSet> sets) {
		DirectedGraph graph = Utils.getDirectedGraphFromRepresentedSets(sets);
		LinkedHashSet<Edge> pickedEdges = Utils.fordFulkerson(graph);
		HashSet<Integer> pickedVertices = new HashSet<>();
		for (Edge e : pickedEdges) {
			pickedVertices.add(e.from);
			pickedVertices.add(e.to);
		}
		HashSet<Integer> twoSetVertices = new HashSet<>();
		for (RepresentedSet rs : sets) {
			Object[] set = rs.getSet().toArray();
			Integer a = (Integer) set[0];
			Integer b = (Integer) set[1];
			if (set.length == 2) {
				twoSetVertices.add(a);
				twoSetVertices.add(b);
			}
			if (set.length == 0) {
				System.out.println("ERROR " + Arrays.toString(set));
			}
		}
		LinkedHashSet<Integer> result = new LinkedHashSet<>();
		for (RepresentedSet rs : sets) {
			LinkedHashSet<Integer> set = rs.getSet();
			if (set.size() == 1) {
				Integer v = (Integer) set.toArray()[0];
				if (!twoSetVertices.contains(v)) {
					result.add(rs.getRepresentant());
				}
			}
		}
		for (Edge e : pickedEdges) {
			for (RepresentedSet rs : sets) {
				LinkedHashSet<Integer> set = rs.getSet();
				if (set.contains(e.from) && set.contains(e.to)) {
					result.add(rs.getRepresentant());
				}
			}
		}
		// System.out.println(result);
		return result;
	}

	private ArrayList<Integer> getUniversum(ArrayList<RepresentedSet> sets) {
		HashSet<Integer> result = new HashSet<>();
		for (RepresentedSet s : sets) {
			result.addAll(s.getSet());
		}
		return new ArrayList<>(result);
	}

	private ArrayList<RepresentedSet> getDel(RepresentedSet set,
			ArrayList<RepresentedSet> sets) {
		ArrayList<RepresentedSet> result = new ArrayList<>();
		for (RepresentedSet rs : sets) {
			LinkedHashSet<Integer> ss = new LinkedHashSet<>(rs.getSet());
			ss.removeAll(set.getSet());
			if (ss.size() > 0) {
				RepresentedSet ns = new RepresentedSet(rs.getRepresentant(), ss);
				result.add(ns);
			}
		}
		return result;
	}

	private LinkedHashSet<Integer> msc(ArrayList<RepresentedSet> sets,
			LinkedHashSet<Integer> chosen, Graph g) {
		if (sets.size() == 0) {
			return g.isMDS(chosen) ? new LinkedHashSet<>(chosen) : null;
		}

		// one include another
		RepresentedSet included = null;
		boolean found = false;
		// RepresentedSet including = null;
		for (RepresentedSet r : sets) {
			if (found)
				break;
			for (RepresentedSet s : sets) {
				if (r.getSet().containsAll(s.getSet()) && r != s) {
					included = s;
					found = true;
					break;
				}
				if (s.getSet().containsAll(r.getSet()) && s != r) {
					included = r;
					found = true;
					break;
				}
			}
		}
		if (included != null) {
			sets.remove(included);
			LinkedHashSet<Integer> result = msc(sets, chosen, g);
			sets.add(included);
			return result;
		}

		// is unique
		ArrayList<Integer> universum = getUniversum(sets);
		for (Integer l : universum) {
			Integer counter = 0;
			RepresentedSet theRightSet = null;
			for (RepresentedSet s : sets) {
				if (s.getSet().contains(l)) {
					counter++;
					theRightSet = s;
				}
			}
			if (counter == 1L) {
				ArrayList<RepresentedSet> newSets = getDel(theRightSet, sets);
				LinkedHashSet<Integer> chosen2 = new LinkedHashSet<>(chosen);
				chosen2.add(theRightSet.getRepresentant());
				LinkedHashSet<Integer> result = msc(newSets, chosen2, g);
				return result;
			}
		}

		// max cardinality
		int maxCardinality = 0;
		RepresentedSet theRightSet = null;
		for (RepresentedSet s : sets) {
			if (s.getSet().size() > maxCardinality) {
				maxCardinality = s.getSet().size();
				theRightSet = s;
			}
		}

		if (maxCardinality == 2L) {
			LinkedHashSet<Integer> finalChoice = polyMSC(sets);
			chosen.addAll(finalChoice);
			return g.isMDS(chosen) ? new LinkedHashSet<>(chosen) : null;
		}
				
		sets.remove(theRightSet);
		LinkedHashSet<Integer> result1 = msc(sets, chosen, g);

		chosen.add(theRightSet.getRepresentant());
		LinkedHashSet<Integer> result2 = msc(getDel(theRightSet, sets), chosen, g);
		chosen.remove(theRightSet.getRepresentant());

		sets.add(theRightSet);
		if (result1 == null) {
			return result2;
		} else if (result2 == null) {
			return result1;
		}
		return result1.size() < result2.size() ? result1 : result2;
	}


	@Override
	public LinkedHashSet<Integer> getMSCforMDS(LinkedHashSet<Integer> universum,
			ArrayList<RepresentedSet> sets, Graph g) {
		return msc(sets, new LinkedHashSet<Integer>(), g);
	}

}
