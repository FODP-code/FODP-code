#!/usr/bin/env python3
import sys
import math
import numpy as np

def epsilon(d, theta1, theta2, lam):
    return math.log((d + (1 + theta1) * lam) / ((1 - theta2) * lam))

def delta(d, theta1, theta2, lam):
    term1 = math.exp(- (theta1 * theta1 * lam) / ((2 + theta1) * d))
    term2 = math.exp(- (theta2 * theta2 * lam) / (2 * d))
    return term1 + term2

def find_min_lambda(d, eps_max, delta_max, theta1_max, 
                    theta_step=0.01, lambda_max=10**9):
    theta_values = np.arange(0.0, theta1_max + 1e-12, theta_step)
    theta_values_one = np.concatenate([
        np.arange(0.0, 1.0 - 1e-12, theta_step),
        np.array([1 - 10**(-k) for k in range(3, 10)])
    ])

    best_lambda = None
    best_tuple = None

    for theta1 in theta_values:
        for theta2 in theta_values_one:

            # Exponential search for lambda
            lam = 1
            ok = False
            while lam <= lambda_max:
                eps = epsilon(d, theta1, theta2, lam)
                delt = delta(d, theta1, theta2, lam)

                if eps <= eps_max and delt <= delta_max:
                    ok = True
                    break
                lam *= 2  # accelerate search

            if not ok:
                continue

            # Binary search between lam/2 and lam
            low = lam // 2
            high = lam
            while low + 1 < high:
                mid = (low + high) // 2
                if (epsilon(d, theta1, theta2, mid) <= eps_max and
                    delta(d, theta1, theta2, mid) <= delta_max):
                    high = mid
                else:
                    low = mid

            lam_candidate = high

            if best_lambda is None or lam_candidate < best_lambda:
                best_lambda = lam_candidate
                best_tuple = (theta1, theta2)

    return best_lambda, best_tuple


def main():
    # ---- parse argv ----
    if len(sys.argv) < 4:
        print("Usage: python find_lambda.py <d> <eps_max> <delta_max> (<theta1-max (default: 2.0)>)")
        sys.exit(1)

    d = int(sys.argv[1])
    eps_max = float(sys.argv[2])
    delta_max = float(sys.argv[3])
    
    theta1_max = 2.0
    if len(sys.argv) >= 5:
        theta1_max = float(sys.argv[4])

    # ---- compute ----
    lam, (theta1, theta2) = find_min_lambda(d, eps_max, delta_max, theta1_max)

    # ---- output ----
    print("lambda =", lam)
    print("best theta1 =", theta1)
    print("best theta2 =", theta2)


if __name__ == "__main__":
    main()
