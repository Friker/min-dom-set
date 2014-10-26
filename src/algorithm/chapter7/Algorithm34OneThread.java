package algorithm.chapter7;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import model.Graph;
import algorithm.AbstractMDSAlgorithm;

public class Algorithm34OneThread implements AbstractMDSAlgorithm {
	private long prepTime = -1L;
	private long runTime = -1L;
	private LinkedHashMap<Long, Algorithm34State> allVertices = new LinkedHashMap<>();
	private LinkedHashSet<Algorithm34State> unfinishedVertices = new LinkedHashSet<>();
	private LinkedHashSet<Long> S = new LinkedHashSet<>();

	// public Object joinLock = new Object();

	private boolean hasWhiteNeighbours(Long v, Algorithm34State state) {
		boolean result;
		// synchronized (state.W) {
		state.W.remove(v);
		result = !state.W.isEmpty();
		state.W.add(v);
		// }
		return result;
	}

	private Long computeSpan(Algorithm34State state) {
		Long w = Long.valueOf(state.W.size());
		return w;
	}

	private boolean recievedFromAll(Algorithm34State state) {
		return state.spans.keySet().containsAll(state.dist2NotSorG);
	}

	private void joinS(Algorithm34State state,
			ArrayList<Algorithm34State> deleteThisRound) {
		for (Long v : state.dist2NotSorG) {
			allVertices.get(v).W.remove(state.v);
			if (!v.equals(state.v)) {
				allVertices.get(v).dist2NotSorG.remove(state.v);
			}
			allVertices.get(v).spans.remove(state.v);
		}
		S.add(state.v);
		deleteThisRound.add(state);
		return;
	}

	private void joinG(Algorithm34State state,
			ArrayList<Algorithm34State> deleteThisRound) {
		for (Long v : state.dist2NotSorG) {
			allVertices.get(v).W.remove(state.v);
			if (!v.equals(state.v)) {
				allVertices.get(v).dist2NotSorG.remove(state.v);
			}
			allVertices.get(v).spans.remove(state.v);
		}
		deleteThisRound.add(state);
		return;
	}

	@Override
	public LinkedHashSet<Long> mdsAlg(Graph g) {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		long start = bean.getCurrentThreadCpuTime();
		for (Long v : g.getVertices()) {
			Algorithm34State state = new Algorithm34State(v, g);
			unfinishedVertices.add(state);
			allVertices.put(v, state);
		}
		ArrayList<Algorithm34State> deleteThisRound = new ArrayList<>();
		prepTime = bean.getCurrentThreadCpuTime() - start;
		while (!unfinishedVertices.isEmpty()) {
			deleteThisRound.clear();
			for (Algorithm34State state : unfinishedVertices) {
				state.w = computeSpan(state);
				for (Long v2 : state.dist2NotSorG) {
					allVertices.get(v2).recieveSpan(state.v, state.w);
				}
			}
			for (Algorithm34State state : unfinishedVertices) {
				// algorithm!
				if (hasWhiteNeighbours(state.v, state)) {
					if (recievedFromAll(state)) {
						boolean isBiggest = true;
						for (Long v : state.dist2NotSorG) {
							Long getV = state.spans.get(v);
							if (getV > state.w
									|| (getV.equals(state.w) && (state.v > v))) {
								isBiggest = false;
							}
						}
						if (isBiggest) {
							joinS(state, deleteThisRound);
							// System.out.println("koniec+ " + state.v);
						}
						// state.spans.clear();
					}
				} else {
					joinG(state, deleteThisRound);
					// System.out.println("koniec- " + state.v);
				}
			}
			unfinishedVertices.removeAll(deleteThisRound);
		}
		runTime = bean.getCurrentThreadCpuTime() - start;
		return S;
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
