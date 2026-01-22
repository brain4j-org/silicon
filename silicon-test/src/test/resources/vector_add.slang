[shader("compute")]
void add(uint3 tid : SV_DispatchThreadID, float* a, float* b, float* c)
{
    uint i = tid.x;
    c[i] = a[i] + b[i];
}
