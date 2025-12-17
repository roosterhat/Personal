import React from 'react'
import { Battery as BatteryIcon, LoaderCircle, X as XIcon } from "lucide-react";
import { loess } from '../utility';

class Battery extends React.Component {
    origin = window.location.origin

    constructor(props) {
        super(props);

        this.state = {
            range: [],
            battery: 0,
            dateRange: "day",
            displayPlot: false,
            loading: false
        }

        this.updateInterval = null
        this.batteryHistory = []         
    }
    
    componentDidUpdate(prevProps) {
        if (this.props.settings["ADSPeriod"] && !this.updateInterval) {
            this.updateInterval = setInterval(this.getBattery, Math.max(this.props.settings["ADSPeriod"] * 1000, 1000))
            this.getBattery()
        }
    }

    getBatteryRange = async (range) => {
        try {
            let response = await fetch(`${this.origin}/api/voltage`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    "start": range[0].toJSON(),
                    "end": range[1].toJSON(),
                    "type": "range"
                })
            });
            if (response.status == 200) {
                this.batteryHistory = await response.json()
            }
        }
        catch (e) {
            console.log(e)
        }
    }

    getBattery = async () => {
        try {
            let response = await fetch(`${this.origin}/api/voltage`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    "type": "current"
                })
            });
            if (response.status == 200) {
                let data = await response.json()
                let percent = this.props.settings ? 
                    Math.max(Math.round((data[0][0] - Number(this.props.settings["batteryMin"])) / (Number(this.props.settings["batteryMax"]) - Number(this.props.settings["batteryMin"])) * 100), 0)
                    : "-"
                this.setState({battery: percent})
            }
        }
        catch (e) {
            console.log(e)
        }
    }

    getDateRange = (date, amount) => {
        if (this.state.dateRange == "day")
            return new Date(date.getTime() + 86400000 * amount)
        else if (this.state.dateRange == "week")
            return new Date(date.getTime() + 86400000 * 7 * amount)
        else if (this.state.dateRange == "month")
            return new Date(new Date(date.getTime()).setMonth(date.getMonth() + amount))
        else if (this.state.dateRange == "year")
            return new Date(new Date(date.getTime()).setYear(date.getFullYear() + amount))
    }

    openPlot = async () => {
        this.setState({displayPlot: true, loading: true})
        let now = new Date()
       
        await this.getBatteryRange([this.getDateRange(now, -1), now])
        this.plot()

        this.setState({loading: false})
    }
    
    setDateRange = (range) => {
        this.setState({dateRange: range})
        this.state.dateRange = range
        this.openPlot()
    }

    getLoessData = (data) => {
        const xval = data.map((x, i) => i)
        const res = loess(xval, data, Number(this.props.settings["trendlineAccuracy"]));
        return res
    }

    plot = () => {
        let plotData = []
        let batteryData = [[], [], []]
        let earliestDate = new Date()
        let offset = new Date().getTimezoneOffset() / 60 * 3600000

        for (let d of this.batteryHistory) {
            let date = new Date(new Date(d[2]).getTime() - offset)

            earliestDate = new Date(Math.min(earliestDate, date))

            batteryData[0].push(date)
            batteryData[1].push(d[0])
            batteryData[2].push(d[1])
        }

        plotData.push({
            x: batteryData[0],
            y: batteryData[1],
            type: 'scatter',
            mode: 'lines',
            line: {
                color: '#3780bf55',
                width: 3
            },
            yaxis: 'y1',
            name: 'Battery'
        })

        plotData.push({
            x: batteryData[0],
            y: this.getLoessData(batteryData[1]),
            showlegend: false,
            type: 'scatter',
            mode: 'lines',
            line: {
                color: '#3780bf',
                width: 3,
                dash: 'longdashdot'
            },
            yaxis: 'y1',
            name: 'Battery'
        })

        plotData.push({
            x: batteryData[0],
            y: batteryData[2],
            type: 'scatter',
            mode: 'lines',
            line: {
                color: '#adadad55',
                width: 3
            },
            yaxis: 'y1',
            name: 'Source'
        })

        plotData.push({
            x: batteryData[0],
            y: this.getLoessData(batteryData[2]),
            showlegend: false,
            type: 'scatter',
            mode: 'lines',
            line: {
                color: '#adadad',
                width: 3,
                dash: 'longdashdot'
            },
            yaxis: 'y1',
            name: 'Source'
        })

        let now = new Date()
        let start = new Date(Math.max(this.getDateRange(now, -1), earliestDate))

        let layout = {
            autosize: true,
            dragmode: "pan",
            selectdirection: "h",
            hovermode: "x unified",
            hoversubplots: "axis",
            paper_bgcolor: '#ffffff50',
            plot_bgcolor: '#ffffff50',
            legend: { x: 0.4, y: 1.2 },
            shapes: [],
            yaxis: {
                title: "Voltage",
                range: [Number(this.props.settings["batteryMin"]), Number(this.props.settings["batteryMax"])]
            },
            xaxis: {
                range: [start.toLocaleString("en-US"), now.toLocaleString("en-US")]
            }
        }
        let config = {
            displayModeBar: true
        }

        let plot = document.getElementById('battery-plot')
        window.Plotly.newPlot(plot, { data: plotData, layout: layout, config: config });
    }

    render = () => {
        return (
            <div className='battery-container'>
                <button className='battery' onClick={this.openPlot}>
                    <BatteryIcon/>
                    <span>{this.state.battery}</span>
                </button>
                { this.state.displayPlot ?
                    <div className='battery-plot-container'>
                        <div className="header">
                            <div className="radio-group">
                            <label>
                                <input type="radio" name="battery-scale" id="battery-day" checked={this.state.dateRange == "day"} onChange={() => this.setDateRange("day")}></input>
                                <span>Day</span>
                            </label>
                            <label>
                                <input type="radio" name="battery-scale" id="battery-week" checked={this.state.dateRange == "week"} onChange={() => this.setDateRange("week")}></input>
                                <span>Week</span>
                            </label>
                            <label>
                                <input type="radio" name="battery-scale" id="battery-month" checked={this.state.dateRange == "month"} onChange={() => this.setDateRange("month")}></input>
                                <span>Month</span>
                            </label>
                            <label>
                                <input type="radio" name="battery-scale" id="battery-year" checked={this.state.dateRange == "year"} onChange={() => this.setDateRange("year")}></input>
                                <span>Year</span>
                            </label>
                        </div>
                            <button className='close' onClick={() => {this.setState({displayPlot: false})}}><XIcon /></button>
                        </div>
                        { this.state.loading ? <span className='spinner'><LoaderCircle /></span> : null }
                        <div id="battery-plot"></div>                
                    </div> : null
                }
            </div>
        )
    }
}

export default Battery