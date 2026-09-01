#pragma once

static const int FRAME_W = 160;                         // QQVGA
static const int FRAME_H = 120;                         //
static const int GRID_COLS = 5;                         // feature grid: one candidate point per cell
static const int GRID_ROWS = 4;                         //
static const int MAX_FEATURES = GRID_COLS * GRID_ROWS;  // 20
static const int CELL_MARGIN = 4;                       // px border inside each cell to avoid edges
static const int LK_WINDOW = 3;                         // half-window size -> (2*LK_WINDOW+1)^2 patch
static const int LK_ITERATIONS = 3;                     // refinement iterations per point
static const int SEARCH_RADIUS = 6;                     // px, around the gyro-predicted location
static const float OUTLIER_KEEP_FRACTION = 0.7f;        // keep best 70% after first fit
static float FOCAL_PX = 119.0f;
static float CX = FRAME_W / 2.0f;
static float CY = FRAME_H / 2.0f;

struct RotationEstimate
{
    bool valid;
    float dtheta_x; // radians, small-angle delta-rotation about camera X
    float dtheta_y; // radians, about camera Y
    float dtheta_z; // radians, about camera Z (optical axis / roll)
    float dt_seconds;
    int inlier_count;
    float residual_rms; // normalized image units
};

struct FeaturePoint
{
    float x, y;                 // sub-pixel location in previous frame
    float pred_x, pred_y;       // gyro-predicted location in current frame
    float tracked_x, tracked_y; // LK-refined location in current frame
    bool track_ok;
    float residual; // filled in during rotation fit
};

// ---------------------------------------------------------------------------
// OpticalRotationTracker
// ---------------------------------------------------------------------------
class OpticalRotationTracker
{
public:
    bool init()
    {
        haveInitialFrame = false;
        predWx = predWy = predWz = 0.0f;
        return true;
    }

    // Call with your gyro-integrated rotation (radians, small-angle) since the
    // last successful call to update(). This seeds the LK search location.
    void setGyroPrediction(float dtheta_x, float dtheta_y, float dtheta_z)
    {
        applyBodyToCameraRotation(dtheta_x, dtheta_y, dtheta_z);
        predWx = dtheta_x;
        predWy = dtheta_y;
        predWz = dtheta_z;
    }

    // Captures a frame, tracks against the previous frame, and returns the
    // rotation estimate. Call at your desired vision-correction rate (1-5 Hz
    // is plenty per the design discussion this file accompanies).
    RotationEstimate update()
    {
        RotationEstimate result = {false, 0, 0, 0, 0, 0, 0};

        camera_fb_t *fb = esp_camera_fb_get();
        if (!fb || fb->format != PIXFORMAT_GRAYSCALE ||
            fb->width != FRAME_W || fb->height != FRAME_H)
        {
            if (fb)
                esp_camera_fb_return(fb);
            return result;
        }

        uint32_t nowUs = micros();

        if (!haveInitialFrame)
        {
            // First frame: just seed the buffer and detect features, nothing to
            // track against yet.
            memcpy(prevFrame, fb->buf, FRAME_W * FRAME_H);
            esp_camera_fb_return(fb);
            detectFeatures(prevFrame);
            prevTimestampUs = nowUs;
            haveInitialFrame = true;
            return result; // valid stays false
        }

        memcpy(currFrame, fb->buf, FRAME_W * FRAME_H);
        esp_camera_fb_return(fb);

        float dt = (nowUs - prevTimestampUs) * 1.0e-6f;
        if (dt <= 0.0f)
            dt = 1.0f / 30.0f; // guard against timer wraparound edge case

        trackFeatures();
        result = fitRotation(dt);

        // Roll the buffers: current becomes previous for next cycle, and we
        // re-detect features fresh each cycle (cheap at this resolution, and
        // avoids slowly losing all your tracks to drift/occlusion).
        memcpy(prevFrame, currFrame, FRAME_W * FRAME_H);
        detectFeatures(prevFrame);
        prevTimestampUs = nowUs;

        return result;
    }

private:
    uint8_t prevFrame[FRAME_W * FRAME_H];
    uint8_t currFrame[FRAME_W * FRAME_H];
    bool haveInitialFrame;
    uint32_t prevTimestampUs;

    FeaturePoint features[MAX_FEATURES];
    int numFeatures;

    float predWx, predWy, predWz; // gyro-predicted delta-rotation since last frame

    static inline uint8_t px(const uint8_t *img, int x, int y)
    {
        if (x < 0)
            x = 0;
        if (x >= FRAME_W)
            x = FRAME_W - 1;
        if (y < 0)
            y = 0;
        if (y >= FRAME_H)
            y = FRAME_H - 1;
        return img[y * FRAME_W + x];
    }

    // Sobel gradient magnitude at a pixel (cheap 3x3, integer math).
    static inline int gradMag(const uint8_t *img, int x, int y)
    {
        int gx = (px(img, x + 1, y - 1) + 2 * px(img, x + 1, y) + px(img, x + 1, y + 1)) - (px(img, x - 1, y - 1) + 2 * px(img, x - 1, y) + px(img, x - 1, y + 1));
        int gy = (px(img, x - 1, y + 1) + 2 * px(img, x, y + 1) + px(img, x + 1, y + 1)) - (px(img, x - 1, y - 1) + 2 * px(img, x, y - 1) + px(img, x + 1, y - 1));
        return abs(gx) + abs(gy); // L1 norm, cheap and good enough for ranking
    }

    // Grid-based feature detection: pick the highest-gradient pixel in each
    // grid cell. Keeps features spatially spread out, which matters for
    // conditioning the rotation fit (especially the roll/theta_z axis).
    void detectFeatures(const uint8_t *img)
    {
        numFeatures = 0;
        int cellW = FRAME_W / GRID_COLS;
        int cellH = FRAME_H / GRID_ROWS;

        for (int gy = 0; gy < GRID_ROWS; gy++)
        {
            for (int gx = 0; gx < GRID_COLS; gx++)
            {
                int x0 = gx * cellW + CELL_MARGIN;
                int x1 = (gx + 1) * cellW - CELL_MARGIN;
                int y0 = gy * cellH + CELL_MARGIN;
                int y1 = (gy + 1) * cellH - CELL_MARGIN;

                int bestX = -1, bestY = -1, bestScore = -1;
                for (int y = y0; y < y1; y++)
                {
                    for (int x = x0; x < x1; x++)
                    {
                        int score = gradMag(img, x, y);
                        if (score > bestScore)
                        {
                            bestScore = score;
                            bestX = x;
                            bestY = y;
                        }
                    }
                }

                // Skip near-flat cells (low texture) — they'll just be noise.
                static const int MIN_GRADIENT = 40;
                if (bestScore >= MIN_GRADIENT && numFeatures < MAX_FEATURES)
                {
                    features[numFeatures].x = (float)bestX;
                    features[numFeatures].y = (float)bestY;
                    numFeatures++;
                }
            }
        }
    }

    // Predicts where a point should land in the current frame, given the
    // gyro-integrated rotation since the last frame, using the same
    // rotation-flow model used in the fit (see fitRotation for derivation).
    void predictPoint(float px0, float py0, float &predX, float &predY)
    {
        float x = (px0 - CX) / FOCAL_PX;
        float y = (py0 - CY) / FOCAL_PX;

        float u = x * y * predWx - (1.0f + x * x) * predWy + y * predWz;
        float v = (1.0f + y * y) * predWx - x * y * predWy - x * predWz;

        predX = px0 + u * FOCAL_PX;
        predY = py0 + v * FOCAL_PX;
    }

    // Single-scale iterative Lucas-Kanade refinement around a predicted
    // location. Small window by design — the gyro prediction is what makes
    // that workable even for large inter-frame rotations.
    bool refineLK(const uint8_t *prevImg, const uint8_t *currImg,
                  float startX, float startY, float seedX, float seedY,
                  float &outX, float &outY)
    {
        float curX = seedX, curY = seedY;

        for (int iter = 0; iter < LK_ITERATIONS; iter++)
        {
            double sumIxIx = 0, sumIxIy = 0, sumIyIy = 0;
            double sumIxIt = 0, sumIyIt = 0;
            int samples = 0;

            int cx = (int)roundf(curX), cy = (int)roundf(curY);
            int px0 = (int)roundf(startX), py0 = (int)roundf(startY);

            // Bail out if the search has wandered outside our budgeted radius —
            // treat as a failed track rather than chasing it further.
            if (abs(cx - px0) > SEARCH_RADIUS + LK_WINDOW ||
                abs(cy - py0) > SEARCH_RADIUS + LK_WINDOW)
            {
                return false;
            }

            for (int dy = -LK_WINDOW; dy <= LK_WINDOW; dy++)
            {
                for (int dx = -LK_WINDOW; dx <= LK_WINDOW; dx++)
                {
                    int sx = px0 + dx, sy = py0 + dy; // sample location in prev frame
                    int tx = cx + dx, ty = cy + dy;   // corresponding location in curr frame

                    float Ix = 0.5f * (px(prevImg, sx + 1, sy) - px(prevImg, sx - 1, sy));
                    float Iy = 0.5f * (px(prevImg, sx, sy + 1) - px(prevImg, sx, sy - 1));
                    float It = (float)px(currImg, tx, ty) - (float)px(prevImg, sx, sy);

                    sumIxIx += Ix * Ix;
                    sumIxIy += Ix * Iy;
                    sumIyIy += Iy * Iy;
                    sumIxIt += Ix * It;
                    sumIyIt += Iy * It;
                    samples++;
                }
            }

            double det = sumIxIx * sumIyIy - sumIxIy * sumIxIy;
            if (fabs(det) < 1e-6)
            {
                return false; // degenerate window (flat/aperture problem) — no fix
            }

            // Solve 2x2 system: [Ix.Ix Ix.Iy; Ix.Iy Iy.Iy] * [du;dv] = -[Ix.It; Iy.It]
            double du = (-sumIyIy * sumIxIt + sumIxIy * sumIyIt) / det;
            double dv = (sumIxIy * sumIxIt - sumIxIx * sumIyIt) / det;

            curX += (float)du;
            curY += (float)dv;

            if (fabs(du) < 0.05 && fabs(dv) < 0.05)
                break; // converged
        }

        outX = curX;
        outY = curY;
        return true;
    }

    void trackFeatures()
    {
        for (int i = 0; i < numFeatures; i++)
        {
            FeaturePoint &f = features[i];
            predictPoint(f.x, f.y, f.pred_x, f.pred_y);

            // Clamp predicted location to stay within frame bounds before search.
            float seedX = f.pred_x, seedY = f.pred_y;
            if (seedX < LK_WINDOW)
                seedX = LK_WINDOW;
            if (seedX > FRAME_W - 1 - LK_WINDOW)
                seedX = FRAME_W - 1 - LK_WINDOW;
            if (seedY < LK_WINDOW)
                seedY = LK_WINDOW;
            if (seedY > FRAME_H - 1 - LK_WINDOW)
                seedY = FRAME_H - 1 - LK_WINDOW;

            float outX, outY;
            f.track_ok = refineLK(prevFrame, currFrame, f.x, f.y, seedX, seedY, outX, outY);
            if (f.track_ok)
            {
                f.tracked_x = outX;
                f.tracked_y = outY;
            }
        }
    }

    // Fits the rotation-only flow model to all successfully tracked points:
    //   u_norm = x*y*theta_x - (1+x^2)*theta_y + y*theta_z
    //   v_norm = (1+y^2)*theta_x - x*y*theta_y - x*theta_z
    // where (x,y) are focal-length-normalized coordinates of the point in the
    // previous frame, and (u_norm, v_norm) is the observed normalized flow.
    // This is the standard first-order (small-angle) rotational flow model —
    // valid because we predicted+searched near the true location, so residual
    // per-step rotation between prediction and truth is small even if the
    // total rotation since the last frame was not.
    //
    // Solved via least squares (normal equations, 3x3 solve), then refined
    // once more after dropping the worst-residual outliers.
    RotationEstimate fitRotation(float dt)
    {
        RotationEstimate result = {false, 0, 0, 0, dt, 0, 0};

        // Build the list of usable correspondences.
        static const int MAXN = MAX_FEATURES;
        float X[MAXN], Y[MAXN], U[MAXN], V[MAXN];
        int idxMap[MAXN]; // maps compact array index -> features[] index
        int n = 0;

        for (int i = 0; i < numFeatures; i++)
        {
            if (!features[i].track_ok)
                continue;
            float x = (features[i].x - CX) / FOCAL_PX;
            float y = (features[i].y - CY) / FOCAL_PX;
            float u = (features[i].tracked_x - features[i].x) / FOCAL_PX;
            float v = (features[i].tracked_y - features[i].y) / FOCAL_PX;
            X[n] = x;
            Y[n] = y;
            U[n] = u;
            V[n] = v;
            idxMap[n] = i;
            n++;
        }

        // Need at least 3 well-spread points to solve a 3-unknown system
        // meaningfully; require a bit more margin for noise tolerance.
        static const int MIN_POINTS = 5;
        if (n < MIN_POINTS)
        {
            result.inlier_count = n;
            return result; // valid stays false — fusion filter should fall back to gyro-only
        }

        float theta[3];
        float residuals[MAXN];
        if (!solveLeastSquares(X, Y, U, V, n, theta, residuals))
        {
            result.inlier_count = n;
            return result;
        }

        // Outlier rejection: keep the best OUTLIER_KEEP_FRACTION of points by
        // residual, refit once. Simple and cheap — a full RANSAC isn't needed
        // at this point count.
        int keepCount = (int)(n * OUTLIER_KEEP_FRACTION);
        if (keepCount < MIN_POINTS)
            keepCount = (n < MIN_POINTS) ? n : MIN_POINTS;

        int order[MAXN];
        for (int i = 0; i < n; i++)
            order[i] = i;
        // simple insertion sort by residual (n is small, <= MAX_FEATURES=20)
        for (int i = 1; i < n; i++)
        {
            int key = order[i];
            float keyVal = residuals[key];
            int j = i - 1;
            while (j >= 0 && residuals[order[j]] > keyVal)
            {
                order[j + 1] = order[j];
                j--;
            }
            order[j + 1] = key;
        }

        float X2[MAXN], Y2[MAXN], U2[MAXN], V2[MAXN];
        for (int i = 0; i < keepCount; i++)
        {
            int k = order[i];
            X2[i] = X[k];
            Y2[i] = Y[k];
            U2[i] = U[k];
            V2[i] = V[k];
        }

        float theta2[3];
        float residuals2[MAXN];
        if (!solveLeastSquares(X2, Y2, U2, V2, keepCount, theta2, residuals2))
        {
            result.inlier_count = n;
            return result;
        }

        float rmsSum = 0;
        for (int i = 0; i < keepCount; i++)
            rmsSum += residuals2[i] * residuals2[i];
        float rms = sqrtf(rmsSum / keepCount);

        applyCameraMountRotation(theta2[0], theta2[1], theta2[2]);

        result.valid = true;
        result.dtheta_x = theta2[0];
        result.dtheta_y = theta2[1];
        result.dtheta_z = theta2[2];
        result.dt_seconds = dt;
        result.inlier_count = keepCount;
        result.residual_rms = rms;
        return result;
    }

    // Solves the linear least-squares system for theta = [theta_x,theta_y,theta_z]
    // given n point correspondences, via normal equations (A^T A) theta = A^T b,
    // solved with a direct 3x3 Cramer's-rule inverse. Also fills per-point
    // residual magnitude (used for outlier ranking).
    bool solveLeastSquares(const float *X, const float *Y, const float *U, const float *V,
                           int n, float theta[3], float *residualsOut)
    {
        // Each point contributes two rows:
        //   row_u: [ x*y , -(1+x^2) ,  y ]  . theta = u
        //   row_v: [ (1+y^2) , -x*y , -x ] . theta = v
        double ATA[3][3] = {{0}};
        double ATb[3] = {0};

        for (int i = 0; i < n; i++)
        {
            double x = X[i], y = Y[i], u = U[i], v = V[i];

            double au[3] = {x * y, -(1.0 + x * x), y};
            double av[3] = {(1.0 + y * y), -x * y, -x};

            for (int r = 0; r < 3; r++)
            {
                for (int c = 0; c < 3; c++)
                {
                    ATA[r][c] += au[r] * au[c] + av[r] * av[c];
                }
                ATb[r] += au[r] * u + av[r] * v;
            }
        }

        double det =
            ATA[0][0] * (ATA[1][1] * ATA[2][2] - ATA[1][2] * ATA[2][1]) - ATA[0][1] * (ATA[1][0] * ATA[2][2] - ATA[1][2] * ATA[2][0]) + ATA[0][2] * (ATA[1][0] * ATA[2][1] - ATA[1][1] * ATA[2][0]);

        if (fabs(det) < 1e-9)
            return false; // degenerate (e.g. all points collinear)

        double inv[3][3];
        inv[0][0] = (ATA[1][1] * ATA[2][2] - ATA[1][2] * ATA[2][1]) / det;
        inv[0][1] = -(ATA[0][1] * ATA[2][2] - ATA[0][2] * ATA[2][1]) / det;
        inv[0][2] = (ATA[0][1] * ATA[1][2] - ATA[0][2] * ATA[1][1]) / det;
        inv[1][0] = -(ATA[1][0] * ATA[2][2] - ATA[1][2] * ATA[2][0]) / det;
        inv[1][1] = (ATA[0][0] * ATA[2][2] - ATA[0][2] * ATA[2][0]) / det;
        inv[1][2] = -(ATA[0][0] * ATA[1][2] - ATA[0][2] * ATA[1][0]) / det;
        inv[2][0] = (ATA[1][0] * ATA[2][1] - ATA[1][1] * ATA[2][0]) / det;
        inv[2][1] = -(ATA[0][0] * ATA[2][1] - ATA[0][1] * ATA[2][0]) / det;
        inv[2][2] = (ATA[0][0] * ATA[1][1] - ATA[0][1] * ATA[1][0]) / det;

        for (int r = 0; r < 3; r++)
        {
            theta[r] = (float)(inv[r][0] * ATb[0] + inv[r][1] * ATb[1] + inv[r][2] * ATb[2]);
        }

        if (residualsOut)
        {
            for (int i = 0; i < n; i++)
            {
                double x = X[i], y = Y[i], u = U[i], v = V[i];
                double predU = x * y * theta[0] - (1.0 + x * x) * theta[1] + y * theta[2];
                double predV = (1.0 + y * y) * theta[0] - x * y * theta[1] - x * theta[2];
                double du = u - predU, dv = v - predV;
                residualsOut[i] = (float)sqrt(du * du + dv * dv);
            }
        }
        return true;
    }

    void rotateXYByDegrees(float &tx, float &ty, int degrees)
    {
        float outX, outY;
        switch (((degrees % 360) + 360) % 360)
        {
        case 90:
            outX = -ty;
            outY = tx;
            break;
        case 180:
            outX = -tx;
            outY = -ty;
            break;
        case 270:
            outX = ty;
            outY = -tx;
            break;
        default: // 0
            outX = tx;
            outY = ty;
            break;
        }
        tx = outX;
        ty = outY;
    }

    void applyCameraMountRotation(float &tx, float &ty, float &tz)
    {
        (void)tz;
        rotateXYByDegrees(tx, ty, 90);
    }

    void applyBodyToCameraRotation(float &tx, float &ty, float &tz)
    {
        (void)tz;
        rotateXYByDegrees(tx, ty, -90);
    }
};