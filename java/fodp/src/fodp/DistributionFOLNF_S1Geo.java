package fodp;

public class DistributionFOLNF_S1Geo extends DistributionFOLNF {

	public DistributionFOLNF_S1Geo(double epsilon_target, double delta_target) {

		beta = 1 - Math.exp(-epsilon_target / 2.0);
		ql = 0;
		qr = SageoUtil.getQr(epsilon_target, beta);

		eta = ql * (1 - Math.pow(ql, nu)) / (1 - ql) + 1.0 / (1 - qr);
		kappa = (int) Math.ceil(Math.log(delta_target / 2.0) / Math.log(qr));
		mu = calcMu();

	}

	public double calcMu() {
		double mu = 0;
		mu = qr / (1 - qr);
		return mu;
	}

	public int dummyCountGeneration(double r) {
		int z = (int) (Math.floor(Math.log(1 - r) / Math.log(qr)));
		return z;
	}

	@Override
	public int botCountGeneration(double r, int zi) {
		return kappa;
	}

}
