<script>
    import OscilloscopeViewer from "../components/OscilloscopeViewer.svelte";
    import Calibration from "../components/Calibration.svelte";
    import Settings from "../components/Settings.svelte";
    import { request, generateAudio, encodeAudio, parseSVG, EventQueue, uuidv4, updateText } from "$lib/Utility.js"       
    import { SVGToAudio } from "$lib/SVGEngine.js" 

    let fileInputElement;
    let imageTypePattern = new RegExp('image/svg.+')
    let isLive = $state(false), 
        isPlaying = $state(false), 
        isBuilt = $state(false),         
        calibrating = $state(false), 
        settings = $state(), 
        loading = $state(true), 
        loadingLiveView = $state(false), 
        loadingProjectView = $state(false), 
        displaySettings = $state(false), 
        currentFrame = $state(0),
        building = $state(false),
        project = $state({
            frames: [{
                elements: []
            }],
            name: ""
        })
    let settingsToggle, setSelected
    let config = {
        translation: { x: 0, y: 0 },
        scale: {x: 1, y: 1 },
        rotation: 0
    }

    const liveUpdateEventQueue = new EventQueue()

    $effect(async () => {
        updateLiveView()
        const id = setInterval(liveUpdateEventQueue.processEventQueue, (1/10) * 1000, liveUpdateEventQueue) //JS is trash

        return () => {
            clearInterval(id)
        }
    })

    async function init() {
        try {
            request("stop")
            
            let response = await request("loadconfig")
            if(response.status == 200) {
                config = await response.json()
            }

            response = await request("loadsettings")
            if(response.status == 200) {
                settings = await response.json()
            }
        }
        catch(ex) {
            console.log(ex)
        }
        finally {
            loading = false
        }
    }    

    function onFileSelected(e) {
        let image = e.target.files[0];
        console.log(e)

        if(!imageTypePattern.test(image.type)){
            console.log("Invalid file type: " + image.type)
            return 
        }

        let reader = new FileReader();        
        reader.addEventListener('load', async e => {
            let fileData = String.fromCharCode.apply(null, new Uint8Array(e.target.result))
            let element = {
                type: "image",
                imageData: btoa(fileData),
                translation: { x: 0, y: 0 },
                scale: { x: 1, y: 1 },
                rotation: 0,
                name: image.name,
                order: project.frames[currentFrame].elements.length,
                id: uuidv4(),
                rootNode: await parseSVG(fileData)
            }
            project.frames[currentFrame].elements.push(element)
            setSelected(element)
        })
        reader.addEventListener('error', e => {
            console.log("Failed to read: " + image.name)
        })
        reader.readAsArrayBuffer(image)
    }

    async function generateProjectAudio() {
        try {
            const rawAudioData = {
                sampleRate: settings["sampleRate"],
                kpbs: settings["kbps"],
                channelData: [[], []]
            }; 

            for(let frame of project.frames) {
                let channelData = await new SVGToAudio.generateAudio(frame, settings["frequency"], settings["duration"], settings["gain"], settings["sampleRate"], config)
                rawAudioData.channelData[0] = rawAudioData.channelData[0].concat(channelData[0])
                rawAudioData.channelData[1] = rawAudioData.channelData[1].concat(channelData[1])
            }
            console.log(rawAudioData)
            return encodeAudio(rawAudioData)
        }
        catch (ex) {
            console.log("generateAudio failed: ", ex)
        }
    }

    async function toggleLiveView() {
        try {
            if(!project) {
                isLive = false
                return;
            }

            isPlaying = false
            if(isLive) {
                let response = await request("stop")
                if(response.status != 200)
                    throw new Error("Failed to stop")
            }
            else {
                updateLiveView()
            }
            isLive = !isLive
        }
        catch(ex) {
            console.log(ex)
        }
    }

    async function updateLiveView() {
        if(isLive && project.frames[currentFrame].elements.length > 0) {
            liveUpdateEventQueue.queueEvent("update", async () => {
                let audioData = await generateAudio(project.frames[currentFrame], settings, config)
                let [encodedAudioData, audioBlob] = await encodeAudio(audioData)
                let response = await request("play", "POST", encodedAudioData, {"Content-Type": "application/octet-stream"})
                if(response.status != 200)
                    throw new Error("Failed to play")
            })
        }
    }

    async function exportAudio() {
        if(elementData) {
            try {
                let [encodedAudioData, audioBlob] = await generateProjectAudio()

                if(audioBlob) {
                    let a = document.createElement("a")
                    let url = URL.createObjectURL(audioBlob)
                    console.log(audioBlob, url)
                    a.href = url;
                    a.download = elementData.name.split(".")[0] + ".wav";
                    document.body.appendChild(a);
                    a.click();
                    setTimeout(function() {
                        document.body.removeChild(a);
                        window.URL.revokeObjectURL(url);  
                    }, 0); 
                }
            }
            catch(ex) {
                console.log('Failed to export audio: ', ex)
            }
        }
    }

    function clear() {
        project.frames[currentFrame].elements = []
        if(isLive)
            request("stop")
    }

    function calibrate() {
        calibrating = true
    }

    function onCalibrationComplete(newConfig) {
        if(newConfig) {
            config = newConfig
        }
        calibrating = false
        updateLiveView()
    }

    function toggleSettings() {
        if(displaySettings)
            settingsToggle()
        else
            displaySettings = true
    }

    function onSettingsComplete() {
        displaySettings = false
    }

    function updateFrame(frame) {
        project.frames[currentFrame] = frame
        if(project.frames[currentFrame].elements.length > 0)
            updateLiveView()
        else
            request("stop")
    }

    function createTextbox() {
        let element = {
            type: "text",
            imageData: null,
            translation: { x: 0, y: 0 },
            scale: { x: 1, y: 1 },
            rotation: 0,
            name: "text" + project.frames[currentFrame].elements.filter(x => x.type == "text").length,
            order: project.frames[currentFrame].elements.length,
            id: uuidv4(),
            rootNode: null,
            fontIndex: 0,
            variantIndex: 0,
        }
        project.frames[currentFrame].elements.push(element)
        setSelected(element)
    }

    function createShape() {
        let element = {
            type: "shape",
            imageData: null,
            translation: { x: 0, y: 0 },
            scale: { x: 1, y: 1 },
            rotation: 0,
            name: "shape" + project.frames[currentFrame].elements.filter(x => x.type == "shape").length,
            order: project.frames[currentFrame].elements.length,
            id: uuidv4(),
            rootNode: null
        }
        project.frames[currentFrame].elements.push(element)
        setSelected(element)
    }

    function createDrawing() {
        let element = {
            type: "draw",
            imageData: null,
            translation: { x: 0, y: 0 },
            scale: { x: 1, y: 1 },
            rotation: 0,
            name: "draw" + project.frames[currentFrame].elements.filter(x => x.type == "draw").length,
            order: project.frames[currentFrame].elements.length,
            id: uuidv4(),
            rootNode: null
        }
        project.frames[currentFrame].elements.push(element)
        setSelected(element)
    }
    
</script>

<div class="main" use:init>
    <div class="menu">
        <div class="menu-container">
            <div class="group">
                <button><i class="fa-solid fa-plus"></i></button>
                <button><i class="fa-regular fa-folder-open"></i></button>
                <button><i class="fa-regular fa-floppy-disk"></i></button>
            </div>
            <div class="group">
                <div>
                    <button onclick={() => fileInputElement.click()}><i class="fa-regular fa-file-image"></i></button>                    
                    <input style="display:none" type="file" accept=".svg" onchange={onFileSelected} onclick={e => e.target.value = ''} bind:this={fileInputElement} >
                </div>        
                <button class="text" onclick={createTextbox}>T</button>
                <button onclick={createDrawing}><i class="fa-solid fa-paintbrush"></i></button>
                <button onclick={createShape}><i class="fa-solid fa-shapes"></i></button>
                <button onclick={clear}><i class="fa-solid fa-eraser"></i></button>     
            </div>
            <div class="group">
                <button onclick={calibrate}><i class="fa-solid fa-ruler-combined"></i></button>
                <button onclick={toggleSettings}><i class="fa-solid fa-gear"></i></button>
            </div>
            <div class="group play-group">
                <button class="{isLive ? "live" : ""}" onclick={toggleLiveView}>{#if loadingLiveView}<img class="spinning" src="loading-spinner.svg" />{:else}<i class="fa-solid fa-pencil"></i>{/if}</button>
                <button class="{isBuilt ? (isPlaying ? "stop" : "play") : ""}">{#if loadingProjectView || building}<img class="spinning" src="loading-spinner.svg" />{:else if isBuilt && !isPlaying}<i class="fa-solid fa-play"></i>{:else if isPlaying}<i class="fa-solid fa-stop"></i>{:else}<i class="fa-solid fa-hammer"></i>{/if}</button>
                <button onclick={exportAudio}><i class="fa-solid fa-file-export"></i></button> 
            </div>
        </div>
    </div>
    {#if calibrating}
        <Calibration config={config} settings={settings} onComplete={onCalibrationComplete}/>
    {/if}
    {#if !loading}
        <OscilloscopeViewer bind:onSetSelected={setSelected} frame={project.frames[currentFrame]} project={project} settings={settings} onFrameUpdate={updateFrame}/>  
    {/if}
    {#if displaySettings}
        <Settings bind:settings={settings} bind:onToggle={settingsToggle} onComplete={onSettingsComplete} />
    {/if}
</div>