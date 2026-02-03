# Supplemental Instructions for ORShuffle/WaksShuffle (Figure 14)

To conduct experiments using WaksShuffle (WaksmanShuffle) and ORShuffle (RecursiveShuffle), please follow the instructions below to add or replace files in each respective artifact_folder.

## Build and Execution
The build and execution procedures are identical to those for the folnf artifact.

### Execution Note
For these specific artifacts, please use ./script_Application instead of the default ./app as follows:
```
$ ./script_Application <file_name> <d> <flag>
```
---

## 1. Waksman Network (wakson-20230704)

### [Add Files]
Add the following files to the directory "artifact_folder/Application":
- data
- data.cpp

Add the following files to the directory "artifact_folder/Enclave/WaksmanNetwork":
- fx_common.h
- geo_dist.cpp
- geo_dist.h
- parameter_gen.cpp
- parameter_gen.h
- parameter_set.cpp
- parameter_set.h
- util_obl.h

### [Replace Files]
Overwrite the following existing files with the provided ones:
- artifact_folder/Makefile
- artifact_folder/Application/Script_Application.cpp
- artifact_folder/Enclave/Enclave.edl
- artifact_folder/Enclave/WaksmanNetwork/WaksmanNetwork.cpp
- artifact_folder/Enclave/WaksmanNetwork/WaksmanNetwork.edl
- artifact_folder/Enclave/WaksmanNetwork/WaksmanNetwork.hpp
- artifact_folder/Untrusted/Untrusted.cpp
- artifact_folder/Untrusted/WN.hpp

---

## 2. Oblivious Shuffle (oblivshuffle-20220901)

### [Add Files]
Add the following files to the directory "artifact_folder/Application":
- data
- data.cpp

Add the following files to the directory "artifact_folder/Enclave/RecursiveShuffle":
- fx_common.h
- geo_dist.cpp
- geo_dist.h
- parameter_gen.cpp
- parameter_gen.h
- parameter_set.cpp
- parameter_set.h
- util_obl.h

### [Replace Files]
Overwrite the following existing files with the provided ones:
- artifact_folder/Makefile
- artifact_folder/Application/Script_Application.cpp
- artifact_folder/Enclave/Enclave.edl
- artifact_folder/Enclave/RecursiveShuffle/RecursiveShuffle.cpp
- artifact_folder/Enclave/RecursiveShuffle/RecursiveShuffle.edl
- artifact_folder/Enclave/RecursiveShuffle/RecursiveShuffle.hpp
- artifact_folder/Untrusted/Untrusted.cpp
- artifact_folder/Untrusted/RS.hpp