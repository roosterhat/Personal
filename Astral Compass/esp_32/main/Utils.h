#pragma once
#include <math.h>

inline double deg(double rad) { return rad * 180.0 / PI; }

struct Vector3 {
    double x, y, z;
    Vector3(double x_ = 0, double y_ = 0, double z_ = 0) : x(x_), y(y_), z(z_) {}
    Vector3(float v[3]) : x(v[0]), y(v[1]), z(v[2]) {}
    Vector3(double v[3]) : x(v[0]), y(v[1]), z(v[2]) {}

    Vector3 operator+(const Vector3& o) const { return {x + o.x, y + o.y, z + o.z}; }
    Vector3 operator-(const Vector3& o) const { return {x - o.x, y - o.y, z - o.z}; }
    Vector3 operator*(double s) const { return {x * s, y * s, z * s}; }

    double norm() const { return sqrt(x * x + y * y + z * z); }

    Vector3 normalized() const {
        double n = norm();
        return (n > 1e-12) ? Vector3(x / n, y / n, z / n) : Vector3();
    }
};

// Convention: q = w + x*i + y*j + z*k, rotates body-frame vectors into
// the world (Earth) frame: v_world = q * [0,v_body] * q_conjugate.
struct Quaternion {
    double w, x, y, z;
    Quaternion(double w_ = 1, double x_ = 0, double y_ = 0, double z_ = 0)
        : w(w_), x(x_), y(y_), z(z_) {}

    Quaternion operator*(const Quaternion& q) const {
        return {
            w * q.w - x * q.x - y * q.y - z * q.z,
            w * q.x + x * q.w + y * q.z - z * q.y,
            w * q.y - x * q.z + y * q.w + z * q.x,
            w * q.z + x * q.y - y * q.x + z * q.w
        };
    }

    Quaternion conjugate() const { return {w, -x, -y, -z}; }

    void normalize() {
        double n = sqrt(w * w + x * x + y * y + z * z);
        if (n > 1e-12) { w /= n; x /= n; y /= n; z /= n; }
    }

    // Rotate a body-frame vector into the world frame.
    Vector3 rotate(const Vector3& v) const {
        Quaternion vq(0, v.x, v.y, v.z);
        Quaternion r = (*this) * vq * conjugate();
        return {r.x, r.y, r.z};
    }

    // Returns (roll, pitch, yaw) in radians, aerospace convention (ZYX).
    Vector3 toEulerRPY() const {
        double roll = atan2(2 * (w * x + y * z), 1 - 2 * (x * x + y * y));

        double sinp = 2 * (w * y - z * x);
        double pitch = (abs(sinp) >= 1) ? copysign(PI / 2, sinp) : asin(sinp);

        double yaw = atan2(2 * (w * z + x * y), 1 - 2 * (y * y + z * z));
        return {roll, pitch, yaw};
    }
};