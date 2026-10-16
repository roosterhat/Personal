#pragma once

#include <main.h>
#include <Utils.h>

/*
  FocalCalibration.ino
  ---------------------------------------------------------------------------
  On-device FOCAL_PX calibration for the optical rotation tracker.

  IDEA
    The gyro is the ground truth for "how far did I really turn". The camera
    reports the same turn in pixels / FOCAL_PX. For pan and tilt, the angle the
    tracker reports is proportional to 1 / FOCAL_PX, so after one test turn:

        FOCAL_PX_new = FOCAL_PX_used_by_tracker * (vision angle / gyro angle)

    Roll about the optical axis does NOT depend on focal length, so it is
    ignored for the estimate (only reported as a cross-check).

  WIRING / ASSUMPTIONS (edit the CONFIG block if yours differ)
    - This sketch runs on the MAIN ESP32 that has the ICM-20948 on I2C.
    - The ESP32-CAM running the tracker prints one line per frame, in the
      format your serial parser already expects:
          "%.5f %.5f %.5f %.4f %d %.5f\n"
          dtheta_x dtheta_y dtheta_z dt inliers residual
      and is wired to this board's UART2 RX (CAM_RX_PIN) with a common GND.
      If the camera data reaches this board some other way, only
      pollCamera() needs to change.
    - Body frame: +X right, +Y forward (boresight), +Z up. Gyro remap is the
      same unified one used in the EKF: (-rawY, -rawX, -rawZ).
    - Camera-line -> body mapping (visionToBody) is the one derived from your
      logs: body = (m1, -m3, m2).

  HOW TO USE
    1. Set CURRENT_FOCAL_PX to the FOCAL_PX the tracker is currently running.
    2. Power up and keep the device STILL for ~4 s (gyro bias is measured).
    3. Rotate the device about ONE axis only (yaw OR pitch) by roughly
       90-180 degrees, SLOWLY (10-30 deg/s), and stop. The run starts and
       ends automatically when motion starts/stops.
    4. Read the result on Serial Monitor (115200). Repeat several times,
       both axes and both directions. The running weighted average is the
       number to use.
    5. Put the value into the tracker's FOCAL_PX, flash it, and run the
       check again: the ratio should come out near 1.00.

    Serial commands:   b = re-measure gyro bias (keep still)
                       r = clear collected runs
                       f<value> = change CURRENT_FOCAL_PX (e.g. f87.5), clears runs

  Tip: for the cleanest result run the tracker WITHOUT a gyro prediction seed
  (or at least with slow turns), so LK search-window clipping can't bias the
  vision total low.
*/

class FocalCalibrator {
public:
  const int   MIN_INLIERS    = 5;      // ignore vision lines with fewer
  const float START_RATE_DPS = 8.0f;   // motion starts above this
  const float STOP_RATE_DPS  = 3.0f;   // "at rest" below this
  const float HOLD_S         = 1.0f;   // rest time that ends a run
  const float MIN_RUN_DEG    = 30.0f;  // smaller turns are discarded
  const float WARN_RATE_DPS  = 40.0f;  // faster than this risks LK losses
  const float R2D = 57.2957795f;

  float CURRENT_FOCAL_PX = 110.0f;

  Vector3 gyroBias = {0, 0, 0};
  Vector3     gSum = {0, 0, 0};   // gyro angle, rad
  Vector3     vSum = {0, 0, 0};   // vision angle, rad (body axes)
  int    vFrames = 0;
  float  inlierSum = 0;
  float  peakRate = 0;
  bool   moving = false, running = false;
  float  restTimer = 0;
  double sumW = 0, sumWF = 0, sumWF2 = 0, pathLen;
  char buffer[256];

  FocalCalibrator(float currentFocalPX) {
    CURRENT_FOCAL_PX = currentFocalPX;
  }

  void resetSegment() {
    gSum = {0, 0, 0};
    vSum = {0, 0, 0};
    vFrames = 0;
    inlierSum = 0;
    peakRate = 0;
  }

  void init(Vector3 bias) {    
    gyroBias = bias;
    resetSegment();
    running = true;
    snprintf(buffer, 256, "\nReady. Cam FOCAL_PX assumed = %.2f. Rotate about ONE axis, slowly, 90-180 deg, then stop.", CURRENT_FOCAL_PX);
    serialPrintln(buffer);
  }

  void update(const RotationEstimate &est, bool freshVision, const Vector3 &gyroBody, float dt) {
    Vector3 w = gyroBody * R2D;

    float rate = sqrtf(w.x*w.x + w.y*w.y + w.z*w.z);

    Serial.println(rate);

    if (freshVision && est.inlier_count >= MIN_INLIERS && est.dt_seconds > 0.0f && est.dt_seconds < 0.5f) {
      vSum.x += est.dtheta_y;      // pitch (body X)
      vSum.y += -est.dtheta_x;     // roll  (body Y)
      vSum.z += est.dtheta_z;      // yaw   (body Z)
      vFrames++;
      inlierSum += est.inlier_count;
    }

    gSum.x += w.x * dt;
    gSum.y += w.y * dt;
    gSum.z += w.z * dt;
    pathLen += rate * dt;
    if (rate > peakRate) peakRate = rate;

    if (rate < STOP_RATE_DPS) restTimer += dt; else restTimer = 0.0f;

    if (!moving) {
      // Keep only a short pre-roll while at rest so the start of the turn isn't lost.
      if (restTimer > 0.3f) resetSegment();
      if (rate > START_RATE_DPS) {
        moving = true;
        serialPrintln("Motion detected...");
      }
    } else if (restTimer > HOLD_S) {
      running = false;
      moving = false;
      Serial.println(pathLen);
      finishRun();
    }
  }

  void finishRun() {
    float GX = gSum.x, GY = gSum.y, GZ = gSum.z;  
    float VX = vSum.x * R2D, VY = vSum.y * R2D, VZ = vSum.z * R2D;
    float G = sqrtf(GX * GX + GZ * GZ);   // pan/tilt turn magnitude

    serialPrintln("-- Complete --");
    snprintf(buffer, 256, "gyro   deg  X %3.2f  Y %3.2f  Z %3.2f", GX, GY, GZ);
    serialPrintln(buffer);
    snprintf(buffer, 256, "vision deg  X %3.2f  Y %3.2f  Z %3.2f\n(%d frames, mean inliers %.1f)", VX, VY, VZ, vFrames, vFrames ? inlierSum / vFrames : 0.0f);
    serialPrintln(buffer);
    snprintf(buffer, 256, "peak rate %.1f deg/s", peakRate);
    serialPrintln(buffer);

    if (G < MIN_RUN_DEG) {
      snprintf(buffer, 256, "Discarded: pan/tilt turn only %.1f deg (need >= %.0f).", G, MIN_RUN_DEG);
      serialPrintln(buffer);
      return;
    }
    if (vFrames < 5) {
      serialPrintln("Discarded: almost no vision frames arrived. Check camera UART wiring/baud.");
      return;
    }
    if (fabsf(GY) > 0.3f * G)
      serialPrintln("WARNING: Excessive roll detected, rotate about yaw or pitch only. Rety");
    if (peakRate > WARN_RATE_DPS)
      serialPrintln("WARNING: Excessive rotation speed detected, rotate slower. Retry");

    if (fabsf(GX) > 10.0f) {
      snprintf(buffer, 256, "pitch ratio (vision/gyro) = %.3f", VX / GX);
      serialPrintln(buffer);
    }
    if (fabsf(GZ) > 10.0f) {
      snprintf(buffer, 256, "yaw   ratio (vision/gyro) = %.3f", VZ / GZ);
      serialPrintln(buffer);
    }
    if (fabsf(GY) > 10.0f) {
      snprintf(buffer, 256, "roll  ratio (vision/gyro) = %.3f   (not used; focal independent)", VY / GY);
      serialPrintln(buffer);
    }

    float r = (VX * GX + VZ * GZ) / (G * G);   // projection of vision onto gyro, pan/tilt plane
    if (r <= 0.0f) {
      snprintf(buffer, 256, "Discarded: ratio %.3f is not positive. Vision and gyro disagree in SIGN on the turned axis", r);
      serialPrintln(buffer);
      return;
    }

    float f = CURRENT_FOCAL_PX * r;
    sumW += G;
    sumWF += (double)G * f;
    sumWF2 += (double)G * f * f;

    double mean = sumWF / sumW;
    double sd = sqrt(fmax(0.0, sumWF2 / sumW - mean * mean));

    snprintf(buffer, 256, "ratio: %.3f, FOCAL_PX: %.2f", r, f);
    serialPrintln(buffer);
    if (r < 0.15f || r > 3.0f)
      serialPrintln("Warning: ratio is far from 1 - suspect tracking losses (fast turn, blur, low texture), not just focal.");
  }
};