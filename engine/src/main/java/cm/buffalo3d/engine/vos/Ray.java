package cm.buffalo3d.engine.vos;

/**
 * Represents a geometric ray, compound of a {@link Number3d}.
 */
public class Ray {

    private Number3d point;
    private Number3d vector;

    /**
     * Constructs a ray from a point and a vector
     *
     * @param p
     *            the point
     * @param v
     *            the vector
     */
    public Ray(Number3d p, Number3d v) {
        this.point = p;
        this.vector = v;
    }

    /**
     * Constructs a ray from a point and a vector. The point is defined as (0,0,0)
     *
     * @param v
     *            the vector
     */
    public Ray(Number3d v) {
        this.point = new Number3d(0, 0, 0);
        this.vector = v;
    }

    /**
     * Constructs a ray from a point and a vector. The point is defined as (0,0,0)
     *
     * @param vec_x x in the vector
     * @param vec_y y in the vector
     * @param vec_z z in the vector
     */
    public Ray(float vec_x, float vec_y, float vec_z) {
        this.point = new Number3d(0, 0, 0);
        this.vector = new Number3d(vec_x, vec_y, vec_z);
    }

    /**
     * Returns the point in the ray that corresponds to the given t parameter
     *
     * @param t
     *            t parameter
     * @return the corresponding point
     */
    public Number3d getPoint(float t) {
        Number3d p = new Number3d(t * vector.x, t * vector.y, t * vector.z);
        p.add(point);
        return p;
    }

    /**
     * Returns the starting point for this ray
     *
     * @return the starting point for this ray
     */
    public Number3d getPoint() {
        return point;
    }

    /**
     * Returns the vector defining the ray
     *
     * @return the vector defining the ray
     */
    public Number3d getVector() {
        return vector;
    }

    public void setVector(float x, float y, float z) {
        this.vector.x = x;
        this.vector.y = y;
        this.vector.z = z;
    }

    private static Ray r = new Ray(new Number3d(0, 0, 0), new Number3d(0, 0, 0));

    public static Ray getVolatileRay(Number3d p, Number3d v) {
        r.point = p;
        r.vector = v;
        return r;
    }

}
