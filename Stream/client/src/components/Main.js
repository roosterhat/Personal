import React, { useState, useEffect, useRef } from "react";
import { Settings as SettingsIcon } from "lucide-react";
import Settings from './Settings';
import { loess } from '../utility';


export default function StreamGuage() {
    let now = new Date()
    const [settings, setSettingsState] = useState({})
    const [dateRange, setDateRangeState] = useState([new Date(now.getTime() - 86400000), now])
    const [dateRangeScale, setDateRangeScaleState] = useState("day")
    const [viewType, setviewTypeState] = useState("full")

    const data = useRef([])
    const toggleDisplaySettings = useRef(null)

    const origin = "http://192.168.1.27:3001"//window.location.origin


    useEffect(() => {
        (async () => {
            await getData()
            await getSettings()
        })()

        return () => { }
    }, [])

    useEffect(() => {
        plot()

        return () => { }
    }, [settings])

    useEffect(() => {
        (async () => {
            await getData()
            plot()
        })()
        return () => { }
    }, [dateRange, viewType])

    const getSettings = async () => {
        try {
            let response = await fetch(`${origin}/api/settings`);
            if (response.status == 200) {
                setSettingsState(await response.json())
            }
        }
        catch (e) {
            console.log(e)
        }
    }

    const saveSettings = async (s) => {
        try {
            let response = await fetch(`${origin}/api/settings`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(s)
            });
        }
        catch (e) {
            console.log(e)
        }
    }

    const getData = async () => {
        try {
            let response = await fetch(`${origin}/api/list`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    "start": dateRange[0].toISOString(),
                    "end": dateRange[1].toISOString(),
                    "type": viewType
                })
            });
            if (response.status == 200) {
                data.current = await response.json()
            }
        }
        catch (e) {
            console.log(e)
        }
    }

    const updateAndSaveSettings = (s) => {
        console.log("updateAndSaveSettings", JSON.stringify(s))
        for (let k in s)
            settings[k] = s[k]
        setSettingsState(s)
        saveSettings(s)
        plot()
    }

    const updateViewType = (type) => {
        setviewTypeState(type)
    }    

    const getDateOffset = (date, amount, scale) => {
        if (scale == "day")
            return new Date(date.getTime() + 86400000 * amount)
        else if (scale == "week")
            return new Date(date.getTime() + 86400000 * amount * 7)
        else if (scale == "month")
            return new Date(new Date(date.getTime()).setMonth(date.getMonth() + amount))
        else if (scale == "year")
            return new Date(new Date(date.getTime()).setYear(date.getYear() + amount))
    }

    const incrementDateRange = (dir) => {
        setDateRangeState([getDateOffset(dateRange[0], dir, dateRangeScale), getDateOffset(dateRange[1], dir, dateRangeScale)])
    }

    const setDateRangeScale = (scale) => {
        setDateRangeScaleState(scale)
        console.log(getDateOffset(dateRange[1], -1, scale), dateRange[1])
        setDateRangeState([getDateOffset(dateRange[1], -1, scale), dateRange[1]])        
    }

    const getLoessData = (data) => {
        const xval = data.map((x, i) => i)
        const res = loess(xval, data, settings["trendlineAccuracy"]);
        return res
    }

    const plot = () => {
        console.log("plot", settings)
        if (!data.current["metadata"] || !data.current["data"]) return

        let plotData = []
        let heightRange = [-5, 5], tempRange = [Number.MAX_VALUE, Number.MIN_VALUE]
        let heightData = [[], []], tempData = [[], []]
        let average = data.current["metadata"]["average"]
        let earliestDate = new Date()

        for (let d of data.current["data"]) {
            let value = d[0] - average
            let date = new Date(d[2])

            earliestDate = Math.min(earliestDate, date)

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

        plotData.push({
            x: heightData[0],
            y: getLoessData(heightData[1]),
            showlegend: false,
            type: 'scatter',
            mode: 'lines',
            line: {
                color: '#3780bf',
                width: 3,
                dash: 'longdashdot'
            },
            yaxis: 'y1',
            name: 'height'
        })

        plotData.push({
            x: tempData[0],
            y: getLoessData(tempData[1]),
            showlegend: false,
            type: 'scatter',
            mode: 'lines',
            line: {
                color: '#adadad',
                width: 3,
                dash: 'longdashdot'
            },
            yaxis: 'y2',
            name: 'temperature'
        })

        let now = new Date()
        let layout = {
            autosize: true,
            dragmode: "pan",
            paper_bgcolor: '#ffffff50',
            plot_bgcolor: '#ffffff50',
            legend: { x: 0.4, y: 1.2 },
            shapes: [],
            yaxis: {
                title: "Relative water height (cm)",
                range: heightRange
            },
            yaxis2: {
                title: "º" + (settings["temperatureUnit"] == "F" ? "F" : "C"),
                side: 'right',
                range: [tempRange[0] == Number.MAX_VALUE ? 0 : tempRange[0] - 5, tempRange[1] == Number.MIN_VALUE ? 0 : tempRange[1] + 5]
            },
            xaxis: {
                range: [Math.max(new Date(now.getTime() - 86400000), earliestDate), now]
            }
        }
        let config = {
            displayModeBar: true
        }

        console.log(plotData)

        let plot = document.getElementById('plot')
        window.Plotly.newPlot(plot, { data: plotData, layout: layout, config: config });
    }

    return (
        <div className="main">
            <div className="plot">
                <div className="plot-header">
                    <div className="radio-group">
                        <label>
                            <input type="radio" name="view" id="full" checked={viewType == "full"} onChange={() => updateViewType("full")}></input>
                            <span>Full</span>
                        </label>
                        <label>                            
                            <input type="radio" name="view" id="hour" checked={viewType == "hour"} onChange={() => updateViewType("hour")}></input>
                            <span>Hour</span>
                        </label>
                        <label>
                            <input type="radio" name="view" id="day" checked={viewType == "day"} onChange={() => updateViewType("day")}></input>
                            <span>Day</span>
                        </label>
                    </div>
                    <div className="radio-group">
                        <label>
                            <input type="radio" name="scale" id="day" checked={dateRangeScale == "day"} onChange={() => setDateRangeScale("day")}></input>
                            <span>Day</span>
                        </label>
                        <label>
                            <input type="radio" name="scale" id="week" checked={dateRangeScale == "week"} onChange={() => setDateRangeScale("week")}></input>
                            <span>Week</span>
                        </label>
                        <label>
                            <input type="radio" name="scale" id="month" checked={dateRangeScale == "month"} onChange={() => setDateRangeScale("month")}></input>
                            <span>Month</span>
                        </label>
                        <label>
                            <input type="radio" name="scale" id="year" checked={dateRangeScale == "year"} onChange={() => setDateRangeScale("year")}></input>
                            <span>Year</span>
                        </label>
                    </div>
                </div>
                <div className="daterange">{dateRange[0].toJSON()} - {dateRange[1].toJSON()}</div>
                <div className="plot-wrapper">
                    <button onClick={() => incrementDateRange(-1)}>{"<"}</button>
                    <div id="plot"></div>
                    <button onClick={() => incrementDateRange(1)}>{">"}</button>
                </div>
            </div>
            <button onClick={toggleDisplaySettings.current} className="settings-icon"><SettingsIcon /></button>
            <Settings settings={settings} setToggleSettings={x => toggleDisplaySettings.current = x} saveSettings={updateAndSaveSettings} />
        </div>
    );
}
