import React from 'react'
import { request } from "../lib/Utility.js"

class Settings extends React.Component {
    constructor(props) {
        super(props)
        
        this.setSettings = props.setSettings
        this.originalSettings = null

        this.state = {
            currentSettings: null,
            loading: false,
            open: false
        }        
    }

    componentDidUpdate() {
        this.props.onToggle.func = this.toggle

        if(this.originalSettings == null) {
            this.originalSettings = {}
            for(let key in this.props.settings)
                this.originalSettings[key] = this.props.settings[key]
        }

        if(this.state.currentSettings == null) {
            this.state.currentSettings = {}
            for(let key in this.props.settings)
                this.state.currentSettings[key] = this.props.settings[key]
            this.setState({currentSettings: this.state.currentSettings})    
        }
    }

    getValue = (key) => {
        return this.state.currentSettings ? this.state.currentSettings[key] : ""
    }

    setValue = (key, value) => {
        this.state.currentSettings[key] = value
        this.setState({currentSettings: this.state.currentSettings})
        this.setSettings(this.state.currentSettings)
    }

    saveSettings = async () => {
        try {
            this.setState({loading: true})
            await request("savesettings", "POST", JSON.stringify(this.state.currentSettings), {"Content-Type": "application/json"})
            this.close(true)
        }
        catch(ex) {
            console.log(ex)
            this.setState({loading: false})
        }        
    }

    toggle = () => {
        this.state.open = !this.state.open
        this.setState({open: this.state.open})
        if(!this.state.open) {
            this.close(false)
        }
    }

    close = (saveSettings) => {
        this.setState({open: false})
        setTimeout(() => { 
            if(!this.state.open) {
                let newSettings = saveSettings ? this.state.currentSettings : this.originalSettings
                this.setSettings(newSettings) 
                for(let key in newSettings)
                    this.state.currentSettings[key] = newSettings[key]
                this.setState({currentSettings: this.state.currentSettings})   
                this.originalSettings = newSettings 
            }
        }, 500)
    }


    render = () => {
        return (
            <div className={"settings " + (this.state.open ? "open" : "")}>
                <div className="settings-wrapper">
                    <div className="title">Settings</div>
                    <div className="settings-container">
                        <div className="setting">
                            <div className="name">Aspect Ratio</div>
                            <input type="number" min="0.1" step="0.1" value={this.getValue("aspectRatio")} onChange={e => this.setValue("aspectRatio", Math.max(e.target.value, 0.1))}/>
                        </div>
                        <div className="setting">
                            <div className="name">Trace Color</div>
                            <input type="color" value={this.getValue("traceColor")} onChange={e => this.setValue("traceColor", e.target.value)}/>
                        </div>
                        <div className="setting">
                            <div className="name">Grid Color</div>
                            <input type="color" value={this.getValue("gridColor")} onChange={e => this.setValue("gridColor", e.target.value)}/>
                        </div>
                        <div className="setting">
                            <div className="name">Background Color</div>
                            <input type="color" value={this.getValue("backgroundColor")} onChange={e => this.setValue("backgroundColor", e.target.value)}/>
                        </div>
                        <div className="setting">
                            <div className="name">Display Grid</div>
                            <div className={"switch" + (this.getValue("displayGrid") ? " selected" : "")} onClick={e => this.setValue("displayGrid", !this.state.currentSettings["displayGrid"])}></div>
                        </div>
                        <div className="setting">
                            <div className="name">Glow Effect</div>
                            <div className={"switch" + (this.getValue("glowEffect") ? " selected" : "")} onClick={e => this.setValue("glowEffect", !this.state.currentSettings["glowEffect"])}></div>
                        </div>
                        <div className="setting">
                            <div className="name">Glow Strength</div>
                            <input type="number" min="0" value={this.getValue("glowStrength")} onChange={e => this.setValue("glowStrength", Math.round(Math.max(e.target.value, 0)))}/>
                        </div>
                        <div className="setting">
                            <div className="name">Sample Rate</div>
                            <input type="number" min="0" value={this.getValue("sampleRate")} onChange={e => this.setValue("sampleRate", Math.round(Math.max(e.target.value, 0)))}/>
                        </div>
                        <div className="setting">
                            <div className="name">Kbps</div>
                            <input type="number" min="0" value={this.getValue("kbps")} onChange={e => this.setValue("kbps", Math.round(Math.max(e.target.value, 0)))}/>
                        </div>
                        <div className="setting">
                            <div className="name">Gain</div>
                            <input type="number" min="0" step="0.001" value={this.getValue("gain")} onChange={e => this.setValue("gain", Math.max(e.target.value, 0))}/>
                        </div>
                        <div className="setting">
                            <div className="name">Frequency</div>
                            <input type="number" min="1" step="1" value={this.getValue("frequency")} onChange={e => this.setValue("frequency", Math.round(Math.max(e.target.value, 1)))}/>
                        </div>
                        <div className="setting">
                            <div className="name">Duration (ms)</div>
                            <input type="number" min="100" step="1" value={this.getValue("duration")} onChange={e => this.setValue("duration", Math.round(Math.max(e.target.value, 100)))}/>
                        </div>
                        <div className="setting">
                            <div className="name">Show Hull</div>
                            <div className={"switch" + (this.getValue("showHull") ? " selected" : "")} onClick={e => this.setValue("showHull", !this.state.currentSettings["showHull"])}></div>
                        </div>
                        <div className="setting">
                            <div className="name">Draw Extra Points</div>
                            <div className={"switch" + (this.getValue("drawExtraPoints") ? " selected" : "")} onClick={e => this.setValue("drawExtraPoints", !this.state.currentSettings["drawExtraPoints"])}></div>
                        </div>
                    </div>
                    <div className="actions">
                        <button className="cancel" onClick={() => this.close(false)}>Cancel</button>
                        <button className="save" onClick={this.saveSettings}>{this.loading ? <img className="spinning" src="loading-spinner-white.svg" /> : "Save"}</button>
                    </div>
                </div>
            </div>
        )
    }
}

export default Settings