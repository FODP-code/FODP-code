package collusion;

import org.apache.commons.math3.distribution.BinomialDistribution;

public class Amplificator {

	public static double binarySearch(double delta, int num_iterations, double epsupper, int n, double epsOrig,
			int step, boolean upperBound) {
		double llim = 0;
		double rlim = epsupper;

		for (int t = 0; t < num_iterations; t++) {
			double mideps = (rlim + llim) / 2;
			double delta_for_mideps = deltacomp(n, epsOrig, mideps, delta, step, upperBound);

			if (delta_for_mideps < delta) {
				rlim = mideps;
			} else {
				llim = mideps;
			}
		}

		return rlim;
	}

	public static double deltacomp(int n, double eps0, double eps, double deltaupper, int step, boolean upperbound) {
		double deltap = 0;
		double deltaq = 0;
		double probused = 0;

		double p = Math.exp(-eps0);
		double expectation = (n - 1) * p;

		int stepLimit = (int) Math.ceil((double) n / step);

		for (int B = 1; B < stepLimit; B++) {
			for (int s = 0; s < 2; s++) {
				int lowerc;
				int upperc;
				boolean inscope;

				if (s == 0) {
					upperc = (int) Math.ceil(expectation + B * step);
					if (B == 1) {
						lowerc = upperc - step;
					} else {
						lowerc = upperc - step + 1;
					}

					inscope = lowerc <= (n - 1);
					if (inscope) {
						upperc = Math.min(upperc, n - 1);
					}
				} else {
					lowerc = (int) Math.ceil(expectation - B * step);
					upperc = lowerc + step - 1;

					inscope = upperc >= 0;
					if (inscope) {
						lowerc = Math.max(0, lowerc);
					}
				}

				if (inscope) {
					BinomialDistribution binomial = new BinomialDistribution(n - 1, p);
					double cdfinterval = binomial.cumulativeProbability(upperc) - binomial.cumulativeProbability(lowerc)
							+ binomial.probability(lowerc);

					if (Math.max(deltap, deltaq) > deltaupper) {
						return deltaupper;
					}

					if (1 - probused < deltap && 1 - probused < deltaq) {
						if (upperbound) {
							return Math.max(deltap + 1 - probused, deltaq + 1 - probused);
						} else {
							return Math.max(deltap, deltaq);
						}
					} else {
						double deltap_upperc = onestep(upperc, eps, eps0, true);
						double deltap_lowerc = onestep(lowerc, eps, eps0, true);
						double deltaq_upperc = onestep(upperc, eps, eps0, false);
						double deltaq_lowerc = onestep(lowerc, eps, eps0, false);

						double deltapadd;
						double deltaqadd;

						if (upperbound) {
							deltapadd = Math.max(deltap_upperc, deltap_lowerc);
							deltaqadd = Math.max(deltaq_upperc, deltaq_upperc);
						} else {
							deltapadd = Math.min(deltap_upperc, deltap_lowerc);
							deltaqadd = Math.min(deltaq_upperc, deltaq_lowerc);
						}

						deltap += cdfinterval * deltapadd;
						deltaq += cdfinterval * deltaqadd;
					}

					probused += cdfinterval;
				}
			}
		}

		return Math.max(deltap, deltaq);
	}

	public static double onestep(int c, double eps, double eps0, boolean pMinusQ) {
		double alpha = Math.exp(eps0) / (Math.exp(eps0) + 1);
		double effEps = Math.log(((Math.exp(eps) + 1) * alpha - 1) / ((1 + Math.exp(eps)) * alpha - Math.exp(eps)));

		double beta;
		if (pMinusQ) {
			beta = 1 / (Math.exp(effEps) + 1);
		} else {
			beta = 1 / (Math.exp(-effEps) + 1);
		}

		int cutoff = (int) (beta * (c + 1));

		BinomialDistribution binomial = new BinomialDistribution(c, 0.5);
		double pConditionedOnC = (alpha * binomial.cumulativeProbability(cutoff))
				+ (1 - alpha) * binomial.cumulativeProbability(cutoff - 1);
		double qConditionedOnC = ((1 - alpha) * binomial.cumulativeProbability(cutoff))
				+ alpha * binomial.cumulativeProbability(cutoff - 1);

		if (pMinusQ) {
			return pConditionedOnC - Math.exp(eps) * qConditionedOnC;
		} else {
			return (1 - qConditionedOnC) - Math.exp(eps) * (1 - pConditionedOnC);
		}
	}

	public static double numericalAnalysis(int n, double epsOrig, double delta, int numIterations, int step,
			boolean upperBound) {
		double epsUpper;
		if (epsOrig < LocalEpsilonCalculator2.getConstraint(delta, n)) {
			epsUpper = closedFormAnalysis(n, epsOrig, delta);
		} else {
			epsUpper = epsOrig;
		}

		return binarySearch(delta, numIterations, epsUpper, n, epsOrig, step, upperBound);
	}

	public static double closedFormAnalysis(int n, double epsOrig, double delta) {
		if (epsOrig > LocalEpsilonCalculator2.getConstraint(delta, n)) {
			System.out.println("This is not a valid parameter regime for this analysis");
			return epsOrig;
		} else {
			double a = 8 * Math.sqrt(Math.exp(epsOrig) * Math.log(4 / delta)) / Math.sqrt(n);
			double c = 8 * Math.exp(epsOrig) / n;
			double b = 1 - Math.exp(-epsOrig);
			double d = 1 + Math.exp(-epsOrig);
			return Math.log(1 + (b / d) * (a + c));
		}
//		return LocalEpsilonCalculator2.getExistingServerEpsilon(epsOrig, delta, n);
	}

	public static void main(String args[]) {
		double localEpsilon = 1;
		int userNum = 10000000;
		double delta = 1e-12;
		double e_t = closedFormAnalysis(userNum, localEpsilon, delta);
		double e_u = numericalAnalysis(userNum, localEpsilon, delta, 10, 10, true);
		double e_l = numericalAnalysis(userNum, localEpsilon, delta, 10, 10, false);

		System.out.println(e_t + ", " + e_u + ", " + e_l);
	}
}
