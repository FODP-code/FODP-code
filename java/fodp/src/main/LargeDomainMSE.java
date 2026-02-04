package main;

import java.util.Random;

import composition.OptimalComposition;
import composition.OptimalComposition.Result;
import fodp.Distribution;
import fodp.DistributionFOLNF;
import fodp.FindLambda2;
import fodp.Method;
import fodp.RunningStats;
import fodp.TauGammaOptimizer;
import fodp.Util2;
import hash.HashFunction;

public class LargeDomainMSE {

	static int turnNum = 1;

	static double sConstant = 1;

	public static void main(String[] args) {

		System.out.println("Method" + "\t" + "Dataset" + "\t" + "epsilon_E" + "\t" + "tau "+ "\t" + "MSE");

		int seed = 12345;

		double[] epsilon_Es = { 0.1, 0.3, 0.5, 1, 3, 5, 10 };
		double[] delta_Es = { 1E-12 };

		String[] dataNames = { "foursquare", "aol" };
		Method methods[] = { Method.FOUD, Method.FOLNF_AGeo, Method.FOLNF_1Geo };

		if (args.length == 1) {
			String name = args[0].toLowerCase();
			if (name.equals("foursquare") || name.equals("aol")) {
				dataNames = new String[] { name };
			}
		}

		for (String dataName : dataNames) {

			int[] orgData = Util2.getOrgVals(dataName);
			int n = orgData.length;

			int d = Util2.getD(dataName);
			double[] originalFrequency = Util2.getOrgFrequency(orgData, d);

			int b = (int) (sConstant * n);

			for (double epsilon_E : epsilon_Es) {
				for (double delta_E : delta_Es) {

					for (Method method : methods) {
						int tau = TauGammaOptimizer.getOptimalTau(method, epsilon_E, delta_E, n, d, b);

						Result r = OptimalComposition.solveMaxEpsThenMaxDelta(epsilon_E, delta_E, tau);
						double epsilonEach = r.eps;
						double deltaEach = r.delta;

						long lambda = 0;// If FOUD is used, lambda value will be updated below.
						double beta = 1;// If S1Geo is used, beta value will be updated below.

						Distribution dist = Distribution.getDistribution(method, epsilonEach, deltaEach);
						double mu = dist.getMu();

						if (method == Method.FOUD) {
							lambda = FindLambda2.findMinLambda(b, epsilonEach, deltaEach);
						} else if (method == Method.FOLNF_1Geo || method == Method.FOLNF2_1Geo) {
							beta = ((DistributionFOLNF) dist).getBeta();
						}

						RunningStats topK1Stats = new RunningStats();

						for (int turn = 0; turn < turnNum; turn++) {

							Random random = new Random(seed + turn);

							HashFunction[] hashes = new HashFunction[tau];
							for (int t = 0; t < tau; t++) {
								hashes[t] = new HashFunction(d, b, random);
							}

							double[][] counts = new double[tau][b];

							for (int t = 0; t < tau; t++) {
								double[] fs = new double[b];

								for (int i = 0; i < n; i++) {
									if (random.nextDouble() < beta) {
										int bucket = hashes[t].calculateHash(orgData[i]);
										fs[bucket] += 1.0;
									}
								}

								for (long i = 0; i < lambda; i++) {
									int bucket = random.nextInt(b);
									fs[bucket] += 1.0;
								}

								for (int j = 0; j < b; j++) {
									int zj = dist.dummyCountGeneration(random.nextDouble());
									int kappaj = dist.botCountGeneration(random.nextDouble(), zj);
									fs[j] += Math.min(zj, kappaj);
								}

								counts[t] = fs;
							}

							double[] frequency = new double[d];

							for (int i = 0; i < d; i++) {
								double minCount = Double.POSITIVE_INFINITY;

								for (int t = 0; t < tau; t++) {
									int bucket = hashes[t].calculateHash(i);
									double c = counts[t][bucket];
									if (c < minCount)
										minCount = c;
								}

								frequency[i] = (1.0 / (n * beta)) * (minCount - ((double) lambda / b) - mu);
							}

							double topK1 = Util2.getMseTopK(originalFrequency, frequency, originalFrequency, 50);

							topK1Stats.push(topK1);

						}

						double meanTopK1 = topK1Stats.mean();

						System.out.println(getMethodName(method) + "\t" + dataName + "\t" + epsilon_E + "\t" + tau
								+ "\t" + meanTopK1);
					}
				}
			}
		}
	}

	private static String getBsize(double sConstant) {
		return "b = " + sConstant + "n";
	}

	private static String getMethodName(Method method) {
		switch (method) {
		case FOUD:
			return "FOUD-L";
		case FOLNF_AGeo:
			return "FOLNF-L (AGeo)";
		case FOLNF_1Geo:
			return "FOLNF-L (1Geo)";
		case FOLNF2_AGeo:
			return "FOLNF*-L (AGeo)";
		case FOLNF2_1Geo:
			return "FOLNF*-L (1Geo)";
		default:
			return null;
		}
	}

}
