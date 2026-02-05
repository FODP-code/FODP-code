# FODP (Fully Oblivious Differential Privacy)
This source code is an implementation of our FODP algorithms. Our code consists of:
1. Accuracy Evaluation (Figures 6, 8, 9, 16, 17)
2. Robustness Evaluation (Figure 13)
3. Runtime Evaluation (Figures 10, 11, 14)
4. Others (Figures 5, 7, 12, 15)

Below, we explain how to run our code to obtain our experimental results for each part.

# 1 Accuracy Evaluation
### 1.1 Execution Environment

All experiments were implemented in Java and are platform-independent.
The code can be executed on any operating system that supports a standard Java Virtual Machine (JVM).

- Programming language: Java
- Java version: Java 8 or later
- Operating system: Windows, macOS, or Linux
- Java Virtual Machine: Any standard JVM

### 1.2 Installation

Compile the code in the `java/fodp` directory:

   ```
   $ javac -cp "lib/*" -d bin src/collusion/*.java src/composition/*.java src/encryption/*.java src/fodp/*.java src/hash/*.java src/main/*.java
   ```

### 1.3 Running the Code

All commands below should be executed in the `java/fodp` directory.

**Figure 6: Mean Squared Error (MSE) Comparison**

| Position    | Dataset / Setting          | Command (On Linux and macOS, replace ; with : in the -cp option.)|
| ----------- | -------------------------- | ----------------------------------------------------------------- |
| Upper Left  | IPUMS, Small Domain        | `java -cp "lib/*;bin" main.SmallDomainMSE ipums`                  |
| Upper Right | Localization, Small Domain | `java -cp "lib/*;bin" main.SmallDomainMSE localization`           |
| Lower Left  | Foursquare, Large Domain   | `java -cp "lib/*;bin" main.LargeDomainMSE foursquare`             |
| Lower Right | AOL, Large Domain          | `java -cp "lib/*;bin" main.LargeDomainMSE aol`                    |


**Figure 8: Tau Optimization Results (AGeo)**

| Position | Dataset    | Command (On Linux and macOS, replace ; with : in the -cp option.)                                                    |
| -------- | ---------- | ----------------------------------------------------------- |
| Left     | Foursquare | `java -cp "lib/*;bin" main.TauOptimization AGeo foursquare` |
| Right    | AOL        | `java -cp "lib/*;bin" main.TauOptimization AGeo aol`        |


**Figure 9: MSE for various values of b (AOL)**

| Position | Method | Command (On Linux and macOS, replace ; with : in the -cp option.)                                              |
| -------- | ------ | ----------------------------------------------------- |
| Left     | FOUD   | `java -cp "lib/*;bin" main.LargeDomainMSE4b foud aol` |
| Center   | AGeo   | `java -cp "lib/*;bin" main.LargeDomainMSE4b ageo aol` |
| Right    | 1Geo   | `java -cp "lib/*;bin" main.LargeDomainMSE4b 1geo aol` |


**Figure 16: Tau Optimization Results (1Geo)**

| Position | Dataset    | Command (On Linux and macOS, replace ; with : in the -cp option.)                                                    |
| -------- | ---------- | ----------------------------------------------------------- |
| Left     | Foursquare | `java -cp "lib/*;bin" main.TauOptimization 1Geo foursquare` |
| Right    | AOL        | `java -cp "lib/*;bin" main.TauOptimization 1Geo aol`        |


**Figure 17: MSE for various values of b (Foursquare)**

| Position | Method | Command (On Linux and macOS, replace ; with : in the -cp option.)                                                     |
| -------- | ------ | ------------------------------------------------------------ |
| Left     | FOUD   | `java -cp "lib/*;bin" main.LargeDomainMSE4b foud foursquare` |
| Center   | AGeo   | `java -cp "lib/*;bin" main.LargeDomainMSE4b ageo foursquare` |
| Right    | 1Geo   | `java -cp "lib/*;bin" main.LargeDomainMSE4b 1geo foursquare` |


# 2 Robustness Evaluation

## 2.1 Execution Environment

See Section 1.1 (Execution Environment).

## 2.2 Installation

See Section 1.2 (Installation).

## 2.3 Running the Code

All commands below should be executed in the `java/fodp` directory.

### Figure 13: Robustness Evaluation under Collusion Attacks

| Position | Scenario | Command |
|---------|----------|---------|
| Left | Collusion Setting 1 | `java -cp "lib/*;bin" main.Collusion1` |
| Right | Collusion Setting 2 | `java -cp "lib/*;bin" main.Collusion2` |

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

Execute the following code in the `java/fodp` directory (on Linux and macOS, replace ; with : in the -cp option.):
```
$ java -cp "lib/*;bin" main.LargeDomainCost 
```
