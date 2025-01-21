const dataURLPattern = new RegExp('data:(?<type>[^;]+);(?<encoding>[^,]+),(?<data>.+)=', 'g')
const transformPattern = new RegExp('(?<action>\\w+)\\((?<params>[^\\)]+)\\)', 'g')
const pathPattern = new RegExp('(?<type>[a-z])(?<params>[\\s\\d\\.,-]+)?', 'gi')
const numberPattern = new RegExp('(-?\\d+(?:\\.\\d+)?)|(-?(?:\\.\\d+))', 'g')

function delay(ms) {
  return new Promise((resolve, reject) => setTimeout(resolve, ms))
}

function decodeDataURL(dataURL) {
    try {
        console.log(dataURL)
        const match = dataURLPattern.exec(dataURL)
        console.log(match)
        return atob(match.groups['data'])
    }
    catch (ex) {
        console.log(ex)
        return null
    }
}

function request(endpoint, method = "GET", body = null, headers = {}) {
    return new Promise(async (resolve, reject) => {
        try{
            var response = await fetch(`http://${window.location.hostname}:3000/${endpoint}`, {
                method: method,
                headers: headers,
                body: body
            });
            resolve(response);
        }
        catch(ex) {
            reject(ex);
        }
    });
}

function endpointToCenterParameterization(x1, y1, x2, y2, srx, sry, xAxisRotationDeg, largeArcFlag, sweepFlag) {
    const xAxisRotation = degToRad(xAxisRotationDeg);

    const cosphi = Math.cos(xAxisRotation);
    const sinphi = Math.sin(xAxisRotation);

    const [x1p, y1p] = mat2DotVec2(
        [cosphi, sinphi, -sinphi, cosphi],
        [(x1 - x2) / 2, (y1 - y2) / 2]
    );

    const [rx, ry] = correctRadii(srx, sry, x1p, y1p);

    const sign = largeArcFlag !== sweepFlag ? 1 : -1;
    const n = Math.pow(rx, 2) * Math.pow(ry, 2) - Math.pow(rx, 2) * Math.pow(y1p, 2) - Math.pow(ry, 2) * Math.pow(x1p, 2);
    const d = Math.pow(rx, 2) * Math.pow(y1p, 2) + Math.pow(ry, 2) * Math.pow(x1p, 2);

    const [cxp, cyp] = vec2Scale(
        [(rx * y1p) / ry, (-ry * x1p) / rx],
        sign * Math.sqrt(Math.abs(n / d))
    );

    const [cx, cy] = vec2Add(
        mat2DotVec2([cosphi, -sinphi, sinphi, cosphi], [cxp, cyp]),
        [(x1 + x2) / 2, (y1 + y2) / 2]
    );

    const a = [(x1p - cxp) / rx, (y1p - cyp) / ry];
    const b = [(-x1p - cxp) / rx, (-y1p - cyp) / ry];
    const startAngle = vec2Angle([1, 0], a);
    const deltaAngle0 = vec2Angle(a, b) % (2 * Math.PI);

    const deltaAngle =
        !sweepFlag && deltaAngle0 > 0
            ? deltaAngle0 - 2 * Math.PI
            : sweepFlag && deltaAngle0 < 0
                ? deltaAngle0 + 2 * Math.PI
                : deltaAngle0;

    const endAngle = startAngle + deltaAngle;

    function correctRadii(signedRx, signedRy, x1p, y1p) {
        const prx = Math.abs(signedRx);
        const pry = Math.abs(signedRy);

        const A = Math.pow(x1p, 2) / Math.pow(prx, 2) + Math.pow(y1p, 2) / Math.pow(pry, 2);

        const rx = A > 1 ? Math.sqrt(A) * prx : prx;
        const ry = A > 1 ? Math.sqrt(A) * pry : pry;

        return [rx, ry];
    }

    function mat2DotVec2([m00, m01, m10, m11], [vx, vy]) {
        return [m00 * vx + m01 * vy, m10 * vx + m11 * vy];
    }

    function vec2Add([ux, uy], [vx, vy]) {
        return [ux + vx, uy + vy];
    }

    function vec2Scale([a0, a1], scalar) {
        return [a0 * scalar, a1 * scalar];
    }

    function vec2Dot([ux, uy], [vx, vy]) {
        return ux * vx + uy * vy;
    }

    function vec2Mag([ux, uy]) {
        return Math.sqrt(ux ** 2 + uy ** 2);
    }

    function vec2Angle(u, v) {
        const [ux, uy] = u;
        const [vx, vy] = v;
        const sign = ux * vy - uy * vx >= 0 ? 1 : -1;
        return sign * Math.acos(vec2Dot(u, v) / (vec2Mag(u) * vec2Mag(v)));
    }

    function degToRad(deg) {
        return (deg * Math.PI) / 180;
    }

    return {
        cx,
        cy,
        rx,
        ry,
        startAngle,
        endAngle,
        xAxisRotation,
        anitClockwise: deltaAngle < 0
    };
}


// https://github.com/colinmeinke/svg-arc-to-cubic-bezier
//
// Convert an arc to a sequence of cubic bézier curves
//


const TAU = Math.PI * 2;


/* eslint-disable space-infix-ops */

// Calculate an angle between two unit vectors
//
// Since we measure angle between radii of circular arcs,
// we can use simplified math (without length normalization)
//
function unit_vector_angle(ux, uy, vx, vy) {
  const sign = (ux * vy - uy * vx < 0) ? -1 : 1;
  let dot = ux * vx + uy * vy;

  // Add this to work with arbitrary vectors:
  // dot /= Math.sqrt(ux * ux + uy * uy) * Math.sqrt(vx * vx + vy * vy);

  // rounding errors, e.g. -1.0000000000000002 can screw up this
  if(dot > 1.0) { dot = 1.0 }
  if(dot < -1.0) { dot = -1.0 }

  return sign * Math.acos(dot);
}


// Convert from endpoint to center parameterization,
// see http://www.w3.org/TR/SVG11/implnote.html#ArcImplementationNotes
//
// Return [cx, cy, theta1, delta_theta]
//
function get_arc_center(x1, y1, x2, y2, fa, fs, rx, ry, sin_phi, cos_phi) {
  // Step 1.
  //
  // Moving an ellipse so origin will be the middlepoint between our two
  // points. After that, rotate it to line up ellipse axes with coordinate
  // axes.
  //
  const x1p = cos_phi*(x1-x2)/2 + sin_phi*(y1-y2)/2;
  const y1p = -sin_phi*(x1-x2)/2 + cos_phi*(y1-y2)/2;

  const rx_sq = rx * rx;
  const ry_sq = ry * ry;
  const x1p_sq = x1p * x1p;
  const y1p_sq = y1p * y1p;

  // Step 2.
  //
  // Compute coordinates of the centre of this ellipse (cx', cy')
  // in the new coordinate system.
  //
  let radicant = (rx_sq * ry_sq) - (rx_sq * y1p_sq) - (ry_sq * x1p_sq);

  if(radicant < 0) {
    // due to rounding errors it might be e.g. -1.3877787807814457e-17
    radicant = 0;
  }

  radicant /= (rx_sq * y1p_sq) + (ry_sq * x1p_sq);
  radicant = Math.sqrt(radicant) * (fa === fs ? -1 : 1);

  const cxp = radicant * rx/ry * y1p;
  const cyp = radicant * -ry/rx * x1p;

  // Step 3.
  //
  // Transform back to get centre coordinates (cx, cy) in the original
  // coordinate system.
  //
  const cx = cos_phi*cxp - sin_phi*cyp + (x1+x2)/2;
  const cy = sin_phi*cxp + cos_phi*cyp + (y1+y2)/2;

  // Step 4.
  //
  // Compute angles (theta1, delta_theta).
  //
  const v1x = (x1p - cxp) / rx;
  const v1y = (y1p - cyp) / ry;
  const v2x = (-x1p - cxp) / rx;
  const v2y = (-y1p - cyp) / ry;

  const theta1 = unit_vector_angle(1, 0, v1x, v1y);
  let delta_theta = unit_vector_angle(v1x, v1y, v2x, v2y);

  if(fs === 0 && delta_theta > 0) {
    delta_theta -= TAU;
  }
  if(fs === 1 && delta_theta < 0) {
    delta_theta += TAU;
  }

  return [cx, cy, theta1, delta_theta];
}

//
// Approximate one unit arc segment with bézier curves,
// see http://math.stackexchange.com/questions/873224
//
function approximate_unit_arc(theta1, delta_theta) {
  const alpha = 4/3 * Math.tan(delta_theta/4);

  const x1 = Math.cos(theta1);
  const y1 = Math.sin(theta1);
  const x2 = Math.cos(theta1 + delta_theta);
  const y2 = Math.sin(theta1 + delta_theta);

  return [x1, y1, x1 - y1*alpha, y1 + x1*alpha, x2 + y2*alpha, y2 - x2*alpha, x2, y2];
}

function a2c(x1, y1, x2, y2, rx, ry, phi, fa, fs) {
  const sin_phi = Math.sin(phi * TAU / 360);
  const cos_phi = Math.cos(phi * TAU / 360);

  // Make sure radii are valid
  //
  const x1p = cos_phi*(x1-x2)/2 + sin_phi*(y1-y2)/2;
  const y1p = -sin_phi*(x1-x2)/2 + cos_phi*(y1-y2)/2;

  if(x1p === 0 && y1p === 0) {
    // we're asked to draw line to itself
    return [];
  }

  if(rx === 0 || ry === 0) {
    // one of the radii is zero
    return [];
  }


  // Compensate out-of-range radii
  //
  rx = Math.abs(rx);
  ry = Math.abs(ry);

  const lambda = (x1p * x1p) / (rx * rx) + (y1p * y1p) / (ry * ry);
  if(lambda > 1) {
    rx *= Math.sqrt(lambda);
    ry *= Math.sqrt(lambda);
  }


  // Get center parameters (cx, cy, theta1, delta_theta)
  //
  const cc = get_arc_center(x1, y1, x2, y2, fa, fs, rx, ry, sin_phi, cos_phi);

  const result = [];
  let theta1 = cc[2];
  let delta_theta = cc[3];

  // Split an arc to multiple segments, so each segment
  // will be less than τ/4 (= 90°)
  //
  const segments = Math.max(Math.ceil(Math.abs(delta_theta) / (TAU / 4)), 1);
  delta_theta /= segments;

  for(let i = 0; i < segments; i++) {
    result.push(approximate_unit_arc(theta1, delta_theta));
    theta1 += delta_theta;
  }

  // We have a bezier approximation of a unit circle,
  // now need to transform back to the original ellipse
  //
  return result.map((curve) => {
    for(let i = 0; i < curve.length; i += 2) {
      let x = curve[i + 0];
      let y = curve[i + 1];

      // scale
      x *= rx;
      y *= ry;

      // rotate
      const xp = cos_phi*x - sin_phi*y;
      const yp = sin_phi*x + cos_phi*y;

      // translate
      curve[i + 0] = xp + cc[0];
      curve[i + 1] = yp + cc[1];
    }

    return curve;
  });
}

export { decodeDataURL, request, delay, endpointToCenterParameterization, a2c, dataURLPattern, transformPattern, pathPattern, numberPattern }