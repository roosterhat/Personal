import React from 'react'
import Menu from './Menu';
import LoadingSpinner from './Spinners/loading1';
import { fetchWithToken, uuidv4 } from '../Utility';

class Schedules extends React.Component {
    Days = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"]
    Operations = [
        {"name": ">", "type": "operator", "color": "#cdffcd"},
        {"name": "<", "type": "operator", "color": "#cdffcd"},
        {"name": "=", "type": "operator", "color": "#cdffcd"},        
        {"name": "(", "type": "operator", "color": "#ffe6ab"},
        {"name": ")", "type": "operator", "color": "#ffe6ab"},
        {"name": "not", "type": "operator", "color": "#cdffcd"},
        {"name": "and", "type": "operator", "color": "#cdffcd"},
        {"name": "or", "type": "operator", "color": "#cdffcd"}
    ]
    System = [
        {"name": "On", "type": "system", "color": "#00ff00"},
        {"name": "Off", "type": "system", "color": "#ff0000"}
    ]
    Sensors = [
        {"name": "Temperature", "type": "sensor", "color": "#abd8ff"},
        {"name": "Humidity", "type": "sensor", "color": "#abd8ff"}        
    ]

    constructor(props) {
        super(props);

        this.state = {
            saving: false,
            config: props.Config,
            scheduleStates: {}
        }        

        for(var schedule of props.Config.schedules)
            this.state.scheduleStates[schedule.id] = { expanded: schedule.conditionEquation.length > 0, index: schedule.conditionEquation.length, loadingTest: false, testResult: null }
    }

    render() {
        return (
            <Menu>
                <div className="edit">
                    <div className="btn-container editing">
                        <div className="title">Schedules</div>
                        <button className="btn" onClick={this.newSchedule}><i className="fa-solid fa-plus"></i></button>
                        <div className='divider'></div>
                        <button className='btn cancel' onClick={this.props.cancel}><i className="fa-solid fa-xmark"></i></button>
                        <button className="btn confirm" onClick={this.complete}>{this.state.saving ? <LoadingSpinner /> : <i className="fa-solid fa-check"></i>}</button>
                    </div>
                    {this.state.config.schedules.length > 0 ? 
                        <div className="schedules">
                            {this.state.config.schedules.map((schedule, scheduleIndex) => 
                                <div className={"schedule" + (schedule.enabled ? "" : " disabled")}>
                                    <div className="schedule-title">
                                        <input type="checkbox" checked={schedule.enabled} onChange={e => this.setScheduleValue(schedule, "enabled", !schedule.enabled)}></input>
                                        <input type="text" value={schedule.name} placeholder="Schedule name" onChange={e => this.setScheduleValue(schedule, "name", e.target.value)}></input>
                                        <button className="close" onClick={() => this.removeSchedule(scheduleIndex)}><i className="fa-solid fa-xmark"></i></button>
                                    </div>
                                    <div className="schedule-body">
                                        <div className="datetime-select">
                                            <div className="date-select">
                                                {this.Days.map(day => 
                                                    <button className={"day"+(schedule.days.some(x => x == day) ? " selected" : "")} onClick={() => this.toggleDay(schedule, day)}>{day[0]}</button>
                                                )}
                                            </div>
                                            <div>
                                                <input className={schedule.anytime ? "disabled" : ""} type="time" value={schedule.time} onChange={e => this.setScheduleValue(schedule, "time", e.target.value)}></input>
                                                <div className="anytime"><input type="checkbox" value={schedule.anytime} onChange={e => this.setScheduleValue(schedule, "anytime", !schedule.anytime)}/> Any time</div>
                                            </div>
                                        </div>
                                        <div className="schedule-state">
                                            <div className="power-state">
                                                <button className={"state " + (schedule.state.power.active ? "on" : "off")} onClick={() => this.toggleState(schedule.state.power)}>
                                                    {schedule.state.power.active ? "On" : "Off"}
                                                </button>
                                            </div>
                                            {schedule.state.power.active ? 
                                                <div className="state-group">
                                                    <select onChange={e => this.setCurrentOCR(schedule, e.target.value)}>
                                                        <option value={null}></option>
                                                        {this.state.config.actions.ocr.map(o =>
                                                            <option value={o.id} selected={schedule.state.ocr.some(x => x.id == o.id)} key={o.id}>{o.name}</option>
                                                        )}
                                                    </select>
                                                    {schedule.state.ocr.length > 0 ?
                                                        <input type="number" min={this.props.Settings.minTemperature} max={this.props.Settings.maxTemperature} 
                                                            value={schedule.state.ocr[0].target} 
                                                            onChange={e => this.updateOCRTarget(schedule, Number(e.target.value))}/>
                                                        : null
                                                    }
                                                </div>
                                                : null 
                                            }
                                            {schedule.state.power.active ? 
                                                <div className="state-groups">
                                                    {this.state.config.actions.stateGroups.map(group => 
                                                        <div className="state-group">
                                                            <div className="name">{group.name}</div>
                                                            <select onChange={e => this.toggleGroupState(schedule, group, e.target.value)}>
                                                                <option value={null}></option>
                                                                {group.states.map(s =>
                                                                    <option value={s.id} selected={schedule.state.states.some(x => x.groupId == group.id && x.id == s.id)}>{s.name}</option>
                                                                )}
                                                            </select>
                                                        </div>
                                                    )}
                                                </div>
                                                : null
                                            }
                                        </div>
                                        <div className="conditions">
                                            <button className={"title" + (this.state.scheduleStates[schedule.id].expanded ? " expanded" : "")} onClick={() => this.toggleConditions(schedule)}><span>Conditions</span><i class="fa-solid fa-chevron-up"></i></button>
                                            <div className={"conditions-body" + (this.state.scheduleStates[schedule.id].expanded ? " expanded" : "")}>
                                                <div className="inner">
                                                    <div className="equation-header">
                                                        <div className="name">Condition Equation</div>
                                                    </div>                                        
                                                    <div className="operations">
                                                        {this.Operations.map(x => 
                                                            <button className="operation" onClick={() => this.addOperation(schedule, x)}>{x.name}</button>    
                                                        )}
                                                        <div className="divider" />
                                                        {this.state.config.frame.states.map(x =>
                                                            <button className="operation" onClick={() => this.addOperation(schedule, {"name": x.name, "id": x.id, "type": "state", "color": "#c5c5ff"})}>{x.name}</button>
                                                        )}
                                                        <div className="divider" />
                                                        {this.System.map(x =>
                                                            <button className="operation" onClick={() => this.addOperation(schedule, x)}>{x.name}</button>
                                                        )}
                                                        <div className="divider" />
                                                        {this.Sensors.map(x =>
                                                            <button className="operation" onClick={() => this.addOperation(schedule, x)}>{x.name}</button>
                                                        )}
                                                        <div className="divider" />
                                                        <button className="operation" onClick={() => this.addOperation(schedule, {"name": "#", "type": "value", "color": "#abd8ff"})}>#</button>                                                        
                                                    </div>
                                                    <div className="equation">
                                                        {schedule.conditionEquation.map((x,index) => 
                                                            <>
                                                                <div class={"cursor" + (this.state.scheduleStates[schedule.id].index == index ? " selected" : "")} onClick={() => this.setIndex(schedule, index)}></div>
                                                                {x.type == "value" ? 
                                                                    <div className="operation" style={{background: x.color}}>
                                                                        <input type="number" value={x.value} onChange={e => this.updateValue(x, Number(e.target.value), e)}/> 
                                                                        <div onClick={() => this.removeOperation(schedule, index)}>{x.name}</div>
                                                                    </div>
                                                                    : <button className="operation" onClick={() => this.removeOperation(schedule, index)} style={{background: x.color}}>{x.name}</button>
                                                                }
                                                            </>                                                            
                                                        )}
                                                        <div class={"cursor" + (this.state.scheduleStates[schedule.id].index == schedule.conditionEquation.length ? " selected" : "")} onClick={() => this.setIndex(schedule, schedule.conditionEquation.length)}></div>
                                                    </div>
                                                    <div className="test-equation">
                                                        <button className="trigger test" onClick={() => this.testEquation(schedule)}>{this.state.scheduleStates[schedule.id].loadingTest ? <LoadingSpinner id="spinner" /> : "Test"}</button>
                                                        { this.state.scheduleStates[schedule.id].testResult ? this.renderTestResult(schedule) : null }
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            )}
                        </div>
                        : null
                    }
                </div>
            </Menu>
        );
    }

    complete = () => {
        this.setState({saving: true})
        this.props.onConfigChange(this.state.config)
        this.props.complete()
    }

    toggleConditions = (schedule) => {
        this.state.scheduleStates[schedule.id].expanded = !this.state.scheduleStates[schedule.id].expanded
        this.setState({scheduleStates: this.state.scheduleStates})
    }

    addOperation = (schedule, operation) => {
        schedule.conditionEquation.splice(this.state.scheduleStates[schedule.id].index++, 0, operation);
        console.log(schedule.conditionEquation)
        this.setState({config: this.state.config, scheduleStates: this.state.scheduleStates})
    }

    removeOperation = (schedule, index) => {
        schedule.conditionEquation.splice(index, 1);
        this.state.scheduleStates[schedule.id].index += this.state.scheduleStates[schedule.id].index > index ? -1 : 0
        this.setState({config: this.state.config, scheduleStates: this.state.scheduleStates})
    }

    updateValue = (operation, value, e) => {
        e.preventDefault()
        e.stopPropagation()
        operation.value = value
        this.setState({config: this.state.config})
    }

    setIndex = (schedule, index) => {
        this.state.scheduleStates[schedule.id].index = index
        this.setState({scheduleStates: this.state.scheduleStates})
    }

    renderTestResult = (schedule) => {
        let scheduleState = this.state.scheduleStates[schedule.id]
        if(scheduleState.testResult.success){
            return <div className="valid">Valid</div>
        }
        else {
            return <div className="error">{scheduleState.testResult.error}</div>
        }
    }

    testEquation = async (schedule) => {
        let testResult = null
        let scheduleState = this.state.scheduleStates[schedule.id]
        try {
            scheduleState.loadingTest = true
            scheduleState.testResult = null
            this.setState({scheduleStates: this.state.scheduleStates})
            var body = JSON.stringify(schedule)
            var response = await fetchWithToken(`api/debug/schedule/${this.state.config.id}`, "POST", body, {"Content-Type": "application/json"})
            if(response.status == 200){
                testResult = { success: true }
            }
            else {
                testResult = { success: false, error: await response.text() }
            }
        }
        catch(ex) {
            testResult = { success: false, error: ex.message }
        }
        finally {
            scheduleState.loadingTest = false
            scheduleState.testResult = testResult
            console.log(testResult)
            this.setState({scheduleStates: this.state.scheduleStates})
        }
    }

    toggleState = (state) => {
        state.active = !state.active
        this.setState({config: this.state.config})
    }

    toggleGroupState = (schedule, group, id) => {
        var oldState = schedule.state.states.find(x => x.groupId == group.id)
        if(oldState){
            var index = schedule.state.states.indexOf(oldState)
            schedule.state.states.splice(index, 1)
        }
        if(id){
            var state = group.states.find(x => x.id == id)
            schedule.state.states.push({
                "id": id,
                "groupId": group.id,
                "active": true,
                "name": state.name
            })
            this.setState({config: this.state.config})
        }
    }

    toggleDay = (schedule, day) => {
        var index = -1;
        if((index = schedule.days.indexOf(day)) >= 0)
            schedule.days.splice(index, 1)
        else
            schedule.days.push(day)
        this.setState({config: this.state.config})
    }

    setScheduleValue = (schedule, key, value) => {
        schedule[key] = value;
        console.log(key, value)
        this.setState({config: this.state.config})
    }

    setCurrentOCR = (schedule, id) => {
        var action = this.state.config.actions.ocr.find(x => x.id == id)
        schedule.state.ocr = []
        if(action)
            schedule.state.ocr.push(JSON.parse(JSON.stringify(action)))
        this.setState({config: this.state.config})
    }

    updateOCRTarget = (schedule, target) => {
        if(schedule.state.ocr.length > 0){
            schedule.state.ocr[0].target = target
        }
        this.setState({config: this.state.config})
    }

    newSchedule = () => {
        this.state.config.schedules.push({
            "name": `Schedule ${this.state.config.schedules.length + 1}`,
            "id": uuidv4(),
            "days": [],
            "time": "00:00",
            "state": {
                "power": { "active": true },
                "states": [],
                "ocr": []
            },
            "enabled": true
        })
        this.setState({config: this.state.config})
    }

    removeSchedule = (index) => {
        this.state.config.schedules.splice(index, 1)
        this.setState({config: this.state.config})
    }
}

export default Schedules