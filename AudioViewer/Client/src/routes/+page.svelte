<script>
    import OscilloscopeViewer from "../components/OscilloscopeViewer.svelte";
    import { request } from "$lib/Utility.js"
    import WavEncoder from "wav-encoder"
    import { SVGToAudio } from "$lib/SVGEngine.js"
    import makerjs from "makerjs"
    import fonts from "$lib/fonts.js"

    let fileInputElement, OscilloscopeViewerDimensions;
    let imageTypePattern = new RegExp('image/svg.+')
    let elementData = $state(), isPlaying = $state(false), loadingPlay = $state(false), rootNode = $state(), frequency = $state(50), gain = $state(0.001)
    let corrections = $state({
        translation: { x: -450, y: 350 },
        scale: {x: 1.3, y: -1.05 },
        rotation: 0
    })
    const sampleRate = 44100
    const AudioGenerator = new SVGToAudio()
    let encodedAudioData

    function init() {
        request("stop")
    }

    async function parseSVG(fileData) {
        var root = new DOMParser().parseFromString(fileData, 'image/svg+xml')
        const error = root.querySelector("parsererror");
        if(error) {
            console.log("Failed to parse svg")
            elementData = null
            return null
        }
        await replaceTextData(root.childNodes[0], root)
        return root      
    }

    async function replaceTextData(root, node) {
        if(node.nodeName == "text") {
            let [font, fontSize] = await getFont(node)
            const textModel = new makerjs.models.Text(font, node.textContent, fontSize);
            let svg = makerjs.exporter.toSVG(textModel);
            let textNode = new DOMParser().parseFromString(svg, 'image/svg+xml').childNodes[0].childNodes[0]
            let bbox = makerjs.measure.modelExtents(textModel);
            let padding = 0
            let viewBox = [
                bbox.low[0] - padding,
                bbox.low[1] - padding,
                bbox.high[0] + padding,
                bbox.high[1] + padding
            ].join(' ');
            if(!root.attributes["viewBox"])
                root.setAttribute("viewBox", viewBox)
            for(let att of node.attributes)
                if(att.name != "style")
                    textNode.setAttribute(att.name, att.value)
            node.parentNode.replaceChild(textNode, node)
        }
        else {
            for(let child of node.children) {
                await replaceTextData(root, child)
            }
        }
    }

    async function getFont(node) {
        let fontFamily = "ABeeZee", fontSize = 12, fontStyle = "regular"
        if(node.attributes["style"]) {
            let css = {}
            let rawcss = node.attributes["style"].value.split(";")
            let fontPattern = new RegExp("((?<size>\\d+px)|(?<style>normal|italic)|(?<family>[\\w-]+)|(?:\"(?<family>[^\"]+)\"))", "g")
            for(let item of rawcss) {
                let pair = item.split(":")
                let key = pair[0].trim()
                let value = pair[1].trim()
                switch(key) {
                    case "font":
                        for(let matches of value.matchAll(fontPattern)) {
                            for(let type of ["size", "style", "family"]) {
                                let match = matches.groups[type]
                                if(match) {
                                    switch(type) {
                                        case "size":
                                            fontSize = parseInt(match.match("\\d+")[0])
                                            break;
                                        case "style":
                                            fontStyle = match
                                            break;
                                        case "family":
                                            fontFamily = match
                                            break;
                                    }
                                }
                            }
                        }
                        break;
                    case "font-family":
                        fontFamily = value
                        break;
                    case "font-size":
                        fontSize = parseInt(value.match("\\d+")[0])
                        break;
                    case "font-style":
                        fontStyle = value
                        break;
                }
            }
        }
        console.log(fontFamily, fontSize, fontStyle)
        fontStyle = fontStyle == "normal" ? "regular" : fontStyle
        let fontGroup = fonts.items.find(x => x.family == fontFamily) || fonts.items[0]
        let url = fontGroup.files[fontStyle] || fontGroup.files["regular"]
        let font = await (new Promise((resolve, reject) => opentype.load(url, (error, result) => {
            if(error)
                reject(error)
            else
                resolve(result)
        })))
        return [font, fontSize]
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
            rootNode = await parseSVG(fileData)
            elementData = {
                imageData: btoa(fileData),
                translation: { x: 0, y: 0 },
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

    async function generateAudio() {
        try {
            const rawAudioData = {
                sampleRate: sampleRate,
                kpbs: 128,
                channelData: await AudioGenerator.generateAudio(rootNode, elementData, frequency, gain, sampleRate, corrections)
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
            if(!elementData) {
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

    function clear() {
        elementData = null
        rootNode = null
    }
    
</script>

<div class="main" use:init>
    <div class="menu">
        <div>
            <button onclick={() => fileInputElement.click()}><i class="fa-solid fa-arrow-up-from-bracket"></i></button>
            <input style="display:none" type="file" accept=".svg" onchange={onFileSelected} onclick={e => e.target.value = ''} bind:this={fileInputElement} >
        </div>        
        <button><i class="fa-regular fa-floppy-disk"></i></button>
        <button><i class="fa-solid fa-file-export"></i></button>
        <button onclick={clear}><i class="fa-solid fa-eraser"></i></button>
        <button class="{isPlaying ? "stop" : "play"}" onclick={togglePlay}>{#if loadingPlay}<img class="spinning" src="loading-spinner.svg" />{:else if isPlaying}<i class="fa-solid fa-stop"></i>{:else}<i class="fa-solid fa-play"></i>{/if}</button>
    </div>
    <OscilloscopeViewer bind:data={elementData} bind:dimensions={OscilloscopeViewerDimensions} rootNode={rootNode}/>  
</div>