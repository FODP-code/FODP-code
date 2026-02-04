package fodp;

public class DistributionFOUD extends Distribution {

	@Override
	public int dummyCountGeneration(double r) {
		return 0;
	}

	@Override
	public int botCountGeneration(double r, int zi) {
		return 0;
	}

	@Override
	public double getMu() {
		return 0;
	}

}
