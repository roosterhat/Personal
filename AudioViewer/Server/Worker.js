const { exec } = require('child_process')
const { parentPort } = require('node:worker_threads');

console.log("Starting Worker")
let play = false, running = true, player

function delay(ms) {
    return new Promise((resolve, reject) => setTimeout(resolve, ms))
}

(async () => {
    parentPort.on('message', message => {
        console.log("Worker: "+message)
        try {
            switch(message) {
                case "play":
                    play = true
                    break;
                case "stop":
                    play = false
                    if(player)
                        player.kill()
                    break;
                case "exit":
                    running = false
                    if(player)
                        player.kill()
                    break;
            }
        }
        catch(ex) {
            console.log(ex)
        }
    })

    try {
        while(running) {
            if(play) {
                player = exec("aplay -c 2 -t wav -r 44100 -D default:CARD=adapter -q ./Server/output.wav")
                await new Promise((resolve, reject) => player.on("close", resolve))
            }
            else
                await delay(100)
        }
    }
    catch(ex) {
        console.log("Worker Error: " + ex)
    }
})()
