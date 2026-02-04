# FODP (Fully Oblivious Differential Privacy)
This source code is an implementation of our FODP algorithms. Our code consists of:
1. Accuracy Evaluation (Figures 6, 8, 9, 16, 17)
2. Robustness Evaluation (Figure 13)
3. Runtime Evaluation (Figures 10, 11, 14)
4. Others (Figures 5, 7, 12, 15)

Below, we explain how to run our code to obtain our experimental results for each part.

# 1 Accuracy Evaluation
### 1.1 Execution Environment
TBD

### 1.2 Installation
TBD

### 1.3 Running the Code
TBD

# 2 Robustness Evaluation
### 2.1 Execution Environment
TBD

### 2.2 Installation
TBD

### 2.3 Running the Code
TBD

# 3 Runtime Evaluation

### 3.1 Execution Environment
To ensure the reproducibility of the experimental results on Intel SGX, the following environment is recommended:

* **Azure VM Type:** Standard DC2s v3
* **Operating System:** Ubuntu 20.04.6 LTS
* **Kernel:** Linux kernel 5.15.0-1089-azure
* **Hardware Feature:** Intel SGX enabled

### 3.2 Installation
Extract the archive and run the makefile from the root directory:

```
$ tar -xzvf folnf.tar.gz
$ cd folnf/artifact
$ make 
```

### 3.3 Data Generation (Randomized Data)
Generate synthetic datasets using the data generator located in the App directory:

```
$ cd App
$ ./data <file_name>.txt <n> <d>
```
file_name: Path to save the generated random data
(e.g., testdata.txt, created in folnf/artifact/App/)<br>
n: Data size (total number of records)<br>
d: Number of items (domain size)

### 3.4 Running Experiments
Execute the main application with the generated dataset:

```
$ ./app <file_name> <d> <flag>
```
The flag is a 3-digit integer abc used to specify the algorithm and protocol types:

a (Algorithm Type)<br>
1: Our algorithm for small-domain data (Algorithm 1)<br>
2: Our algorithm for large-domain data (Algorithm 2)<br>
5: Histogram-based algorithm (Algorithm 5)<br>

b (Method Type)<br>
1: FOUD<br>
2: FOLNF<br>
3: FOLNF*<br>

c (Distribution Type)<br>
0: AGeo<br>
1: 1Geo

Example: Algorithm 1 + FOLNF + AGeo → flag = 120.<br>
Note: To select FOUD, set c = 0 (e.g., flag = 110 or 210). To select Algorithm 5, set b = 0 and c = 0 (e.g., flag = 500).


Example Output: 
Upon successful execution, the system outputs the processing time (in microseconds) for each enclave step (step1: random sampling, step2: dummy data addition, step3: shuffling):

```
Plaintext

n:10000
d:10000
d_max:9999
[enclave] step1  = 906 us
[enclave] step2  = 69310 us
[enclave] shuffle size: 10750000
[enclave] step3  = 2328447 us

```

By changing n, d, and flag to various values, we obtained `results/res_runtime.xlsx`, from which we obtained Figures 10 and 11. For Figure 14, see `OR-Waks/README.md`.

###  3.5 System Implementation Details
- Main Enclave Routine:
folnf/artifact/Enclave/FOLNF/FOLNF.cpp

- Oblivious Shuffle:
Heap-allocated recursive shuffle in
folnf/artifact/Enclave/FOLNF/Recursive_OR_Shuffle.cpp

- Oblivious Primitives:
Defined in util_obl.h. <br>
The floor_ln function employs a log-free (bit-manipulation-based) implementation to ensure high performance and stability within the SGX enclave.

# 4 Others
### Figure 5
We directly obtained Figure 5 by `results/res_additive_error_bound.xlsx`. See this file.

### Figure 7
Execute the following code:
```
$ cd python/
$ python bot_count.py <epsilon_E> <epsilon_I> <delta_I> <distribution type (1:AGeo,0:1Geo)>
```
By changing epsilon_E, epsilon_I, and delta_I, we obtained `results/res_bot_counts.xlsx`, from which we obtained Figure 7.

### Figure 12
We directly obtained Figure 12 by `results/res_FOUD_bound.xlsx`. See this file.

### Figure 15
TBD
