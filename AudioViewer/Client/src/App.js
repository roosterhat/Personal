import React from 'react'
import OscilloscopeViewer from "./components/OscilloscopeViewer.js";
import Calibration from "./components/Calibration.js";
import Settings from "./components/Settings.js";
import { request, generateAudio, encodeAudio, parseSVG, EventQueue, uuidv4 } from "./lib/Utility.js"
import { SVGToAudio } from "./lib/SVGEngine.js"

class App extends React.Component {
    constructor(props) {
        super(props);

        this.settingsToggleWrapper = { func: () => {} }
        this.imageTypePattern = new RegExp('image/svg.+')
        this.setSelected = () => {}
        this.config = {
            translation: { x: 0, y: 0 },
            scale: { x: 1, y: 1 },
            rotation: 0
        }
        this.liveUpdateEventQueue = new EventQueue()
        setInterval(this.liveUpdateEventQueue.processEventQueue, (1/30) * 1000, this.liveUpdateEventQueue)

        this.state = {
            isLive: false,
            isPlaying: false,
            isBuilt: false,
            calibrating: false,
            settings: null,
            loading: true,
            loadingLiveView: false,
            loadingProjectView: false,
            displaySettings: false,
            currentFrame: 0,
            building: false,
            project: {
                frames: [{
                    elements: []
                }],
                name: ""
            }
        }

        this.init()
    }

    init = async () => {
        try {
            request("stop")

            let response = await request("loadconfig")
            if (response.status == 200) {
                this.config = await response.json()
            }

            response = await request("loadsettings")
            if (response.status == 200) {
                this.setState({ settings: await response.json() })
            }
        }
        catch (ex) {
            console.log(ex)
        }
        finally {
            this.setState({ loading: false })
        }
    }

    onFileSelected = (e) => {
        let image = e.target.files[0];
        console.log(e)

        if (!this.imageTypePattern.test(image.type)) {
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
                order: this.state.project.frames[this.state.currentFrame].elements.length,
                id: uuidv4(),
                rootNode: await parseSVG(fileData)
            }
            this.addNewElement(element)
        })
        reader.addEventListener('error', e => {
            console.log("Failed to read: " + image.name)
        })
        reader.readAsArrayBuffer(image)
    }

    generateProjectAudio = () => {
        try {
            const rawAudioData = {
                sampleRate: this.state.settings["sampleRate"],
                kpbs: this.state.settings["kbps"],
                channelData: [[], []]
            };

            for (let frame of this.state.project.frames) {
                let channelData = new SVGToAudio.generateAudio(frame, this.state.settings["frequency"], this.state.settings["duration"], this.state.settings["gain"], this.state.settings["sampleRate"], this.config)
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

    toggleLiveView = async () => {
        try {
            if (!this.state.project) {
                this.setState({ isLive: false, isPlaying: false })
                return;
            }

            this.state.isLive = !this.state.isLive
            
            if (this.state.isLive) {
                this.updateLiveView()
            }
            else {                
                let response = await request("stop")
                if (response.status != 200)
                    throw new Error("Failed to stop")
            }            
            this.setState({ isLive: this.state.isLive, isPlaying: false })
        }
        catch (ex) {
            console.log(ex)
        }
    }

    updateLiveView = () => {
        if (this.state.isLive && this.state.project.frames[this.state.currentFrame].elements.length > 0) {
            this.liveUpdateEventQueue.queueEvent("update", async () => {
                let audioData = generateAudio(this.state.project.frames[this.state.currentFrame], this.state.settings, this.config)
                let [encodedAudioData, audioBlob] = await encodeAudio(audioData)
                let response = await request("play", "POST", encodedAudioData, { "Content-Type": "application/octet-stream" })
                if (response.status != 200)
                    throw new Error("Failed to play")
            })
        }
    }

    exportAudio = () => {
        if (this.elementData) {
            try {
                let [encodedAudioData, audioBlob] = this.generateProjectAudio()

                if (audioBlob) {
                    let a = document.createElement("a")
                    let url = URL.createObjectURL(audioBlob)
                    console.log(audioBlob, url)
                    a.href = url;
                    a.download = this.elementData.name.split(".")[0] + ".wav";
                    document.body.appendChild(a);
                    a.click();
                    setTimeout(function () {
                        document.body.removeChild(a);
                        window.URL.revokeObjectURL(url);
                    }, 0);
                }
            }
            catch (ex) {
                console.log('Failed to export audio: ', ex)
            }
        }
    }

    clear = () => {
        this.state.project.frames[this.state.currentFrame].elements = []
        this.setState({project: this.state.project})
        if (this.state.isLive)
            request("stop")
        this.setSelected(null)
    }

    calibrate = () => {
        this.setState({ calibrating: true })
    }

    onCalibrationComplete = (newConfig) => {
        if (newConfig) {
            this.config = newConfig
        }
        this.setState({ calibrating: false })
        this.updateLiveView()
    }

    toggleSettings = () => {
        this.settingsToggleWrapper.func()
    }

    setSettings = (settings) => {
        this.setState({ settings: settings })
    }

    updateFrame = (frame) => {
        this.state.project.frames[this.state.currentFrame] = frame
        this.setState({project: this.state.project})
        if (this.state.project.frames[this.state.currentFrame].elements.length > 0)
            this.updateLiveView()
        else
            request("stop")
    }

    createTextbox = () => {
        let element = {
            type: "text",
            imageData: null,
            translation: { x: 0, y: 0 },
            scale: { x: 1, y: 1 },
            rotation: 0,
            name: "text" + this.state.project.frames[this.state.currentFrame].elements.filter(x => x.type == "text").length,
            order: this.state.project.frames[this.state.currentFrame].elements.length,
            id: uuidv4(),
            rootNode: document.createElementNS("http://www.w3.org/1999/xhtml", "svg"),
            fontIndex: 0,
            variantIndex: 0,
        }
        this.addNewElement(element)
    }

    createShape = () => {
        let element = {
            type: "shape",
            imageData: null,
            translation: { x: 0, y: 0 },
            scale: { x: 1, y: 1 },
            rotation: 0,
            name: "shape" + this.state.project.frames[this.state.currentFrame].elements.filter(x => x.type == "shape").length,
            order: this.state.project.frames[this.state.currentFrame].elements.length,
            id: uuidv4(),
            rootNode: document.createElementNS("http://www.w3.org/1999/xhtml", "svg")
        }
        this.addNewElement(element)
    }

    createDrawing = () => {
        let element = {
            type: "draw",
            imageData: null,
            translation: { x: 0, y: 0 },
            scale: { x: 1, y: 1 },
            rotation: 0,
            name: "draw" + this.state.project.frames[this.state.currentFrame].elements.filter(x => x.type == "draw").length,
            order: this.state.project.frames[this.state.currentFrame].elements.length,
            id: uuidv4(),
            rootNode: document.createElementNS("http://www.w3.org/1999/xhtml", "svg")
        }
        this.addNewElement(element)
    }

    addNewElement = (e) => {
        this.state.project.frames[this.state.currentFrame].elements.push(e)
        this.setState({ project: this.state.project })
        this.setSelected(e)
        this.updateLiveView()
    }

    setSetSelected = (f) => {
        this.setSelected = f
    }

    render = () => {
        return (
            <div className="main">
                <div className="menu">
                    <div className="menu-container">
                        <div className="group">
                            <button><i className="fa-solid fa-plus"></i></button>
                            <button><i className="fa-regular fa-folder-open"></i></button>
                            <button><i className="fa-regular fa-floppy-disk"></i></button>
                        </div>
                        <div className="group">
                            <div>
                                <button onClick={() => document.getElementById("fileInput").click()}><i className="fa-regular fa-file-image"></i></button>
                                <input id="fileInput" style={{display: "none"}} type="file" accept=".svg" onChange={this.onFileSelected} onClick={e => e.target.value = ''} />
                            </div>
                            <button className="text" onClick={this.createTextbox}>T</button>
                            <button onClick={this.createShape}><i className="fa-solid fa-shapes"></i></button>
                            <button onClick={this.createDrawing}><i className="fa-solid fa-paintbrush"></i></button>
                            <button onClick={this.clear}><i className="fa-solid fa-eraser"></i></button>
                        </div>
                        <div className="group">
                            <button onClick={this.calibrate}><i className="fa-solid fa-ruler-combined"></i></button>
                            <button onClick={this.toggleSettings}><i className="fa-solid fa-gear"></i></button>
                        </div>
                        <div className="group play-group">
                            <button className={this.state.isLive ? "live" : ""} onClick={this.toggleLiveView}>{this.state.loadingLiveView ? <img className="spinning" src="this.state.loading-spinner.svg" /> : <i className="fa-solid fa-pencil"></i>}</button>
                            <button className={this.state.isBuilt ? (this.state.isPlaying ? "stop" : "play") : ""}>{
                                this.state.loadingProjectView || this.state.building ? <img className="spinning" src="this.state.loading-spinner.svg" /> :
                                    (this.state.isBuilt && !this.state.isPlaying ? <i className="fa-solid fa-play"></i> :
                                        (this.state.isPlaying ? <i className="fa-solid fa-stop"></i> : <i className="fa-solid fa-hammer"></i>))}
                            </button>
                            <button onClick={this.exportAudio}><i className="fa-solid fa-file-export"></i></button>
                        </div>
                    </div>
                </div>
                {this.state.calibrating ? <Calibration config={this.config} settings={this.state.settings} onComplete={this.onCalibrationComplete} /> : null}
                {!this.state.loading ? 
                    <OscilloscopeViewer 
                        frame={this.state.project.frames[this.state.currentFrame]} 
                        project={this.state.project} 
                        settings={this.state.settings} 
                        onFrameUpdate={this.updateFrame} 
                        setSetSelected={this.setSetSelected}/> : null}
                {<Settings settings={this.state.settings} onToggle={this.settingsToggleWrapper} setSettings={this.setSettings} />}
            </div>
        )
    }

}


export default App