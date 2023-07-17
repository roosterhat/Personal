import React from 'react'
import Menu from './Menu';
import LoadingSpinner from './Spinners/loading1';
import { fetchWithToken } from '../Utility';

class Settings extends React.Component {
    constructor(props) {
        super(props);

        this.UpdateQueue = []

        this.state = {
            settings: props.Settings,
            config: props.Config,
            debugState: null,
            debugSetState: null,
            selectedState: null,
            targetState: {"power": {"active": true}, "states": []},
            loadStateDebug: false,
            loadSetStateDebug: false,
            saving: false
        }
    }

    componentDidUpdate () {
        while(this.UpdateQueue.length > 0)
            this.UpdateQueue.pop()();
    }

    render = () => {
        return (
            <Menu>
                <div className="edit">
                    <div className="btn-container editing">
                        <div className="title">Settings</div>
                        <div className='divider'></div>
                        <button className='btn cancel' onClick={this.props.cancel}><i className="fa-solid fa-xmark"></i></button>
                        <button className="btn confirm" onClick={this.complete}>{this.state.saving ? <LoadingSpinner /> : <i className="fa-solid fa-check"></i>}</button>
                    </div>
                    <div className="settings">
                        <div className="setting">
                            <div className="setting-title">Camera Index</div>
                            <input type="number" min="0" value={this.state.settings["cameraIndex"]} onChange={e => this.updateSettings("cameraIndex", Number(e.target.value))}/>
                        </div>
                        <div className="setting">
                            <div className="setting-title">Camera Exposure Setting</div>
                            <input type="number" max="0" min="-13" value={this.state.settings["cameraExposure"]} onChange={e => this.updateSettings("cameraExposure", Number(e.target.value))}/>
                        </div>
                        <div className="setting">
                            <div className="setting-title">Frame Refresh Delay (ms)</div>
                            <input type="number" min="0" value={this.state.settings["frameRefreshDelay"]} onChange={e => this.updateSettings("frameRefreshDelay", Number(e.target.value))}/>
                        </div>
                        <div className="setting">
                            <div className="setting-title">IR Trigger Attempts</div>
                            <input type="number" min="1" value={this.state.settings["triggerAttempts"]} onChange={e => this.updateSettings("triggerAttempts", Number(e.target.value))}/>
                        </div>
                        <div className="setting">
                            <div className="setting-title">Set State Delay (ms)</div>
                            <input type="number" min="0" value={this.state.settings["setStateDelay"]} onChange={e => this.updateSettings("setStateDelay", Number(e.target.value))}/>
                        </div>
                        {this.renderDebugState()}
                        {this.renderDebugSetState()}
                    </div>
                </div>
            </Menu>
        )
    }

    renderDebugSetState = () => {
        if(this.state.config) {
            return (
                <div className="setting">
                    <div className="setting-title">Debug Set State</div>
                    <div className="debug-container">
                        <div className="debug-properties-container">
                            <div className="power-state">
                                <div className="name">Power</div>
                                <button className={"state " + (this.state.targetState.power.active ? "on" : "off")} onClick={() => this.toggleState(this.state.targetState.power)}>
                                    {this.state.targetState.power.active ? "On" : "Off"}
                                </button>
                            </div>
                            {this.state.targetState.power.active ? 
                                <div className="state-groups">
                                    {this.state.config.actions.stateGroups.map(group => 
                                        <div className="state-group">
                                            <div className="name">{group.name}</div>
                                            <select onChange={e => this.toggleGroupState(group, e.target.value)}>
                                                <option value={null}></option>
                                                {group.states.map(s =>
                                                    <option value={s.id} selected={this.state.targetState.states.some(x => x.groupId == group.id && x.id == s.id)}>{s.name}</option>
                                                )}
                                            </select>
                                        </div>
                                    )}
                                </div>
                                : null
                            }
                        </div>
                        <div className="debug-properties-container">
                            <div>
                                <div className="debug-property">
                                    <div>IR Trigger Attempts</div>
                                    <input type="number" min="0" value={this.state.settings["triggerAttempts"]} onChange={e => this.updateSettings("triggerAttempts", Number(e.target.value))}/>
                                </div>
                                <div className="debug-property">
                                    <div>Set State Delay (ms)</div>
                                    <input type="number" min="0" value={this.state.settings["setStateDelay"]} onChange={e => this.updateSettings("setStateDelay", Number(e.target.value))}/>
                                </div>
                            </div>
                            <button className="debug-test" onClick={this.debugSetState}>{this.state.loadSetStateDebug ? <LoadingSpinner id="spinner" /> : "Test"}</button>
                        </div>
                        {this.state.debugSetState ?
                            <div className={"debug-result " + (this.state.debugSetState.success ? "success" : "error")}>
                                {(this.state.debugSetState.success ? "Success" : ("Error: " + this.state.debugSetState.error))}
                            </div>
                            : null
                        }
                    </div>
                </div>
            )
        }
        else {
            return null;
        }
    }

    renderDebugState = () => {
        if(this.state.config) {
            return (
                <div className="setting">
                    <div className="setting-title">Debug State</div>
                    <div className="debug-container">
                        <div className="debug-header">
                            <select name="buttons" id="toggle-select" onChange={() => this.setState({debugState: null})}>
                                {this.state.config.frame.states.map(x => <option key={x.id} value={x.id}>{x.name}</option>)}
                            </select>
                            <div>
                                <button className="debug" onClick={() => this.debugState()}>{this.state.loadStateDebug ? <LoadingSpinner id="spinner" /> : "Debug"}</button>
                                <button onClick={() => this.setState({debugState: null})}>Clear</button>
                            </div>
                        </div>
                        {this.state.debugState ? 
                            <div>
                                <div className="debug-canvas-container">
                                    <div className="canvas-container">
                                        <div>{`Activation (${Math.round(this.state.debugState.activation * 100) / 100}%)`}</div>
                                        <canvas id="debug-canvas-mask"></canvas>
                                    </div>
                                    <div className="canvas-container">
                                        <div>View</div>
                                        <canvas id="debug-canvas-patch"></canvas>
                                    </div>                                    
                                </div>
                                <div className="debug-status">{"Status: "}
                                    <span className={(this.state.selectedState.properties.stateActivationPercentage < this.state.debugState.activation ? "active" : "inactive")}>
                                        {(this.state.selectedState.properties.stateActivationPercentage < this.state.debugState.activation ? "Active" : "Inactive")}
                                    </span>
                                </div>
                                <div className="debug-properties-container">
                                    <div>
                                        <div className="debug-property">
                                            <div>CDT</div>
                                            <input type="number" min="0" max="200" 
                                                value={this.state.selectedState.properties.colorDistanceThreshold} 
                                                onChange={e => this.updateStateProperty("colorDistanceThreshold", Number(e.target.value))}/>
                                        </div>
                                        <div className="debug-property">
                                            <div>SAP</div>
                                            <input type="number" min="1" max="100" 
                                                value={this.state.selectedState.properties.stateActivationPercentage}
                                                onChange={e => this.updateStateProperty("stateActivationPercentage", Number(e.target.value))}/>
                                        </div>
                                        <div className="debug-active-color" style={{background: this.state.selectedState.properties.activeColor}}></div>
                                    </div>
                                    <button className="debug-test" onClick={() => this.debugState(this.state.selectedState)}>{this.state.loadStateDebug ? <LoadingSpinner id="spinner" /> : "Test"}</button>
                                </div>
                            </div>
                            : null
                        }
                    </div>                            
                </div>
            );
        }
        else 
            return null;
    }

    updateSettings = (key, value) => {
        this.state.settings[key] = value;
        this.setState({settings: this.state.settings})
    }

    complete = () => {
        this.setState({saving: true})
        this.props.complete(this.state.settings)
    }

    debugState = async (config) => {
        try{
            this.setState({loadStateDebug: true})
            if(config) {
                var body = JSON.stringify(this.state.selectedState)
                var response = await fetchWithToken(`api/debug/state/${this.state.config.id}/${this.state.selectedState.id}`, "POST", body, {"Content-Type": "application/json"})
                if(response.status == 200){
                    this.setState({debugState: await response.json()})
                }                        
            }
            else {
                var elem = document.getElementById("toggle-select")
                if(elem){
                    var id = elem.value;
                    if(id){
                        this.setState({selectedState: this.state.config.frame.states.find(x => x.id == id)})
                        var response = await fetchWithToken(`api/debug/state/${this.state.config.id}/${id}`)
                        if(response.status == 200){
                            this.setState({debugState: await response.json()})
                        }
                    }                    
                }
            }

            this.UpdateQueue.push(() => {
                this.addImageToCanvas("debug-canvas-mask", this.state.debugState["mask"], true);
                this.addImageToCanvas("debug-canvas-patch", this.state.debugState["patch"], false);
            })
        }
        finally {
            this.setState({loadStateDebug: false})
        }
    }

    debugSetState = async () => {
        try{
            this.setState({loadSetStateDebug: true})
            var body = JSON.stringify({"state": this.state.targetState, "settings": this.state.settings})
            var response = await fetchWithToken(`api/debug/setstate/${this.state.config.id}`, "POST", body, {"Content-Type": "application/json"})
            if(response.status == 200){
                this.setState({debugSetState: await response.json()})
            } 
            else {
                this.setState({debugSetState: {"success": false, "error": "unable to trigger Set State: " + await response.text()}})
            }
        }
        catch(ex) {
            this.setState({debugSetState: {"success": false, "error": "unable to trigger Set State: " + ex}})
        }
        finally {
            this.setState({loadSetStateDebug: false})
        }
    }

    toggleGroupState = (group, id) => {
        var oldState = this.state.targetState.states.find(x => x.groupId == group.id)
        if(oldState){
            var index = this.state.targetState.states.indexOf(oldState)
            this.state.targetState.states.splice(index, 1)
        }
        if(id){
            this.state.targetState.states.push({
                "id": id,
                "groupId": group.id,
                "active": true
            })
        }
    }

    toggleState = (state) => {
        state.active = !state.active
        this.setState({config: this.state.config})
    }

    addImageToCanvas = (id, data, bmp) => {
        var canvas = document.getElementById(id);
        var dims = canvas.getBoundingClientRect();
        canvas.width = dims.width;
        canvas.height = dims.height;
        var context = canvas.getContext("2d");
        context.clearRect(0, 0, canvas.width, canvas.height);

        var scale = Math.min(context.canvas.width / data[0].length, context.canvas.height / data.length);
        var result = context.createImageData(data[0].length * scale, data.length * scale);      
        for (var row = 0; row < result.height; row++) {
            for (var col = 0; col < result.width; col++) {
                var index = (row * result.width + col) * 4;
                var srcY = Math.floor(row / scale);
                var srcX = Math.floor(col / scale);
                if(bmp)
                    result.data.set(Array(3).fill(data[srcY][srcX]), index);
                else
                    result.data.set(data[srcY][srcX], index);
                result.data[index + 3] = 255
            }
        }
        context.putImageData(result, (context.canvas.width - result.width) / 2, (context.canvas.height - result.height) / 2);
    }

    updateStateProperty(key, value) {
        this.state.selectedState.properties[key] = value;
        this.setState({selectedState: this.state.selectedState});
    }
}

export default Settings