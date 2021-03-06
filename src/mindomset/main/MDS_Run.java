package mindomset.main;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import mindomset.algorithm.AbstractMDSAlgorithm;
import mindomset.algorithm.AbstractMDSResult;
import mindomset.datastructure.Dataset;
import mindomset.datastructure.graph.CompactUndirectedGraph;
import mindomset.datastructure.graph.Graph;

public class MDS_Run {
	public static final String MY_ARGS = "ba10k.txt greedy false";

	/**
	 * @param args
	 *            filename of processed graph
	 */
	public static void main(String[] args) {
		if (args.length == 0 && MY_ARGS != null && !MY_ARGS.equals("")) {
			args = MY_ARGS.split(" ");
		}
		for (String s : args) {
			System.out.print(s);
			System.out.print("\t");
		}
		System.out.println();
		Graph g;
		if (args.length < 1 || args[0].equals("")) {
			System.out
					.println("Please use the first argument as an input filename.");
			return;
		}
		long startReading = System.currentTimeMillis();
		String datasetName = args[0];
		String filename = Utils.getDatasetFilename(Utils.DATASET_DIRECTORY,
				datasetName);
		Dataset dataset = Utils.readEdgeListFromFile(filename);
		System.out.println("Read time: "
				+ (System.currentTimeMillis() - startReading) + "ms.");
		g = new CompactUndirectedGraph(dataset);
		Graph gg = new CompactUndirectedGraph(dataset);
		// g = new UndirectedGraph(new LinkedHashSet<Integer>(), edgeList);
		System.out.println("Graph loaded - vertices: "
				+ g.getNumberOfVertices() + ", edges: " + g.getEdges().size()
				+ ". Time: " + (System.currentTimeMillis() - startReading)
				+ "ms.");
		if ((g.getEdges().size() == 0) && (g.getNumberOfVertices() == 0)) {
			System.out
					.println("The graph has no vertices or does not load correctly.");
		}
		String algorithmName = "";
		if (args.length < 2) {
			System.out
					.println("You can use the second argument to choose an algorithm.");
		} else {
			algorithmName = args[1];
		}
		AbstractMDSResult mds;
		AbstractMDSAlgorithm alg;
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		long start2 = System.nanoTime();
		long start = bean.getCurrentThreadCpuTime();
		alg = Utils.getAlgorithm(algorithmName);
		assert alg != null : "Algorith is null!";
		mds = g.getMDS(alg);
		long elapsed = (bean.getCurrentThreadCpuTime() - start)
				/ Utils.NANOS_IN_MILI;
		long elapsed2 = (System.nanoTime() - start2) / Utils.NANOS_IN_MILI;
		System.out.println("Graph ...... - vertices: "
				+ g.getNumberOfVertices() + ", edges: " + g.getEdges().size()
				+ ".");
		System.out.println(mds.size() + " "
				+ mds);
		System.out.println("The set is " + (gg.isMDS(mds) ? "" : "not ")
				+ "a dominating set.");
		StringBuilder sb = new StringBuilder();
		sb.append("Time elapsed: ");
		sb.append(elapsed);
		sb.append("ms. \t(");
		sb.append(alg.getLastPrepTime() / Utils.NANOS_IN_MILI);
		sb.append("ms + ");
		sb.append((alg.getLastRunTime() - alg.getLastPrepTime())
				/ Utils.NANOS_IN_MILI);
		sb.append("ms)\t\t\t");
		sb.append("Wall time: ");
		sb.append(elapsed2);
		sb.append("ms.");
		System.out.println(sb.toString());

		boolean writeOutput = false;
		if (args.length >= 3) {
			writeOutput = Boolean.parseBoolean(args[2]);
		}
		if (writeOutput) {
			String outputFilename = Utils.getResultFilename(
					Utils.RESULTS_DIRECTORY, algorithmName, datasetName);
			Utils.exportResult(outputFilename, mds);
			System.out.println("MDS written to: " + outputFilename);
		}
		
		try
		{
		    String appendFile = "lastRuns.txt";
		    FileWriter fw = new FileWriter(appendFile, true); //the true will append the new data
		    StringBuilder sb2 = new StringBuilder();
		    for (String arg : args) {
		    	sb2.append(arg);
		    	sb2.append("\t");
		    }
		    sb2.append(mds.size());
		    sb2.append("\t");
		    sb2.append(elapsed2);
		    sb2.append("\n");
		    fw.write(sb2.toString()); //appends the string to the file
		    fw.close();
		}
		catch(IOException ioe)
		{
		    System.err.println("IOException: " + ioe.getMessage());
		}		
	}
}
