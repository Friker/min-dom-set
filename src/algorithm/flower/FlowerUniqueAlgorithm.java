package algorithm.flower;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntObjectOpenHashMap;
import com.carrotsearch.hppc.IntOpenHashSet;
import com.carrotsearch.hppc.cursors.IntCursor;

import datastructure.graph.Graph;
import algorithm.AbstractMDSAlgorithm;
import algorithm.AbstractMDSResult;
import algorithm.MDSResultBackedByIntOpenHashSet;

public class FlowerUniqueAlgorithm implements AbstractMDSAlgorithm {
	private long prepTime = -1L;
	private long runTime = -1L;

	private static class ResultHolder {
		public int result;
		public int iterations;
		public int neighCount;
		public int skipped;
	}

	private ResultHolder maxByN1(IntOpenHashSet white,
			IntObjectOpenHashMap<IntOpenHashSet> neig, int oldMax) {
		ResultHolder rh = new ResultHolder();
		int max = 0;
		int maxCount = 0;
		rh.skipped = 0;
		int iterations = 0;
		for (IntCursor current : white) {
			iterations = iterations + 1;
			int currentCount = neig.get(current.value).size();
			if (currentCount > maxCount) {
				max = current.value;
				maxCount = currentCount;
			}
			if (oldMax == currentCount) {
				rh.skipped = 1;
				break;
			}
		}
		rh.result = max;
		rh.neighCount = maxCount;
		rh.iterations = iterations;
		return rh;
	}

	@Override
	public AbstractMDSResult mdsAlg(Graph g) {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		long start = bean.getCurrentThreadCpuTime();
		IntOpenHashSet W = new IntOpenHashSet(g.getVertices());
		IntOpenHashSet G = new IntOpenHashSet(g.getVertices());
		int initialSize = (int)Math.ceil(g.getNumberOfVertices() * (1/0.65)) + 1;

		IntObjectOpenHashMap<IntOpenHashSet> neigW = new IntObjectOpenHashMap<>(initialSize);
		for (IntCursor vcur : W) {
			neigW.put(vcur.value, g.getN1(vcur.value));
		}

		prepTime = bean.getCurrentThreadCpuTime() - start;
		IntOpenHashSet S = new IntOpenHashSet(initialSize);
		int iterations = 0;
		IntArrayList uniqueFlowers = new IntArrayList();
		for (IntCursor vcur : W) {
			iterations = iterations + 1;
			IntOpenHashSet neighs = neigW.get(vcur.value);
			if (neighs.size() == 2) {
				for (IntCursor vvcur : neighs) {
					if (vvcur.value != vcur.value) {
						uniqueFlowers.add(vvcur.value);
					}
				}
			}
		}
		System.out.println("Unique flowers: " + uniqueFlowers.size());
		for (IntCursor flowercur : uniqueFlowers) {
			iterations = iterations + 1;
			W.remove(flowercur.value);
			IntOpenHashSet greying = new IntOpenHashSet(neigW.get(flowercur.value));
			G.removeAll(greying);
			for (IntCursor v : g.getN2(flowercur.value)) {
				neigW.get(v.value).removeAll(greying);
			}
			S.add(flowercur.value);
		}
		int lastMax = -1;
		int skipped = 0;
		while (!G.isEmpty()) {
			iterations = iterations + 1;
			ResultHolder rh = maxByN1(W, neigW, lastMax);
			iterations = iterations + rh.iterations;
			lastMax = rh.neighCount;
			int pick = rh.result;
			skipped = skipped + rh.skipped;
			W.remove(pick);
			IntOpenHashSet greying = new IntOpenHashSet(neigW.get(pick));
			G.removeAll(greying);
			for (IntCursor v : g.getN2(pick)) {
				neigW.get(v.value).removeAll(greying);
			}
			S.add(pick);
		}
		runTime = bean.getCurrentThreadCpuTime() - start;
		System.out.println("Number of iterations: " + iterations);
		System.out.println("Skipped: " + skipped);
		MDSResultBackedByIntOpenHashSet result = new MDSResultBackedByIntOpenHashSet();
		result.setResult(S);
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
