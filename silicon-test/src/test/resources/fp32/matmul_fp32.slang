[shader("compute")]
void matmul(
    uint3 tid : SV_DispatchThreadID,

    StructuredBuffer<float> A,
    StructuredBuffer<float> B,
    RWStructuredBuffer<float> C,
    StructuredBuffer<uint> Sizes
)
{
    uint M = Sizes[0];
    uint N = Sizes[1];
    uint K = Sizes[2];

    uint col = tid.x;
    uint row = tid.y;

    if (row >= M || col >= N)
        return;

    float acc = 0.0f;

    uint aBase = row * K;
    uint bBase = col;

    for (uint k = 0; k < K; ++k)
    {
        acc += A[aBase + k] * B[k * N + bBase];
    }

    C[row * N + col] = acc;
}
