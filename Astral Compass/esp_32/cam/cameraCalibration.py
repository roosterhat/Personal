"""
calibrate_camera.py
--------------------------------------------------------------------------
Derives camera intrinsics (fx, fy, cx, cy) and distortion coefficients from
a set of checkerboard images, for use as FOCAL_PX / CX / CY in
OpticalRotationTracker.ino.

Usage:
    pip install opencv-python numpy
    python calibrate_camera.py --images "captures/*.jpg" --pattern 6x4 --square-size 25

    --pattern is the number of INTERNAL corners (squares - 1 per side),
    e.g. a 7x5-square board has a 6x4 pattern.
    --square-size is the real-world size of one square, in mm (only affects
    absolute scale reporting — fx/fy/cx/cy in pixels are unaffected by it).

Output:
    Prints the camera matrix, distortion coefficients, reprojection error,
    and the exact constants to paste into the .ino file.
"""

import argparse
import glob
import cv2
import numpy as np
from PIL import Image


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--images", required=True,
                         help="Glob pattern for calibration images, e.g. 'captures/*.jpg'")
    parser.add_argument("--pattern", required=True,
                         help="Internal corner count as COLSxROWS, e.g. 6x4")
    parser.add_argument("--square-size", type=float, default=25.0,
                         help="Real-world square size in mm (default 25)")
    parser.add_argument("--target-width", type=int, default=None,
                         help="If your calibration images are NOT at your tracker's "
                              "resolution, pass the tracker's frame width (e.g. 160) "
                              "to scale fx/fy/cx/cy down proportionally.")
    args = parser.parse_args()

    cols, rows = (int(v) for v in args.pattern.lower().split("x"))
    pattern_size = (cols, rows)

    # Prepare object points, e.g. (0,0,0), (1,0,0), (2,0,0), ... in square units,
    # scaled to real-world mm.
    objp = np.zeros((cols * rows, 3), np.float32)
    objp[:, :2] = np.mgrid[0:cols, 0:rows].T.reshape(-1, 2)
    objp *= args.square_size

    objpoints = []  # 3D points in real-world space
    imgpoints = []  # 2D points in image plane
    image_size = None

    files = sorted(glob.glob(args.images))
    if not files:
        raise SystemExit(f"No images matched: {args.images}")

    used = 0
    for fname in files:
        img = cv2.imread(fname)
        if img is None:
            print(f"  skip (unreadable): {fname}")
            continue
        lwr = np.array(0)
        upr = np.array(200)
        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        image_size = gray.shape[::-1]  # (width, height)
        msk = cv2.inRange(gray, lwr, upr)        

        # Extract chess-board
        krn = cv2.getStructuringElement(cv2.MORPH_RECT, (3,3))
        dlt = cv2.dilate(msk, krn, iterations=1)
        erd = cv2.erode(msk, krn)
        res = 255 - cv2.bitwise_and(erd, msk)
        #res = np.uint8(res)        
        res = np.uint8(255 - msk)

        #if "capture.jpg" in fname:
        Image.fromarray(res).show()

        found, corners = cv2.findChessboardCorners( res, pattern_size)

        if not found:
            print(f"  no checkerboard found: {fname}")
            continue

        Image.fromarray(cv2.drawChessboardCorners(img, pattern_size, corners, found)).show()

        # Refine corner locations to sub-pixel accuracy.
        criteria = (cv2.TERM_CRITERIA_EPS + cv2.TERM_CRITERIA_MAX_ITER, 30, 0.001)
        corners = cv2.cornerSubPix(res, corners, (5, 5), (-1, -1), criteria)

        objpoints.append(objp)
        imgpoints.append(corners)
        used += 1
        print(f"  ok: {fname}")

    if used < 1:
        print(f"\nWarning: only {used} usable images. 15-20+ spread across the "
              f"frame (center, edges, corners, varied tilt) gives a much more "
              f"reliable result.")
        exit(0)

    print(f"\nCalibrating from {used} images at resolution {image_size} ...")
    reproj_err, camera_matrix, dist_coeffs, rvecs, tvecs = cv2.calibrateCamera(
        objpoints, imgpoints, image_size, None, None
    )

    fx = camera_matrix[0, 0]
    fy = camera_matrix[1, 1]
    cx = camera_matrix[0, 2]
    cy = camera_matrix[1, 2]

    print("\n--- Calibration result ---")
    print(f"Resolution used for calibration : {image_size[0]}x{image_size[1]}")
    print(f"fx, fy                           : {fx:.3f}, {fy:.3f}")
    print(f"cx, cy                           : {cx:.3f}, {cy:.3f}")
    print(f"Distortion coeffs (k1,k2,p1,p2,k3): {dist_coeffs.ravel()}")
    print(f"Mean reprojection error (px)     : {reproj_err:.4f}  "
          f"(under ~0.5 is good, over ~1.0 means retake images)")

    out_w, out_h = image_size
    out_fx, out_fy, out_cx, out_cy = fx, fy, cx, cy

    if args.target_width and args.target_width != image_size[0]:
        scale = args.target_width / image_size[0]
        out_fx *= scale
        out_fy *= scale
        out_cx *= scale
        out_cy *= scale
        out_w = args.target_width
        out_h = round(image_size[1] * scale)
        print(f"\nScaled to target width {args.target_width} "
              f"(assumes same aspect/no sensor cropping between frame sizes):")
        print(f"  fx, fy at {out_w}x{out_h} : {out_fx:.3f}, {out_fy:.3f}")
        print(f"  cx, cy at {out_w}x{out_h} : {out_cx:.3f}, {out_cy:.3f}")

    print("\n--- Paste into OpticalRotationTracker.ino ---")
    print(f"static float FOCAL_PX = {(out_fx + out_fy) / 2:.2f}f;  // average of fx, fy")
    print(f"static float CX = {out_cx:.2f}f;")
    print(f"static float CY = {out_cy:.2f}f;")

    max_diff_pct = abs(out_fx - out_fy) / ((out_fx + out_fy) / 2) * 100
    if max_diff_pct > 3:
        print(f"\nNote: fx and fy differ by {max_diff_pct:.1f}% — pixels aren't quite "
              f"square, or the checkerboard set didn't constrain both axes evenly. "
              f"The rotation-flow model in the tracker assumes a single focal length; "
              f"the average is a reasonable approximation unless this gap is large.")


if __name__ == "__main__":
    main()