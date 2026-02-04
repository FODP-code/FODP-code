package fodp;

import composition.OptimalComposition;

public class TauGammaOptimizer {

	static double D(double x, double y) {
		double d = x * Math.log(x / y) + (1.0 - x) * Math.log((1.0 - x) / (1.0 - y));
		return d;
	}

	static double d1FOUD(double gamma, int n, int b, double lambda) {

		double thresh = (2.0 * lambda * (b - 1.0)) / (n * (double) b);
		if (!(gamma < thresh)) {
			return 0.0;
		}

		double x = 1.0 / b + (n * gamma) / (2.0 * lambda);
		double y = 1.0 / b;

		double kl = D(x, y);
		return Math.exp(-lambda * kl);
	}

	static double d2FOUD(double gamma, int n, int b, double lambda) {

		double thresh = lambda / (n * (double) b);
		if (!(gamma < thresh)) {
			return 0.0;
		}

		double x = 1.0 / b - (n * gamma) / lambda;
		double y = 1.0 / b;

		double kl = D(x, y);
		return Math.exp(-lambda * kl);
	}

	static double lowerBoundFOUD(double gamma, int tau, int n, int b, double lambda) {

		double d1 = d1FOUD(gamma, n, b, lambda);
		double d2 = d2FOUD(gamma, n, b, lambda);

		double pow = Math.pow(2.0 / (b * gamma) + d1, tau);

		return 1.0 - pow - tau * d2;
	}

	static Double minGammaForTauFOUD(double P, int tau, int n, int b, double lambda) {
		double gLo = 1e-12;
		double gHi = 1.0;

		for (int it = 0; it < 100; it++) {
			double mid = (gLo + gHi) / 2.0;
			if (lowerBoundFOUD(mid, tau, n, b, lambda) >= P) {
				gHi = mid;
			} else {
				gLo = mid;
			}
		}
		return gHi;
	}

	static Result bestTauFOUD(double P, int n, int b, double epsilon, double delta, int tauMax) {
		Integer bestTau = null;
		Double bestGamma = null;

		for (int tau = 1; tau <= tauMax; tau++) {
			double lambda = FindLambda2.findMinLambda(b, epsilon / tau, delta / tau);
			Double g = minGammaForTauFOUD(P, tau, n, b, lambda);
			if (g == null)
				continue;
			if (bestGamma == null || g < bestGamma) {
				bestGamma = g;
				bestTau = tau;
			}
		}
		return (bestTau == null) ? null : new Result(bestTau, bestGamma);
	}

	static class Result {
		final int tau;
		final double gamma;

		Result(int tau, double gamma) {
			this.tau = tau;
			this.gamma = gamma;
		}

		@Override
		public String toString() {
			return "Result{tau=" + tau + ", gamma=" + gamma + "}";
		}
	}

	static double etaAGeo(int nu, double ql, double qr) {

		double left;
		if (ql == 0.0) {
			left = 0.0;
		} else {
			left = (ql * (1.0 - Math.pow(ql, nu))) / (1.0 - ql);
		}
		double right = 1.0 / (1.0 - qr);
		return left + right;
	}

	static double meanAGeo(int nu, double ql, double qr, double eta) {
		double s1 = 0.0;
		if (nu > 0 && ql != 0.0) {
			double sumR = ql * (1.0 - Math.pow(ql, nu)) / (1.0 - ql);
			double rPowNu = Math.pow(ql, nu);
			double sumJR = (ql * (1.0 - (nu + 1.0) * rPowNu + nu * rPowNu * ql)) / ((1.0 - ql) * (1.0 - ql));
			s1 = nu * sumR - sumJR;
		}

		double oneMinusQr = 1.0 - qr;
		double s2 = (nu / oneMinusQr) + (qr / (oneMinusQr * oneMinusQr));

		return (s1 + s2) / eta;
	}

	static double xi2(double gamma, int tau, int n, double mu, int nu, double ql, double eta) {
		if (ql == 0.0) {
			return 0.0;
		}

		if (gamma < mu / n) {
			long al = (long) Math.floor(mu - n * gamma);
			double d2 = 1.0 / (eta * (1 - ql)) * (1 - Math.pow(ql, al + 1)) * Math.pow(ql, nu - al);
			return tau * d2;
		}
		return 0.0;
	}

	static double lowerBoundEq14(double gamma, int tau, int n, int b, int nu, double ql, double qr, double eta,
			double mu) {

		long ar = (long) Math.ceil(mu + (n * gamma) / 2.0);
		double d3 = 1.0 / (eta * (1.0 - qr)) * Math.pow(qr, ar - nu);
		double pow = Math.pow(2.0 / (b * gamma) + d3, tau);
		return 1.0 - pow - xi2(gamma, tau, n, mu, nu, ql, eta);
	}

	static Double minGammaForTauEq14(double P, int tau, int n, int b, int nu, double ql, double qr) {
		double eta = etaAGeo(nu, ql, qr);
		double mu = meanAGeo(nu, ql, qr, eta);

		double gLo = 1e-12;
		double gHi = 1.0;

		for (int it = 0; it < 100; it++) {
			double mid = (gLo + gHi) / 2.0;
			if (lowerBoundEq14(mid, tau, n, b, nu, ql, qr, eta, mu) >= P)
				gHi = mid;
			else
				gLo = mid;
		}
		return gHi;
	}

	static Result bestTauFOLNF(double P, int n, int b, double epsilon, double delta, int tauMax, Method method) {
		Integer bestTau = null;
		Double bestGamma = null;

		for (int tau = 1; tau <= tauMax; tau++) {
			OptimalComposition.Result r = OptimalComposition.solveMaxEpsThenMaxDelta(epsilon, delta, tau);
			double epsilonEach = r.eps;
			double deltaEach = r.delta;

			Double g = null;
			if (method == Method.FOLNF_AGeo || method == Method.FOLNF_1Geo) {
				DistributionFOLNF_SAGeo dist = (DistributionFOLNF_SAGeo) Distribution.getDistribution(Method.FOLNF_AGeo,
						epsilonEach, deltaEach);
				g = minGammaForTauEq14(P, tau, n, b, dist.getNu(), dist.getQl(), dist.getQr());

			} else if (method == Method.FOLNF2_AGeo || method == Method.FOLNF2_1Geo) {
				DistributionFOLNF2_SAGeo dist = (DistributionFOLNF2_SAGeo) Distribution
						.getDistribution(Method.FOLNF2_AGeo, epsilonEach, deltaEach);
				g = minGammaForTauEq14(P, tau, n, b, dist.getNu(), dist.getQl(), dist.getQr());
			}

			if (g == null)
				continue;
			if (bestGamma == null || g < bestGamma) {
				bestGamma = g;
				bestTau = tau;
			}

		}
		return (bestTau == null) ? null : new Result(bestTau, bestGamma);

	}

	// !! tauMax should be adjusted appropriately
	public static int getOptimalTau(Method method, double epsilon, double delta, int n, int d, int b) {
		double P = 0.5;
		int tauMax = 3;
		Result r;
		if (method == Method.FOUD) {
			r = bestTauFOUD(P, n, b, epsilon, delta, tauMax);
		} else {
			tauMax = 15;
			r = bestTauFOLNF(P, n, b, epsilon, delta, tauMax, method);
		}
		return r.tau;
	}
}
