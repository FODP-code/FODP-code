package main;

import java.util.Random;

import fodp.Distribution;
import fodp.DistributionFOLNF;
import fodp.FindLambda2;
import fodp.Method;
import fodp.RunningStats;
import fodp.Util2;

public class SmallDomainMSE {
	final static int bot = -1;
	static int turnNum = 1;

	public static void main(String args[]) {

		System.out.println("Method" + "\t" + "Dataset" + "\t" + "epsilon_E" + "\t" + "MSE");
		int seed = 12345;

		double epsilons[] = { 0.1, 0.3, 0.5, 1, 3, 5, 10 };
		double deltas[] = { 1E-12 };
		String dataNames[] = { "ipums", "localization" };
		Method methods[] = { Method.FOUD, Method.FOLNF_AGeo, Method.FOLNF_1Geo };

		if (args.length == 1) {
			String name = args[0].toLowerCase();
			if (name.equals("ipums") || name.equals("localization")) {
				dataNames = new String[] { name };
			}
		}

		int d = -1;
		int n = -1;

		for (String dataName : dataNames) {
			int orgData[] = Util2.getOrgVals(dataName);
			n = orgData.length;
			d = Util2.getD(dataName);
			double[] originalFrequency = Util2.getOrgFrequency(orgData, d);

			for (double epsilon : epsilons) {
				for (double delta : deltas) {
					for (Method method : methods) {
						long lambda = 0;// If FOUD is used, lambda value will be updated below.
						double beta = 1;// If S1Geo is used, beta value will be updated below.
						Distribution dist = Distribution.getDistribution(method, epsilon, delta);
						if (method == Method.FOUD) {
							lambda = FindLambda2.findMinLambda(d, epsilon, delta);
						} else if (method == Method.FOLNF_1Geo || method == Method.FOLNF2_1Geo) {
							beta = ((DistributionFOLNF) dist).getBeta();
						}

						RunningStats mseStats = new RunningStats();
						for (int turn = 0; turn < turnNum; turn++) {

							Random random = new Random(seed + turn);

							double fs[] = new double[d];

							for (int i = 0; i < n; i++) {
								double r = random.nextDouble();
								if (r < beta) {
									fs[orgData[i]]++;
								}
							}

							for (long i = 0; i < lambda; i++) {
								int r = random.nextInt(d);
								fs[r]++;

							}

							for (int i = 0; i < d; i++) {
								double r = random.nextDouble();
								int zi = dist.dummyCountGeneration(r);
								r = random.nextDouble();
								int kappai = dist.botCountGeneration(r, zi);
								fs[i] += Math.min(zi, kappai);
							}

							double fHat[] = new double[d];
							double mu = dist.getMu();
							for (int i = 0; i < d; i++) {
								double val = 1.0 / n / beta * (fs[i] - (double) lambda / d - mu);
								fHat[i] = val;
							}

							double mse = Util2.getMSE(originalFrequency, fHat);
							mseStats.push(mse);

						}

						double mean = mseStats.mean();
						System.out.println(getMethodName(method) + "\t" + dataName + "\t" + epsilon + "\t" + mean);

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
			return "FOUD";
		case FOLNF_AGeo:
			return "FOLNF (AGeo)";
		case FOLNF_1Geo:
			return "FOLNF (1Geo)";
		case FOLNF2_AGeo:
			return "FOLNF* (AGeo)";
		case FOLNF2_1Geo:
			return "FOLNF* (1Geo)";
		default:
			return null;
		}
	}

}
