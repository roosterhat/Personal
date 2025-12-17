import React from "react";
import { Settings as SettingsIcon, RefreshCcw } from "lucide-react";
import Settings from './Settings';
import { loess } from '../utility';
import Battery from './Battery';

class StreamGuage extends React.Component {
    constructor(props) {
        super(props);
        
        const now = this.getNow();

        this.state = {
            settings: {},
            dateRange: [new Date(now.getTime() - 86400000), now],
            dateRangeScale: "day",
            viewType: "full",
            loading: false,
        };

        this.data = [];
        this.toggleDisplaySettings = null;

        this.origin = window.location.origin;
    }

    async componentDidMount() {
        await this.getData();
        await this.getSettings();

        window.addEventListener("resize", this.plot);
    }

    componentDidUpdate(prevProps, prevState) {
        if (prevState.settings !== this.state.settings) {
            this.plot();
        }

        if (prevState.dateRange !== this.state.dateRange || prevState.viewType !== this.state.viewType) {
            this.refreshData();
        }
    }

    componentWillUnmount() {
        window.removeEventListener("resize", this.plot);
    }

    getNow = () => new Date(new Date(new Date().setTime(Date.now() + 86400000)).toDateString())

    getLoessData = (data) => {
        const xval = data.map((x, i) => i);
        const { settings } = this.state;
        const accuracy = settings["trendlineAccuracy"] ? Number(settings["trendlineAccuracy"]) : 0.5;
        const res = loess(xval, data, accuracy);
        return res;
    }

    formatDate = (date) => {
        const format = new Intl.DateTimeFormat('en-US', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
        });
        return format.format(date);
    }

    getDateOffset = (date, amount, scale) => {
        if (scale === "day")
            return new Date(date.getTime() + 86400000 * amount)
        else if (scale === "week")
            return new Date(date.getTime() + 86400000 * amount * 7)
        else if (scale === "month")
            return new Date(new Date(date.getTime()).setMonth(date.getMonth() + amount))
        else if (scale === "year")
            return new Date(new Date(date.getTime()).setYear(date.getFullYear() + amount))
    }

    getSettings = async () => {
        try {
            let response = await fetch(`${this.origin}/api/settings`);
            if (response.status === 200) {
                this.setState({ settings: await response.json() })
            }
        }
        catch (e) {
            console.error("Error fetching settings:", e)
        }
    }

    saveSettings = async (s) => {
        try {
            await fetch(`${this.origin}/api/settings`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(s)
            });
        }
        catch (e) {
            console.error("Error saving settings:", e)
        }
    }

    getData = async () => {
        const { dateRange, viewType } = this.state;
        try {
            this.setState({ loading: true });
            let response = await fetch(`${this.origin}/api/measurements`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    "start": dateRange[0].toISOString(),
                    "end": dateRange[1].toISOString(),
                    "type": viewType
                })
            });
            if (response.status === 200) {
                this.data = await response.json();
            }
        }
        catch (e) {
            console.error("Error fetching data:", e)
        }
        finally {
            this.setState({ loading: false });
        }
    }

    updateAndSaveSettings = (newSettings) => {       
        let updatedSettings = { ...this.state.settings, ...newSettings };
        
        this.setState({ settings: updatedSettings }, () => {
            this.saveSettings(updatedSettings);
            this.plot();
        });
    }

    updateViewType = (type) => {
        this.setState({ viewType: type });
    }

    incrementDateRange = (dir) => {
        const { dateRange, dateRangeScale } = this.state;
        this.setState({
            dateRange: [
                this.getDateOffset(dateRange[0], dir, dateRangeScale),
                this.getDateOffset(dateRange[1], dir, dateRangeScale)
            ]
        });
    }

    setDateRangeScale = (scale) => {
        const { viewType } = this.state;
        let newViewType = viewType;

        if ((scale === "month" || scale === "year") && viewType === "full") {
            newViewType = "hour";
        }

        const now = this.getNow();
        
        this.setState({
            dateRangeScale: scale,
            viewType: newViewType,
            dateRange: [this.getDateOffset(now, -1, scale), now]
        });
    }

    refreshData = async () => {
        await this.getData()
        this.plot()
    }
    
    plot = () => {
        if (!this.data["metadata"] || !this.data["data"] || !window.Plotly) return;

        const { dateRange, settings } = this.state;

        let plotData = []
        let heightRange = [-5, 5], tempRange = [Number.MAX_VALUE, Number.MIN_VALUE]
        let heightData = [[], []], tempData = [[], []]
        let average = this.data["metadata"]["average"]
        let earliestDate = new Date()
        let offset = new Date().getTimezoneOffset() / 60 * 3600000

        for (let d of this.data["data"]) {
            let value = average - d[0]
            let date = new Date(new Date(d[2]).getTime() - offset)

            earliestDate = new Date(Math.min(earliestDate, date))

            heightData[0].push(date)
            heightData[1].push(value)
            heightRange[0] = Math.min(heightRange[0], value)
            heightRange[1] = Math.max(heightRange[1], value)

            tempData[0].push(date)
            tempData[1].push(d[1])
            tempRange[0] = Math.min(tempRange[0], d[1])
            tempRange[1] = Math.max(tempRange[1], d[1])
        }

        plotData.push({
            x: heightData[0],
            y: heightData[1],
            type: 'scatter',
            mode: 'lines',
            line: {
                color: '#3780bf55',
                width: 3
            },
            yaxis: 'y1',
            name: 'height'
        })

        plotData.push({
            x: tempData[0],
            y: tempData[1],
            type: 'scatter',
            mode: 'lines',
            line: {
                color: '#adadad55',
                width: 3
            },
            yaxis: 'y2',
            name: 'temperature'
        })

        if (settings["trendlineAccuracy"]) {
            plotData.push({
                x: heightData[0],
                y: this.getLoessData(heightData[1]),
                showlegend: false,
                type: 'scatter',
                mode: 'lines',
                line: {
                    color: '#3780bf',
                    width: 3,
                    dash: 'longdashdot'
                },
                yaxis: 'y1',
                name: 'height trend'
            })

            plotData.push({
                x: tempData[0],
                y: this.getLoessData(tempData[1]),
                showlegend: false,
                type: 'scatter',
                mode: 'lines',
                line: {
                    color: '#adadad',
                    width: 3,
                    dash: 'longdashdot'
                },
                yaxis: 'y2',
                name: 'temperature trend'
            })
        }

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
                title: "Relative water height (cm)",
                range: heightRange
            },
            yaxis2: {
                title: "º" + (settings["temperatureUnit"] === "F" ? "F" : "C"),
                side: 'right',
                range: [tempRange[0] === Number.MAX_VALUE ? 0 : tempRange[0] - 5, tempRange[1] === Number.MIN_VALUE ? 0 : tempRange[1] + 5]
            },
            xaxis: {
                range: [new Date(Math.max(dateRange[0], earliestDate)).toLocaleString("en-US"), dateRange[1].toLocaleString("en-US")]
            }
        }
        let config = {
            displayModeBar: true
        }

        let plotElement = document.getElementById('plot')
        window.Plotly.newPlot(plotElement, { data: plotData, layout: layout, config: config });
    }

    render() {
        const { settings, dateRange, dateRangeScale, viewType, loading } = this.state;
        
        return (
            <div className="main">
                <div className="plot">
                    <div className="plot-header">
                        <div className="radio-group">
                            <label>
                                <input type="radio" name="view" id="full" checked={viewType === "full"} onChange={() => this.updateViewType("full")}></input>
                                <span>Full</span>
                            </label>
                            <label>
                                <input type="radio" name="view" id="hour" checked={viewType === "hour"} onChange={() => this.updateViewType("hour")}></input>
                                <span>Hour</span>
                            </label>
                            <label>
                                <input type="radio" name="view" id="day" checked={viewType === "day"} onChange={() => this.updateViewType("day")}></input>
                                <span>Day</span>
                            </label>
                        </div>
                        <div className="radio-group">
                            <label>
                                <input type="radio" name="scale" id="day" checked={dateRangeScale === "day"} onChange={() => this.setDateRangeScale("day")}></input>
                                <span>Day</span>
                            </label>
                            <label>
                                <input type="radio" name="scale" id="week" checked={dateRangeScale === "week"} onChange={() => this.setDateRangeScale("week")}></input>
                                <span>Week</span>
                            </label>
                            <label>
                                <input type="radio" name="scale" id="month" checked={dateRangeScale === "month"} onChange={() => this.setDateRangeScale("month")}></input>
                                <span>Month</span>
                            </label>
                            <label>
                                <input type="radio" name="scale" id="year" checked={dateRangeScale === "year"} onChange={() => this.setDateRangeScale("year")}></input>
                                <span>Year</span>
                            </label>
                        </div>
                        <button className={"refresh" + (loading ? " loading" : "")} onClick={() => this.setDateRangeScale(dateRangeScale)}><RefreshCcw /></button>
                        <Battery settings={settings}/>
                    </div>
                    <div className="daterange">{this.formatDate(dateRange[0])} - {this.formatDate(dateRange[1])}</div>
                    <div className="plot-wrapper">
                        <button onClick={() => this.incrementDateRange(-1)}>{"<"}</button>
                        <div id="plot"></div>
                        <button onClick={() => this.incrementDateRange(1)}>{">"}</button>
                    </div>
                </div>
                <button onClick={() => this.toggleDisplaySettings()} className="settings-icon"><SettingsIcon /></button>
                <Settings 
                    settings={settings} 
                    setToggleSettings={x => this.toggleDisplaySettings = x} 
                    saveSettings={this.updateAndSaveSettings} 
                />
            </div>
        );
    }
}

export default StreamGuage;