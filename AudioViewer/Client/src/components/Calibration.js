import React from 'react'
import { request, encodeAudio, parseSVG } from "../lib/Utility.js"
import { SVGToAudio } from "../lib/SVGEngine.js" 
import { multiply, inv } from 'mathjs'

class Calibration extends React.Component {
    constructor(props) {
        super(props);
        
        this.channelData = null
        this.center = {x: 0, y: 0}
        this.frame  = {
            elements: []
        }

        this.state = {
            loading: false,
            tempConfig: {
                translation: { x: props.config.translation.x, y: props.config.translation.y },
                scale: { x: props.config.scale.x, y: props.config.scale.y },
                rotation: props.config.rotation
            }
        }
        console.log("constructor", props.config.translation)

        this.init()
    }

    init = async () => {
        console.log("init")
        try {
            let response = await request("cal")
            if(response.status == 200) {
                let data = await response.blob()
                let svgData = await data.text()
                this.frame.elements.push({
                    translation: { x: 0, y: 0 },
                    scale: { x: 1, y: 1 },
                    rotation: 0,
                    rootNode: await parseSVG(svgData)
                })
                this.updateCalibrationAudio()
            }
            else {
                this.endCalibration()
            }
        }
        catch (ex) {
            console.log(ex)
            this.endCalibration()
        }
    }

    updateCalibrationAudio = async () => {
        this.channelData = new SVGToAudio().generateAudio(this.frame, 15, 200, this.props.settings["gain"], this.props.settings["sampleRate"], this.state.tempConfig)
        let minX = Infinity, minY = Infinity
        let maxX = -Infinity, maxY = -Infinity
        for(let i in this.channelData[0]) {
            minX = Math.min(this.channelData[0][i])
            minY = Math.min(this.channelData[1][i])
            maxX = Math.max(this.channelData[0][i])
            maxY = Math.max(this.channelData[1][i])
        } 
        this.center = {x: (maxX - minX) / 2 + minX, y: (maxY - minY) / 2 + minY}

        let rawAudioData = {
            sampleRate: this.props.settings["sampleRate"],
            kpbs: this.props.settings["kbps"],
            channelData: this.channelData
        };
        let [encodedAudioData, audioBlob] = await encodeAudio(rawAudioData)
        await request("play", "POST", encodedAudioData, {"Content-Type": "application/octet-stream"})
    }

    setCalibration = (type, value) => {
        switch(type) {
            case "tx":
                this.state.tempConfig.translation.x = value
                break;
            case "ty":
                this.state.tempConfig.translation.y = value
                break;
            case "sx":
                this.updateAndCenter(() => this.state.tempConfig.scale.x = value)                
                break;
            case "sy":
                this.updateAndCenter(() => this.state.tempConfig.scale.y = value)                
                break;
            case "r":
                this.updateAndCenter(() => this.state.tempConfig.rotation = value)
                break;
        }
        this.setState({tempConfig: this.state.tempConfig})
        this.updateCalibrationAudio()
    }

    updateCalibration = (type, diff) => {
        switch(type) {
            case "tx":
                this.state.tempConfig.translation.x += diff
                break;
            case "ty":
                this.state.tempConfig.translation.y += diff
                break;
            case "sx":
                this.updateAndCenter(() => this.state.tempConfig.scale.x = Math.sign(this.state.tempConfig.scale.x) * Math.max(Math.abs(diff + this.state.tempConfig.scale.x), 0.01))                
                break;
            case "sy":
                this.updateAndCenter(() => this.state.tempConfig.scale.y = Math.sign(this.state.tempConfig.scale.y) * Math.max(Math.abs(diff + this.state.tempConfig.scale.y), 0.01))                
                break;
            case "sb":
                this.updateAndCenter(() => {
                    this.state.tempConfig.scale.x = Math.sign(this.state.tempConfig.scale.x) * Math.max(Math.abs(Math.sign(this.state.tempConfig.scale.x) * diff + this.state.tempConfig.scale.x), 0.01)
                    this.state.tempConfig.scale.y = Math.sign(this.state.tempConfig.scale.y) * Math.max(Math.abs(Math.sign(this.state.tempConfig.scale.y) * diff + this.state.tempConfig.scale.y), 0.01)
                })                
                break;
            case "ix":
                this.updateAndCenter(() => {
                    this.state.tempConfig.scale.x *= -1
                    this.state.tempConfig.translation.x *= -1
                })                
                break;
            case "iy":
                this.updateAndCenter(() => {
                    this.state.tempConfig.scale.y *= -1
                    this.state.tempConfig.translation.y *= -1
                })
                break;
            case "r":
                this.updateAndCenter(() => this.state.tempConfig.rotation += diff)
                break;
            case "ct":
                this.state.tempConfig.translation.x = 0
                this.state.tempConfig.translation.y = 0
                break;
            case "cs":
                this.state.tempConfig.scale.x = 1
                this.state.tempConfig.scale.y = 1
                this.state.tempConfig.rotation = 0
                break;
        }
        this.setState({tempConfig: this.state.tempConfig})
        this.updateCalibrationAudio()
    }

    updateAndCenter = (action) => {
        let r = this.state.tempConfig.rotation
        let sx = this.state.tempConfig.scale.x
        let sy = this.state.tempConfig.scale.y
        let x = this.state.tempConfig.translation.x
        let y = this.state.tempConfig.translation.y

        let scale = [[sx, 0, 0,], [0, sy, 0], [0, 0, 1]]
        let rotation = [[Math.cos(r), -Math.sin(r), 0], [Math.sin(r), Math.cos(r), 0], [0, 0, 1]]
        let translation = [[1, 0, x], [0, 1, y], [0, 0, 1]]

        let transform = multiply(translation, rotation, scale)
        let invTransform = inv(transform)

        let cx = invTransform[0][0] * this.center.x + invTransform[0][1] * this.center.y + invTransform[0][2]
        let cy = invTransform[1][0] * this.center.x + invTransform[1][1] * this.center.y + invTransform[1][2]

        action()

        r = this.state.tempConfig.rotation
        sx = this.state.tempConfig.scale.x
        sy = this.state.tempConfig.scale.y
        x = this.state.tempConfig.translation.x
        y = this.state.tempConfig.translation.y
        
        scale = [[sx, 0, 0,], [0, sy, 0], [0, 0, 1]]
        rotation = [[Math.cos(r), -Math.sin(r), 0], [Math.sin(r), Math.cos(r), 0], [0, 0, 1]]
        translation = [[1, 0, x], [0, 1, y], [0, 0, 1]]
        transform = multiply(translation, rotation, scale)

        let x1 = transform[0][0] * cx + transform[0][1] * cy + transform[0][2]
        let y1 = transform[1][0] * cx + transform[1][1] * cy + transform[1][2]

        this.state.tempConfig.translation.x += this.center.x - x1
        this.state.tempConfig.translation.y += this.center.y - y1
        console.log(cx,cy,x1,y1,this.center)
    }

    saveCalibration = async () => {
        if(this.state.loading) return
        try {
            this.setState({loading: true})
            await request("saveconfig", "POST", JSON.stringify(this.state.tempConfig), {"Content-Type": "application/json"})
            this.endCalibration(true)
        }
        catch(ex) {
            console.log(ex)
            this.setState({loading: false})
        }
    }    

    endCalibration = (saveConfig = false) => {
        request("stop")
        this.setState({loading: false})
        this.props.onComplete(saveConfig ? this.state.tempConfig : null)
    }

    round = (x, decimals = 1) => {
        const mult = Math.pow(10, decimals)
        return Math.round(x * mult) / mult
    }

    render = () => {
        return (
            <div className="popover">
                <div className="calibrate">
                    <div className="title">Calibrate</div>
                    <div className="controls">
                        <div className="control-section">
                            <div className="section-title">Translate</div>
                            <div className="section-title">
                                (<input type="number" value={this.round(this.state.tempConfig.translation.x, 2)} onChange={e => this.setCalibration("tx", e.target.value)} />,<input type="number" value={this.round(this.state.tempConfig.translation.y, 2)} onChange={e => this.setCalibration("ty", e.target.value)} />)
                            </div>
                            <div className="control-container">
                                <div><button onClick={() => this.updateCalibration("ty", 1)}><i className="fa-solid fa-angles-up"></i></button></div>
                                <div><button onClick={() => this.updateCalibration("ty", 0.1)}><i className="fa-solid fa-angle-up"></i></button></div>
                                <div>
                                    <button onClick={() => this.updateCalibration("tx", -1)}><i className="fa-solid fa-angles-left"></i></button>
                                    <button onClick={() => this.updateCalibration("tx", -0.1)}><i className="fa-solid fa-angle-left"></i></button>
                                    <button onClick={() => this.updateCalibration("ct")}><i className="fa-solid fa-trash-can"></i></button>
                                    <button onClick={() => this.updateCalibration("tx", 0.1)}><i className="fa-solid fa-angle-right"></i></button>
                                    <button onClick={() => this.updateCalibration("tx", 1)}><i className="fa-solid fa-angles-right"></i></button>
                                </div>
                                <div><button onClick={() => this.updateCalibration("ty", -0.1)}><i className="fa-solid fa-angle-down"></i></button></div>
                                <div><button onClick={() => this.updateCalibration("ty", -1)}><i className="fa-solid fa-angles-down"></i></button></div>
                            </div>
                        </div>
                        <div className="control-section">
                            <div className="section-title">Rotate, Scale</div>
                            <div className="section-title">
                                <input type="number" value={this.round(this.state.tempConfig.rotation, 3)} onChange={e => this.setCalibration("r", e.target.value)} />
                                (<input type="number" step="0.1" value={this.round(this.state.tempConfig.scale.x, 2)} onChange={e => this.setCalibration("sx", e.target.value)} />,<input type="number" step="0.1" value={this.round(this.state.tempConfig.scale.y, 2)} onChange={e => this.setCalibration("sy", e.target.value)} />)
                            </div>
                            <div className="control-container">
                                <div>
                                    <button onClick={() => this.updateCalibration("sb", -0.1)}><i className="fa-solid fa-minimize"></i></button>
                                    <span className="spacer"></span>
                                    <button onClick={() => this.updateCalibration("sy", -0.1)}>-0.1</button>                        
                                    <button onClick={() => this.updateCalibration("iy")}><i className="fa-solid fa-up-down"></i></button>
                                    <button onClick={() => this.updateCalibration("r", Math.PI / 4)}><i className="fa-solid fa-arrow-rotate-left"></i></button>
                                </div>
                                <div>
                                    <span className="spacer"></span>
                                    <button onClick={() => this.updateCalibration("sb", -0.01)}><i className="fa-solid fa-minimize"></i></button>
                                    <button onClick={() => this.updateCalibration("sy", -0.01)}>-0.01</button>
                                    <span className="spacer"></span>
                                    <button onClick={() => this.updateCalibration("ix")}><i className="fa-solid fa-left-right"></i></button>                        
                                </div>
                                <div>
                                    <button onClick={() => this.updateCalibration("sx", -0.1)}>-0.1</button>
                                    <button onClick={() => this.updateCalibration("sx", -0.01)}>-0.01</button>
                                    <button onClick={() => this.updateCalibration("cs")}><i className="fa-solid fa-trash-can"></i></button>
                                    <button onClick={() => this.updateCalibration("sx", 0.01)}>+0.01</button>
                                    <button onClick={() => this.updateCalibration("sx", 0.1)}>+0.1</button>
                                </div>
                                <div>
                                    <span className="spacer"></span>
                                    <span className="spacer"></span>
                                    <button onClick={() => this.updateCalibration("sy", 0.01)}>+0.01</button>
                                    <button onClick={() => this.updateCalibration("sb", 0.01)}><i className="fa-solid fa-maximize"></i></button>
                                    <span className="spacer"></span>
                                </div>
                                <div>
                                    <button onClick={() => this.updateCalibration("r", -Math.PI / 4)}><i className="fa-solid fa-arrow-rotate-right"></i></button>
                                    <span className="spacer"></span>
                                    <button onClick={() => this.updateCalibration("sy", 0.1)}>+0.1</button>
                                    <span className="spacer"></span>
                                    <button onClick={() => this.updateCalibration("sb", 0.1)}><i className="fa-solid fa-maximize"></i></button>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className="actions">
                        <button className="cancel" onClick={() => this.endCalibration(false)}>Cancel</button>
                        <button className="save" onClick={this.saveCalibration}>{this.state.loading ? <img className="spinning" src="loading-spinner-white.svg" /> : "Save"}</button>
                    </div>
                </div>
            </div>
        )
    }
}

export default Calibration