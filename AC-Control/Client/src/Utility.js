function uuidv4() {
    return ([1e7] + -1e3 + -4e3 + -8e3 + -1e11).replace(/[018]/g, c =>
        (c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16)
    );
}

function getCookie (name) {
    var match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
    if (match) return match[2];
}

function setCookie(name, value) {
    var expires = "";
    var date = new Date();
    date.setTime(date.getTime() + (24*60*60*1000));
    expires = "; expires=" + date.toUTCString();
    document.cookie = name + "=" + (value || "")  + expires + "; path=/";
}

function fetchWithToken(endpoint, method = "GET", body = null, headers = {}, allowUnauthorized = false) {
    var token = getCookie("ac_token");
    if(!token) throw new Error("No token found");
    headers["token"] = token;
    return new Promise(async (resolve, reject) => {
        try{
            var response = await fetch(`https://${window.location.origin}/${endpoint}`, {
                method: method,
                headers: headers,
                body: body
            });
            if(!allowUnauthorized && response.status == 401)
                window.location.reload();
            else
                resolve(response);
        }
        catch(ex) {
            reject(ex);
        }
    });
}

const TimePattern = /(?:(?<hours>\d{1,2}):)?(?<minutes>\d{1,2}):(?<seconds>\d{1,2})\.?(?<millis>\d+)?/

function parseTime(data) {
    var result = TimePattern.exec(data)
    var time = new Date(Date.now())
    var stages = { "hours": x => time.setHours(x), "minutes": x => time.setMinutes(x), "seconds": x => time.setSeconds(x), "millis": x => time.setMilliseconds(Number(x.padEnd(6, "0"))/1000) }
    for(var stage in stages)
        stages[stage](result && stage in result.groups ? result.groups[stage] : 0)
    return time
}

function delay(time) {
    return new Promise(r => setTimeout(r, time));
}

function interpolateColors(colors, percent) {
    if(colors.length == 1) return colors[0]

    const sections = (colors.length - 1)
    const index = Math.floor(sections * percent)
    const sectionPercent = percent * sections - index

    if(index == sections) return colors[index]
    
    const color1 = colors[index]
    const color2 = colors[index + 1]        

    const r1 = parseInt(color1.substring(1, 3), 16);
    const g1 = parseInt(color1.substring(3, 5), 16);
    const b1 = parseInt(color1.substring(5, 7), 16);
  
    const r2 = parseInt(color2.substring(1, 3), 16);
    const g2 = parseInt(color2.substring(3, 5), 16);
    const b2 = parseInt(color2.substring(5, 7), 16);
  
    const r = Math.round(r1 + (r2 - r1) * sectionPercent);
    const g = Math.round(g1 + (g2 - g1) * sectionPercent);
    const b = Math.round(b1 + (b2 - b1) * sectionPercent);
  
    return "#" + ((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1);
}

function loess(xval, yval, bandwidth)
{
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

		if (i > 0) {
			if (right < xval.length - 1 &&
				xval[right+1] - xval[i] < xval[i] - xval[left]) {
					left++;
					right++;
			}
		}

		var edge;
		if (xval[i] - xval[left] > xval[right] - xval[i])
			edge = left;
		else
			edge = right;

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

		res[i] = beta * x + alpha;
	}

	return res;
}

export { uuidv4, fetchWithToken, getCookie, setCookie, parseTime, delay, interpolateColors, loess }