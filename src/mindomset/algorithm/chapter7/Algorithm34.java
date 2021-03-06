package mindomset.algorithm.chapter7;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import mindomset.algorithm.AbstractMDSAlgorithm;
import mindomset.algorithm.AbstractMDSResult;
import mindomset.algorithm.MDSResultBackedByIntOpenHashSet;
import mindomset.datastructure.graph.Graph;

import com.carrotsearch.hppc.IntOpenHashSet;
import com.carrotsearch.hppc.cursors.IntCursor;

// TODO prerobit na HPPC

public class Algorithm34 implements AbstractMDSAlgorithm {
	private long prepTime = -1L;
	private long runTime = -1L;
	private LinkedHashMap<Integer, Algorithm34State> allVertices = new LinkedHashMap<>();
	private LinkedList<Algorithm34State> unfinishedVertices = new LinkedList<>();
	private LinkedHashSet<Integer> S = new LinkedHashSet<>();
	public final Object joinLock = new Object();
	// public final Object waitForStart = new Object();

	@Override
	public AbstractMDSResult mdsAlg(Graph g) {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		long start = bean.getCurrentThreadCpuTime();
		LinkedList<Long> times = new LinkedList<>();
		int bla = (int) Math.ceil(g.getNumberOfVertices() * 1.5);
		allVertices = new LinkedHashMap<>(bla);
		Integer nv = g.getNumberOfVertices();
		for (IntCursor v : g.getVertices()) {
			Algorithm34State state = new Algorithm34State(v.value, g);
			unfinishedVertices.add(state);
			allVertices.put(v.value, state);
		}
		times.addLast(bean.getCurrentThreadCpuTime() - start);
		// Integer nt = Math.min(nv, 1000);
		Long nt = Math
				.min(nv, Runtime.getRuntime().availableProcessors() * 256L);
		// Integer nt = nv;
		// System.out.println(nt);
		
		// variant 1
		ArrayList<Thread> pool = new ArrayList<>();
		for (int i = 0; i < nt; i++) {
			Thread t = new Thread(new Algorithm34Task(this));
			pool.add(t);
		}
		times.addLast(bean.getCurrentThreadCpuTime() - start);
		prepTime = bean.getCurrentThreadCpuTime() - start;
		for (Thread t : pool) {
			t.start();
		}
		/*
		 * times.addLast(bean.getCurrentThreadCpuTime() - start); try {
		 * Thread.sleep(50); } catch (InterruptedException e1) {
		 * e1.printStackTrace(); } synchronized (waitForStart) {
		 * waitForStart.notifyAll(); }
		 */
		times.addLast(bean.getCurrentThreadCpuTime() - start);
		for (Thread t : pool) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		times.addLast(bean.getCurrentThreadCpuTime() - start);

		/*
		 * // variant 2 Thread[] pool = new Thread[nv.intValue()]; for (int i =
		 * 0; i < nt; i++) { Thread t = new Thread(new Algorithm34Task(this));
		 * pool[i] = t; } times.addLast(bean.getCurrentThreadCpuTime() - start); for
		 * (int i = 0; i < nt; i++) { pool[i].start(); }
		 * times.addLast(bean.getCurrentThreadCpuTime() - start);
		 * 
		 * for (int i = 0; i < nt; i++) { try { pool[i].join(); } catch
		 * (InterruptedException e) { e.printStackTrace(); } }
		 * times.addLast(bean.getCurrentThreadCpuTime() - start);
		 */
		// System.out.println("Time elapsed: " + times);
		runTime = bean.getCurrentThreadCpuTime() - start;
		MDSResultBackedByIntOpenHashSet result = new MDSResultBackedByIntOpenHashSet();
		IntOpenHashSet resultData = new IntOpenHashSet(S.size());
		for (Integer i : S) {
			resultData.add(i);
		}
		result.setResult(resultData);
		return result;
	}

	public LinkedHashMap<Integer, Algorithm34State> getAllVertices() {
		return this.allVertices;
	}

	public Algorithm34State chooseNextVertex() {
		Algorithm34State state;
		synchronized (unfinishedVertices) {
			state = unfinishedVertices.pollFirst();
		}
		return state;
	}

	public void saveState(Algorithm34State state) {
		synchronized (unfinishedVertices) {
			unfinishedVertices.addLast(state);
		}
		return;
	}

	public void joinS(Integer v) {
		synchronized (S) {
			S.add(v);
		}
		return;
	}

	@Override
	public long getLastPrepTime() {
		return prepTime;
	}

	@Override
	public long getLastRunTime() {
		return runTime;
	}
	
	public Object getJoinLock() {
		return joinLock;
	}

}
