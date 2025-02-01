<script>
    import { request, encodeAudio, parseSVG } from "$lib/Utility.js"
    import { SVGToAudio } from "$lib/SVGEngine.js" 

    let { 
        config,
        settings,
        onComplete
    } = $props()

    let loading = $state(false), tempConfig = $state({
        translation: { x: config.translation.x, y: config.translation.y },
        scale: { x: config.scale.x, y: config.scale.y },
        rotation: config.rotation
    })   
    let frame = {
        elements: []
    }

    async function init() {
        console.log("init")
        try {
            let response = await request("cal")
            if(response.status == 200) {
                let data = await response.blob()
                let svgData = await data.text()
                frame.elements.push({
                    translation: { x: 0, y: 0 },
                    scale: { x: 1, y: 1 },
                    rotation: 0,
                    rootNode: await parseSVG(svgData)
                })
                updateCalibrationAudio()
            }
            else {
                endCalibration()
            }
        }
        catch (ex) {
            console.log(ex)
            endCalibration()
        }
    }

    async function updateCalibrationAudio() {
        let rawAudioData = {
            sampleRate: settings["sampleRate"],
            kpbs: settings["kbps"],
            channelData: await new SVGToAudio().generateAudio(frame, 10, 200, settings["gain"], settings["sampleRate"], tempConfig)
        };
        let [encodedAudioData, audioBlob] = await encodeAudio(rawAudioData)
        await request("play", "POST", encodedAudioData, {"Content-Type": "application/octet-stream"})
    }

    function updateCalibration(type, diff) {
        switch(type) {
            case "tx":
                tempConfig.translation.x += Math.sign(tempConfig.scale.x) * diff
                break;
            case "ty":
                tempConfig.translation.y += Math.sign(tempConfig.scale.y) * diff
                break;
            case "sx":
                tempConfig.scale.x = Math.sign(tempConfig.scale.x) * Math.max(Math.abs(diff + tempConfig.scale.x), 0.1)
                break;
            case "sy":
                tempConfig.scale.y = Math.sign(tempConfig.scale.y) * Math.max(Math.abs(diff + tempConfig.scale.y), 0.1)
                break;
            case "sb":
                tempConfig.scale.x = Math.sign(tempConfig.scale.x) * Math.max(Math.abs(Math.sign(tempConfig.scale.x) * diff + tempConfig.scale.x), 0.1)
                tempConfig.scale.y = Math.sign(tempConfig.scale.y) * Math.max(Math.abs(Math.sign(tempConfig.scale.y) * diff + tempConfig.scale.y), 0.1)
                break;
            case "ix":
                tempConfig.scale.x *= -1
                tempConfig.translation.x *= -1
                break;
            case "iy":
                tempConfig.scale.y *= -1
                tempConfig.translation.y *= -1
                break;
            case "r":
                tempConfig.rotation += diff
                break;
            case "ct":
                tempConfig.translation.x = 0
                tempConfig.translation.y = 0
                break;
            case "cs":
                tempConfig.scale.x = 1
                tempConfig.scale.y = 1
                break;
        }
        updateCalibrationAudio()
    }

    async function saveCalibration() {
        if(loading) return
        try {
            loading = true
            await request("saveconfig", "POST", JSON.stringify(tempConfig), {"Content-Type": "application/json"})
            endCalibration(true)
        }
        catch(ex) {
            console.log(ex)
            loading = false
        }
    }    

    function endCalibration(saveConfig = false) {
        request("stop")
        loading = false
        onComplete(saveConfig ? tempConfig : null)
    }

    function round(x, decimals = 1) {
        const mult = Math.pow(10, decimals)
        return Math.round(x * mult) / mult
    }
</script>

<div class="popover">
    <div class="calibrate" use:init>
        <div class="title">Calibrate</div>
        <div class="controls">
            <div class="control-section">
                <div class="section-title">Translate</div>
                <div class="section-title">(<input type="number" value={round(tempConfig.translation.x, 2)} onchange={e => tempConfig.translation.x = e.target.value} />,<input type="number" value={round(tempConfig.translation.y, 2)} onchange={e => tempConfig.translation.y = e.target.value} />)</div>
                <div class="control-container">
                    <div><button onclick={() => updateCalibration("ty", -1)}><i class="fa-solid fa-angles-up"></i></button></div>
                    <div><button onclick={() => updateCalibration("ty", -0.1)}><i class="fa-solid fa-angle-up"></i></button></div>
                    <div>
                        <button onclick={() => updateCalibration("tx", -1)}><i class="fa-solid fa-angles-left"></i></button>
                        <button onclick={() => updateCalibration("tx", -0.1)}><i class="fa-solid fa-angle-left"></i></button>
                        <button onclick={() => updateCalibration("ct")}><i class="fa-solid fa-trash-can"></i></button>
                        <button onclick={() => updateCalibration("tx", 0.1)}><i class="fa-solid fa-angle-right"></i></button>
                        <button onclick={() => updateCalibration("tx", 1)}><i class="fa-solid fa-angles-right"></i></button>
                    </div>
                    <div><button onclick={() => updateCalibration("ty", 0.1)}><i class="fa-solid fa-angle-down"></i></button></div>
                    <div><button onclick={() => updateCalibration("ty", 1)}><i class="fa-solid fa-angles-down"></i></button></div>
                </div>
            </div>
            <div class="control-section">
                <div class="section-title">Rotate, Scale</div>
                <div class="section-title">
                    <input type="number" value={round(tempConfig.rotation, 3)} onchange={e => tempConfig.rotation = e.target.value} />
                    (<input type="number" step="0.1" value={round(tempConfig.scale.x, 2)} onchange={e => tempConfig.scale.x = e.target.value} />,<input type="number" step="0.1" value={round(tempConfig.scale.y, 2)} onchange={e => tempConfig.scale.y = e.target.value} />)
                </div>
                <div class="control-container">
                    <div>
                        <button onclick={() => updateCalibration("sb", -1)}><i class="fa-solid fa-minimize"></i></button>
                        <span class="spacer"></span>
                        <button onclick={() => updateCalibration("sy", -1)}>-1</button>                        
                        <button onclick={() => updateCalibration("iy")}><i class="fa-solid fa-up-down"></i></button>
                        <button onclick={() => updateCalibration("r", -Math.PI / 2)}><i class="fa-solid fa-arrow-rotate-left"></i></button>
                    </div>
                    <div>
                        <span class="spacer"></span>
                        <button onclick={() => updateCalibration("sb", -0.1)}><i class="fa-solid fa-minimize"></i></button>
                        <button onclick={() => updateCalibration("sy", -0.1)}>-0.1</button>
                        <span class="spacer"></span>
                        <button onclick={() => updateCalibration("ix")}><i class="fa-solid fa-left-right"></i></button>                        
                    </div>
                    <div>
                        <button onclick={() => updateCalibration("sx", -1)}>-1</button>
                        <button onclick={() => updateCalibration("sx", -0.1)}>-0.1</button>
                        <button onclick={() => updateCalibration("cs")}><i class="fa-solid fa-trash-can"></i></button>
                        <button onclick={() => updateCalibration("sx", 0.1)}>+0.1</button>
                        <button onclick={() => updateCalibration("sx", 1)}>+1</button>
                    </div>
                    <div>
                        <span class="spacer"></span>
                        <span class="spacer"></span>
                        <button onclick={() => updateCalibration("sy", 0.1)}>+0.1</button>
                        <button onclick={() => updateCalibration("sb", 0.1)}><i class="fa-solid fa-maximize"></i></button>
                        <span class="spacer"></span>
                    </div>
                    <div>
                        <button onclick={() => updateCalibration("r", Math.PI / 2)}><i class="fa-solid fa-arrow-rotate-right"></i></button>
                        <span class="spacer"></span>
                        <button onclick={() => updateCalibration("sy", 1)}>+1</button>
                        <span class="spacer"></span>
                        <button onclick={() => updateCalibration("sb", 1)}><i class="fa-solid fa-maximize"></i></button>
                    </div>
                </div>
            </div>
        </div>
        <div class="actions">
            <button class="cancel" onclick={() => endCalibration(false)}>Cancel</button>
            <button class="save" onclick={saveCalibration}>{#if loading}<img class="spinning" src="loading-spinner-white.svg" />{:else}Save{/if}</button>
        </div>
    </div>
</div>