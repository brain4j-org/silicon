#ifndef _SWIFT_CCUDA_CUDA_H_
#define _SWIFT_CCUDA_CUDA_H_

#if defined(__APPLE__) || defined(__linux__)
#include "/usr/local/cuda/include/cuda.h"
#else
#include <cuda.h>
#endif

#endif // _SWIFT_CCUDA_CUDA_H_
