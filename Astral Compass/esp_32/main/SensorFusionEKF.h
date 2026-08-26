/*
  SensorFusionEKF.h
  ---------------------------------------------------------------------------
  A multiplicative Extended Kalman Filter (MEKF) that fuses gyroscope,
  accelerometer, and the camera-derived delta-rotation from
  OpticalRotationTracker.ino into a stable orientation + gyro bias estimate.

  STATE
    Nominal state (not directly part of the covariance):
      q     : orientation quaternion, body-frame -> world-frame (q_wb)
      bias  : gyro bias estimate, rad/s

    Error state (what the 6x6 covariance P actually tracks):
      delta_theta (3) : small-angle attitude error, defined via
                         R_true = R_hat * R(delta_theta)   (body-frame,
                         right-multiplicative — the standard MEKF
                         convention; see Trawny & Roumeliotis, "Indirect
                         Kalman Filter for 3D Attitude Estimation")
      delta_bias  (3) : bias error

  WHY THIS SPLIT (nominal quaternion + linear error state) INSTEAD OF A
  7-STATE EKF DIRECTLY ON THE QUATERNION:
    A quaternion has a unit-norm constraint, so its "natural" covariance is
    only 3-dimensional, not 4. Filtering the error state directly avoids
    the singular/over-parameterized covariance a naive 4-state EKF would
    have, and avoids ad hoc re-normalization hacks. This is the standard,
    well-conditioned way to do attitude EKFs.

  MEASUREMENT 1 — ACCELEROMETER (tilt / pitch+roll reference)
    Predicted body-frame gravity direction: R(q)^T * [0,0,1]
    (convention: world Z is up; a stationary accelerometer reads +1g along
    world "up" — same convention used by the Madgwick filter, so this is
    easy to cross-check against).
    Innovation: measured normalized accel vector - predicted.
    This observes attitude error but NOT bias (Jacobian's bias block is
    zero) and — importantly — it's blind to rotation about the gravity
    vector itself, i.e. it cannot and does not correct yaw. That's expected;
    yaw only comes from vision here (no magnetometer).
    Gated/down-weighted when |accel| deviates from 1g, since that means
    you're sensing linear acceleration, not just gravity.

  MEASUREMENT 2 — VISION (delta-rotation from OpticalRotationTracker)
    This is the interesting one. The tracker gives you a delta-rotation
    over some short interval. Rather than treating that as an absolute
    attitude fix, we compare it against what the RAW (bias-uncorrected)
    gyro measured over that same interval:

        h(x) = theta_gyro_raw_integrated - bias_hat * dt_interval
        y    = theta_vision - h(x)

    To first order, y is a measurement of BIAS ERROR, not attitude error —
    the Jacobian's attitude-error block is zero, only the bias block is
    populated (H = [0, -dt_interval * I]).

    You might expect this means it only prevents *future* drift and can't
    fix attitude error that's already accumulated. It does fix current
    attitude too, but indirectly: during every prediction step, bias error
    leaks into attitude error (that's what the F matrix's coupling term
    does), so the covariance P builds up correlation between the two. When
    the vision update corrects bias, the Kalman gain — which is a function
    of P — automatically distributes some of that correction into current
    attitude as well, in the statistically optimal proportion. This is
    exactly how bias-aided reference measurements work in GPS-aided INS
    and star-tracker-aided spacecraft attitude systems; it's not a hack.

    One real consequence worth knowing: right after startup (or after a
    long vision outage), P's attitude-bias cross-covariance is small, so
    the first vision update(s) mostly fix bias with only a small immediate
    attitude nudge — full correction of accumulated yaw error catches up
    over the next several prediction/update cycles, not instantly. If you
    need instant yaw lock-on, you'd want a heavier scheme (stochastic
    cloning / a delayed-state filter) — out of scope here, but worth
    knowing this is a deliberate simplification, not an oversight.

  A CAVEAT CARRIED OVER FROM THE TRACKER
    OpticalRotationTracker.ino fits a first-order (small-angle) rotational
    flow model to the TOTAL observed flow between frames. That
    linearization degrades at large inter-frame rotation. Keep your vision
    update rate high enough that typical inter-frame rotation stays modest
    (order 10-15 deg or less) for the theta_vision values to be trustworthy
    at face value. residual_rms will rise when this assumption is
    stressed, which this filter uses to automatically down-weight noisy
    vision updates — but it can't fully undo a badly-conditioned fit.

  CAMERA/IMU ALIGNMENT
    This assumes the camera's rotation axes are aligned with the IMU's body
    frame. If they're mounted at a fixed relative rotation, calibrate that
    extrinsic and rotate theta_vision into the body frame before calling
    updateVision() (a one-time fixed 3x3 rotation applied to the vector).
    Left as identity here since board layouts vary.

  TUNING
    All noise parameters below are reasonable starting points, not measured
    values for your specific IMU/camera. Expect to tune GYRO_NOISE_DENSITY
    and GYRO_BIAS_WALK against your IMU's datasheet, and ACCEL_MEAS_VARIANCE
    / VISION_BASE_VARIANCE empirically (increase if the corresponding
    estimate looks jittery, decrease if you see it failing to correct
    drift).
*/
#pragma once

#include <math.h>
#include <Utils.h>

struct RotationEstimate {
  float dtheta_x;      // radians, small-angle delta-rotation about camera X
  float dtheta_y;      // radians, about camera Y
  float dtheta_z;      // radians, about camera Z (optical axis / roll)
  float dt_seconds;
  int   inlier_count;
  float residual_rms;  // normalized image units
};

// ---------------------------------------------------------------------------
// SensorFusionEKF
// ---------------------------------------------------------------------------
class SensorFusionEKF {
public:
  // --- Tunable noise parameters (see header comment: tune for your IMU) ---
  // Gyro white-noise density, rad/s per sqrt(Hz). ~0.0017 is a typical
  // low-cost MEMS gyro (roughly 0.1 deg/s/sqrt(Hz), e.g. MPU6050-class).
  float GYRO_NOISE_DENSITY = 0.0017f;
  // Gyro bias random-walk density, rad/s per sqrt(s). Small — bias drifts
  // slowly. Tune from an Allan variance plot if you have one; otherwise
  // start small and increase if the filter seems slow to trust new bias info.
  float GYRO_BIAS_WALK = 1.0e-5f;
  // Accelerometer measurement noise, in normalized-gravity-vector units
  // (not m/s^2 — the accel measurement is normalized before use).
  float ACCEL_MEAS_VARIANCE = 0.03f;
  // Reject/inflate accel updates when |accel| deviates from 1g by more
  // than this (g units) — indicates linear acceleration corrupting gravity.
  float ACCEL_MAX_DEVIATION_G = 0.25f;
  // Vision measurement base variance (rad^2) at INLIER_REFERENCE inliers
  // and near-zero residual; scaled down as track quality improves.
  float VISION_BASE_VARIANCE = 0.0006f;   // ~1.4 deg std dev at reference quality
  int   VISION_INLIER_REFERENCE = 10;
  float VISION_RESIDUAL_WEIGHT = 50.0f;   // inflates R as residual_rms grows

  void init() {
    q = Quaternion(1, 0, 0, 0);
    bias = Vector3(0, 0, 0);
    matIdentity(&P[0][0], 6);
    // Modest initial uncertainty; scale up if you don't have a good
    // startup attitude guess (e.g. init from a single accel reading — see
    // note in the example .ino).
    for (int i = 0; i < 3; i++) P[i][i] = 0.05f;       // attitude, rad^2
    for (int i = 3; i < 6; i++) P[i][i] = 1.0e-4f;     // bias, (rad/s)^2
    accumRawGyroQuat = Quaternion(1, 0, 0, 0);
    timeSinceVision = 0.0f;
  }

  // Call at gyro sample rate. gyroRadPerSec is the RAW gyro reading
  // (bias still included) in rad/s. dt in seconds.
  void predict(const Vector3 &gyroRadPerSec, float dt) {
    if (dt <= 0.0f) return;

    Vector3 omegaCorrected = gyroRadPerSec - bias;

    // --- Nominal state propagation (exact, not small-angle) ---
    float angle = omegaCorrected.normal() * dt;
    Quaternion dq = omegaCorrected.quatFromAxisAngle(angle);
    q = q * dq;
    q.normalize();

    // --- Accumulate RAW (bias-uncorrected) rotation for the next vision
    //     update's comparison. Composed via quaternion multiplication
    //     (not naive vector summation) so it stays accurate even when the
    //     accumulated rotation across many steps is large. ---
    float rawAngle = gyroRadPerSec.normal() * dt;
    Quaternion dqRaw = gyroRadPerSec.quatFromAxisAngle(rawAngle);
    accumRawGyroQuat = accumRawGyroQuat * dqRaw;
    accumRawGyroQuat.normalize();
    timeSinceVision += dt;

    // --- Error-state covariance propagation ---
    // F = [[I - skew(omegaCorrected)*dt,  -I*dt],
    //      [0,                             I    ]]
    float skewOm[9];
    omegaCorrected.skew3(skewOm);

    float F[36];
    matIdentity(F, 6);
    for (int i = 0; i < 3; i++)
      for (int j = 0; j < 3; j++)
        F[i*6+j] -= skewOm[i*3+j] * dt;
    for (int i = 0; i < 3; i++)
      F[i*6 + (3+i)] = -dt;

    float Ft[36];
    matTranspose(F, 6, 6, Ft);

    float FP[36], FPFt[36];
    matMul(F, 6, 6, &P[0][0], 6, FP);
    matMul(FP, 6, 6, Ft, 6, FPFt);

    // Diagonal process noise (standard simplification — ignores the small
    // off-diagonal contribution the -I*dt term technically also induces;
    // fine in practice at typical IMU rates).
    float gyroVar = GYRO_NOISE_DENSITY * GYRO_NOISE_DENSITY * dt;
    float biasVar = GYRO_BIAS_WALK * GYRO_BIAS_WALK * dt;
    for (int i = 0; i < 3; i++) FPFt[i*6+i] += gyroVar;
    for (int i = 3; i < 6; i++) FPFt[i*6+i] += biasVar;

    memcpy(&P[0][0], FPFt, sizeof(float) * 36);
  }

  // Call whenever you have a fresh accelerometer sample. accelG is the
  // raw reading in units of g (does not need to be pre-normalized).
  void updateAccel(const Vector3 &accelG) {
    float aNorm = accelG.normal();
    if (aNorm < 1e-6f) return;
    float deviation = fabsf(aNorm - 1.0f);
    if (deviation > ACCEL_MAX_DEVIATION_G) return; // too dynamic to trust as gravity

    Vector3 accelDir = accelG * (1.0f / aNorm);
    Vector3 gWorld(0, 0, 1);
    Vector3 predicted = q.rotateWorldToBody(gWorld);

    Vector3 y = accelDir - predicted; // innovation

    float Hb[9];
    predicted.skew3(Hb);
    // H = [Hb, 0] as a 3x6
    float H[18] = {0};
    for (int i = 0; i < 3; i++)
      for (int j = 0; j < 3; j++)
        H[i*6+j] = Hb[i*3+j];

    float Rvar = ACCEL_MEAS_VARIANCE * (1.0f + 4.0f * deviation * deviation);
    float R[9] = {0};
    R[0] = R[4] = R[8] = Rvar;

    applyUpdate(H, R, y);
  }

  // Call whenever OpticalRotationTracker::update() returns a valid
  // estimate. Resets the internal raw-gyro accumulator afterward.
  void updateVision(const RotationEstimate &est) {
    float dtInterval = (timeSinceVision > 1e-4f) ? timeSinceVision : est.dt_seconds;
    if (dtInterval <= 1e-4f) { 
      resetVisionAccumulator(); 
      return; 
    }

    Vector3 thetaVision(est.dtheta_x, est.dtheta_y, est.dtheta_z);
    Vector3 thetaGyroRaw = accumRawGyroQuat.logMap();

    Vector3 hPredicted = thetaGyroRaw - bias * dtInterval;
    Vector3 y = thetaVision - hPredicted;

    // H = [0, -dtInterval * I] as a 3x6
    float H[18] = {0};
    for (int i = 0; i < 3; i++) H[i*6 + (3+i)] = -dtInterval;

    int inliers = est.inlier_count > 1 ? est.inlier_count : 1;
    float qualityScale = (float)VISION_INLIER_REFERENCE / (float)inliers;
    float Rvar = VISION_BASE_VARIANCE * qualityScale * (1.0f + VISION_RESIDUAL_WEIGHT * est.residual_rms * est.residual_rms);
    float R[9] = {0};
    R[0] = R[4] = R[8] = Rvar;

    applyUpdate(H, R, y);
    resetVisionAccumulator();
  }

  Quaternion getOrientation() const { return q; }
  Vector3 getGyroBias() const { return bias; }

  // Roll/pitch/yaw in degrees, ZYX (aerospace) convention, for logging/
  // debugging. Degrades near pitch = +/-90 deg (gimbal lock in the
  // Euler representation itself, not a filter issue).
  Vector3 getEulerRPY_deg() const {
    float sinr_cosp = 2.0f * (q.w*q.x + q.y*q.z);
    float cosr_cosp = 1.0f - 2.0f * (q.x*q.x + q.y*q.y);
    float roll = atan2f(sinr_cosp, cosr_cosp);

    float sinp = 2.0f * (q.w*q.y - q.z*q.x);
    float pitch;
    if (fabsf(sinp) >= 1.0f) pitch = copysignf((float)M_PI / 2.0f, sinp);
    else pitch = asinf(sinp);

    float siny_cosp = 2.0f * (q.w*q.z + q.x*q.y);
    float cosy_cosp = 1.0f - 2.0f * (q.y*q.y + q.z*q.z);
    float yaw = atan2f(siny_cosp, cosy_cosp);

    const float RAD2DEG = 180.0f / (float)M_PI;
    return Vector3(roll * RAD2DEG, pitch * RAD2DEG, yaw * RAD2DEG);
  }

private:
  Quaternion q;
  Vector3 bias;
  float P[6][6];

  Quaternion accumRawGyroQuat;
  float timeSinceVision;

  void resetVisionAccumulator() {
    accumRawGyroQuat = Quaternion(1, 0, 0, 0);
    timeSinceVision = 0.0f;
  }

  // Shared Joseph-form EKF update given a 3x6 H, 3x3 R, and 3-vector
  // innovation y. Joseph form (rather than the simpler P=(I-KH)P) is used
  // because it stays numerically well-behaved (P remains symmetric and
  // positive semi-definite) even after thousands of updates on a
  // long-running embedded filter, which the simplified form is more
  // prone to degrade under with float precision.
  void applyUpdate(const float H[18], const float R[9], const Vector3 &y) {
    float Ht[18];
    matTranspose(H, 3, 6, Ht);

    float PHt[18]; // 6x3
    matMul(&P[0][0], 6, 6, Ht, 3, PHt);

    float HPHt[9]; // 3x3
    matMul(H, 3, 6, PHt, 3, HPHt);

    float S[9];
    matAddInPlace(HPHt, R, 9);
    memcpy(S, HPHt, sizeof(S));

    float Sinv[9];
    if (!mat3Inverse(S, Sinv)) return; // singular — skip this update

    float K[18]; // 6x3
    matMul(PHt, 6, 3, Sinv, 3, K);

    float yArr[3] = {y.x, y.y, y.z};
    float dx[6];
    matMul(K, 6, 3, yArr, 1, dx);

    Vector3 dtheta(dx[0], dx[1], dx[2]);
    Vector3 dbias(dx[3], dx[4], dx[5]);

    q = q * dtheta.quatFromSmallAngle();
    q.normalize();
    bias = bias + dbias;

    // Joseph form: P = (I-KH) P (I-KH)^T + K R K^T
    float KH[36]; // 6x6
    matMul(K, 6, 3, H, 6, KH);
    float ImKH[36];
    matIdentity(ImKH, 6);
    for (int i = 0; i < 36; i++) ImKH[i] -= KH[i];

    float ImKHt[36];
    matTranspose(ImKH, 6, 6, ImKHt);
    float term1a[36];
    matMul(ImKH, 6, 6, &P[0][0], 6, term1a);
    float term1[36];
    matMul(term1a, 6, 6, ImKHt, 6, term1);

    float Kt[18];
    matTranspose(K, 6, 3, Kt);
    float KR[18];
    matMul(K, 6, 3, R, 3, KR);
    float term2[36];
    matMul(KR, 6, 3, Kt, 6, term2);

    for (int i = 0; i < 36; i++) term1[i] += term2[i];
    memcpy(&P[0][0], term1, sizeof(float) * 36);
  }
};
