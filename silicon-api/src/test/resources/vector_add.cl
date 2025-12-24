__kernel void add(
    __global const float* A,
    __global const float* B,
    __global float* C,
    const int N
) {
    int i = get_global_id(0);
    if (i < N) {
        C[i] = A[i] + B[i];
    }
}
