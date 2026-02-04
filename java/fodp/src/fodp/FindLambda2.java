package fodp;

import java.util.ArrayList;
import java.util.List;

public class FindLambda2 {

	static double epsilon(int d, double theta1, double theta2, long lam) {
		double numerator = d + (1.0 + theta1) * (double) lam;
		double denominator = (1.0 - theta2) * (double) lam;
		return Math.log(numerator / denominator);
	}

	static double delta(int d, double theta1, double theta2, long lam) {
		double term1 = Math.exp(-(theta1 * theta1 * (double) lam) / ((2.0 + theta1) * (double) d));
		double term2 = Math.exp(-(theta2 * theta2 * (double) lam) / (2.0 * (double) d));
		return term1 + term2;
	}

	static boolean satisfies(int d, double epsMax, double deltaMax, double theta1, double theta2, long lam) {
		return epsilon(d, theta1, theta2, lam) <= epsMax && delta(d, theta1, theta2, lam) <= deltaMax;
	}

	static List<Double> buildTheta2Values(double thetaStep) {
		List<Double> list = new ArrayList<>();

		for (double t2 = 0.0; t2 < 1.0 - 1e-12; t2 += thetaStep) {
			list.add(t2);
		}

		for (int k = 3; k <= 9; k++) {
			double special = 1.0 - Math.pow(10.0, -k);
			boolean exists = false;
			for (double v : list) {
				if (Math.abs(v - special) < 1e-15) {
					exists = true;
					break;
				}
			}
			if (!exists)
				list.add(special);
		}

		return list;
	}

	static class Result {
		final Long lambda;
		final Double bestTheta1;
		final Double bestTheta2;

		Result(Long bestLambda, Double bestTheta1, Double bestTheta2) {
			this.lambda = bestLambda;
			this.bestTheta1 = bestTheta1;
			this.bestTheta2 = bestTheta2;
		}
	}

	public static long findMinLambda(int d, double epsMax, double deltaMax) {
		return findMinLambda(d, epsMax, deltaMax, 2.0, 0.001, 1000000000000L).lambda;

	}

	static Result findMinLambda(int d, double epsMax, double deltaMax, double theta1Max, double thetaStep,
			long lambdaMax) {

		List<Double> theta2Values = buildTheta2Values(thetaStep);

		Long bestLambda = null;
		Double bestTheta1 = null;
		Double bestTheta2 = null;

		for (double theta1 = 0.0; theta1 <= theta1Max + 1e-12; theta1 += thetaStep) {
			for (double theta2 : theta2Values) {

				long lam = 1L;
				boolean ok = false;

				while (lam <= lambdaMax) {
					if (satisfies(d, epsMax, deltaMax, theta1, theta2, lam)) {
						ok = true;
						break;
					}
					lam = lam > (Long.MAX_VALUE / 2) ? Long.MAX_VALUE : lam * 2;
				}

				if (!ok)
					continue;

				long low = lam / 2;
				long high = lam;

				while (low + 1 < high) {
					long mid = (low + high) / 2;
					if (satisfies(d, epsMax, deltaMax, theta1, theta2, mid)) {
						high = mid;
					} else {
						low = mid;
					}
				}

				long candidate = high;
				if (bestLambda == null || candidate < bestLambda) {
					bestLambda = candidate;
					bestTheta1 = theta1;
					bestTheta2 = theta2;
				}
			}
		}

		return new Result(bestLambda, bestTheta1, bestTheta2);
	}

	public static void main(String[] args) {

		int d = 18201;

		double epsMax = 10;
		double deltaMax = 1E-13;

		double theta1Max = 2.0;
		if (args.length >= 4) {
			theta1Max = Double.parseDouble(args[3]);
		}

		double thetaStep = 0.001;
		long lambdaMax = 1000000000000L;

		Result r = findMinLambda(d, epsMax, deltaMax, theta1Max, thetaStep, lambdaMax);

		if (r.lambda == null) {
			System.out.println("No feasible lambda found up to lambda_max = " + lambdaMax);
		} else {
			System.out.println("lambda = " + r.lambda);
			System.out.println("best theta1 = " + r.bestTheta1);
			System.out.println("best theta2 = " + r.bestTheta2);
		}
	}
}
