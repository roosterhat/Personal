import React from 'react'
import Menu from './Menu';
import LoadingSpinner from './Spinners/loading1';
import { uuidv4 } from '../Utility';

class Schedules extends React.Component {
    Days = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"]

    constructor(props) {
        super(props);

        this.state = {
            saving: false,
            config: props.Config
        }
        
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
                                            <input type="time" value={schedule.time} onChange={e => this.setScheduleValue(schedule, "time", e.target.value)}></input>
                                        </div>
                                        <div className="schedule-state">
                                            <div className="power-state">
                                                <div className="name">Power</div>
                                                <button className={"state " + (schedule.state.power.active ? "on" : "off")} onClick={() => this.toggleState(schedule.state.power)}>
                                                    {schedule.state.power.active ? "On" : "Off"}
                                                </button>
                                            </div>
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
            schedule.state.states.push({
                "id": id,
                "groupId": group.id,
                "active": true
            })
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
        console.log(key, value)
        schedule[key] = value;
        this.setState({config: this.state.config})
    }

    newSchedule = () => {
        this.state.config.schedules.push({
            "name": `Schedule ${this.state.config.schedules.length}`,
            "id": uuidv4(),
            "days": [],
            "time": "00:00",
            "state": {
                "power": { "active": true },
                "states": []
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