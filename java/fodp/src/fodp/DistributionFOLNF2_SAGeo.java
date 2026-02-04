package fodp;

import java.util.Random;

public class DistributionFOLNF2_SAGeo extends DistributionFOLNF {

	private Random random = new Random();

	public static void main(String args[]) {
		int a = sampleAGeo(0.2, 44, 0.580771048, 0, 2.385331443);
		System.out.println(a);

	}

	public DistributionFOLNF2_SAGeo(double epsilon_target, double delta_target) {

		double epsilon = epsilon_target;
		double delta = delta_target / 2;
		beta = 1.0;
		ql = SageoUtil.getQl(epsilon, beta);
		qr = SageoUtil.getQr(epsilon, beta);

		nu = SageoUtil.getNu(delta, beta, ql, qr);
		eta = ql * (1 - Math.pow(ql, nu)) / (1 - ql) + 1.0 / (1 - qr);

		int c = 10;

		ql_prime = SageoUtil.getQlPrime(c * epsilon, beta, qr);
		qr_prime = SageoUtil.getQrPrime(epsilon, c * epsilon, beta, ql);

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
		double threshold = 1.0 - (qrX / (etaX * (1.0 - qrX)));
		int B = (r >= threshold) ? 1 : 0;

		double insideL = etaX * (1.0 - qlX) * r + Math.pow(qlX, nuX + 1.0);
		double logBaseQl = Math.log(insideL) / Math.log(qlX);
		int zL = nuX - (int) Math.ceil(logBaseQl) + 1;

		double insideR = etaX * (1.0 - qrX) * (1.0 - r);
		double logBaseQr = Math.log(insideR) / Math.log(qrX);
		int zR = (int) Math.floor(logBaseQr) + nuX;

		int z = (1 - B) * zL + B * zR;
		return Math.max(0, z);
	}

	@Override
	public int dummyCountGeneration(double r) {
		// z_i ~ D
		return sampleAGeo(r, nu, ql, qr, eta);
	}

	@Override
	public int botCountGeneration(double r, int zi) {
		// omega_i ~ D'
		double r2 = random.nextDouble();
		int omega = sampleAGeo(r2, nu_prime, ql_prime, qr_prime, eta_prime);
		return zi + omega;
	}

}
