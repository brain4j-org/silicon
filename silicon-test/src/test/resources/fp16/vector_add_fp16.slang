[shader("compute")]
void add(
    uint3 tid : SV_DispatchThreadID,
    StructuredBuffer<half> a : register(t0),
    StructuredBuffer<half> b : register(t1),
    RWStructuredBuffer<half> c : register(t2)
)
{
    uint i = tid.x;
    c[i] = a[i] + b[i];
}
