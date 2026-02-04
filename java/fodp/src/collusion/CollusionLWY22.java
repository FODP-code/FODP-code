package collusion;

public class CollusionLWY22 {

	public static double collusionLWY22(double collusion_ratio, double target_epsilon, double target_delta,
			int orgUserNum, int categoryNum, double dummyRatio) {
		int dummyNum = (int) (orgUserNum * dummyRatio);
		int allUserNum = orgUserNum + dummyNum;

		if (target_epsilon > 3.0) {
			return Double.NaN;
		}
		double rho = 32.0 * Math.log(2.0 / target_delta) / target_epsilon / target_epsilon * categoryNum / allUserNum;

		if (rho >= 1) {
			return Double.NaN;
		}

		if (collusion_ratio != 1) {
			int actual_user = (int) (orgUserNum * (1 - collusion_ratio)) + dummyNum;
			double actual_epsilon = 4.0 * Math.sqrt(2) * Math.sqrt(categoryNum * Math.log(2 / target_delta))
					/ Math.sqrt(actual_user * rho);
			if (actual_epsilon > 3.0) {
				return Double.NaN;
			}
			return actual_epsilon;
		} else {
			int actual_user = 1 + dummyNum;
			double actual_epsilon = 4.0 * Math.sqrt(2) * Math.sqrt(categoryNum * Math.log(2 / target_delta))
					/ Math.sqrt(actual_user * rho);
			if (actual_epsilon > 3.0) {
				return Double.NaN;
			}
			return actual_epsilon;
		}
	}

}
