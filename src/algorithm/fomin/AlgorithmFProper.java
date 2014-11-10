package algorithm.fomin;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import com.carrotsearch.hppc.IntOpenHashSet;
import com.carrotsearch.hppc.ObjectArrayList;
import com.carrotsearch.hppc.cursors.IntCursor;

import datastructure.graph.Graph;
import algorithm.AbstractMDSAlgorithm;
import algorithm.AbstractMDSResult;
import algorithm.MDSResultBackedByIntOpenHashSet;
import algorithm.RepresentedSet;

public class AlgorithmFProper implements AbstractMDSAlgorithm {
	private long prepTime = -1L;
	private long runTime = -1L;

	@Override
	public AbstractMDSResult mdsAlg(Graph g) {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		long start = bean.getCurrentThreadCpuTime();
		AlgorithmMSCFProper fn = new AlgorithmMSCFProper();
		ObjectArrayList<RepresentedSet> sets = new ObjectArrayList<>(g.getNumberOfVertices());
		for (IntCursor v : g.getVertices()) {
			IntOpenHashSet neighs = new IntOpenHashSet(g.getN1(v.value));
			sets.add(new RepresentedSet(v.value, neighs));
		}
		prepTime = bean.getCurrentThreadCpuTime() - start;
		IntOpenHashSet linkedResult = new IntOpenHashSet(fn.getMSCforMDS(null,
				sets, g));
		runTime = bean.getCurrentThreadCpuTime() - start;
		MDSResultBackedByIntOpenHashSet result = new MDSResultBackedByIntOpenHashSet();
		result.setResult(linkedResult);
		return result;
	}

	@Override
	public long getLastPrepTime() {
		return prepTime;
	}

	@Override
	public long getLastRunTime() {
		return runTime;
	}

}
