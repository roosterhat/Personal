#pragma once

#include <Utils.h>

// ------------------------- Madgwick AHRS filter -------------------------
// Fuses gyro (rate of rotation), accelerometer (gravity reference) and
// magnetometer (heading reference) into a single orientation quaternion.
// beta controls the trust in accel/mag correction vs. pure gyro
// integration: higher beta = faster correction of drift but noisier;
// lower beta = smoother but slower to correct gyro drift.

class MadgwickAHRS {
public:
    explicit MadgwickAHRS(double beta = 0.08) : beta_(beta) {}

    // Optional: tell the filter what a clean magnetometer reading's
    // magnitude should look like at your location (measure this once, away
    // from motors/metal, e.g. during accel calibration -- see main() demo).
    // Any live reading whose magnitude falls outside +-toleranceFraction of
    // this value is treated as disturbed (e.g. by a nearby stepper motor)
    // and the filter falls back to gyro+accel only for that step, skipping
    // the yaw correction rather than trusting a corrupted reading.
    void setExpectedMagFieldMagnitude(double magnitude, double toleranceFraction = 0.25) {
        expectedMagMagnitude_ = magnitude;
        magTolerance_ = toleranceFraction;
    }

    // gyro in rad/s, accel and mag in any consistent units (will be
    // normalized internally). dt in seconds.
    void update(const Vector3& gyro, Vector3 accel, Vector3 mag, double dt) {
        double q1 = q_.w, q2 = q_.x, q3 = q_.y, q4 = q_.z;

        // Rate of change of quaternion from gyroscope.
        Quaternion gyroQ(0, gyro.x, gyro.y, gyro.z);
        Quaternion qDot = q_ * gyroQ;
        qDot.w *= 0.5; qDot.x *= 0.5; qDot.y *= 0.5; qDot.z *= 0.5;

        double accelNorm = accel.norm();
        double magNorm = mag.norm();

        bool magReliable = true;
        if (expectedMagMagnitude_ > 0) {
            double lower = expectedMagMagnitude_ * (1 - magTolerance_);
            double upper = expectedMagMagnitude_ * (1 + magTolerance_);
            magReliable = (magNorm >= lower && magNorm <= upper);
        }

        // Only fuse accel/mag if readings are sane (avoids corrupting the
        // estimate during, e.g., free-fall, or a magnetically disturbed
        // reading near a stepper motor).
        if (accelNorm > 1e-6 && magNorm > 1e-6 && magReliable) {
            accel = accel.normalized();
            mag = mag.normalized();

            double ax = accel.x, ay = accel.y, az = accel.z;
            double mx = mag.x, my = mag.y, mz = mag.z;

            // Reference direction of Earth's magnetic field (project
            // measured field into the horizontal/vertical components
            // using the current orientation estimate).
            double _2q1mx = 2 * q1 * mx, _2q1my = 2 * q1 * my;
            double _2q2mx = 2 * q2 * mx;
            double hx = mx * q1*q1 - 2*q1*my*q4 + 2*q1*mz*q3 + mx*q2*q2
                        + 2*q2*my*q3 + 2*q2*mz*q4 - mx*q3*q3 - mx*q4*q4;
            double hy = _2q1mx*q4 + my*q1*q1 - 2*q1*mz*q2 + _2q2mx*q3
                        - my*q2*q2 + my*q3*q3 + 2*q3*mz*q4 - my*q4*q4;
            double bx = std::sqrt(hx * hx + hy * hy);
            double bz = -_2q1mx*q3 + _2q1my*q2 + mz*q1*q1 + _2q2mx*q4
                        - mz*q2*q2 + 2*q3*my*q4 - mz*q3*q3 + mz*q4*q4;

            // Gradient descent algorithm corrective step (objective
            // function: predicted gravity/field vs. measured gravity/field).
            double _2bx = 2 * bx, _2bz = 2 * bz;
            double _4bx = 2 * _2bx, _4bz = 2 * _2bz;

            double f1 = 2*(q2*q4 - q1*q3) - ax;
            double f2 = 2*(q1*q2 + q3*q4) - ay;
            double f3 = 2*(0.5 - q2*q2 - q3*q3) - az;
            double f4 = _2bx*(0.5 - q3*q3 - q4*q4) + _2bz*(q2*q4 - q1*q3) - mx;
            double f5 = _2bx*(q2*q3 - q1*q4) + _2bz*(q1*q2 + q3*q4) - my;
            double f6 = _2bx*(q1*q3 + q2*q4) + _2bz*(0.5 - q2*q2 - q3*q3) - mz;

            // Jacobian^T * f  (standard Madgwick MARG gradient terms)
            double J11 = -2*q3, J12 = 2*q4, J13 = -2*q1, J14 = 2*q2;
            double J21 = 2*q2,  J22 = 2*q1, J23 = 2*q4,  J24 = 2*q3;
            double J31 = 0,     J32 = -4*q2, J33 = -4*q3, J34 = 0;
            double J41 = -_2bz*q3, J42 = _2bz*q4, J43 = -_4bx*q3 - _2bz*q1, J44 = -_4bx*q4 + _2bz*q2;
            double J51 = -_2bx*q4 + _2bz*q2, J52 = _2bx*q3 + _2bz*q1, J53 = _2bx*q2 + _2bz*q4, J54 = -_2bx*q1 + _2bz*q3;
            double J61 = _2bx*q3, J62 = _2bx*q4 - _4bz*q2, J63 = _2bx*q1 - _4bz*q3, J64 = _2bx*q2;

            double gx = J11*f1 + J21*f2 + J31*f3 + J41*f4 + J51*f5 + J61*f6;
            double gy = J12*f1 + J22*f2 + J32*f3 + J42*f4 + J52*f5 + J62*f6;
            double gz = J13*f1 + J23*f2 + J33*f3 + J43*f4 + J53*f5 + J63*f6;
            double gw = J14*f1 + J24*f2 + J34*f3 + J44*f4 + J54*f5 + J64*f6;

            double gn = std::sqrt(gx*gx + gy*gy + gz*gz + gw*gw);
            if (gn > 1e-12) { gx /= gn; gy /= gn; gz /= gn; gw /= gn; }

            qDot.w -= beta_ * gx;
            qDot.x -= beta_ * gy;
            qDot.y -= beta_ * gz;
            qDot.z -= beta_ * gw;
        } else if (accelNorm > 1e-6) {
            // Mag disturbed (or absent) but accel still good: fall back to
            // accel-only correction so roll/pitch keep getting corrected;
            // only yaw is left to free-run on the gyro until mag clears up.
            accel = accel.normalized();
            double ax = accel.x, ay = accel.y, az = accel.z;

            double f1 = 2*(q2*q4 - q1*q3) - ax;
            double f2 = 2*(q1*q2 + q3*q4) - ay;
            double f3 = 2*(0.5 - q2*q2 - q3*q3) - az;

            double J11 = -2*q3, J12 = 2*q4, J13 = -2*q1, J14 = 2*q2;
            double J21 = 2*q2,  J22 = 2*q1, J23 = 2*q4,  J24 = 2*q3;
            double J31 = 0,     J32 = -4*q2, J33 = -4*q3, J34 = 0;

            double gx = J11*f1 + J21*f2 + J31*f3;
            double gy = J12*f1 + J22*f2 + J32*f3;
            double gz = J13*f1 + J23*f2 + J33*f3;
            double gw = J14*f1 + J24*f2 + J34*f3;

            double gn = std::sqrt(gx*gx + gy*gy + gz*gz + gw*gw);
            if (gn > 1e-12) { gx /= gn; gy /= gn; gz /= gn; gw /= gn; }

            qDot.w -= beta_ * gx;
            qDot.x -= beta_ * gy;
            qDot.y -= beta_ * gz;
            qDot.z -= beta_ * gw;
        }

        q_.w += qDot.w * dt;
        q_.x += qDot.x * dt;
        q_.y += qDot.y * dt;
        q_.z += qDot.z * dt;
        q_.normalize();
    }

    Quaternion orientation() const { return q_; }

    void reset() { Quaternion q_; }

private:
    Quaternion q_;   // world<-body orientation estimate, starts level & north-ish
    double beta_;
    double expectedMagMagnitude_ = -1.0;  // unset by default (no disturbance gating)
    double magTolerance_ = 0.25;
};

// ------------------------- Standalone gyro+mag heading ---------------------
// Computes a tilt-compensated compass heading from ONLY the gyroscope and
// magnetometer, independent of the full Madgwick/accelerometer fusion above.
// Useful as a lightweight standalone heading readout, or if you want heading
// without pulling in the whole AHRS machinery.
//
// CAVEAT: with no accelerometer to correct it, the roll/pitch used here to
// tilt-compensate the magnetometer come purely from integrating the gyro,
// which drifts over time just like any pure-gyro estimate -- fine for short
// stretches or a device that stays close to level. For a long-running or
// significantly tilted device, get roll/pitch from the full ImuFusion/
// MadgwickAHRS above instead (which corrects them with the accelerometer)
// and tilt-compensate with those -- see headingFromRollPitchMag() below,
// which this class uses internally and which you can call directly if you
// already have a better roll/pitch estimate from elsewhere.
 
// Tilt-compensated compass heading, given roll/pitch (radians, from
// whatever source) and a raw magnetometer reading. Returns degrees, 0-360,
// where 0 = magnetic north (assuming mag.x reads north when level).
inline double headingFromRollPitchMag(double roll, double pitch, const Vector3& mag) {
    double cr = std::cos(roll), sr = std::sin(roll);
    double cp = std::cos(pitch), sp = std::sin(pitch);
 
    double Xh = mag.x * cp + mag.y * sr * sp + mag.z * cr * sp;
    double Yh = mag.y * cr - mag.z * sr;
 
    double heading = deg(std::atan2(Yh, Xh));
    if (heading < 0) heading += 360.0;
    return heading;
}
 
inline double wrap360(double deg) {
    deg = std::fmod(deg, 360.0);
    if (deg < 0) deg += 360.0;
    return deg;
}
 
// Shortest signed difference from `from` to `to`, in degrees, range (-180,180].
inline double shortestAngleDiff(double to, double from) {
    double diff = std::fmod(to - from + 180.0, 360.0);
    if (diff < 0) diff += 360.0;
    return diff - 180.0;
}
 
class GyroMagHeading {
public:
    // headingGain controls how quickly the estimate snaps toward a fresh
    // mag-derived heading each update (0-1). Lower values smooth out
    // occasional bad samples that slip past the disturbance gate (at the
    // cost of responding a bit slower to genuine heading changes); higher
    // values trust each accepted mag reading more directly.
    explicit GyroMagHeading(double headingGain = 0.3) : headingGain_(headingGain) {}
 
    // Same idea as MadgwickAHRS::setExpectedMagFieldMagnitude() -- measure
    // the mag magnitude once in a clean spot, away from motors, and pass it
    // here. Readings that deviate from it are treated as disturbed.
    void setExpectedMagFieldMagnitude(double magnitude, double toleranceFraction = 0.25) {
        expectedMagMagnitude_ = magnitude;
        magTolerance_ = toleranceFraction;
    }
 
    // Feed gyro (rad/s) and mag (any consistent unit) each timestep along
    // with the elapsed time dt (s) since the last call. Returns the current
    // heading in degrees, 0-360.
    double update(const Vector3& gyro, const Vector3& mag, double dt) {
        // Track roll/pitch by integrating gyro (yaw itself isn't tracked
        // this way -- that's exactly what we're solving for via mag).
        roll_  += gyro.x * dt;
        pitch_ += gyro.y * dt;
 
        bool magReliable = true;
        if (expectedMagMagnitude_ > 0) {
            double m = mag.norm();
            double lower = expectedMagMagnitude_ * (1 - magTolerance_);
            double upper = expectedMagMagnitude_ * (1 + magTolerance_);
            magReliable = (m >= lower && m <= upper);
        }
 
        // Always advance by the gyro's yaw rate first -- this alone is
        // what carries the estimate through a disturbed period.
        yawDeg_ = wrap360(yawDeg_ + deg(gyro.z * dt));
 
        if (magReliable) {
            // Blend gradually toward the mag-derived heading rather than
            // snapping straight to it, so an occasional bad sample that
            // slips past the magnitude gate (e.g. its magnitude happens to
            // look normal despite a wrong direction) only nudges the
            // estimate instead of causing a large single-step glitch.
            double magHeading = headingFromRollPitchMag(roll_, pitch_, mag);
            yawDeg_ = wrap360(yawDeg_ + headingGain_ * shortestAngleDiff(magHeading, yawDeg_));
        }
        // else: disturbed -- keep the gyro-only estimate from above as-is.
 
        return yawDeg_;
    }
 
    // Reset the internal tilt/heading estimate, e.g. if you know the device
    // just started level and facing a known heading, or periodically to
    // bound drift.
    void reset(double rollRad = 0.0, double pitchRad = 0.0, double headingDeg = 0.0) {
        roll_ = rollRad;
        pitch_ = pitchRad;
        yawDeg_ = wrap360(headingDeg);
    }
 
private:
    double roll_ = 0.0, pitch_ = 0.0;
    double yawDeg_ = 0.0;
    double headingGain_;
    double expectedMagMagnitude_ = -1.0;
    double magTolerance_ = 0.25;
};
 
struct AccelCalibration {
    Vector3 bias  = Vector3(0, 0, 0);
    Vector3 scale = Vector3(1, 1, 1);
 
    Vector3 apply(const Vector3& raw) const {
        return Vector3((raw.x - bias.x) * scale.x,
                        (raw.y - bias.y) * scale.y,
                        (raw.z - bias.z) * scale.z);
    }
};

Vector3 averageSamples(const std::vector<Vector3>& samples) {
    Vector3 sum;
    for (const auto& s : samples) sum = sum + s;
    return samples.empty() ? sum : sum * (1.0 / samples.size());
}

class AutoAccelCalibrator {
public:
    enum Face { PX = 0, NX = 1, PY = 2, NY = 3, PZ = 4, NZ = 5, NONE = -1 };
 
    AutoAccelCalibrator(double gyroStillThresh = 0.05,      // rad/s
                         double accelTolerance = 1,     // m/s^2, |accel|-g
                         double dominanceRatio = 3.0,         // dominant axis vs. others
                         int samplesNeededPerFace = 40,
                         double gravity = 9.80665)
        : gravity_(gravity), gyroStillThresh_(gyroStillThresh),
          accelTolerance_(accelTolerance), dominanceRatio_(dominanceRatio),
          samplesNeeded_(samplesNeededPerFace) {}
 
    Face addSample(const Vector3& gyro, const Vector3& accel) { 
        if (!(gyro.norm() < gyroStillThresh_ && abs(accel.norm() - gravity_) < accelTolerance_)) {
            buffer_.clear();   
            return NONE;
        }
 
        buffer_.push_back(accel);
        if ((int)buffer_.size() < samplesNeeded_) return NONE;
 
        Vector3 avg = averageSamples(buffer_);
        buffer_.clear();
 
        Face f = identifyFace(avg);
        if (f == NONE || captured_[f]) return NONE;
 
        faceReading_[f] = avg;
        captured_[f] = true;
        return f;
    }
 
    bool ready() const {
        for (bool c : captured_) if (!c) return false;
        return true;
    }
 
    AccelCalibration compute() const {
        AccelCalibration cal;
        if (!ready()) return cal;
 
        double xUp = faceReading_[PX].x, xDown = faceReading_[NX].x;
        double yUp = faceReading_[PY].y, yDown = faceReading_[NY].y;
        double zUp = faceReading_[PZ].z, zDown = faceReading_[NZ].z;
 
        cal.bias.x = (xUp + xDown) / 2.0;
        cal.bias.y = (yUp + yDown) / 2.0;
        cal.bias.z = (zUp + zDown) / 2.0;
 
        cal.scale.x = (2.0 * gravity_) / (xUp - xDown);
        cal.scale.y = (2.0 * gravity_) / (yUp - yDown);
        cal.scale.z = (2.0 * gravity_) / (zUp - zDown);
 
        return cal;
    }
 
private:
    Face identifyFace(const Vector3& a) const {
        double ax = abs(a.x), ay = abs(a.y), az = abs(a.z);
        double dominant = max({ax, ay, az});
 
        // Require the dominant axis to actually be near g, and clearly
        // bigger than the other two -- otherwise it's a tilted/ambiguous hold.
        if (abs(dominant - gravity_) > accelTolerance_) return NONE;
 
        double second = (dominant == ax) ? max(ay, az)
                        : (dominant == ay) ? max(ax, az)
                                           : max(ax, ay);
        if (second > dominant / dominanceRatio_) return NONE;
 
        if (dominant == ax) return (a.x > 0) ? PX : NX;
        if (dominant == ay) return (a.y > 0) ? PY : NY;
        return (a.z > 0) ? PZ : NZ;
    }
 
    double gravity_, gyroStillThresh_, accelTolerance_, dominanceRatio_;
    int samplesNeeded_;
 
    std::vector<Vector3> buffer_;
    std::array<bool, 6> captured_ = {false, false, false, false, false, false};
    std::array<Vector3, 6> faceReading_;
};

// --------------------------- Position tracker ---------------------------
// Strapdown inertial integration with a very simple zero-velocity update
// (ZUPT): if the device looks stationary (low gyro rate AND accel close
// to 1g with little deviation) we assume true velocity is zero and clamp
// it. This alone will NOT give you accurate long-term position; it just
// keeps the demo's drift from spiraling out of control between still
// periods. Real systems need an external position/velocity reference.

class PositionTracker {
public:
    void update(const Vector3& accelBody, const Vector3& gyro, const Quaternion& orientation,
                double dt, double gravity = 9.80665) {
        Vector3 accelWorld = orientation.rotate(accelBody);
        Vector3 linearAccel = accelWorld - Vector3(0, 0, gravity);
 
        bool likelyStill = gyro.norm() < 0.02 && abs(accelBody.norm() - gravity) < 0.05;
 
        if (likelyStill) {
            velocity_ = Vector3();       // ZUPT: clamp velocity to zero
        } else {
            velocity_ = velocity_ + linearAccel * dt;
        }
        position_ = position_ + velocity_ * dt;
    }
 
    Vector3 position() const { return position_; }
    Vector3 velocity() const { return velocity_; }
 
private:
    Vector3 velocity_;
    Vector3 position_;
};

struct FusionOutput {
    Vector3 position, angle;
};

class ImuFusion {
public:
    ImuFusion(double beta = 0.08) : ahrs_(beta) {}
 
    void setAccelCalibration(const AccelCalibration& cal) { 
        accelCal_ = cal; 
        ahrs_.reset();
    }
 
    FusionOutput update(const Vector3& gyro, const Vector3& rawAccel, const Vector3& mag, double dt) {
        Vector3 accel = accelCal_.apply(rawAccel);
        ahrs_.update(gyro, accel, mag, dt);
        Quaternion q = ahrs_.orientation();
        tracker_.update(accel, gyro, q, dt);
 
        Vector3 rpy = q.toEulerRPY();
 
        return { Vector3(deg(rpy.x), deg(rpy.y), deg(rpy.z)), tracker_.position() };
    }
 
private:
    MadgwickAHRS ahrs_;
    PositionTracker tracker_;
    AccelCalibration accelCal_;  // identity (no-op) until setAccelCalibration() is called
};