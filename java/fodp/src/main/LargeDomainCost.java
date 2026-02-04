package main;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import composition.OptimalComposition;
import composition.OptimalComposition.Result;
import encryption.ENCRYPTION_MODE;
import encryption.EncryptionUtil;
import fodp.Distribution;
import fodp.DistributionFOLNF;
import fodp.FindLambda2;
import fodp.Method;
import fodp.TauGammaOptimizer;
import fodp.Util2;

public class LargeDomainCost {

	static int turnNum = 1;
	static double sConstant = 1;

	public static void main(String[] args)
			throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException {

		System.out.println("Method" + "\t" + "DataSet" + "\t" + "epsilon_E" + "\t" + "C_tot");
		
		int seed = 1;
		double[] epsilon_Es = { 0.1 };
		double[] delta_Es = { 1E-12 };

		String[] dataNames = { "foursquare", "aol" };
		Method methods[] = { Method.FOUD, Method.FOLNF_AGeo, Method.FOLNF_1Geo, Method.FOLNF2_AGeo,
				Method.FOLNF2_1Geo };

		for (double epsilon_E : epsilon_Es) {
			for (double delta_E : delta_Es) {
				for (String dataName : dataNames) {

					int[] orgData = Util2.getOrgVals(dataName);
					int n = orgData.length;

					int d = Util2.getD(dataName);
					int b = (int) (sConstant * n);

					for (Method method : methods) {
						int tau = TauGammaOptimizer.getOptimalTau(method, epsilon_E, delta_E, n, d, b);

						Result r = OptimalComposition.solveMaxEpsThenMaxDelta(epsilon_E, delta_E, tau);
						double epsilonEach = r.eps;
						double deltaEach = r.delta;

						long lambda = 0;// If FOUD is used, lambda value will be updated below.
						double beta = 1;// If S1Geo is used, beta value will be updated below.

						Distribution dist = Distribution.getDistribution(method, epsilonEach, deltaEach);
						double mu = dist.getMu();

						if (method == Method.FOUD) {
							lambda = FindLambda2.findMinLambda(b, epsilonEach, deltaEach);
						} else if (method == Method.FOLNF_1Geo || method == Method.FOLNF2_1Geo) {
							beta = ((DistributionFOLNF) dist).getBeta();
						}

						double sumBits = 0;

						for (int turn = 0; turn < turnNum; turn++) {
							Random random = new Random(seed);

							int bitLength = ceilLog2(b);
							int bytesPerTriple = (bitLength + 7) / 8;
							byte[] triplePlain = new byte[bytesPerTriple];

							KeyPair keyPair = EncryptionUtil.getKeyPair(ENCRYPTION_MODE.ECIES);
							PublicKey publicKey = keyPair.getPublic();

							byte encryptedBytes[] = EncryptionUtil.encrypt(publicKey, triplePlain);
							int encryptedBits = encryptedBytes.length * 8;

							int users2shuffler = n * tau * encryptedBits;

							long count = (n + lambda) * tau;

							for (int t = 0; t < tau; t++) {
								for (int j = 0; j < b; j++) {
									int zj = dist.dummyCountGeneration(random.nextDouble());
									int kappaj = dist.botCountGeneration(random.nextDouble(), zj);
									count += kappaj;
								}
							}

							long shuffler2server = count * encryptedBits;
							sumBits += users2shuffler + shuffler2server;
						}

						System.out.print(method + "\t" + dataName + "\t" + epsilon_E + "\t");
						System.out.printf("%.2E%n", sumBits / turnNum);
					}
				}
			}
		}
	}

	static int ceilLog2(long x) {
		if (x <= 1) {
			return 0;
		}
		return 64 - Long.numberOfLeadingZeros(x - 1);
	}

}
