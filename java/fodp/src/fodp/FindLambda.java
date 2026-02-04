package fodp;

public class FindLambda {

	public static double epsilon(int d, double xi1, double xi2, long lam) {
		return Math.log((d + (1 + xi1) * lam) / ((1 - xi2) * lam));
	}

	public static double delta(int d, double xi1, double xi2, long lam) {
		double term1 = Math.exp(-(xi1 * xi1 * lam) / ((2 + xi1) * d));
		double term2 = Math.exp(-(xi2 * xi2 * lam) / (2 * d));
		return term1 + term2;
	}

	public static class Result {
		long lambda;
		double xi1;
		double xi2;

		public Result(long lambda, double xi1, double xi2) {
			this.lambda = lambda;
			this.xi1 = xi1;
			this.xi2 = xi2;
		}
	}

	public static long findMinLambda(int d, double epsMax, double deltaMax) {
		return findMinLambda(d, epsMax, deltaMax, 0.001, 1000000000000L).lambda;

	}

	public static Result findMinLambda(int d, double epsMax, double deltaMax, double xiStep, long lambdaMax) {
		Long bestLambda = null;
		Double bestXi1 = null;
		Double bestXi2 = null;

		// Generate xi values
		for (double xi1 = xiStep; xi1 <= 1.0 + 1e-12; xi1 += xiStep) {
			for (double xi2 = xiStep; xi2 <= 1.0 - 1e-12; xi2 += xiStep) {
				// Exponential search for λ
				long lam = 1;
				boolean ok = false;

				while (lam <= lambdaMax) {
					double eps = epsilon(d, xi1, xi2, lam);
					double delt = delta(d, xi1, xi2, lam);

					if (eps <= epsMax && delt <= deltaMax) {
						ok = true;
						break;
					}
					lam *= 2; // accelerate search

					// Prevent overflow
					if (lam < 0) {
						lam = lambdaMax;
						break;
					}
				}

				if (!ok) {
					continue;
				}

				// Binary search between lam/2 and lam
				long low = lam / 2;
				long high = lam;

				while (low + 1 < high) {
					long mid = (low + high) / 2;
					if (epsilon(d, xi1, xi2, mid) <= epsMax && delta(d, xi1, xi2, mid) <= deltaMax) {
						high = mid;
					} else {
						low = mid;
					}
				}

				long lamCandidate = high;

				if (bestLambda == null || lamCandidate < bestLambda) {
					bestLambda = lamCandidate;
					bestXi1 = xi1;
					bestXi2 = xi2;
				}
			}
		}

		return new Result(bestLambda, bestXi1, bestXi2);
	}

	public static void main(String[] args) {

		int d = 18201;
		double epsMax = 10;
		double deltaMax = 1E-13;

		// Compute
		Result result = findMinLambda(d, epsMax, deltaMax, 0.001, 1000000000000L);

		// Output
		System.out.println("lambda = " + result.lambda);
		System.out.println("best xi1 = " + result.xi1);
		System.out.println("best xi2 = " + result.xi2);

		double epsilon = Math.log((d + (1 + result.xi1) * result.lambda) / ((1 - result.xi2) * result.lambda));
		System.out.println(epsilon);
	}
}