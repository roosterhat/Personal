#pragma once
#include <math.h>

inline double deg(double rad) {
  return rad * 180.0 / PI;
}

struct Vector3;
struct Quaternion;

struct Vector3 {
  double x, y, z;
  Vector3(double x_ = 0, double y_ = 0, double z_ = 0)
    : x(x_), y(y_), z(z_) {}
  Vector3(float v[3])
    : x(v[0]), y(v[1]), z(v[2]) {}
  Vector3(double v[3])
    : x(v[0]), y(v[1]), z(v[2]) {}

  Vector3 operator+(const Vector3 &o) const {
    return { x + o.x, y + o.y, z + o.z };
  }
  Vector3 operator-(const Vector3 &o) const {
    return { x - o.x, y - o.y, z - o.z };
  }
  Vector3 operator*(double s) const {
    return { x * s, y * s, z * s };
  }
  double operator[](int index) const {
    switch(index) {
      case 0:
        return x;
      case 1:
        return y;
      case 2:
        return z;
    }
  }

  double normal() const {
    return sqrt(x * x + y * y + z * z);
  }

  Vector3 normalized() const {
    double n = normal();
    return (n > 1e-12) ? Vector3(x / n, y / n, z / n) : Vector3();
  }

  Vector3 remapImuToBody() {
    return Vector3(-y, -x, -z);
  }

  void skew3(float out[9]) const {
    out[0] = 0;
    out[1] = -z;
    out[2] = y;
    out[3] = z;
    out[4] = 0;
    out[5] = -x;
    out[6] = -y;
    out[7] = x;
    out[8] = 0;
  }

  Quaternion quatFromAxisAngle(float angle) const;
  Quaternion quatFromSmallAngle() const;
  Quaternion quatFromEulerRPY() const;
};

struct Quaternion {
  double w, x, y, z;
  Quaternion(double w_ = 1, double x_ = 0, double y_ = 0, double z_ = 0)
    : w(w_), x(x_), y(y_), z(z_) {}
  Quaternion(Vector3 vec)
    : w(1), x(vec.x), y(vec.y), z(vec.z) {}

  Quaternion operator*(const Quaternion &q) const {
    return {
      w * q.w - x * q.x - y * q.y - z * q.z,
      w * q.x + x * q.w + y * q.z - z * q.y,
      w * q.y - x * q.z + y * q.w + z * q.x,
      w * q.z + x * q.y - y * q.x + z * q.w
    };
  }

  Quaternion conjugate() const {
    return { w, -x, -y, -z };
  }

  void normalize() {
    double n = sqrt(w * w + x * x + y * y + z * z);
    if (n > 1e-12) {
      w /= n;
      x /= n;
      y /= n;
      z /= n;
    }
  }

  // Rotate a body-frame vector into the world frame.
  Vector3 rotate(const Vector3 &v) const {
    Quaternion vq(0, v.x, v.y, v.z);
    Quaternion r = *this * vq * conjugate();
    return { r.x, r.y, r.z };
  }

  // Returns (roll, pitch, yaw) in radians, aerospace convention (ZYX).
  Vector3 toEulerRPY() const {
    double roll = atan2(2 * (w * x + y * z), 1 - 2 * (x * x + y * y));

    double sinp = 2 * (w * y - z * x);
    double pitch = (abs(sinp) >= 1) ? copysign(PI / 2, sinp) : asin(sinp);

    double yaw = atan2(2 * (w * z + x * y), 1 - 2 * (y * y + z * z));
    return { roll, pitch, yaw };
  }

  Vector3 logMap() {
    float wc = w;
    if (wc > 1.0f) wc = 1.0f;
    if (wc < -1.0f) wc = -1.0f;

    float angle = 2.0f * acosf(wc);
    float s = sinf(angle * 0.5f);

    if (fabsf(s) < 1e-6f) return Vector3(0, 0, 0);  // near-identity rotation

    float scale = angle / s;
    return Vector3(x * scale, y * scale, z * scale);
  }

  Vector3 rotateWorldToBody(const Vector3 &v) const {
    Quaternion qi = conjugate();
    // v as a pure quaternion, compute qi * v * q
    Quaternion vq(0, v.x, v.y, v.z);
    Quaternion t = qi * vq;
    Quaternion r = t * (*this);
    return Vector3(r.x, r.y, r.z);
  }
};

inline Quaternion Vector3::quatFromAxisAngle(float angle) const {
  float n = normal();
  if (n < 1e-9f || fabsf(angle) < 1e-9f) return Quaternion(1, 0, 0, 0);
  Vector3 axis = *this * (1.0f / n);
  float half = angle * 0.5f;
  float s = sinf(half);
  return Quaternion(cosf(half), axis.x * s, axis.y * s, axis.z * s);
}

inline Quaternion Vector3::quatFromSmallAngle() const {
  return Quaternion(1.0f, x * 0.5f, y * 0.5f, z * 0.5f);
}

inline Quaternion Vector3::quatFromEulerRPY() const {
  const double D2R = M_PI / 180.0;
  double roll = x * D2R, pitch = y * D2R, yaw = z * D2R;
  double cr = cos(roll * 0.5),  sr = sin(roll * 0.5);
  double cp = cos(pitch * 0.5), sp = sin(pitch * 0.5);
  double cy = cos(yaw * 0.5),   sy = sin(yaw * 0.5);
  return Quaternion(
    cr*cp*cy + sr*sp*sy,
    sr*cp*cy - cr*sp*sy,
    cr*sp*cy + sr*cp*sy,
    cr*cp*sy - sr*sp*cy
  );
}

static void matMul(const float *A, int ra, int ca, const float *B, int cb, float *C) {
  for (int i = 0; i < ra; i++) {
    for (int j = 0; j < cb; j++) {
      float sum = 0;
      for (int k = 0; k < ca; k++) sum += A[i * ca + k] * B[k * cb + j];
      C[i * cb + j] = sum;
    }
  }
}

static void matTranspose(const float *A, int r, int c, float *At) {
  for (int i = 0; i < r; i++)
    for (int j = 0; j < c; j++)
      At[j * r + i] = A[i * c + j];
}

static void matAddInPlace(float *A, const float *B, int n) {
  for (int i = 0; i < n; i++) A[i] += B[i];
}

static void matIdentity(float *A, int n) {
  for (int i = 0; i < n; i++)
    for (int j = 0; j < n; j++)
      A[i * n + j] = (i == j) ? 1.0f : 0.0f;
}

// 3x3 inverse via the adjugate/cofactor method. Returns false (leaves out[] untouched) if the matrix is singular.
static bool mat3Inverse(const float A[9], float out[9]) {
  float det =
    A[0] * (A[4] * A[8] - A[5] * A[7])
    - A[1] * (A[3] * A[8] - A[5] * A[6])
    + A[2] * (A[3] * A[7] - A[4] * A[6]);
  if (fabsf(det) < 1e-12f) return false;
  float invDet = 1.0f / det;
  out[0] = (A[4] * A[8] - A[5] * A[7]) * invDet;
  out[1] = -(A[1] * A[8] - A[2] * A[7]) * invDet;
  out[2] = (A[1] * A[5] - A[2] * A[4]) * invDet;
  out[3] = -(A[3] * A[8] - A[5] * A[6]) * invDet;
  out[4] = (A[0] * A[8] - A[2] * A[6]) * invDet;
  out[5] = -(A[0] * A[5] - A[2] * A[3]) * invDet;
  out[6] = (A[3] * A[7] - A[4] * A[6]) * invDet;
  out[7] = -(A[0] * A[7] - A[1] * A[6]) * invDet;
  out[8] = (A[0] * A[4] - A[1] * A[3]) * invDet;
  return true;
}

struct AzEl {
  double az;
  double el;

  bool operator==(AzEl ref) {
    return az == ref.az && el == ref.el;
  }
};

inline Vector3 azElToVector(double azDeg, double elDeg) {
  double az = azDeg * PI / 180.0;
  double el = elDeg * PI / 180.0;
  double cosEl = cos(el);
  return Vector3(cosEl * sin(az), cosEl * cos(az), sin(el)); // (X,Y,Z)
}

inline AzEl vectorToAzEl(const Vector3 &v) {
  double az = atan2(v.x, v.y) * 180.0 / PI; // clockwise from +Y
  if (az < 0.0) az += 360.0;
  double horizMag = sqrt(v.x * v.x + v.y * v.y);
  double el = atan2(v.z, horizMag) * 180.0 / PI;
  return { az, el };
}

inline double wrapDeg360(double deg) {
  double r = fmod(deg, 360.0);
  if (r < 0.0) r += 360.0;
  return r;
}

inline double captureNorthOffset(const Quaternion &orientation, const Vector3 &magReadingBody) {
  Vector3 magWorld = orientation.rotate(magReadingBody);
  double ekfWorldAzOfNorth = vectorToAzEl(magWorld).az;
  return wrapDeg360(-ekfWorldAzOfNorth);
}

inline AzEl computeRequiredAzEl(const Quaternion &orientation, double targetAzDeg, double targetElDeg, double northOffsetDeg) {
  double ekfWorldAz = wrapDeg360(targetAzDeg - northOffsetDeg);

  Vector3 targetWorld = azElToVector(ekfWorldAz, targetElDeg);

  Vector3 targetBody = orientation.rotateWorldToBody(targetWorld);

  AzEl result = vectorToAzEl(targetBody);

  if (result.el < 0.0) result.el = 0.0;
  if (result.el > 90.0) result.el = 90.0;

  return result;
}