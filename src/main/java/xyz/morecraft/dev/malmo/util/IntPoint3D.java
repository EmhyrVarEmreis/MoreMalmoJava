package xyz.morecraft.dev.malmo.util;

import com.sun.istack.internal.NotNull;
import lombok.ToString;

import java.util.Objects;

@ToString
public class IntPoint3D implements Comparable<IntPoint3D> {

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

    public IntPoint3D withX(double x) {
        return new IntPoint3D(x, y, z);
    }

    public IntPoint3D withY(double y) {
        return new IntPoint3D(x, y, z);
    }

    public IntPoint3D withZ(double z) {
        return new IntPoint3D(x, y, z);
    }

    public IntPoint3D floor() {
        return new IntPoint3D(
                Math.floor(x),
                Math.floor(y),
                Math.floor(z)
        );
    }

    public IntPoint3D round() {
        return new IntPoint3D(
                (double) Math.round(x),
                (double) Math.round(y),
                (double) Math.round(z)
        );
    }

    public IntPoint3D ceil() {
        return new IntPoint3D(
                Math.ceil(x),
                Math.ceil(y),
                Math.ceil(z)
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntPoint3D that = (IntPoint3D) o;
        return Objects.equals(x, that.x) &&
                Objects.equals(y, that.y) &&
                Objects.equals(z, that.z);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public IntPoint3D clone() {
        return new IntPoint3D(x, y, z);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(@NotNull IntPoint3D o) {
        if (o == this) {
            return 0;
        }
        if (Objects.isNull(o)) {
            return -1;
        }
        int cmp = x.compareTo(o.x);
        if (cmp == 0) {
            cmp = y.compareTo(o.y);
        } else {
            return cmp;
        }
        if (cmp == 0) {
            cmp = z.compareTo(o.z);
        } else {
            return cmp;
        }
        return cmp;
    }

}
