const express = require('express');
const cors = require('cors')
const bodyParser = require('body-parser')
const fs = require('fs');
const { Worker } = require('node:worker_threads');
const path = require('path');

const app = express();

app.use(bodyParser.raw({limit: '50mb'}))
app.use(bodyParser.json())
app.use(cors())
app.use((req, res, next) => {
    console.log(`# ${req.method} ${req.path} - ${req.ip}`)
    next()
})

const worker = new Worker("./Server/Worker.js");
worker.on('exit', (code) => {
    console.log(`Worker stopped with exit code ${code}`);
});

app.get('/cal', (req, res) => {
    try {
        const options = {
            root: path.join(__dirname, 'data')
        }
        res.sendFile('cal.svg', options)          
    }
    catch (ex) {
        console.log(ex)
        res.sendStatus(500)
    }    
})

app.get('/loadconfig', (req, res) => {
    try {
        const options = {
            root: path.join(__dirname, 'data')
        }
        res.sendFile('config', options)        
    }
    catch (ex) {
        console.log(ex)
        res.sendStatus(500)
    }    
})

app.post('/saveconfig', (req, res) => {
    try {
        fs.writeFile("./Server/data/config", JSON.stringify(req.body, null, 2), e => {
            if(e) {
                console.log("File write error: ", e)
                res.sendStatus(500)
            }
            else { 
                console.log("config written successfully")
                res.sendStatus(200)
            }
        })    
    }
    catch (ex) {
        console.log(ex)
        res.sendStatus(500)
    } 
})

app.get('/loadsettings', (req, res) => {
    try {
        const options = {
            root: path.join(__dirname, 'data')
        }
        res.sendFile('settings', options)        
    }
    catch (ex) {
        console.log(ex)
        res.sendStatus(500)
    }    
})

app.post('/savesettings', (req, res) => {
    try {
        fs.writeFile("./Server/data/settings", JSON.stringify(req.body, null, 2), e => {
            if(e) {
                console.log("File write error: ", e)
                res.sendStatus(500)
            }
            else { 
                console.log("config written successfully")
                res.sendStatus(200)
            }
        })    
    }
    catch (ex) {
        console.log(ex)
        res.sendStatus(500)
    } 
})

app.get('/load/:file', (req, res) => {
    res.sendStatus(200)
})

app.post('/save/:file', (req, res) => {
    res.sendStatus(200)
})

app.post('/play', express.raw({type: "*/*"}), (req, res) => {
    if(req.body) {
        try{
            fs.writeFile('./Server/output.wav', Buffer.from(req.body), e => {
                if(e) {
                    console.log("File write error: ", e)
                    res.sendStatus(500)
                }
                else { 
                    console.log("file written successfully")
                    worker.postMessage("play")
                    res.sendStatus(200)
                }
            });            
        }
        catch(ex) {
            console.log("Error", ex)
            res.sendStatus(500)
        }
    }
    else {
        console.log("No data")
        res.sendStatus(400)
    }
})

app.get('/play', (req, res) => {
    if(fs.existssync("output.wav")) {
        worker.postMessage("play")
        res.sendStatus(200)
    }
    else {
        console.log("No audio")
        res.sendStatus(400)
    }
})

app.get('/stop', (req, res) => {
    worker.postMessage("stop")
    res.sendStatus(200)
})

app.get('/shapes', (req, res) => {    
    res.json(fs.readdirSync("./Client/static/shapes"))
})

const port = 3000 
app.listen(port, function () {
    console.log("Server started, listening on port " + port);
})