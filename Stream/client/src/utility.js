function uuidv4() {
    return ([1e7] + -1e3 + -4e3 + -8e3 + -1e11).replace(/[018]/g, c =>
        (c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16)
    );
}

function loess(xval, yval, bandwidth) {
    function tricube(x) {
        var tmp = 1 - x * x * x;
        return tmp * tmp * tmp;
    }

	var res = [];

	var left = 0;
	var right = Math.floor(bandwidth * xval.length) - 1;

	for(var i in xval)
	{
		var x = xval[i];

		if (right < xval.length - 1 && xval[right+1] - x < x - xval[left]) {
			left++;
			right++;
		}

		var edge = x - xval[left] > xval[right] - x ? left : right

		var denom = Math.abs(1.0 / (xval[edge] - x));

		var sumWeights = 0;
		var sumX = 0, sumXSquared = 0, sumY = 0, sumXY = 0;

		var k = left;
		while(k <= right)
		{
			var xk = xval[k];
			var yk = yval[k];
			var dist;
			if (k < i) {
				dist = (x - xk);
			} else {
				dist = (xk - x);
			}
			var w = tricube(dist * denom);
			var xkw = xk * w;
			sumWeights += w;
			sumX += xkw;
			sumXSquared += xk * xkw;
			sumY += yk * w;
			sumXY += yk * xkw;
			k++;
		}

		var meanX = sumX / sumWeights;
		var meanY = sumY / sumWeights;
		var meanXY = sumXY / sumWeights;
		var meanXSquared = sumXSquared / sumWeights;

		var beta;
		if (meanXSquared == meanX * meanX)
			beta = 0;
		else
			beta = (meanXY - meanX * meanY) / (meanXSquared - meanX * meanX);

		var alpha = meanY - beta * meanX;

		let val = beta * x + alpha
		res.push(isNaN(val) || !isFinite(val) ? yval[i] : val);
	}

	return res;
}

export { uuidv4, loess }