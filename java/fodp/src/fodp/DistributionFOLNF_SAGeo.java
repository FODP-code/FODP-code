package fodp;

public class DistributionFOLNF_SAGeo extends DistributionFOLNF {

	public DistributionFOLNF_SAGeo(double epsilon_target, double delta_target) {

		double delta = delta_target / 2.0;
		beta = 1.0;
		ql = SageoUtil.getQl(epsilon_target, beta);
		qr = SageoUtil.getQr(epsilon_target, beta);
		nu = SageoUtil.getNu(delta, beta, ql, qr);
		eta = ql * (1 - Math.pow(ql, nu)) / (1 - ql) + 1.0 / (1 - qr);
		kappa = (int) Math.ceil((nu + Math.log((delta / 2.0) * eta * (1 - qr)) / Math.log(qr)));

		mu = calcMu();
	}

	public double calcMu() {
		double mu = 0;
		for (int k = 0; k <= nu - 1; k++) {
			mu += k * Math.pow(ql, nu - k);
		}
		mu += (qr + (1 - qr) * nu) / Math.pow(1 - qr, 2);
		mu /= eta;
		return mu;
	}

	public int dummyCountGeneration(double r) {

		double threshold = 1.0 - (qr / (eta * (1.0 - qr)));
		int B = (r >= threshold) ? 1 : 0;

		double insideL = eta * (1.0 - ql) * r + Math.pow(ql, nu + 1.0);
		double logBaseQl = Math.log(insideL) / Math.log(ql);
		int zL = nu - (int) Math.ceil(logBaseQl) + 1;

		double insideR = eta * (1.0 - qr) * (1.0 - r);
		double logBaseQr = Math.log(insideR) / Math.log(qr);
		int zR = (int) Math.floor(logBaseQr) + nu;

		int z = (1 - B) * zL + B * zR;

		return z;
	}

	@Override
	public int botCountGeneration(double r, int zi) {
		return kappa;
	}

}
