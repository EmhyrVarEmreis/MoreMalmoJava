package xyz.morecraft.dev.malmo.util;

public class IntPoint3D {

    public Double x, y, z;

    public IntPoint3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float fX() {
        return x.floatValue();
    }

    public float fY() {
        return y.floatValue();
    }

    public float fZ() {
        return z.floatValue();
    }

    public int iX() {
        return x.intValue();
    }

    public int iY() {
        return y.intValue();
    }

    public int iZ() {
        return z.intValue();
    }

}
