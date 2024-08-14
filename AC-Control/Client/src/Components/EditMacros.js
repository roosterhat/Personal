import React from 'react'
import Menu from './Menu';
import LoadingSpinner from './Spinners/loading1';
import { uuidv4 } from '../Utility';
import { Icons } from '../Assets/Icons';

class Macros extends React.Component {
    

    constructor(props) {
        super(props);

        this.state = {
            saving: false,
            showIconList: false,
            currentMacro: null,
            iconFilter: "",
            config: props.Config
        }
        
    }

    render() {
        return (
            <Menu>
                <div className="edit">
                    <div className="btn-container editing">
                        <div className="title">Macros</div>
                        <button className="btn" onClick={this.newMacro}><i className="fa-solid fa-plus"></i></button>
                        <div className='divider'></div>
                        <button className='btn cancel' onClick={this.props.cancel}><i className="fa-solid fa-xmark"></i></button>
                        <button className="btn confirm" onClick={this.complete}>{this.state.saving ? <LoadingSpinner /> : <i className="fa-solid fa-check"></i>}</button>
                    </div>
                    {this.state.config.macros.length > 0 ? 
                        <div className="schedules">
                            {this.state.config.macros.map((macro, index) => 
                                <div className="schedule">
                                    <div className="schedule-title">
                                        <button className="icon-select" onClick={() => this.selectAnIcon(macro)}><i className={macro.icon}></i></button>
                                        <input type="text" value={macro.name} placeholder="Macro name" onChange={e => this.setMacroValue(macro, "name", e.target.value)}></input>
                                        <button className="close" onClick={() => this.removeMacro(index)}><i className="fa-solid fa-xmark"></i></button>
                                    </div>
                                    <div className="schedule-body">
                                        <div className="schedule-state">
                                            <div className="power-state">
                                                <button className={"state " + (macro.state.power.active ? "on" : "off")} onClick={() => this.toggleState(macro.state.power)}>
                                                    {macro.state.power.active ? "On" : "Off"}
                                                </button>
                                            </div>
                                            {macro.state.power.active ? 
                                                <div className="state-group">
                                                    <select onChange={e => this.setCurrentOCR(macro, e.target.value)}>
                                                        <option value={null}></option>
                                                        {this.state.config.actions.ocr.map(o =>
                                                            <option value={o.id} selected={macro.state.ocr.some(x => x.id == o.id)} key={o.id}>{o.name}</option>
                                                        )}
                                                    </select>
                                                    {macro.state.ocr.length > 0 ?
                                                        <input type="number" min={this.props.Settings.minTemperature} max={this.props.Settings.maxTemperature} 
                                                            value={macro.state.ocr[0].target} 
                                                            onChange={e => this.updateOCRTarget(macro, Number(e.target.value))}/>
                                                        : null
                                                    }
                                                </div>
                                                : null 
                                            }
                                            {macro.state.power.active ? 
                                                <div className="state-groups">
                                                    {this.state.config.actions.stateGroups.map(group => 
                                                        <div className="state-group">
                                                            <div className="name">{group.name}</div>
                                                            <select onChange={e => this.toggleGroupState(macro, group, e.target.value)}>
                                                                <option value={null}></option>
                                                                {group.states.map(s =>
                                                                    <option value={s.id} selected={macro.state.states.some(x => x.groupId == group.id && x.id == s.id)}>{s.name}</option>
                                                                )}
                                                            </select>
                                                        </div>
                                                    )}
                                                </div>
                                                : null
                                            }
                                        </div>
                                    </div>
                                </div>
                            )}
                        </div>
                        : null
                    }
                </div>
                {this.state.showIconList ? 
                    <div className="icons-background">
                        <div className="icons-container">
                            <div className="icons-header">
                                <div className="name">Icons</div>
                                <input onChange={e => this.setState({iconFilter: e.target.value})} placeholder="Search"></input>
                                <button className="close" onClick={() => this.setState({showIconList: false})}><i className="fa-solid fa-xmark"></i></button>
                            </div>
                            <div className="icons">
                                {Icons.filter(x => !this.state.iconFilter || x.includes(this.state.iconFilter.toLowerCase())).map(x => 
                                    <button onClick={() => this.selectIcon(x)}><i className={x}></i></button>    
                                )}
                            </div>
                        </div>
                    </div>
                    : null
                }
            </Menu>
        );
    }

    complete = () => {
        this.setState({saving: true})
        this.props.onConfigChange(this.state.config)
        this.props.complete()
    }

    toggleState = (state) => {
        state.active = !state.active
        this.setState({config: this.state.config})
    }

    toggleGroupState = (macro, group, id) => {
        var oldState = macro.state.states.find(x => x.groupId == group.id)
        if(oldState){
            var index = macro.state.states.indexOf(oldState)
            macro.state.states.splice(index, 1)
        }
        if(id){
            macro.state.states.push({
                "id": id,
                "groupId": group.id,
                "active": true
            })
        }
    }

    setMacroValue = (macro, key, value) => {
        macro[key] = value;
        this.setState({config: this.state.config})
    }

    setCurrentOCR = (macro, id) => {
        var action = this.state.config.actions.ocr.find(x => x.id == id)
        macro.state.ocr = []
        if(action)
            macro.state.ocr.push(JSON.parse(JSON.stringify(action)))
        this.setState({config: this.state.config})
    }

    updateOCRTarget = (macro, target) => {
        if(macro.state.ocr.length > 0){
            macro.state.ocr[0].target = target
        }
        this.setState({config: this.state.config})
    }

    newMacro = () => {
        this.state.config.macros.push({
            "name": `Macro ${this.state.config.macros.length + 1}`,
            "id": uuidv4(),
            "state": {
                "power": { "active": true },
                "states": [],
                "ocr": []
            },
            "icon": Icons[Math.floor(Math.random() * Icons.length)]
        })
        this.setState({config: this.state.config})
    }

    removeMacro = (index) => {
        this.state.config.macros.splice(index, 1)
        this.setState({config: this.state.config})
    }

    selectAnIcon = (macro) => {
        this.setState({currentMacro: macro, showIconList: true})
    }

    selectIcon = (icon) => {
        this.state.currentMacro.icon = icon
        this.setState({currentMacro: this.state.currentMacro, showIconList: false})
    }
}

export default Macros