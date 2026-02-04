package fodp;

public abstract class Distribution {

	public abstract int dummyCountGeneration(double r);

	public abstract int botCountGeneration(double r, int zi);

	public static Distribution getDistribution(Method method, double epsilon_E, double delta_E) {
		if (method == Method.FOUD) {
			return new DistributionFOUD();
		} else if (method == Method.FOLNF_AGeo) {
			return new DistributionFOLNF_SAGeo(epsilon_E, delta_E);
		} else if (method == Method.FOLNF_1Geo) {
			return new DistributionFOLNF_S1Geo(epsilon_E, delta_E);
		} else if (method == Method.FOLNF2_AGeo) {
			return new DistributionFOLNF2_SAGeo(epsilon_E, delta_E);
		} else if (method == Method.FOLNF2_1Geo) {
			return new DistributionFOLNF2_S1Geo(epsilon_E, delta_E);
		}

		return null;
	}

	public abstract double getMu();
}
