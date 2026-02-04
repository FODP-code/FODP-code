package collusion;

public class CollusionCM22 {

	public static double getRapporEpsilon(double q) {
		// 2 Log[(1 - q)/q]
		double epsilon = 2 * Math.log((1 - q) / q);
		return epsilon;
	}

	public static double getRapporShufflerEpsilon(double q, double target_delta, int userNum) {
		double epsilon_local = getRapporEpsilon(q);
		double epsilon_server = LocalEpsilonCalculator2.getExistingNumericalServerEpsilon(epsilon_local, target_delta,
				userNum);

		return epsilon_server;
		// return epsilon_local;
	}

	public static int requireK(double epsilon, double delta, int n) {
		double threshold = 132.0 * Math.pow(1 + Math.exp(epsilon), 2) * Math.log(4.0 / delta);
		threshold /= 5.0 * Math.pow(-1 + Math.exp(epsilon), 2) * n;
		int th = (int) Math.ceil(threshold);
		return th;
	}

	public static double collustionCM22(double collusion_ratio, double target_epsilon, double target_delta, int userNum,
			double dummyRatio) {

		int totalDummyUsers = (int) (userNum * dummyRatio);

		int k = requireK(target_epsilon, target_delta, userNum);
		k = Math.max(10, k);

		double q = getQ(target_epsilon, target_delta, userNum + totalDummyUsers, k);

		if (collusion_ratio != 1) {
			int n = (userNum + totalDummyUsers) - (int) (userNum * collusion_ratio);
			double actualEpsilon = getMinEpsilon(target_delta, n, k, q);
			if (Double.isNaN(actualEpsilon)) {
				// continue;
				actualEpsilon = getRapporEpsilon(q);
			}

			return actualEpsilon;
		} else {
			double minEpsilon1 = getMinimumEpsilonFromK(target_delta, (totalDummyUsers + 1), k);
			double minEpsilon2 = getMinEpsilon(target_delta, (totalDummyUsers + 1), k, q);

			double actualEpsilon = Math.max(minEpsilon1, minEpsilon2);
			if (Double.isNaN(actualEpsilon)) {
				actualEpsilon = getRapporShufflerEpsilon(q, target_delta, (totalDummyUsers + 1));
			} else {
				actualEpsilon = Math.min(actualEpsilon,
						getRapporShufflerEpsilon(q, target_delta, (totalDummyUsers + 1)));
			}
			return actualEpsilon;

		}

	}

	private static double getMinimumEpsilonFromK(double delta, int n, int k) {
		double minEpsilon = Math.log(-1
				+ 1.0 / (0.5 - Math.sqrt(33.0 / 5) * Math.sqrt(Math.log(4.0 / delta)) / Math.sqrt(k) / Math.sqrt(n)));
		return minEpsilon;
	}

	private static double getMinEpsilon(double delta, int n, int k, double q) {
		double minEpsilon = 5 * k * n * (-1 + q) * q - 33 * Math.log(4 / delta)
				- 2 * Math.sqrt(165) * Math.sqrt(-k * n * (-1 + q) * q * Math.log(4 / delta));
		minEpsilon /= 5 * k * n * (-1 + q) * q + 33 * Math.log(4 / delta);
		minEpsilon = Math.log(minEpsilon);
		return minEpsilon;
	}

	public static double getQ(double epsilon, double delta, int n, int k) {

		double q = 0.1 * (5 - Math.sqrt(
				25 - (660 * Math.pow(1.0 / Math.tanh(epsilon / 2.0), 2) * Math.log(4.0 / delta)) / ((double) k * n)));
		return q;
	}

}
