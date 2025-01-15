const express = require('express');
const cors = require('cors')
const bodyParser = require('body-parser')
const fs = require('fs');
const { Worker } = require('node:worker_threads');

const app = express();

app.use(bodyParser.raw({limit: '50mb'}))
app.use(bodyParser.json())
app.use(cors())
app.use(express.static(__dirname + '../Assets')); 
app.use((req, res, next) => {
    console.log(`# ${req.method} ${req.path} - ${req.ip}`)
    next()
})

const worker = new Worker("./Server/Worker.js");
worker.on('exit', (code) => {
    console.log(`Worker stopped with exit code ${code}`);
});

app.get('/load/:file', (req, res) => {
    console.log('Load: '+req.params.file)
    res.sendStatus(200)
})

app.post('/save/:file', (req, res) => {
    console.log('save: '+req.params.file)
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

const port = 3000 
app.listen(port, function () {
    console.log("Server started, listening on port " + port);
})