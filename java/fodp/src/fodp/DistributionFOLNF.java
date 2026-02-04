package fodp;

public abstract class DistributionFOLNF extends Distribution {

	public double getBeta() {
		return beta;
	}

	public double getEta() {
		return eta;
	}

	public double getQl() {
		return ql;
	}

	public double getQr() {
		return qr;
	}

	public double getQl_prime() {
		return ql_prime;
	}

	public double getQr_prime() {
		return qr_prime;
	}

	public int getNu_prime() {
		return nu_prime;
	}

	public double getEta_prime() {
		return eta_prime;
	}

	public int getKappa() {
		return kappa;
	}

	public double getMu() {
		return mu;
	}

	public int getNu() {
		return nu;
	}

	protected double beta;
	protected double eta;
	protected double ql;
	protected double qr;
	protected int nu;
	protected double ql_prime;
	protected double qr_prime;
	protected int nu_prime;
	protected double eta_prime;
	protected int kappa;
	protected double mu;

	@Override
	public String toString() {
		return "DistributionFOLNF{" + "beta=" + beta + ", eta=" + eta + ", ql=" + ql + ", qr=" + qr + ", nu=" + nu
				+ ", ql_prime=" + ql_prime + ", qr_prime=" + qr_prime + ", nu_prime=" + nu_prime + ", eta_prime="
				+ eta_prime + ", kappa=" + kappa + ", mu=" + mu + '}';
	}

}
