package mindomset.main;

import java.util.Random;

public class MyRandom {
	static Random G = new Random(System.currentTimeMillis());

	public static boolean heads() {
		return G.nextInt(2) == 1;
	}

	public static boolean tails() {
		return G.nextInt(2) == 0;
	}

	public static int bit() {
		return G.nextInt(2);
	}

	public static int nextInt(int n) {
		return G.nextInt(n);
	}

	public static int nextInt(int min, int max) {
		return G.nextInt(max - min + 1) + min;
	}

	public static double nextDouble() {
		return G.nextDouble();
	}

	public static long nextLong() {
		return G.nextLong();
	}
}
