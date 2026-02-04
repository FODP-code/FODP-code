package composition;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OptimalComposition {

	static double log1pExp(double x) {
		if (x > 0)
			return x + Math.log1p(Math.exp(-x));
		return Math.log1p(Math.exp(x));
	}

	static double logAdd(double logA, double logB) {
		if (Double.isInfinite(logA))
			return logB;
		if (Double.isInfinite(logB))
			return logA;
		double m = Math.max(logA, logB);
		return m + Math.log1p(Math.exp(Math.min(logA, logB) - m));
	}

	static double logExpm1Pos(double x) {
		if (x <= 0)
			return Double.NEGATIVE_INFINITY;
		if (x > 20)
			return x;
		return Math.log(Math.expm1(x));
	}

	public static double[] computeDeltaI(double eps, int k) {
		int imax = k / 2;
		double[] deltas = new double[imax + 1];

		double logDen = k * log1pExp(eps);
		double logA = eps;

		for (int i = 0; i <= imax; i++) {
			if (i == 0) {
				deltas[i] = 0.0;
				continue;
			}

			double logC = 0.0;
			double logSum = Double.NEGATIVE_INFINITY;

			for (int ell = 0; ell <= i - 1; ell++) {

				int powBase = k - 2 * i + ell;
				int m = 2 * i - 2 * ell;
				double logTerm = logC + powBase * logA + logExpm1Pos(m * logA) - logDen;

				logSum = logAdd(logSum, logTerm);

				if (ell < i - 1) {
					logC += Math.log(k - ell) - Math.log(ell + 1);
				}
			}
			deltas[i] = (Double.isInfinite(logSum) ? 0.0 : Math.exp(logSum));
			// numerical safety clamp
			if (deltas[i] < 0)
				deltas[i] = 0.0;
			if (deltas[i] > 1)
				deltas[i] = 1.0;
		}
		return deltas;
	}

	// ---------- Candidate evaluation for a given i (denom>0) ----------
	static Result candidateForIndexMaxEpsMaxDelta(double epsTarget, double deltaTarget, int k, int i) {
		int denom = k - 2 * i;
		if (denom <= 0)
			return null;

		double eps = epsTarget / denom;
		if (!Double.isFinite(eps) || eps < 0)
			return null;

		double[] deltaI = computeDeltaI(eps, k);
		double di = deltaI[i];
		if (!(di >= 0.0 && di < 1.0))
			return null;

		double logRhs = Math.log1p(-deltaTarget) - Math.log1p(-di);
		if (logRhs > 1e-15)
			return null;
		double t = logRhs / k;
		double delta = -Math.expm1(t);
		if (delta < 0)
			delta = 0.0;
		if (delta > 1)
			delta = 1.0;

		// Compose back to verify
		double epsOut = denom * eps; // should equal epsTarget (up to rounding)

		double logOneMinusDeltaOut = k * Math.log1p(-delta) + Math.log1p(-di);
		double deltaOut = -Math.expm1(logOneMinusDeltaOut);

		if (epsOut > epsTarget + 1e-12)
			return null;
		if (deltaOut > deltaTarget + 1e-12)
			return null;

		return new Result(eps, delta, i, epsOut, deltaOut, di);
	}

	public static Result solveMaxEpsThenMaxDelta(double epsTarget, double deltaTarget, int k) {
		if (k <= 0)
			return null;
		int imax = (k - 1) / 2;

		Result best = null;
		for (int i = 0; i <= imax; i++) {
			Result cand = candidateForIndexMaxEpsMaxDelta(epsTarget, deltaTarget, k, i);
			if (cand == null)
				continue;

			if (best == null || cand.eps > best.eps + 1e-15
					|| (Math.abs(cand.eps - best.eps) <= 1e-15 && cand.delta > best.delta + 1e-15)) {
				best = cand;
			}
		}
		return best;
	}

	public static class Result {
		public final double eps; // per-step epsilon
		public final double delta; // per-step delta
		public final int i; // index i that defines the active constraint
		public final double epsOut; // composed epsilon
		public final double deltaOut; // composed delta
		public final double delta_i; // the δ_i(eps) that was used

		public Result(double eps, double delta, int i, double epsOut, double deltaOut, double delta_i) {
			this.eps = eps;
			this.delta = delta;
			this.i = i;
			this.epsOut = epsOut;
			this.deltaOut = deltaOut;
			this.delta_i = delta_i;
		}

		@Override
		public String toString() {
			return String.format(Locale.US,
					"per-step: eps=%.12f, delta=%.6e | via i=%d | composed: eps_out=%.12f, delta_out=%.6e | delta_i(epsilon)=%.6e",
					eps, delta, i, epsOut, deltaOut, delta_i);
		}
	}

	public static void main(String[] args) {
		double epsTarget = 1;
		double deltaTarget = 1e-4;
		int k = 50;

		if (args.length >= 1)
			epsTarget = Double.parseDouble(args[0]);
		if (args.length >= 2)
			deltaTarget = Double.parseDouble(args[1]);
		if (args.length >= 3)
			k = Integer.parseInt(args[2]);

		Result res = solveMaxEpsThenMaxDelta(epsTarget, deltaTarget, k);
		if (res == null) {
			System.out.println("No feasible per-step (eps, delta) found for given (epsTarget, deltaTarget, k).");
			return;
		}

		System.out.println("[Solution maximizing eps then delta]");
		System.out.println(res);

		List<double[]> pairs = computeEq5Pairs(res.eps, res.delta, k);
		System.out.println("\nEq.(5) pairs induced by designed per-step (eps, delta):");
		for (int idx = 0; idx < pairs.size(); idx++) {
			System.out.printf(Locale.US, "i=%d: eps_out=%.6f, delta_out=%.6e%n", idx, pairs.get(idx)[0],
					pairs.get(idx)[1]);
		}
	}

	public static List<double[]> computeEq5Pairs(double eps, double delta, int k) {
		double[] deltaI = computeDeltaI(eps, k);
		int imax = k / 2;
		double oneMinusDeltaPowK = Math.pow(1.0 - delta, k);
		List<double[]> pairs = new ArrayList<>(imax + 1);
		for (int i = 0; i <= imax; i++) {
			double epsOut = (k - 2.0 * i) * eps;
			double deltaOut = 1.0 - oneMinusDeltaPowK * (1.0 - deltaI[i]);
			pairs.add(new double[] { epsOut, deltaOut });
		}
		return pairs;
	}
}
