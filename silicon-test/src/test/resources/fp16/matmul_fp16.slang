[shader("compute")]
void matmul(
    uint3 tid : SV_DispatchThreadID,

    StructuredBuffer<half> A : register(t0),
    StructuredBuffer<half> B : register(t1),
    RWStructuredBuffer<half> C : register(t2),
    StructuredBuffer<uint> Sizes : register(t3)
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
        acc += (float) A[aBase + k] * (float) B[k * N + bBase];
    }

    C[row * N + col] = (half) acc;
}
