<script>
    import OscilloscopeViewer from "../components/OscilloscopeViewer.svelte";
    import { request } from "../Utility.js"
    import lamejs from "lamejs"


    let fileInputElement, OscilloscopeViewerDimensions;
    let imageTypePattern = new RegExp('image/svg.+')
    let fileData = $state(), isPlaying = $state(false), loadingPlay = $state(false)

    const gain = 32767.0
    const rawAudioData = {
        sampleRate: 44100,
        kpbs: 128,
        channelData: [
            new Float32Array(44100).map(() => (Math.random() - 0.5) * gain),
            new Float32Array(44100).map(() => (Math.random() - 0.5) * gain)
        ]
    };
    let encodedAudioData
    // WavEncoder.encode(rawAudioData).then((buffer) => {        
    //     //compressedData = Readable.fromBytes(new Buffer(buffer)).pipeThrough(new CompressionStream("gzip"))
    //     encodedAudioData = new Uint32Array(buffer)
    // });
    const audioEncoder = new lamejs.Mp3Encoder(2, 44100, 128)
    const audioData = []
    let leftChunk, rightChunk, buffer, sampleBlockSize = 1152; 

    for (var i = 0; i < rawAudioData.channelData[0].length; i += sampleBlockSize) {
        leftChunk = rawAudioData.channelData[0].subarray(i, i + sampleBlockSize);
        let rightChunk = rawAudioData.channelData[1].subarray(i, i + sampleBlockSize);
        buffer = audioEncoder.encodeBuffer(leftChunk, rightChunk);
        if (buffer.length > 0) {
            audioData.push(buffer);
        }
    }

    buffer = audioEncoder.flush(); 
    if (buffer.length > 0) {
        audioData.push(buffer);
    }

    function onFileSelected(e) {
        let image = e.target.files[0];
        console.log(e)

        if(!imageTypePattern.test(image.type)){
            console.log("Invalid file type: " + image.type)
            return 
        }

        let reader = new FileReader();        
        reader.addEventListener('load', e => {
            fileData = {
                imageData: btoa(String.fromCharCode.apply(null, new Uint8Array(e.target.result))),
                x: 0,
                y: 0,
                scale: { x: 1, y: 1 },
                originalSize: { w: OscilloscopeViewerDimensions.width, h: OscilloscopeViewerDimensions.height},
                rotation: 0,
                name: image.name
            }
        })
        reader.addEventListener('error', e => {
            console.log("Failed to read: " + image.name)
        })
        reader.readAsArrayBuffer(image)
    }

    async function togglePlay() {
        try {
            loadingPlay = true
            if(isPlaying) {
                let response = await request("stop")
                if(response.status != 200)
                    throw new Error("Failed to stop")
            }
            else {
                let response = await request("play", "POST", new Blob(audioData, {type: 'audio/mp3'}), {"Content-Type": "application/octet-stream"})
                if(response.status != 200)
                    throw new Error("Failed to play")
            }
            isPlaying = !isPlaying
        }
        catch(ex) {
            console.log(ex)
        }
        finally {
            loadingPlay = false
        }
    }
    
</script>

<div class="main">
    <div class="menu">
        <div>
            <button onclick={() => fileInputElement.click()}><i class="fa-solid fa-arrow-up-from-bracket"></i></button>
            <input style="display:none" type="file" accept=".svg" onchange={onFileSelected} onclick={e => e.target.value = ''} bind:this={fileInputElement} >
        </div>        
        <button><i class="fa-regular fa-floppy-disk"></i></button>
        <button><i class="fa-solid fa-file-export"></i></button>
        <button onclick={() => fileData = null}><i class="fa-solid fa-eraser"></i></button>
        <button class="{isPlaying ? "stop" : "play"}" onclick={togglePlay}>{#if loadingPlay}<img class="spinning" src="loading-spinner.svg" />{:else if isPlaying}<i class="fa-solid fa-stop"></i>{:else}<i class="fa-solid fa-play"></i>{/if}</button>
    </div>
    <OscilloscopeViewer bind:data={fileData} bind:dimensions={OscilloscopeViewerDimensions}/>  
    <div class="canvas-container"><canvas id="waveform"></canvas></div>
</div>