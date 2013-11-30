package algorithm.fomin;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import model.Graph;
import algorithm.AbstractMDSAlgorithm;
import algorithm.RepresentedSet;

public class AlgorithmFNaive implements AbstractMDSAlgorithm {

	@Override
	public LinkedHashSet<Long> mdsAlg(Graph g) {
		AlgorithmMSCFNaive fn = new AlgorithmMSCFNaive();
		ArrayList<RepresentedSet> sets = new ArrayList<>();
		for (Long v : g.getVertices()) {
			sets.add(new RepresentedSet(v, g.getNeighboursOfVertexIncluded(v)));
		}
		LinkedHashSet<Long> result = new LinkedHashSet<>(fn.getMSCforMDS(null,
				sets));

		return result;
	}

}