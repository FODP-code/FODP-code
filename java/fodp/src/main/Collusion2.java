package main;

import collusion.CollusionBC20;
import collusion.CollusionCM22;
import collusion.CollusionLWY22;
import collusion.LocalEpsilonCalculator2;

public class Collusion2 {

	static double collusion_ratios[] = { 0.01 };
	static double epsilon_Es[] = { 0.1, 0.3, 0.5, 1, 3, 5, 10 };

	public static void main(String args[]) {
		double delta_E = 1E-12;
		int n = 1000000;
		int d = 1000;

		System.out.println("Method\tepsilon_E before collusion\tcollusion_ratio\tepsilon_E after colusion");

		for (double collusion_ratio : collusion_ratios) {
			for (double epsilon_E : epsilon_Es) {
				double fodp = collusionFODP(collusion_ratio, epsilon_E, delta_E, n);
				double grr = collusionGRR(collusion_ratio, epsilon_E, delta_E, n, 0, d);
				double oueolh = collusionOUEOLH(collusion_ratio, epsilon_E, delta_E, n, 0, true);
				double cm22 = CollusionCM22.collustionCM22(collusion_ratio, epsilon_E, delta_E, n, 0);
				double bc20 = CollusionBC20.collusionBC20(collusion_ratio, epsilon_E, delta_E, n, 0);
				double lwy22 = CollusionLWY22.collusionLWY22(collusion_ratio, epsilon_E, delta_E, n, d, 0);

				System.out.println("FOUD/FOLNF/FOLNF*" + "\t" + epsilon_E + "\t" + collusion_ratio + "\t" + fodp);
				System.out.println("GRR" + "\t" + epsilon_E + "\t" + collusion_ratio + "\t" + grr);
				System.out.println("OUE/OLH" + "\t" + epsilon_E + "\t" + collusion_ratio + "\t" + oueolh);
				if (!Double.isNaN(bc20)) {
					System.out.println("BC20" + "\t" + epsilon_E + "\t" + collusion_ratio + "\t" + bc20);
				}
				System.out.println("CM22" + "\t" + epsilon_E + "\t" + collusion_ratio + "\t" + cm22);
				if (!Double.isNaN(lwy22)) {
					System.out.println("LWY22" + "\t" + epsilon_E + "\t" + collusion_ratio + "\t" + lwy22);
				}
			}
		}
	}

	public static double collusionFODP(double collusion_ratio, double epsilon_E, double delta_E, int n) {
		return epsilon_E;
	}

	public static double collusionOUEOLH(double collusion_ratio, double target_epsilon, double target_delta,
			int userNum, double dummyRatio, boolean isNumerical) {
		double epsilon_local = 0.0;
		int totalDummyUsers = (int) (userNum * dummyRatio);

		if (isNumerical) {
			epsilon_local = LocalEpsilonCalculator2.getExistingNumericalLocalEpsilon(target_epsilon, target_delta,
					userNum + totalDummyUsers);
		} else {
			epsilon_local = LocalEpsilonCalculator2.getExistingLocalEpsilon(target_epsilon, target_delta,
					userNum + totalDummyUsers);
		}

		if (collusion_ratio != 1) {
			return getServerEpsilon(epsilon_local, target_delta, userNum + totalDummyUsers,
					(int) (userNum * collusion_ratio), isNumerical);
		} else {
			return getServerEpsilon(epsilon_local, target_delta, userNum + totalDummyUsers, userNum - 1, isNumerical);
		}

	}

	public static double collusionGRR(double collusion_ratio, double target_epsilon, double target_delta, int userNum,
			double dummyRatio, int categoryNum) {
		int totalDummyUsers = (int) (userNum * dummyRatio);

		double epsilon_local = LocalEpsilonCalculator2.getGrrLocalEpsilon(target_epsilon, target_delta, categoryNum,
				userNum + totalDummyUsers);

		if (collusion_ratio != 1) {
			return getServerEpsilonGrr(epsilon_local, target_delta, userNum + totalDummyUsers, categoryNum,
					(int) (userNum * collusion_ratio));
		} else {
			return getServerEpsilonGrr(epsilon_local, target_delta, userNum + totalDummyUsers, categoryNum,
					userNum - 1);
		}
	}

	public static double getServerEpsilon(double epsilon_local, double delta, int n, int collusionNum,
			boolean isNumerical) {

		double epsilon_server = 0.0;
		if (isNumerical) {
			epsilon_server = LocalEpsilonCalculator2.getExistingNumericalServerEpsilon(epsilon_local, delta,
					n - collusionNum);
		} else {
			epsilon_server = LocalEpsilonCalculator2.getExistingServerEpsilon(epsilon_local, delta, n - collusionNum);
		}

		epsilon_server = Math.min(epsilon_local, epsilon_server);
		return epsilon_server;
	}

	public static double getServerEpsilonGrr(double epsilon_local, double delta, int n, int categoryNum,
			int collusionNum) {
		double epsilon_server = LocalEpsilonCalculator2.getGrrServerEpsilon(epsilon_local, delta, categoryNum,
				n - collusionNum);

		epsilon_server = Math.min(epsilon_local, epsilon_server);
		return epsilon_server;
	}
}
