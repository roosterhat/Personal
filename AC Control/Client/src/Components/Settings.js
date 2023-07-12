import React from 'react'
import Menu from './Menu';
import LoadingSpinner from './Spinners/loading1';

class Settings extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            settings: props.Settings,
            saving: false
        }
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
                            <input type="number" min="0" value={this.state.settings["cameraIndex"]} onChange={e => this.updateSettings("cameraIndex", e.target.value)}/>
                        </div>
                        <div className="setting">
                            <div className="setting-title">Camera Exposure Setting</div>
                            <input type="number" max="0" min="-13" value={this.state.settings["cameraExposure"]} onChange={e => this.updateSettings("cameraExposure", e.target.value)}/>
                        </div>
                        <div className="setting">
                            <div className="setting-title">Frame Refresh Delay</div>
                            <input type="number" min="0" value={this.state.settings["frameRefreshDelay"]} onChange={e => this.updateSettings("frameRefreshDelay", e.target.value)}/>
                        </div>
                    </div>
                </div>
            </Menu>
        )
    }

    updateSettings = (key, value) => {
        console.log(key, value)
        this.state.settings[key] = value;
        this.setState({settings: this.state.settings})
    }

    complete = () => {
        this.setState({saving: true})
        this.props.complete(this.state.settings)
    }
}

export default Settings