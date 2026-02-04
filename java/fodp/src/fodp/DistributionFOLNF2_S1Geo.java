package fodp;

import java.util.Random;

public class DistributionFOLNF2_S1Geo extends DistributionFOLNF {

	private Random random = new Random();

	public double getQl() {
		return 0;
	}

	public double getQr() {
		return qr;
	}

	public int getNu() {
		return 0;
	}

	public DistributionFOLNF2_S1Geo(double epsilon_target, double delta_target) {

		beta = 1 - Math.exp(-epsilon_target / 2);
		ql = 0;
		qr = SageoUtil.getQr(epsilon_target, beta);
		eta = ql * (1 - Math.pow(ql, nu)) / (1 - ql) + 1.0 / (1 - qr);

		int c = 10;

		ql_prime = SageoUtil.getQlPrime(c * epsilon_target, beta, qr);
		qr_prime = SageoUtil.getQrPrime(epsilon_target, c * epsilon_target, beta, ql);
		nu_prime = SageoUtil.getNu(delta_target, beta, ql_prime, qr_prime);
		eta_prime = ql_prime * (1 - Math.pow(ql_prime, nu_prime)) / (1 - ql_prime) + 1.0 / (1 - qr_prime);

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

	private static int sampleAGeo(double r, int nuX, double qlX, double qrX, double etaX) {

		if (qlX == 0.0) {
			double z = Math.log1p(-r) / Math.log(qrX); // >=0
			return Math.max(0, (int) Math.floor(z));
		}

		if (qrX == 0.0) {
			double insideL = etaX * (1.0 - qlX) * r + Math.pow(qlX, nuX + 1.0);
			double logBaseQl = Math.log(insideL) / Math.log(qlX);
			int zL = nuX - (int) Math.ceil(logBaseQl) + 1;
			return Math.max(0, zL);
		}

		// --- General AGeo case (ql, qr in (0,1)) ---
		double threshold = 1.0 - (qrX / (etaX * (1.0 - qrX)));
		int B = (r >= threshold) ? 1 : 0;

		if (B == 0) {
			double insideL = etaX * (1.0 - qlX) * r + Math.pow(qlX, nuX + 1.0);
			double logBaseQl = Math.log(insideL) / Math.log(qlX);
			int zL = nuX - (int) Math.ceil(logBaseQl) + 1;
			return Math.max(0, zL);
		} else {
			double insideR = etaX * (1.0 - qrX) * (1.0 - r);
			double logBaseQr = Math.log(insideR) / Math.log(qrX);
			int zR = (int) Math.floor(logBaseQr) + nuX;
			return Math.max(0, zR);
		}
	}

	@Override
	public int dummyCountGeneration(double r) {
		// z_i ~ D

		int sample = sampleAGeo(r, nu, ql, qr, eta);
		return sample;
	}

	@Override
	public int botCountGeneration(double r, int zi) {
		// omega_i ~ D'
		double r2 = random.nextDouble();
		int omega = sampleAGeo(r2, nu_prime, ql_prime, qr_prime, eta_prime);
		return zi + omega;
	}

}
