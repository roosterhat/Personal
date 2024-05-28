import React from 'react'
import Menu from './Menu';
import LoadingSpinner from './Spinners/loading1';
import { fetchWithToken, uuidv4 } from '../Utility';

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
            loadingModels: true,
            models: [],
            saving: false,
            actionTriggers: {}
        }

        this.listModels();

        var defaultButton = this.state.config.buttons[0].id
        var defaultView = this.state.config.frame.ocr[0].id
        

        if(!this.state.config.actions.ocr) {
            var defaultModel = this.state.models["OCR"][0]
            this.state.config.actions.ocr = [
                {
                    "name": "Temperature",
                    "buttons": [
                        {"name": "Up", "button": defaultButton},
                        {"name": "Down", "button": defaultButton}
                    ],
                    "view": {
                        "id": defaultView,
                        "properties": {
                            "scale": 1,
                            "grayscale": true,
                            "invert": true
                        }
                    },
                    "model": defaultModel,
                    "id": uuidv4()
                }
            ]
        }
        if(!this.state.config.actions.power) {
            this.state.config.actions.power = {
                "name": "Power",
                "stateEquation": [],
                "button": defaultButton
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
                            <div className="section-header">
                                <div className="header-group">
                                    <span>OCR</span>
                                    <button className="add-group" onClick={this.addOCR}><i className="fa-solid fa-plus"></i></button>
                                </div>
                            </div>
                            <div className="section-body">
                                {this.state.config.actions.ocr.filter(x => x.name != "Temperature").map((o, index) => 
                                    this.renderOCRAction(o, index)
                                )}
                            </div>
                        </div>
                        <div className="section temperature">
                            <div className="section-header">Temperature</div>
                            <div className="section-body">
                                {this.renderOCRAction(this.state.config.actions.ocr.find(x => x.name == "Temperature"))}
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
                                        </div>                                        
                                        <div className="operations">
                                            {this.Operations.map(x => 
                                                <button className="operation" onClick={() => this.addOperation(x)}>{x.name}</button>    
                                            )}
                                            <div className="divider" />
                                            {this.state.config.frame.states.map(x =>
                                                <button className="operation" onClick={() => this.addOperation({"name": x.name, "id": x.id, "type": "state", "color": "#c5c5ff"})}>{x.name}</button>
                                            )}
                                        </div>
                                        <div className="equation">
                                            {this.state.config.actions.power.stateEquation.map((x,index) =>
                                                <button className="operation" onClick={() => this.removeOperation(index)} style={{background: x.color}}>{x.name}</button>
                                            )}
                                        </div>
                                        <div className="test-equation">
                                            <button className="trigger test" onClick={this.testEquation}>{this.state.loadingTest ? <LoadingSpinner id="spinner" /> : "Test"}</button>
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
                                    <button className="add-group" onClick={this.addGroup}><i className="fa-solid fa-plus"></i></button>
                                </div>                            
                            </div>
                            {this.state.config.actions.stateGroups.length > 0 ?
                                <div className="section-body">
                                    {this.state.config.actions.stateGroups.map((group, groupIndex) => 
                                        <div className="pairing-container" key={group.id}>
                                            <div className="group-container">
                                                <div className="group-header">
                                                    <input value={group.name} placeholder="Group name" onChange={e => this.setValue(group, "name", e.target.value)}></input>
                                                    <select onChange={e => this.setValue(group, "button", e.target.value)}>
                                                        {this.state.config.buttons.map(b =>
                                                            <option selected={group.button == b.id} value={b.id}>{b.name}</option>
                                                        )}
                                                    </select>
                                                    <button className="close" onClick={() => this.removeItem(this.state.config.actions.stateGroups, groupIndex)}><i className="fa-solid fa-xmark"></i></button>
                                                </div>
                                                <div className="group-header">
                                                    <div className="name">State Model</div>
                                                    <div className="model-select">
                                                        {this.state.loadingModels ? 
                                                            <LoadingSpinner /> : 
                                                            <select onChange={e => this.setValue(group, "model", e.target.value)}>
                                                                {this.state.models["State"].map(model => 
                                                                    <option selected={group.model == model} value={model}>{model}</option>
                                                                )}
                                                            </select>
                                                        }
                                                    </div>
                                                </div>
                                                <div className="group-body">
                                                    <div className="name">States</div>
                                                    <div className="add-state-container">
                                                        <select id={`group${groupIndex}`}>
                                                            {this.state.config.frame.states.filter(s => !this.state.config.actions.stateGroups.some(sg => sg.states.some(x => x.id == s.id))).map(s =>
                                                                <option value={s.id}>{s.name}</option>
                                                            )}
                                                        </select>
                                                        <button className="add-state" onClick={e => this.addState(group, groupIndex)}><i className="fa-solid fa-plus"></i></button>
                                                    </div>
                                                </div>
                                                { group.states.length > 0 ?
                                                    <div className="states">
                                                        {group.states.map((s, index) =>
                                                            <div className="state">
                                                                <div>{s.name}</div>
                                                                <button className="close" onClick={() => this.removeState(group, index)}><i className="fa-solid fa-xmark"></i></button>
                                                            </div>
                                                        )}
                                                    </div>
                                                    : null
                                                }
                                            </div>
                                        </div>
                                    )}
                                </div>
                                : null
                            }                            
                        </div>
                    </div>
                </div>
            </Menu>
        )
    }

    renderOCRAction = (ocr, index) => {
        return (
            <div className="pairing-container" key={ocr.id}>
                <div className="group-container">
                    {index != null ? 
                        <div className="group-header">
                            <input value={ocr.name} placeholder="Action name" onChange={e => this.setValue(ocr, "name", e.target.value)}></input>
                            <button className="close" onClick={() => this.removeItem(this.state.config.actions.ocr, index+1)}><i className="fa-solid fa-xmark"></i></button>
                        </div>
                        : null
                    }
                    {ocr.buttons.map(x => 
                        <div className="pairing-container" key={x.id}>
                            <div className="name">{x.name}</div>
                            <select onChange={e => this.setValue(x, "button", e.target.value)}>
                                {this.state.config.buttons.map(b =>
                                    <option selected={x.button == b.id} value={b.id} key={b.id}>{b.name}</option>
                                )}
                            </select>
                        </div>
                    )}
                    <div className="pairing-container">
                        <div className="name">OCR View</div>
                        <div className="model-select">
                            {this.state.loadingModels ? 
                                <LoadingSpinner /> : 
                                <select onChange={e => this.setValue(ocr, "model", e.target.value)}>
                                    {this.state.models["OCR"].map(model => 
                                        <option selected={ocr.model == model} value={model}>{model}</option>
                                    )}
                                </select>
                            }
                        </div>
                        <select onChange={e => this.setValue(ocr.view, "id", e.target.value)}>
                            {this.state.config.frame.ocr.map(v => 
                                <option selected={ocr.view.id == v.id} value={v.id}>{v.name}</option>
                            )}
                        </select>
                    </div>
                    <div className="pairing-container">
                        <div className="name">Filters</div>
                        <div className="state-properties">
                            <div>
                                <div>Gray Scale</div>
                                <input type="checkbox"
                                    checked={ocr.view.properties.grayscale}
                                    onChange={e => this.setValue(ocr.view.properties, "grayscale", !ocr.view.properties.grayscale)}/>
                            </div>
                            <div>
                                <div>Invert</div>
                                <input type="checkbox"
                                    checked={ocr.view.properties.invert}
                                    onChange={e => this.setValue(ocr.view.properties, "invert", !ocr.view.properties.invert)}/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        )
    }

    addOCR = () => {
        if(this.state.config.frame.ocr.length <= this.state.config.actions.ocr.length)
            return;

        var defaultButton = this.state.config.buttons[0].id
        var defaultView = this.state.config.frame.ocr[0].id
        var defaultModel = this.loadingModels ? null : this.state.models["OCR"][0]

        this.state.config.actions.ocr.push(
            {
                "name": "",
                "buttons": [
                    {"name": "Trigger", "button": defaultButton}
                ],
                "view": {
                    "id": defaultView,
                    "properties": {
                        "scale": 1,
                        "grayscale": true,
                        "invert": true
                    }
                },
                "model": defaultModel,
                "id": uuidv4()
            }
        )
        this.setState({config: this.state.config})
    }

    complete = () => {
        this.setState({saving: true})
        this.props.onConfigChange(this.state.config)
        this.props.complete()
    }

    renderTestResult = () => {
        if(this.state.testResult.success){
            return <div>Power: <span className={"power "+(this.state.testResult.active ? "on" : "off")}>{this.state.testResult.active ? "On" : "Off"}</span></div>
        }
        else {
            return <div>Error: <span className="error">{this.state.testResult.error}</span></div>
        }
    }

    testEquation = async () => {
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

    setValue = (action, key, value) => {
        action[key] = value
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

    addGroup = () => {
        var defaultButton = this.state.config.buttons[0].id

        this.state.config.actions.stateGroups.push({
            "states": [],
            "button": defaultButton,
            "name": "Group " + this.state.config.actions.stateGroups.length,
            "model": this.loadingModels ? null : this.state.models["State"][0],
            "id": uuidv4()
        })
        this.setState({config: this.state.config})
    }

    removeItem = (list, index) => {
        list.splice(index, 1)
        this.setState({config: this.state.config})
    }

    addState = (group, index) => {
        var elem = document.getElementById(`group${index}`)
        if(elem){
            var id = elem.value
            if(id) {
                var state = this.state.config.frame.states.find(x => x.id == id)
                group.states.push({'name': state.name, 'id': state.id})
                this.setState({config: this.state.config})
            }
        }
    }

    removeState = (group, index) => {
        group.states.splice(index, 1)
        this.setState({config: this.state.config})
    }

    listModels = async () => {
        try {
            this.setState({loadingModels: true})
            const response = await fetchWithToken(`api/models`)
            if(response.status == 200) {
                var models = await response.json()
                models.sort()
                this.setState({models: models})
            }
        }
        catch(ex) {
            console.error(ex);
        } 
        finally { 
            this.setState({loadingModels: false})
        }
    }
}

export default EditActions