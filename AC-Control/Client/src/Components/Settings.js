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
            selectedState: null,
            loadDebug: false,
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
                            <div className="setting-title">Frame Refresh Delay</div>
                            <input type="number" min="0" value={this.state.settings["frameRefreshDelay"]} onChange={e => this.updateSettings("frameRefreshDelay", Number(e.target.value))}/>
                        </div>
                        <div className="setting">
                            <div className="setting-title">Debug State</div>
                            <div className="debug-container">
                                <div className="debug-header">
                                    <select name="buttons" id="toggle-select" onChange={() => this.setState({debugState: null})}>
                                        {this.state.config.frame.states.map(x => <option key={x.id} value={x.id}>{x.name}</option>)}
                                    </select>
                                    <div>
                                        <button className="debug" onClick={() => this.debugState()}>{this.state.loadDebug ? <LoadingSpinner id="spinner" /> : "Debug"}</button>
                                        <button onClick={() => this.setState({debugState: null})}>Clear</button>
                                    </div>
                                </div>
                                {this.state.debugState ? 
                                    <div>
                                        <div className="debug-canvas-container">
                                            <div className="canvas-container">
                                                <div>{`Activated (${Math.round(this.state.debugState.activation * 100) / 100}%)`}</div>
                                                <canvas id="debug-canvas-mask"></canvas>
                                            </div>
                                            <div className="canvas-container">
                                                <div>Mask</div>
                                                <canvas id="debug-canvas-patch"></canvas>
                                            </div>                                    
                                        </div>
                                        <div className="debug-status">{"Status: "}<span className={(this.state.selectedState.properties.stateActivationThreshold < this.state.debugState.activation ? "active" : "inactive")}>{(this.state.selectedState.properties.stateActivationThreshold < this.state.debugState.activation ? "Active" : "Inactive")}</span></div>
                                        <div className="debug-properties-container">
                                            <div>
                                                <div className="debug-property">
                                                    <div>CDT</div>
                                                    <input type="number" min="0" max="200" 
                                                        value={this.state.selectedState.properties.colorDistanceThreshold} 
                                                        onChange={e => this.updateStateProperty("colorDistanceThreshold", Number(e.target.value))}/>
                                                </div>
                                                <div className="debug-property">
                                                    <div>SAT</div>
                                                    <input type="number" min="1" max="100" 
                                                        value={this.state.selectedState.properties.stateActivationThreshold}
                                                        onChange={e => this.updateStateProperty("stateActivationThreshold", Number(e.target.value))}/>
                                                </div>
                                                <div className="debug-active-color" style={{background: this.state.selectedState.properties.activeColor}}></div>
                                            </div>
                                            <button className="debug-test" onClick={() => this.debugState(this.state.selectedState)}>{this.state.loadDebug ? <LoadingSpinner id="spinner" /> : "Test"}</button>
                                        </div>
                                    </div>
                                    : null
                                }
                            </div>                            
                        </div>
                    </div>
                </div>
            </Menu>
        )
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
            this.setState({loadDebug: true})
            if(config) {
                var body = JSON.stringify(this.state.selectedState)
                var response = await fetchWithToken(`api/state/debug/${this.state.config.id}/${this.state.selectedState.id}`, "POST", body, {"Content-Type": "application/json"})
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
                        var response = await fetchWithToken(`api/state/debug/${this.state.config.id}/${id}`)
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
            this.setState({loadDebug: false})
        }
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