<script>
    import OscilloscopeViewer from "../components/OscilloscopeViewer.svelte";
    import { request } from "../Utility.js"
    import WavEncoder from "wav-encoder"
    import { SVGToAudio } from "../SVGEngine.js"


    let fileInputElement, OscilloscopeViewerDimensions;
    let imageTypePattern = new RegExp('image/svg.+')
    let fileData = $state(), isPlaying = $state(false), loadingPlay = $state(false), rootNode = $state(), stepSize = $state({"line": 10, "bezier": 5, "ellipse": Math.PI / 40}), gain = $state(0.001)
    const AudioGenerator = new SVGToAudio()
    let encodedAudioData

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
                translation: { x: 0, y: 0 },
                scale: { x: 1, y: 1 },
                originalSize: { w: OscilloscopeViewerDimensions.width, h: OscilloscopeViewerDimensions.height},
                rotation: 0,
                name: image.name
            }

            var svg = atob(fileData.imageData)
            rootNode = new DOMParser().parseFromString(svg, 'image/svg+xml')
            const errorNode = rootNode.querySelector("parsererror");
            if(errorNode) {
                console.log("Failed to parse svg")
                fileData = null
            }
        })
        reader.addEventListener('error', e => {
            console.log("Failed to read: " + image.name)
        })
        reader.readAsArrayBuffer(image)
    }

    async function generateAudio() {
        try {
            const rawAudioData = {
                sampleRate: 44100,
                kpbs: 128,
                channelData: await AudioGenerator.generateAudio(rootNode, fileData, stepSize, gain)
            };    
            console.log(rawAudioData)
            WavEncoder.encode(rawAudioData).then((buffer, error) => {        
                if(error)
                    throw error
                encodedAudioData = new Uint32Array(buffer)
            });
        }
        catch (ex) {
            console.log("generateAudio failed: ", ex)
        }
    }

    async function togglePlay() {
        try {
            if(!fileData) {
                isPlaying = false
                return;
            }

            loadingPlay = true            
            if(isPlaying) {
                let response = await request("stop")
                if(response.status != 200)
                    throw new Error("Failed to stop")
            }
            else {
                await generateAudio()
                let response = await request("play", "POST", encodedAudioData, {"Content-Type": "application/octet-stream"})
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
    <OscilloscopeViewer bind:data={fileData} bind:dimensions={OscilloscopeViewerDimensions} rootNode={rootNode}/>  
    <div class="canvas-container"><canvas id="waveform"></canvas></div>
</div>