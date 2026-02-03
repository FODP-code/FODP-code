#!/usr/bin/env python3
import sys
import math

# Calculate ql and qr in AGeo(nu, ql, qr) from eps and beta
def ql_qr_from_eps_beta(eps: float, beta: float) -> tuple[float, float]:
    a = math.exp(-eps / 2.0)
    b = math.exp(eps / 2.0)
    ql = (a - 1.0 + beta) / beta
    qr = beta / (b - 1.0 + beta)
    return ql, qr

# Calculate the normalizing constant eta in AGeo(nu, ql, qr)
def calc_eta(nu: int, ql: float, qr: float) -> float:
    return ql * (1.0 - (ql ** nu)) / (1.0 - ql) + 1.0 / (1.0 - qr)

# Calculate delta from nu in AGeo(nu, ql, qr)
def delta_from_nu(eps: float, beta: float, nu: int, ql: float, qr: float) -> float:
    beta0 = 1.0 - math.exp(-eps / 2.0)
    if beta == beta0:
        return 0.0
    if beta < beta0:
        raise ValueError(f"beta must satisfy beta >= 1 - exp(-epsilon/2) = {beta0}")

    eta = calc_eta(nu, ql, qr)
    factor = 1.0 - math.exp(eps / 2.0) + beta * math.exp(eps / 2.0)
    return (2.0 / eta) * (ql ** nu) * factor

# Find the minimum nu in AGeo(nu, ql, qr) for delta
def find_min_nu_for_delta(eps: float, delta_req: float, beta: float, ql: float, qr: float,
                                 nu_max: int = 10_000_000) -> int:
    beta0 = 1.0 - math.exp(-eps / 2.0)
    if beta == beta0:
        return 0

    for nu in range(0, nu_max + 1):
        if delta_from_nu(eps, beta, nu, ql, qr) <= delta_req:
            return nu

    raise RuntimeError(f"nu did not reach delta <= {delta_req} up to nu_max={nu_max}")

# Find the minimum nu s.t. delta_req >= (2 / eta) * beta * ql^nu
def find_min_nu_for_delta2(delta_req: float, beta: float, ql: float, qr: float,
                              nu_max: int = 10_000_000) -> int:
    for nu in range(0, nu_max + 1):
        eta = calc_eta(nu, ql, qr)
        rhs = (2.0 / eta) * beta * (ql ** nu)
        if delta_req >= rhs:
            return nu

    raise RuntimeError(f"No nu found up to nu_max={nu_max}")

# Calculate mean mu of AGeo(nu, ql, qr)
def ageo_mean(nu: int, ql: float, qr: float) -> float:
    # normalizing constant eta
    eta = calc_eta(nu, ql, qr)

    # left sum: 
    # S_left = sum_{k=0}^{nu-1} k * ql^{nu-k} 
    #        = sum_{j=1}^{nu} (nu-j) q^j (j=nu-k) 
    #        = nu*sum_{j=1}^{nu} q^j - sum_{j=1}^{nu} j q^j
    if nu == 0:
        S_left = 0.0
    else:
        q = ql
        A = q * (1.0 - (q ** nu)) / (1.0 - q)  # sum_{j=1}^{nu} q^j
        B = q * (1.0 - (nu + 1) * (q ** nu) + nu * (q ** (nu + 1))) / ((1.0 - q) ** 2)  # sum_{j=1}^{nu} j q^j
        S_left = nu * A - B

    # right sum:
    # T-right = (qr + (1-qr)*nu) / (1-qr)^2
    T_right = (qr + (1.0 - qr) * nu) / ((1.0 - qr) ** 2)

    # mean mu = (S_left + T_right) / eta
    mu = (S_left + T_right) / eta
    return mu

# Calculate a survival function P[X >= z] (= 1 - F(z-1)) for AGeo(nu, ql, qr)
def ageo_survival_function(z: int, nu: int, ql: float, qr: float, eta: float | None = None) -> float:
    if z <= 0:
        return 1.0

    # 0 < z <= nu
    if z <= nu:
        left = ql * (1.0 - ql**(nu - z)) / (1.0 - ql)   # sum_{k=z}^{nu-1} ql^{nu-k}
        right = 1.0 / (1.0 - qr)                        # sum_{k=nu}^{inf} qr^{k-nu}
        return (left + right) / eta

    # z > nu
    right_tail = (qr ** (z - nu)) / (1.0 - qr)          # sum_{k=z}^{inf} qr^{k-nu}
    return right_tail / eta

# Find the minimum z s.t. 2*(1 - F(z-1)) <= delta0_req
def find_min_z_for_delta0(delta0_req: float, nu: int, ql: float, qr: float, z_max: int = 10_000_000) -> int:
    t = delta0_req / 2.0  # target tail
    eta = calc_eta(nu, ql, qr)

    for z in range(0, z_max + 1):
        if ageo_survival_function(z, nu, ql, qr, eta) <= t:
            return z

    raise RuntimeError(f"No z found up to z_max={z_max}")

#################################### Main #####################################
if len(sys.argv) < 4:
    print("Usage:",sys.argv[0],"[epsilon_E (> 0)] [epsilon_I (> epsilon_E)] [delta_I (in (0,1))] [distribution type (1:AGeo,0:1Geo)]")
    sys.exit(0)

# Paramter epsilon_E
epsilon_E = float(sys.argv[1])
# Paramter epsilon_I
epsilon_I = float(sys.argv[2])
# Parameter delta_I
delta_I_req = float(sys.argv[3])
# distribution type (1:AGeo,0:1Geo)
dist_type = int(sys.argv[4])

if epsilon_E <= 0.0:
    raise ValueError("epsilon_E must be > 0")
if epsilon_I <= epsilon_E:
    raise ValueError("epsilon_I must be > epsilon_E")
if not (0 < delta_I_req < 1.0):
    raise ValueError("delta_I must be in (0, 1)")

# AGeo
if dist_type == 1:
    beta = 1.0
    delta_req = delta_I_req / 2.0
    delta0_req = delta_I_req / 2.0
# 1Geo
else:
    beta = 1.0 - math.exp(-epsilon_E / 2.0)
    delta_req = 0
    delta0_req = delta_I_req

################# Dummy-count distribution D #################
# ql, qr of AGeo --> ql_D, qr_D
ql_D, qr_D = ql_qr_from_eps_beta(epsilon_E, beta)
# nu of AGeo --> nu_D
nu_D = find_min_nu_for_delta(epsilon_E, delta_req, beta, ql_D, qr_D)
# mean of AGeo --> mu_D
mu_D = ageo_mean(nu_D, ql_D, qr_D)

# Find the minimum kappa_D s.t. 2*(1 - F(kappa_D-1)) <= delta0_req
kappa_D = find_min_z_for_delta0(delta0_req, nu_D, ql_D, qr_D)

# Output E[kappa_i] in FOLNF
print(f"E[kappa_i] (FOLNF): {kappa_D:.12g}")
# Output delta_E in FOLNF
print(f"delta_E (FOLNF): {delta_I_req:.12g}")

################# Bot-count distribution D' ##################
# ql, qr of AGeo --> ql_B, qr_B
L, R = ql_qr_from_eps_beta(epsilon_I, beta)
ql_B = R / qr_D
if dist_type == 1:
    qr_B = L / ql_D
else:
    qr_B = 0

# nu of AGeo --> nu_B
nu_B = find_min_nu_for_delta2(delta_I_req, beta, ql_B, qr_B)
# mean of AGeo --> mu_B
mu_B = ageo_mean(nu_B, ql_B, qr_B)

# Output E[kappa_i] in FOLNF*
print(f"E[kappa_i] (FOLNF*): {mu_D + mu_B:.12g}")
# Output delta_E in FOLNF*
print(f"delta_E (FOLNF*): {delta_req:.12g}")
