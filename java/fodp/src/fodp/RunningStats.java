package fodp;

public class RunningStats {
	private long n = 0;
	private double mean = 0.0;
	private double m2 = 0.0; // sum of squares of differences from the current mean

	public void push(double x) {
		n++;
		double delta = x - mean;
		mean += delta / n;
		double delta2 = x - mean;
		m2 += delta * delta2;
	}

	public long count() {
		return n;
	}

	public double mean() {
		return mean;
	}

	public double sampleVariance() {
		if (n < 2)
			return Double.NaN;
		return m2 / (n - 1);
	}

	public double sampleStdDev() {
		double v = sampleVariance();
		return Double.isNaN(v) ? Double.NaN : Math.sqrt(v);
	}

	public double standardError() {
		if (n < 2)
			return Double.NaN;
		return sampleStdDev() / Math.sqrt(n);
	}
}