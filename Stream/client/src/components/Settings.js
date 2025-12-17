import React from 'react'
import Switch from './Switch';
import { X as XIcon } from 'lucide-react'

class Settings extends React.Component {
    constructor(props) {
        super(props);

        this.fields = [
            ["Ultrasonic Period (s)", "ultrasonicPeriod", "input"],
            ["Temperature Period (s)", "temperaturePeriod", "input"],
            ["ADS Period (s)", "ADSPeriod", "input"],
            ["Trend Line Accuracy", "trendlineAccuracy", "input"],
            ["Battery Min", "batteryMin", "input"],
            ["Battery Max", "batteryMax", "input"]
        ]

        this.props.setToggleSettings(this.toggleSettings)

        this.state = {
            displaySettings: false,
            settings: {}
        }
    }

    componentDidUpdate(prevProps) {
        if (prevProps.settings !== this.props.settings) {
            for (let k in this.props.settings)
                this.state.settings[k] = this.props.settings[k]

            this.setState(this.state.settings)
        }
    }

    updateSettings = (key, value) => {
        this.state.settings[key] = value;
        this.setState({ settings: this.state.settings })
    }

    toggleSettings = () => this.setState({ displaySettings: !this.state.displaySettings })

    saveSettings = () => {
        this.props.saveSettings(this.state.settings)
        this.toggleSettings()
    }

    toggleValue = (key) => {
        this.props.settings[key] = !this.props.settings[key]
        this.forceUpdate()
    }

    close = () => {
        for (let k in this.props.settings)
            this.state.settings[k] = this.props.settings[k]
        this.setState({ settings: this.state.settings })

        this.toggleSettings()
    }

    render = () => {
        return (
            <div className="settings" style={{ transform: `translateX(${this.state.displaySettings ? "0" : "100%"})`, transition: "all 0.5s ease" }}>
                <div className="title">
                    <b>Settings</b>
                    <button onClick={this.close} className='close'><XIcon /></button>
                </div>
                <div className="body">
                    {this.fields.map(x => 
                        (x[1] in this.state ?
                            <div className="setting" key={x[1]}>
                                <span>{x[0]}</span>
                                {x[2] == "input" ? 
                                    <input value={this.state.settings[x[1]]} onChange={e => this.updateSettings(x[1], e.target.value)} />
                                    : <Switch State={this.state.settings[x[1]]} OnSelect={x => this.toggleValue(x[1])} />
                                }
                            </div>
                            : null
                        )
                    )}                
                </div>
                <button onClick={this.saveSettings} className="save">Save</button>
            </div>
        )
    }
}

export default Settings