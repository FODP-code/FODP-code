package fodp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.TreeSet;

public class Util2 {

	public static double[] getOrgFrequency(int data[], int d) {
		double[] originalFrequency = new double[d];
		for (int v : data) {
			originalFrequency[v]++;
		}
		for (int i = 0; i < d; i++) {
			originalFrequency[i] /= data.length;
		}
		return originalFrequency;
	}

	public static double getMseTopK(double[] f1, double[] f2, double[] g, int K) {
		if (f1.length != f2.length || f1.length != g.length) {
			throw new IllegalArgumentException("Should be the same length.");
		}

		int d = g.length;
		K = Math.min(K, d);

		// min-heap: smallest g at the top
		PriorityQueue<Integer> pq = new PriorityQueue<>((i, j) -> Double.compare(g[i], g[j]));

		// keep only top-K indices by g
		for (int i = 0; i < d; i++) {
			if (pq.size() < K) {
				pq.offer(i);
			} else if (g[i] > g[pq.peek()]) {
				pq.poll();
				pq.offer(i);
			}
		}

		// compute MSE over top-K
		double sum = 0.0;
		while (!pq.isEmpty()) {
			int idx = pq.poll();
			if (Double.isNaN(f1[idx]) || Double.isNaN(f2[idx])) {
//				continue;
				System.err.println("isNan");
			}
			double diff = f1[idx] - f2[idx];
			sum += diff * diff;
		}

		return sum / K;
	}

	public static double getMse(double[] f1, double[] f2, double g[], int K) {
		if (f1.length != f2.length || f1.length != g.length) {
			throw new IllegalArgumentException("SHould be the same length.");
		}

		int d = g.length;
		Integer[] indices = new Integer[d];
		for (int i = 0; i < d; i++) {
			indices[i] = i;
		}

		Arrays.sort(indices, (i, j) -> Double.compare(g[j], g[i]));

		double t1[] = new double[K];
		double t2[] = new double[K];

		double sum = 0.0;
		for (int i = 0; i < K && i < d; i++) {
			int idx = indices[i];
			if (Double.isNaN(f1[idx])) {
				continue;
			}
			double diff = f1[idx] - f2[idx];
			sum += diff * diff;

			t1[i] = f1[idx];
			t2[i] = f2[idx];
		}

		return sum / K;
	}

	public static double getMSE(double originalFrequency[], double expectedFrequency[]) {
		int categoryNum = originalFrequency.length;
		double error = 0.0;
		for (int i = 0; i < categoryNum; i++) {
			if (Double.isNaN(originalFrequency[i])) {
				continue;
			}
			error += Math.pow(originalFrequency[i] - expectedFrequency[i], 2);
		}
		return error / categoryNum;
	}

	public static int[] getOrgVals(String dataName) {
		String fileName = "dataset/" + dataName + ".txt";
		int vals[] = null;
		try {
			TreeSet<String> set = new TreeSet<String>();
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			int count = 0;
			String line = "";
			while ((line = br.readLine()) != null) {
				count++;
				set.add(line);
			}
			br.close();

			int newId = 0;
			HashMap<String, Integer> map = new HashMap<String, Integer>();
			for (String val : set) {
				map.put(val, newId++);
			}

			vals = new int[count];
			br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			count = 0;
			while ((line = br.readLine()) != null) {
				vals[count++] = map.get(line);
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return vals;
	}
	
	public static int getN(String dataName) {
		int userNum = -1;
		if (dataName.equals("census")) {
			userNum = 602156;
		} else if (dataName.equals("localization")) {
			userNum = 164860;
		} else if (dataName.equals("foursquare")) {
			userNum = 18201;
		} else if (dataName.equals("aol")) {
			userNum = 10000;
		}
		return userNum;
	}

	public static int getD(String dataName) {
		int categoryNum = -1;
		if (dataName.equals("ipums")) {
			categoryNum = 915;
		} else if (dataName.equals("localization")) {
			categoryNum = 11;
		} else if (dataName.equals("foursquare")) {
			categoryNum = 1000000;
		} else if (dataName.equals("aol")) {
			categoryNum = 16777216;
		}
		return categoryNum;
	}
}
