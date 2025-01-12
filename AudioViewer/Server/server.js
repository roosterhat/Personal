const express = require('express');
const cors = require('cors')
const bodyParser = require('body-parser')
const {Howl, Howler} = require('howler');
const fs = require('fs');

const app = express();

app.use(bodyParser.raw({limit: '50mb'}))
app.use(bodyParser.json())
app.use(cors())
app.use(express.static(__dirname + '../Assets')); 
app.use((req, res, next) => {
    console.log(`# ${req.method} ${req.path} - ${req.ip}`)
    next()
})

var currentSound = null

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
            fs.writeFile('./output.mp3', Buffer.from(req.body), e => e ? console.log("File write error: ",e) : console.log("file written successfully"));
            const base64Str = Buffer.from(req.body).toString("base64");
            console.log(base64Str.length)         
            const url = `data:audio/mp3;base64,${base64Str}`

            Howler.noAudio = false
            currentSound = new Howl({ 
                src: ["./output.mp3"], 
                loop: true,
                format: ["mp3"],
                onload: () => console.log("onload"),
                onloaderror: (id, e) =>  { console.log("onloaderror: ", e, Howler.noAudio); Howler.noAudio = false},
                onplay : () => console.log("onplay"),
                onplayerror: (id, e) => console.log("onplayerror: ", e)
            });

            currentSound.play()         
            
            res.sendStatus(200)
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
    if(currentSound) {
        currentSound.play();
        res.sendStatus(200)
    }
    else {
        console.log("No sound to play")
        res.sendStatus(400)
    }
})

app.get('/stop', (req, res) => {
    if(currentSound)
        currentSound.stop()
    res.sendStatus(200)
})

const port = 3000 
app.listen(port, function () {
    console.log("Server started, listening on port " + port);
})