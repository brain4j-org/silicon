#define TILE 16

groupshared float As[TILE][TILE];
groupshared float Bs[TILE][TILE];

[shader("compute")]
[numthreads(TILE, TILE, 1)]
void matmul(
    uint3 tid              : SV_DispatchThreadID,
    uint3 groupId          : SV_GroupID,
    uint3 local            : SV_GroupThreadID,

    StructuredBuffer<half> A    ,
    StructuredBuffer<half> B    ,
    RWStructuredBuffer<half> C  ,
    StructuredBuffer<uint> Sizes
)
{
    uint M = Sizes[0];
    uint N = Sizes[1];
    uint K = Sizes[2];

    // global coordinates computed from group + local
    uint row = groupId.y * TILE + local.y;
    uint col = groupId.x * TILE + local.x;

    float acc = 0.0f;

    // number of K-tiles to iterate
    uint numTiles = (K + TILE - 1) / TILE;

    // iterate over K in tiles
    for (uint t = 0; t < numTiles; ++t)
    {
        uint aCol = t * TILE + local.x;
        uint bRow = t * TILE + local.y;

        if (row < M && aCol < K)
        {
            As[local.y][local.x] = float(A[row * K + aCol]);
        }
        else
        {
            As[local.y][local.x] = 0.0f;
        }

        if (bRow < K && col < N)
        {
            Bs[local.y][local.x] = float(B[bRow * N + col]);
        }
        else
        {
            Bs[local.y][local.x] = 0.0f;
        }

        GroupMemoryBarrierWithGroupSync();

        [unroll]
        for (uint k = 0; k < TILE; ++k)
        {
            acc += As[local.y][k] * Bs[k][local.x];
        }

        GroupMemoryBarrierWithGroupSync();
    }

    if (row >= M || col >= N) return;

    C[row * N + col] = (half) acc;
}
