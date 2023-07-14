import React from 'react'
import Menu from './Menu';
import LoadingSpinner from './Spinners/loading1';
import { fetchWithToken } from '../Utility';

class EditActions extends React.Component {
    Operations = [
        {"name": "(", "type": "operator", "color": "#ffe6ab"},
        {"name": ")", "type": "operator", "color": "#ffe6ab"},
        {"name": "not", "type": "operator", "color": "#cdffcd"},
        {"name": "and", "type": "operator", "color": "#cdffcd"},
        {"name": "or", "type": "operator", "color": "#cdffcd"}
    ]

    constructor(props) {
        super(props);

        this.state = {
            config: props.Config,
            testResult: null,
            loadingTest: false,
            saving: false,
            actionTriggers: {}
        }

        if(!this.state.config.actions.digits) {
            this.state.config.actions.digits = []
        }
        if(!this.state.config.actions.temperature) {
            this.state.config.actions.temperature = [
                {"name": "Up", "button": null},
                {"name": "Down", "button": null}
            ]
        }
        if(!this.state.config.actions.power) {
            this.state.config.actions.power = {
                "name": "Power",
                "stateEquation": [],
                "button": null
            }
        }
        if(!this.state.config.actions.stateGroups) {
            this.state.config.actions.stateGroups = []
        }
    }

    render = () => {
        return (
            <Menu>
                <div className="edit">
                    <div className="btn-container editing">
                        <div className="title">Actions</div>
                        <div className='divider'></div>
                        <button className='btn cancel' onClick={this.props.cancel}><i className="fa-solid fa-xmark"></i></button>
                        <button className="btn confirm" onClick={this.complete}>{this.state.saving ? <LoadingSpinner /> : <i className="fa-solid fa-check"></i>}</button>
                    </div>
                    <div className="actions">
                        <div className="section digits">
                            <div className="section-header">OCR</div>
                            <div className="section-body">

                            </div>
                        </div>
                        <div className="section temperature">
                            <div className="section-header">Temperature</div>
                            <div className="section-body">
                                {this.state.config.actions.temperature.map(x => 
                                    <div className="pairing-container">
                                        <div className="name">{x.name}</div>
                                        <select onChange={e => this.setButton(x, e.target.value)}>
                                            {this.state.config.buttons.map(b =>
                                                <option selected={x.button == b.id} value={b.id}>{b.name}</option>
                                            )}
                                        </select>
                                    </div>
                                )}
                            </div>                            
                        </div>
                        <div className="section power">
                            <div className="section-header">Power</div>
                            <div className="section-body">
                                <div className="pairing-container">
                                    <div className="name">Power</div>
                                    <select onChange={e => this.setButton(this.state.config.actions.power, e.target.value)}>
                                        {this.state.config.buttons.map(b =>
                                            <option selected={this.state.config.actions.power.button == b.id} value={b.id}>{b.name}</option>
                                        )}
                                    </select>
                                </div>
                                <div className="pairing-container">
                                    <div className="equation-container">
                                        <div className="equation-header">
                                            <div className="name">State Equation</div>
                                            <button className="trigger" onClick={() => this.triggerAction(this.state.config.actions.power)}>
                                                {this.state.actionTriggers.power ? <LoadingSpinner id="spinner" /> : "Trigger"}
                                            </button>
                                        </div>                                        
                                        <div className="operations">
                                            {this.Operations.map(x => 
                                                <button className="operation" onClick={() => this.addOperation(x)}>{x.name}</button>    
                                            )}
                                            <div className="divider" />
                                            {this.state.config.frame.states.filter(x => !this.state.config.actions.power.stateEquation.some(y => x.id == y.id)).map(x =>
                                                <button className="operation" onClick={() => this.addOperation({"name": x.name, "id": x.id, "type": "state", "color": "#c5c5ff"})}>{x.name}</button>
                                            )}
                                        </div>
                                        <div className="equation">
                                            {this.state.config.actions.power.stateEquation.map((x,index) =>
                                                <button className="operation" onClick={() => this.removeOperation(index)} style={{background: x.color}}>{x.name}</button>
                                            )}
                                        </div>
                                        <div className="test-equation">
                                            <button className="trigger test" onClick={this.testEqustion}>{this.state.loadingTest ? <LoadingSpinner id="spinner" /> : "Test"}</button>
                                            { this.state.testResult ? this.renderTestResult() : null }
                                        </div>
                                    </div>
                                </div>
                            </div>                            
                        </div>
                        <div className="section temperature">
                            <div className="section-header">
                                <div className="header-group">
                                    <span>State Groups</span>
                                    <button className="btn" ><i className="fa-solid fa-plus"></i></button>
                                </div>                            
                            </div>
                            <div className="section-body">

                            </div>
                        </div>
                    </div>
                </div>
            </Menu>
        )
    }

    complete = () => {
        if(this.state.config.buttons.length > 0){
            var defaultId = this.state.config.buttons[0].id;
            for(var temp in this.state.config.actions.temperatures)
                if(!temp.button)
                    temp.button = defaultId
            if(!this.state.config.actions.power.button)
                this.state.config.actions.power.button = defaultId
            for(var group in this.state.config.actions.stateGroups)
                if(!group.button)
                    group.button = defaultId
        }
        this.setState({saving: true})
        this.props.onConfigChange(this.state.config)
        this.props.complete()
    }

    renderTestResult = () => {
        if(this.state.testResult.success){
            return <div>Parsed successfully: Power: <span className={"power "+(this.state.testResult.active ? "on" : "off")}>{this.state.testResult.active ? "On" : "Off"}</span></div>
        }
        else {
            return <div>Error: <span className="error">{this.state.testResult.error}</span></div>
        }
    }

    testEqustion = async () => {
        try {
            this.setState({loadingTest: true})
            var body = JSON.stringify(this.state.config.actions.power)
            var response = await fetchWithToken(`api/debug/power/${this.state.config.id}`, "POST", body, {"Content-Type": "application/json"})
            if(response.status == 200){
                this.setState({testResult: await response.json()})
            }
        }
        finally {
            this.setState({loadingTest: false})
        }
    }

    triggerAction = async (action) => {
        this.state.actionTriggers[action.name] = true
        this.setState({actionTriggers: this.state.actionTriggers})
    }

    setButton = (action, id) => {
        action.button = id;
        this.setState({config: this.state.config})
    }

    addOperation = (operation) => {
        this.state.config.actions.power.stateEquation.push(operation)
        this.setState({config: this.state.config})
    }

    removeOperation = (index) => {
        this.state.config.actions.power.stateEquation.splice(index, 1);
        this.setState({config: this.state.config})
    }
}

export default EditActions