package collusion;

/**
 * @author u_nyo
 *
 */
public class LocalEpsilonCalculator2 {

	private static boolean isUpperBounda = false;

	public static void main(String args[]) {

//		double epsilon_local = 4.44448;
		double delta = 1e-6;
		int n = 10000;
//		double e1 = getExistingServerEpsilon(epsilon_local, delta, n);
//		double e2 = getExistingNumericalServerEpsilon(epsilon_local, delta, n);
//		double e3 = getGrrServerEpsilon(epsilon_local, delta, 100, n);
//		System.out.println(e1 + ":" + e2 + ":" + e3);

		double epsilon_locals[] = { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
		for (double epsilon_local : epsilon_locals) {
			double e1 = getExistingNumericalServerEpsilon(epsilon_local, delta, n);
			System.out.println((Math.exp(epsilon_local) - 1) + "\t" + (Math.exp(e1) - 1));
		}
	}

	// public static double getLocalEpsilonOf_PEOS_SOLH(double epsilon_server,
	// double delta, int orgUserNum,
	// int fakeUserNum, int d) {
	// double c = epsilon_server;
	// double a = delta;
	// double n = orgUserNum;
	// double nr = fakeUserNum;
	//
	// double local_epsilon = Math.log(1 + d * (-1 + c * c * (n - 1) / (-c * c * nr
	// + 14 * d * Math.log(2 / a))));
	// return local_epsilon;
	// }

	private static double getBdExpectedError(int M, int categoryNum, int userNum) {
		double error = (double) M * categoryNum / (4.0 * userNum * userNum);
		return error;
	}

	public static double getConstraint(double delta, int userNum) {
		double constraint = Math.log(userNum / (16 * Math.log(2 / delta)));
//		double constraint = Math.log(userNum / (8 * Math.log(2 / delta)) - 1);
		return constraint;
	}

	public static double getUdsExpectedError(double samplingRate, int fakeNum, int categoryNum, int userNum) {
		double error = (1.0 - samplingRate) * (userNum + (double) fakeNum * categoryNum) / samplingRate / userNum
				/ userNum;
		return error;
	}

	// public static void getOptimalSamplingRateAndFakeNum(double epsilon_server,
	// double targetDelta, int userNum,
	// int categoryNum) {
	//
	// int checknum = 100;
	// // This samplingRate is maximum to realize \epsilon.
	// double maximumSamplingRate = getProposalSamplingRate(epsilon_server);
	// for (int i = 0; i < checknum; i++) {
	// double samplingRate = maximumSamplingRate - i * maximumSamplingRate /
	// checknum;
	// int fakeNum = getOptimalFakeNum(samplingRate, epsilon_server, userNum,
	// targetDelta);
	// double error = getUdsExpectedError(samplingRate, fakeNum, categoryNum,
	// userNum);
	// System.out.println(samplingRate + "\t" + fakeNum + "\t" + error);
	// }
	// }

	// public static int getOptimalT(int fakeNum, double beta, double epsilon) {
	// int optimalT = 0;
	//
	// double gamma = (Math.exp(epsilon / 2) - 1 + beta) / Math.exp(epsilon / 2);
	//
	// int base = (int) (gamma * fakeNum + 1);
	// for (int t = fakeNum; t < fakeNum + 1 / gamma + 1; t++) {
	// int val = (int) (gamma * t + 1);
	// if (val > base) {
	// optimalT = t - 1;
	// break;
	// }
	// }
	//
	// return optimalT;
	// }

	public static double getProposalEpsilon(double samplingRate) {
		double epsilon = -2 * Math.log(1 - samplingRate);
		return epsilon;
	}

	public static double getExistingServerEpsilon(double epsilon_local, double delta, int userNum) {

		if (epsilon_local > getConstraint(delta, userNum)) {
			return epsilon_local;
		}

		double upper = Math.log(1 + (Math.exp(epsilon_local) - 1) / (Math.exp(epsilon_local) + 1)
				* (8 * Math.sqrt(Math.exp(epsilon_local) * Math.log(4 / delta)) / Math.sqrt(userNum)
						+ 8 * Math.exp(epsilon_local) / userNum));

//		double term1 = 4 * Math.sqrt(2 * Math.log(4 / delta)) / Math.sqrt((Math.exp(epsilon_local) + 1) * userNum);
//		double term2 = 4.0 / userNum;
//		double combinedTerm = term1 + term2;
//
//		double upper = Math.log(1 + (Math.exp(epsilon_local) - 1) * combinedTerm);

		return upper;
	}

	public static double getPeosServerEpsilon(double epsilon_local, double delta, int bitLength, int n, int nr) {
		double epsilon_server = Math.sqrt(14.0 * Math.log(2.0 / delta)
				/ ((n - 1.0) / (Math.exp(epsilon_local) + bitLength - 1) + (double) nr / bitLength));

		epsilon_server = Math.min(epsilon_server, epsilon_local);

		return epsilon_server;
	}

	public static double getPeosColludingServerEpsilon(double epsilon_local, double delta, int bitLength, int nr) {
		double epsilon_colluding = Math.sqrt(14.0 * Math.log(2.0 / delta) * (double) bitLength / nr);

		epsilon_colluding = Math.min(epsilon_colluding, epsilon_local);

		return epsilon_colluding;
	}

	public static double getPeosGrrLocalEpsilon(double serverEpsilon, double delta, int bitLength, int userNum,
			int nr) {

		double localEpsilon = 0.0;

		double llim = 0;
		double rlim = 20;

		for (int t = 0; t < 20; t++) {
			localEpsilon = (rlim + llim) / 2;
			double tempServerEpsilon = getPeosServerEpsilon(localEpsilon, delta, bitLength, userNum, nr);

			if (tempServerEpsilon > serverEpsilon) {
				rlim = localEpsilon;
			} else {
				llim = localEpsilon;
			}
		}

		rlim -= 0.0000001;
		localEpsilon = Math.max(serverEpsilon, rlim);
		return localEpsilon;
	}

	public static int getPeosGrrColludingNr(double colludingServerEpsilon, double serverEpsilon, double delta,
			double d) {

		int nr = (int) (14 * d * Math.log(2 / delta));
		return nr;
	}

	public static int getPeosSOLHColludingNr(double colludingServerEpsilon, double serverEpsilon, double delta,
			int userNum) {

		int nr = (int) Math
				.ceil((-Math.exp(2 * serverEpsilon) + Math.exp(2 * serverEpsilon) * userNum + 28 * Math.log(2 / delta))
						/ (3 * colludingServerEpsilon * colludingServerEpsilon + Math.exp(2 * serverEpsilon)));

		return nr;
	}

	/**
	 * @param epsilon_local
	 * @param delta
	 * @param categoryNum
	 * @param userNum
	 * @return
	 */
	public static double getGrrServerEpsilon(double epsilon_local, double delta, int categoryNum, int userNum) {

		double constraint = getConstraint(delta, userNum);
		if (constraint < epsilon_local) {
			return epsilon_local;
		}
		double epsilon_server = Math.log(1.0
				+ (Math.exp(epsilon_local) - 1.0) * (4.0 * Math.sqrt(2.0 * (categoryNum + 1.0) * Math.log(4.0 / delta))
						/ Math.sqrt((Math.exp(epsilon_local) + categoryNum - 1.0) * (double) categoryNum * userNum)
						+ 4.0 * (categoryNum + 1.0) / ((double) categoryNum * userNum)));

//		double term1 = 4 * Math.sqrt(2 * Math.log(4 / delta))
//				/ Math.sqrt((Math.exp(epsilon_local) + categoryNum - 1) * userNum);
//		double term2 = 4.0 / userNum;
//		double combinedTerm = term1 + term2;
//		double epsilon_server = Math.log(1 + (Math.exp(epsilon_local) - 1) * combinedTerm);

		return epsilon_server;
	}

	public static double getGrrLocalEpsilon(double serverEpsilon, double delta, int categoryNum, int userNum) {

		double constraint = getConstraint(delta, userNum);
		if (constraint < serverEpsilon) {
			return serverEpsilon;
		}

		double localEpsilon = 0.0;

		double llim = 0;
		double rlim = constraint;

		for (int t = 0; t < 20; t++) {
			localEpsilon = (rlim + llim) / 2;
			double tempServerEpsilon = getGrrServerEpsilon(localEpsilon, delta, categoryNum, userNum);

			if (tempServerEpsilon > serverEpsilon) {
				rlim = localEpsilon;
			} else {
				llim = localEpsilon;
			}
		}

		localEpsilon = Math.max(serverEpsilon, rlim);
		return localEpsilon;

	}

	public static double getExistingLocalEpsilon(double serverEpsilon, double delta, int userNum) {

		// Note that local_epsilon should be less than a threshold. Because
		// local_epsilon should be larger than serverEpsilon when using shuffling, if
		// serverEpsilon is originally larger than the threshold, local_epsilon will be
		// also larger than the threshold.
		double constraint = getConstraint(delta, userNum);
		if (constraint < serverEpsilon) {
			return serverEpsilon;
		}

		double localEpsilon = 0.0;

		double llim = 0;
		double rlim = constraint;

		for (int t = 0; t < 20; t++) {
			localEpsilon = (rlim + llim) / 2;
			double tempServerEpsilon = getExistingServerEpsilon(localEpsilon, delta, userNum);
			if (tempServerEpsilon > serverEpsilon) {
				rlim = localEpsilon;
			} else {
				llim = localEpsilon;
			}
		}
		localEpsilon = Math.max(serverEpsilon, rlim);

		return localEpsilon;
	}

	public static double getExistingNumericalServerEpsilon(double localEpsilon, double delta, int userNum) {
		double constraint = getConstraint(delta, userNum);
		if (constraint < localEpsilon) {
			return localEpsilon;
		}

		double serverEpsilon = Amplificator.numericalAnalysis(userNum, localEpsilon, delta, 10, 10, isUpperBounda);
		return serverEpsilon;

	}

	public static double getExistingNumericalLocalEpsilon(double serverEpsilon, double delta, int userNum) {

		double constraint = getConstraint(delta, userNum);
		if (constraint < serverEpsilon) {
			return serverEpsilon;
		}

		double localEpsilon = 0.0;

		double llim = 0;
		double rlim = constraint;

		for (int t = 0; t < 20; t++) {
			localEpsilon = (rlim + llim) / 2;
			double tempServerEpsilon = Amplificator.numericalAnalysis(userNum, localEpsilon, delta, 10, 10,
					isUpperBounda);

			if (tempServerEpsilon > serverEpsilon) {
				rlim = localEpsilon;
			} else {
				llim = localEpsilon;
			}
		}

		localEpsilon = Math.max(serverEpsilon, rlim);
		return rlim;
	}
}
