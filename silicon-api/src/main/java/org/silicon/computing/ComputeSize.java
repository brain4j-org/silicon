package org.silicon.computing;

public record ComputeSize(int x, int y, int z) {

    public ComputeSize {
        if (x <= 0) throw new IllegalArgumentException("X component of compute size must not be 0 or negative!");
        if (y <= 0) throw new IllegalArgumentException("Y component of compute size must not be 0 or negative!");
        if (z <= 0) throw new IllegalArgumentException("Z component of compute size must not be 0 or negative!");
    }
    
    public int workDim() {
        if (z > 1) return 3;
        if (y > 1) return 2;
        return 1;
    }
}
