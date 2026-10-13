#pragma once
#include <Utils.h>

/**
 * Estimates the yaw of magnetic north from a sweep, using 3-axis magnetometer readings plus the
 * absolute roll/pitch/yaw orientation estimate at the moment of each reading.
 *
 * All angles are DEGREES. Yaw is clockwise positive and absolute, i.e. in whatever fixed frame
 * your orientation estimate uses; it does not need to start at 0 when the sweep starts, and it may
 * be reported wrapped (e.g. -180..180 or 0..360) or unwrapped.
 *
 * Model (body frame; "ref" = the frame the yaw is measured in: x at yaw 0, y at yaw 90, z down):
 *     m = c + R^T * B_ref          R = Rz(yaw) * Ry(pitch) * Rx(roll)   (body -> ref)
 * Unknowns: c (hard-iron offset, 3) and the horizontal field B_ref = (Bx, By). The vertical
 * component Bz is supplied (setVerticalField) or taken as 0. The model is linear in the
 * unknowns, so every sample just adds to a 5x5 normal-equation matrix: no sample list is stored
 * and estimate() can be called at any time.
 *
 * Why roll/pitch go inside the fit rather than tilt-compensating each reading first: the
 * hard-iron offset is fixed in the body frame, so it has to be removed in the body frame.
 *
 * Result: magnetic heading of the device = yaw - northYawDeg. True heading adds the declination
 * (east positive); the yaw at which the body +X axis points at true north is
 * northYawDeg - declinationDeg (trueNorthYawDeg()).
 *
 * Coverage slices are absolute: slice i covers yaw [i*360/n, (i+1)*360/n). That matches a dial
 * with yaw 0 at the top and slice 0 starting there.
 */
class NorthEstimator {
 public:
  static constexpr int MAX_SLICES = 32;

  struct Result {
    bool valid = false;        // solvable and finite (also check complete() before trusting it)
    float northYawDeg = 0;     // [0, 360): yaw at which the body +X axis points at magnetic north
    float hardIron[3] = {0, 0, 0};  // body-frame offset, same units as the readings
    float horizField = 0;      // fitted horizontal field strength; compare with the expected value
    float rms = 0;             // rms residual per axis; should be small vs horizField
    uint32_t samples = 0;
  };

  explicit NorthEstimator(int slices = 12, int minPerSlice = 10) { begin(slices, minPerSlice); }

  /** Start a new sweep. Slices only drive the coverage mask; the fit uses every sample. */
  void begin(int slices, int minPerSlice = 10) {
    slices_ = slices < 1 ? 1 : (slices > MAX_SLICES ? MAX_SLICES : slices);
    minPerSlice_ = minPerSlice < 1 ? 1 : minPerSlice;
    reset();
  }

  void reset() {
    std::memset(ata_, 0, sizeof ata_);
    std::memset(atb_, 0, sizeof atb_);
    std::memset(sliceCount_, 0, sizeof sliceCount_);
    bb_ = 0;
    count_ = 0;
  }

  /** Down component of the Earth's field (positive down, same units as the readings).
   *  Call before the first sample. 0 is fine if the device stays level; otherwise use the WMM
   *  value for your location. */
  void setVerticalField(float bz) { bz_ = bz; }

  /** Add one reading. Returns false (and ignores it) if any input is not finite. */
  int addSample(Vector3 mag, Vector3 orientation) {
    if (!std::isfinite(mag.x) || !std::isfinite(mag.y) || !std::isfinite(mag.z) ||
        !std::isfinite(orientation.x) || !std::isfinite(orientation.y) || !std::isfinite(orientation.z))
      return -1;

    int idx = (int)wrap360(orientation.z) / (360.0 / slices_);
    if (idx >= slices_) idx = slices_ - 1;
    if (sliceCount_[idx] > 100) return idx;
    sliceCount_[idx]++;

    const double roll = orientation.x * DEG2RAD, pitch = orientation.y * DEG2RAD, yaw = orientation.z * DEG2RAD;
    const double cr = std::cos(roll), sr = std::sin(roll);
    const double cp = std::cos(pitch), sp = std::sin(pitch);
    const double cy = std::cos(yaw), sy = std::sin(yaw);
    const double R[3][3] = {
        {cy * cp, cy * sp * sr - sy * cr, cy * sp * cr + sy * sr},
        {sy * cp, sy * sp * sr + cy * cr, sy * sp * cr - cy * sr},
        {-sp,     cp * sr,                cp * cr}};
    const double m[3] = {mag.x, mag.y, mag.z};

    // One equation per axis k:  m_k - Bz*R[2][k] = c_k + Bx*R[0][k] + By*R[1][k]
    for (int k = 0; k < 3; k++) {
      double a[N] = {0, 0, 0, R[0][k], R[1][k]};
      a[k] = 1.0;
      const double b = m[k] - bz_ * R[2][k];
      for (int i = 0; i < N; i++) {
        for (int j = 0; j < N; j++) ata_[i][j] += a[i] * a[j];
        atb_[i] += a[i] * b;
      }
      bb_ += b * b;
    }
    count_++;
    return idx;
  }

  /** Bit i set = slice i has >= minPerSlice samples. Slice 0 starts at absolute yaw 0. */
  uint32_t coverageMask() const {
    uint32_t mask = 0;
    for (int i = 0; i < slices_; i++)
      if (sliceCount_[i] >= minPerSlice_) mask |= (1u << i);
    return mask;
  }

  bool complete() const {
    const uint64_t full = (slices_ >= 32) ? 0xFFFFFFFFull : ((1ull << slices_) - 1);
    return coverageMask() == full;
  }

  bool sliceComplete(int i) {
    return sliceCount_[i] >= minPerSlice_;
  }

  /** Solve for the current best estimate. Cheap enough to call on every sample. */
  Result estimate() const {
    Result r;
    r.samples = count_;
    if (count_ < 3) return r;

    double M[N][N + 1];
    double maxDiag = 0;
    for (int i = 0; i < N; i++) {
      for (int j = 0; j < N; j++) M[i][j] = ata_[i][j];
      M[i][N] = atb_[i];
      if (ata_[i][i] > maxDiag) maxDiag = ata_[i][i];
    }
    for (int col = 0; col < N; col++) {          // Gaussian elimination, partial pivoting
      int piv = col;
      for (int row = col + 1; row < N; row++)
        if (std::fabs(M[row][col]) > std::fabs(M[piv][col])) piv = row;
      if (std::fabs(M[piv][col]) < 1e-9 * maxDiag) return r;   // not enough rotation to separate offset from field
      if (piv != col)
        for (int j = col; j <= N; j++) { double t = M[col][j]; M[col][j] = M[piv][j]; M[piv][j] = t; }
      for (int row = col + 1; row < N; row++) {
        const double f = M[row][col] / M[col][col];
        for (int j = col; j <= N; j++) M[row][j] -= f * M[col][j];
      }
    }
    double u[N];
    for (int i = N - 1; i >= 0; i--) {
      double s = M[i][N];
      for (int j = i + 1; j < N; j++) s -= M[i][j] * u[j];
      u[i] = s / M[i][i];
    }

    double sse = bb_;                            // |b - A u|^2 = b'b - 2u'g + u'Nu
    for (int i = 0; i < N; i++) {
      sse -= 2.0 * u[i] * atb_[i];
      for (int j = 0; j < N; j++) sse += u[i] * ata_[i][j] * u[j];
    }
    if (sse < 0) sse = 0;

    r.hardIron[0] = (float)u[0];
    r.hardIron[1] = (float)u[1];
    r.hardIron[2] = (float)u[2];
    r.horizField = (float)std::hypot(u[3], u[4]);
    r.northYawDeg = (float)wrap360(std::atan2(u[4], u[3]) * RAD2DEG);
    r.rms = (float)std::sqrt(sse / (3.0 * count_));
    r.valid = std::isfinite(r.northYawDeg) && std::isfinite(r.horizField) && r.horizField > 0;

    //Serial.printf("%.5f %.5f %.5f %.5f %.5f %.5f %i\n", r.hardIron[0], r.hardIron[1], r.hardIron[2], r.horizField, r.northYawDeg, r.rms, r.valid);
    return r;
  }

  /** Yaw (deg, [0,360)) at which the body +X axis points at TRUE north; declination is east positive. */
  static float trueNorthYaw(float northYawDeg, float declinationDeg) {
    return (float)wrap360((double)northYawDeg - declinationDeg);
  }

 private:
  static constexpr int N = 5;
  static constexpr double DEG2RAD = PI / 180.0;
  static constexpr double RAD2DEG = 180.0 / PI;

  static double wrap360(double deg) {
    double d = std::fmod(deg, 360.0);
    if (d < 0) d += 360.0;
    if (d >= 360.0) d = 0.0;           // guards a tiny negative input rounding up to 360
    return d;
  }

  double ata_[N][N];
  double atb_[N];
  double bb_;
  uint32_t count_;
  uint16_t sliceCount_[MAX_SLICES];
  int slices_ = 12, minPerSlice_ = 10;
  float bz_ = 0;
};