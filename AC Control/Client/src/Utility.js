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

function fetchWithToken(endpoint, method = "GET", body = null, headers = {}) {
    var token = getCookie("ac_token");
    if(!token) throw new Error("No token found");
    headers["token"] = token;
    return fetch(`https://${window.location.hostname}:3001/${endpoint}`, {
        method: method,
        headers: headers,
        body: body
    })
}

export { uuidv4, fetchWithToken, getCookie, setCookie }