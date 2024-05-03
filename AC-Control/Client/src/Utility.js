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
    return new Promise(async resolve => {
        try{
            var response = await fetch(`https://${window.location.hostname}:3001/${endpoint}`, {
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
            if(!allowUnauthorized)
                window.location.reload();
            else
                throw ex;
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

export { uuidv4, fetchWithToken, getCookie, setCookie, parseTime, delay }