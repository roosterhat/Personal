import React from 'react'
import { ChevronDown } from 'lucide-react'
import RadialPlot from './RadialPlot';

class Editor extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            enabled: false,
            active: null,
            highlightedZone: null,
            highlightedPoint: null,
            settings: JSON.parse(JSON.stringify(this.props.settings))
        }

        this.props.setToggleEnable(this.toggleEnabled)

        this.updateRadialPlot = null
        this.updateQueue = []
    }

    componentDidUpdate(prevProps, prevState) {
        while(this.updateQueue.length > 0)
            this.updateQueue.pop()()

        if (prevProps != this.props) {
            this.setState({settings: JSON.parse(JSON.stringify(this.props.settings))})
        }
    }

    toggleEnabled = () => {
        this.setState({enabled: !this.state.enabled})
    }

    addZone = () => {
        this.state.settings["keepOutZones"].push([])
        let index = this.state.settings["keepOutZones"].length - 1
        this.updateQueue.push(() => this.toggleActive(index))
        this.setState({highlightedZone: index, highlightedPoint: null})        
    }

    removeZone = (i) => {
        this.state.settings["keepOutZones"].splice(i,1)
        this.state.active = i == this.state.active ? null : this.state.active
        this.setState({
            highlightedZone: i == this.state.highlightedZone ? null : this.state.highlightedZone, 
            highlightedPoint: i == this.state.highlightedZone ? null : this.state.highlightedPoint,
            active: this.state.active
        })
    }

    removePoint = (zoneIndex, i) => {
        this.state.settings["keepOutZones"][zoneIndex].splice(i,1)
        this.updateQueue.push(this.updateHeights)
        this.setState({highlightedPoint: null})
    }

    toggleActive = (i) => {
        this.state.active = this.state.active == i ? null : i
        this.updateHeights()
        this.setState({active: this.state.active})
    }
    
    updateHeights = () => {
        let zones = Array.from(document.getElementsByClassName("zone"))
        for(let zi in zones) {
            let tray = zones[zi].getElementsByClassName("points-tray")[0]
            tray.style["height"] = zi == this.state.active ? tray.childNodes[0].clientHeight + "px" : "0"
        }
    }

    onSettingsChange = () => {
        this.forceUpdate()
        this.updateQueue.push(this.updateHeights)
    }

    saveAndClose = () => {
        this.props.onSettingsChanged(this.state.settings)
        this.setState({enabled: false})
    }

    cancel = () => {
        this.setState({settings: JSON.parse(JSON.stringify(this.props.settings)), enabled: false})
    }

    render = () => {
        return (
            this.state.enabled ? 
                <div className='background'>
                    <div className='editor'>
                        {this.state.settings["usePathing"] ? null : <div style={{color: "red"}}>Enable pathing to use keep out zones</div>}
                        <div className='body'>
                            <RadialPlot 
                                enableEditing={true}
                                setUpdateHandler={x => this.updateRadialPlot = x} 
                                settings={this.state.settings} 
                                highlightedZone={this.state.highlightedZone} 
                                highlightedPoint={this.state.highlightedPoint} 
                                currentZone={this.state.active} 
                                onSettingsChange={this.onSettingsChange}
                                />
                            <div className='zones' onMouseLeave={() => this.setState({highlightedPoint: null, highlightedZone: null})}>
                                <div className='title'>
                                    <span>Keep Out Zones</span>
                                    <button className='add' onClick={this.addZone}>+</button>
                                </div>                        
                                <div className='list'>
                                    {this.state.settings["keepOutZones"].map((z, zi) => 
                                        <div className="zone" key={`zone${zi}`} id={`zone${zi}`} onMouseEnter={() => this.setState({highlightedZone: zi})} onMouseLeave={() => this.setState({highlightedZone: null})}>
                                            <div className='title'>
                                                <span>Zone {zi}</span>
                                                <button className={"expand-toggle" + (this.state.active == zi ? " expand" : "")} onClick={() => this.toggleActive(zi)}><ChevronDown /></button>
                                                <button className='remove' onClick={() => this.removeZone(zi)}>-</button>
                                            </div>
                                            <div className="points-tray">
                                                <div className="points">
                                                    {z.map((p, pi) => 
                                                        <div className="point" key={`point${pi}`} id={`point${pi}`} onMouseEnter={() => this.setState({highlightedPoint: pi})} onMouseLeave={() => this.setState({highlightedPoint: null})}>
                                                            <span>{`${Math.round(p.az)}, ${Math.round(p.el)}`}</span>
                                                            <button className='remove' onClick={() => this.removePoint(zi, pi)}>-</button>
                                                        </div>
                                                    )}
                                                </div>
                                            </div>
                                        </div>
                                    )}
                                </div>
                            </div>
                        </div>
                        <div className='actions'>
                            <button className="cancel" onClick={this.cancel}>Cancel</button>
                            <button className="save" onClick={this.saveAndClose}>Save</button>
                        </div>
                    </div>
                </div>
            : null
        )
    }
}

export default Editor