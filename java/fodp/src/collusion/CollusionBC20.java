package collusion;

public class CollusionBC20 {

	public static double collusionBC20(double collusion_ratio, double target_epsilon, double target_delta,
			int orgUserNum, double dummyRatio) {
		int dummyNum = (int) (orgUserNum * dummyRatio);
		int allUserNum = orgUserNum + dummyNum;
		double epsilon2 = target_epsilon / 2.0;
		double delta2 = target_delta / 2.0;

		double constraint = (100.0 / Math.pow(epsilon2, 2)) * Math.log(2.0 / (delta2));
		if (!(allUserNum >= constraint)) {
			return Double.NaN;
		}
		if (epsilon2 > 1.0) {
			return Double.NaN;
		}
		double p = 1.0 - 50.0 / epsilon2 / epsilon2 / allUserNum * Math.log(2.0 / delta2);

		if (collusion_ratio != 1) {
			int actual_user = (int) (orgUserNum * (1 - collusion_ratio)) + dummyNum;
			double actual_epsilon = 10 * Math.sqrt(2)
					* Math.sqrt(Math.log(4 / delta2) / (actual_user - actual_user * p));

			if (actual_epsilon / 2 > 1) {
				actual_epsilon = Double.NaN;
			}
			return actual_epsilon;
		} else {
			int actual_user = 1 + dummyNum;
			double actual_epsilon = 10 * Math.sqrt(2)
					* Math.sqrt(Math.log(4 / delta2) / (actual_user - actual_user * p));

			if (actual_epsilon / 2 > 1) {
				actual_epsilon = Double.NaN;
			}

			return actual_epsilon;
		}

	}

}
