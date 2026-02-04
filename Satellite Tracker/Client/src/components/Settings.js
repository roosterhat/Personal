import React from 'react'
import Switch from './Switch';
import { X as XIcon } from 'lucide-react'

class Settings extends React.Component {
    constructor(props) {
        super(props);

        this.props.setToggleSettings(this.toggleSettings)

        this.state = { 
            displaySettings: false
        }

        this.trays = [
            "displayPathTrail",
            "usePathing"
        ]        
    }

    componentDidMount () {
        for(let key of this.trays)
            this.updateHeight(key)
    }

    toggleSettings = () => this.setState({displaySettings: !this.state.displaySettings})

    saveSettings = () => {
        this.props.saveSettings()
        this.toggleSettings()
    }

    toggleValue(key) {
        this.props.settings[key] = !this.props.settings[key]
        this.forceUpdate()
        if(this.trays.indexOf(key) >= 0)
            this.updateHeight(key)
    }

    updateHeight = (key) => {
        let tray = document.getElementById(`${key}-tray`)
        tray.style["height"] = this.props.settings[key] ? (tray.childNodes[0].clientHeight + 16) + "px" : "0"
    }

    render = () => {
        return (
            <div className="fixed top-0 h-full w-100 right-0 flex flex-col p-4 gap-4 bg-white shadow-2xl" style={{transform: `translateX(${this.state.displaySettings ? "0" : "100%"})`, transition: "all 0.5s ease"}}>
                <div className="flex justify-between items-center">
                    <b>Settings</b>
                    <button onClick={this.toggleSettings} className="cursor-pointer text-white bg-black rounded-sm p-1 bg-red-500"><XIcon /></button>
                </div>
                <div className="flex flex-col gap-4 h-full overflow-auto">
                    <div className="flex justify-between items-center gap-2">
                        <span>North Offset</span>
                        <input className="flex-1 border rounded p-2 font-mono text-sm w-30 h-6 grow-0 bg-white" placeholder={this.props.settings["offset"]} onChange={e => this.props.settings["offset"] = Number(e.target.value)} />
                    </div>
                    <div className="flex justify-between items-center gap-2">
                        <span>Idle Timeout (s)</span>
                        <input className="flex-1 border rounded p-2 font-mono text-sm w-30 h-6 grow-0 bg-white" placeholder={this.props.settings["idleTimeout"]} onChange={e => this.props.settings["idleTimeout"] = Number(e.target.value)} />
                    </div>
                    <div className="flex justify-between items-center gap-2">
                        <span>Homing Period (m)</span>
                        <input className="flex-1 border rounded p-2 font-mono text-sm w-30 h-6 grow-0 bg-white" placeholder={this.props.settings["homePeriod"]} onChange={e => this.props.settings["homePeriod"] = Number(e.target.value)} />
                    </div>
                    <div className="flex justify-between items-center gap-2">
                        <span>Stow Position</span>
                        <input className="flex-1 border rounded p-2 font-mono text-sm w-30 h-6 grow-0 bg-white" placeholder={this.props.settings["stowPosition"]} onChange={e => this.props.settings["stowPosition"] = Number(e.target.value)} />
                    </div>
                    <div className="flex justify-between items-center gap-2">
                        <span>Max Messages</span>
                        <input className="flex-1 border rounded p-2 font-mono text-sm w-30 h-6 grow-0 bg-white" placeholder={this.props.settings["maxMessages"]} onChange={e => this.props.settings["maxMessages"] = Number(e.target.value)} />
                    </div>                                      
                    <div className="flex justify-between items-center gap-2">
                        <span>Home On Start</span>
                        <Switch State={this.props.settings["homeOnStart"]} OnSelect={x => this.toggleValue("homeOnStart")}/>
                    </div>
                    <div className="flex justify-between items-center gap-2">
                        <span>Display Path</span>
                        <Switch State={this.props.settings["displayPath"]} OnSelect={x => this.toggleValue("displayPath")}/>
                    </div>
                    <div>
                        <div className="flex justify-between items-center gap-2">
                            <span>Display Path Trail</span>
                            <Switch State={this.props.settings["displayPathTrail"]} OnSelect={x => this.toggleValue("displayPathTrail")}/>
                        </div>
                        <div className="tray" id="displayPathTrail-tray">
                            <div className="tray-items">
                                <div className="flex justify-between items-center gap-2">
                                    <span>Trail Duration (s)</span>
                                    <input className="flex-1 border rounded p-2 font-mono text-sm w-30 h-6 grow-0 bg-white" placeholder={this.props.settings["pathTrailDuration"]} onChange={e => this.props.settings["pathTrailDuration"] = Number(e.target.value)} />
                                </div>
                                <div className="flex justify-between items-center gap-2">
                                    <span>Display Full Track</span>
                                    <Switch State={this.props.settings["displayFullTrack"]} OnSelect={x => this.toggleValue("displayFullTrack")}/>
                                </div>
                                <div className="flex justify-between items-center gap-2">
                                    <span>Only Display Tracking Trail</span>
                                    <Switch State={this.props.settings["onlyDisplayTrackingTrail"]} OnSelect={x => this.toggleValue("onlyDisplayTrackingTrail")}/>
                                </div> 
                            </div>
                        </div>    
                    </div>
                    <div>
                        <div className="flex justify-between items-center gap-2">
                            <span>Use Pathing</span>
                            <Switch State={this.props.settings["usePathing"]} OnSelect={x => this.toggleValue("usePathing")}/>
                        </div>
                        <div className="tray" id="usePathing-tray">
                            <div className="tray-items">
                                <div className="flex justify-between items-center gap-2">
                                    <span>Grid Resolution Modifier</span>
                                    <input className="flex-1 border rounded p-2 font-mono text-sm w-30 h-6 grow-0 bg-white" placeholder={this.props.settings["gridResolutionModifier"]} onChange={e => this.props.settings["gridResolutionModifier"] = Number(e.target.value)} />
                                </div>  
                                <div className="flex justify-between items-center gap-2">
                                    <span>Wait While Pathing</span>
                                    <Switch State={this.props.settings["waitWhilePathing"]} OnSelect={x => this.toggleValue("waitWhilePathing")}/>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <button onClick={this.saveSettings} className="cursor-pointer bg-green-500 text-white rounded-md p-1">Save</button>
            </div>
        )
    }
}

export default Settings