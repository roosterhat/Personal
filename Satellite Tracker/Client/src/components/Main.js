import React, { useState, useEffect, useRef } from "react";
import { ArrowUp, ArrowDown, ArrowLeft, ArrowRight, Home, RefreshCcw, Trash, Settings as SettingsIcon, PencilRuler, ZoomIn, ZoomOut } from "lucide-react";
import Settings from './Settings';
import RadialPlot from './RadialPlot'
import Editor from './Editor'
import ConfirmationModal from './ConfirmationModal'

export default function SatelliteTracker() {
    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState("");
    const [azimuth, setAzimuth] = useState(0);
    const [elevation, setElevation] = useState(0);
    const [target, setTarget] = useState(null);
    const [port, setPort] = useState("");
    const [baudRate, setBaudRate] = useState(9600);
    const [baudRates, setBaudRates] = useState([]);
    const [ports, setPorts] = useState([]);
    const [displayScroll, setDisplayScrollState] = useState(false);
    const [settings, setSettingsState] = useState({})
    const [path, setPath] = useState([])
    const [trail, setTrail] = useState([])
    const [zoom, setZoom] = useState(false)

    const commandIndex = useRef(-1)
    const commands = useRef([])
    const newMessageCount = useRef(0)
    const toggleDisplaySettings = useRef(null)
    const toggleDisplayEditor = useRef(null)
    const toggleDisplayConfirmation = useRef(null)
    const updateRadialPlot = useRef(null)
    const scrollDisplayed = useRef(false)
    const maxMessages = useRef(1000)
    const trailProxy = useRef([])
    const settingsProxy = useRef({})

    const messageHeight = 20    
    const origin = window.location.origin

    
    useEffect(() => {
        initEventListeners()            
        getPortsAndBaud()
        getSettings()
        setInterval(manageTrail, 1000)

        return () => { }
    }, [])

    useEffect(() => {
        let socket = window.io(origin);
        socket.on('connect', () => {
            console.log('Connected to server')
            getPosition()
        });

        socket.on('disconnect', () => {
            console.log('Disconnected from server')
        });

        socket.on('message', (json) => {
            const data = JSON.parse(json)
            switch(data["type"]) {
                case "read":
                case "write":
                    setMessages((prev) => [...prev.slice(Math.max((prev.length + 1) - (scrollDisplayed.current ? prev.length + 1 : Math.round(maxMessages.current)), 0), prev.length), data])
                    newMessageCount.current++
                    break;
                case "position":
                    setAzimuth(data["data"]["x"])
                    setElevation(data["data"]["y"])
                    if((!settingsProxy.current["onlyDisplayTrackingTrail"] || data["data"]["tracking"]) 
                        && (trailProxy.current.length == 0 || (trailProxy.current[trailProxy.current.length - 1]["x"] != data["data"]["x"] || trailProxy.current[trailProxy.current.length - 1]["y"] != data["data"]["y"]))) {
                        setTrail([...trailProxy.current, {"time": Date.now(), ...data["data"]}])
                    }
                    break;
                case "target":
                    setTarget({'azimuth': data["data"]["x"], 'elevation': data["data"]["y"]})
                    break;
                case "path":
                    setPath(data["data"])                    
                    break;
            }
        });

        return () => {
            if(socket) {
                console.log('Socket close');
                socket.close()
            }
        }
    }, [])

    useEffect(() => {
        settingsProxy.current = settings
    }, [settings])

    useEffect(() => {
        trailProxy.current = trail
    }, [trail])

    useEffect(() => {
        updateRadialPlot.current()
    }, [azimuth, elevation, target])    

    const initEventListeners = () => {
        let command = document.getElementById('command')
        command.addEventListener('keydown', handleKeyboardEvent)

        let messageContainer = document.getElementById("messages")
        messageContainer.addEventListener("scroll", e => {
            setDisplayScroll(e.target.scrollHeight - (e.target.clientHeight + e.target.scrollTop) > 100)
        })

        const observer = new MutationObserver((mutationsList, observer) => {
            for (const mutation of mutationsList) {
                if (mutation.type === 'childList') {
                    if(messages.length < maxMessages.current) {
                        if(messageContainer.scrollHeight - (messageContainer.scrollTop + messageContainer.clientHeight) <= newMessageCount.current * messageHeight) {
                            newMessageCount.current = 0
                            messageContainer.scrollTo(0, messageContainer.scrollHeight)
                            setDisplayScroll(false)
                        }
                        else {
                            setDisplayScroll(true)
                        }
                    }
                }
            }
        });

        const config = { attributes: false, childList: true, subtree: true };

        observer.observe(messageContainer, config);
    }

    const manageTrail = () => {
        if(settingsProxy.current["displayPathTrail"]) {
            const now = Date.now()
            const tracking = settingsProxy.current["displayFullTrack"] && trailProxy.current[trailProxy.current.length - 1]["tracking"]
            setTrail(trailProxy.current
                .map(x => {
                    if(tracking && x.tracking)
                        x.time = now
                    return x
                })
                .filter(x => (now - x.time) / 1000 < settingsProxy.current["pathTrailDuration"])                
            )
        }
        else if(trailProxy.current.length)
            setTrail([])
    }

    const handleKeyboardEvent = e => {
        switch(e.key) {
            case "ArrowUp":
                e.preventDefault()
                if(commandIndex.current < commands.current.length - 1) {
                    setInput(commands.current[++commandIndex.current])
                }
                break
            case "ArrowDown":
                e.preventDefault()
                if(commandIndex.current > 0) {
                    setInput(commands.current[--commandIndex.current])
                }
                else {
                    commandIndex.current = -1
                    setInput("")
                }
                break
        }
    }

    const sendCommand = async (cmd) => {
        commands.current = [cmd, ...commands.current.slice(0, Math.min(commands.current.length, 100))]
        commandIndex.current = -1
        let response = await fetch(`${origin}/api/send/command`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ "command": cmd })
        });
    };

    const handleSend = () => {
        if (input.trim() === "") return;
        sendCommand(input);
        setInput("");
    };

    const handleScan = async () => {
        try {
            let response = await fetch(`${origin}/api/scan/baud`);
            if(response.status == 200)
                await getSettings()
        }
        catch(e) {
            console.log(e)
        }
    };

    const setDisplayScroll = (value) => {
        setDisplayScrollState(value)
        scrollDisplayed.current = value
    }

    const setSettings = (value) => {
        setSettingsState(value)
        maxMessages.current = value["maxMessages"]
    }

    const getSettings = async () => {
        try {
            let response = await fetch(`${origin}/api/get/settings`);
            if(response.status == 200) {
                setSettings(await response.json())                
            }
            updateRadialPlot.current()
        }
        catch(e) {
            console.log(e)
        }
    }

    const saveSettings = async () => {
        try {
            let response = await fetch(`${origin}/api/set/settings`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(settings)
            });
            updateRadialPlot.current()
        }
        catch(e) {
            console.log(e)
        }
    }

    const setPosition = async (coords, absolute) => {
        try {
            let response = await fetch(`${origin}/api/set/position`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ "coordinates": coords, "absolute": absolute })
            });
        }
        catch(e) {
            console.log(e)
        }
    }

    const getPosition = async () => {
        try {
            let response = await fetch(`${origin}/api/get/position`);
            if(response.status == 200) {
                const body = await response.json()
                setAzimuth(body["x"])
                setElevation(body["y"])
                setTarget({'azimuth': body["x"], 'elevation': body["y"]})
            }
        }
        catch(e) {
            console.log(e)
        }
    }

    const initHoming = async () => {
        try {
            let response = await fetch(`${origin}/api/home`);
        }
        catch(e) {
            console.log(e)
        }
    }

    const toggleMotorHold = async () => {
        try {
            let response = await fetch(`${origin}/api/toggle/motors`);
        }
        catch(e) {
            console.log(e)
        }
    }

    const stow = async () => {
        try {
            let response = await fetch(`${origin}/api/stow`);
        }
        catch(e) {
            console.log(e)
        }
    }

    const updatePortAndBaud = async () => {
        try {
            let response = await fetch(`${origin}/api/set/baudport`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ "baud": baudRate, "port": port })
            });
        }
        catch(e) {
            console.log(e)
        }        
    }

    const getPortsAndBaud = async () => {
        try {
            let response = await fetch(`${origin}/api/get/ports`);
            if(response.status == 200) {
                let body = await response.json()
                setPorts(body)
                if (body.length > 0 && body.indexOf(port) == -1)
                    setPort(body[0])
            }
        }
        catch(e) {
            console.log(e)
        }

        try{
            let response = await fetch(`${origin}/api/get/baud`);
            if(response.status == 200) {
                let body = await response.json()
                setBaudRates(body)
                if (body.length > 0 && body.indexOf(baudRate) == -1)
                    setBaudRate(body[0])
            }
        }
        catch(e) {
            console.log(e)
        }
    }

    const setNorth = async () => {
        settings["offset"] = azimuth
        await saveSettings()  
    }    

    const resetController = async () => {
        try{
            let response = await fetch(`${origin}/api/reset`);
        }
        catch(e) {
            console.log(e)
        }
    }

    const directionWithOffset = (dir) => {
        return (dir + settings["offset"]) % 360
    }

    const setManualPosition = () => {
        let x = Number.parseFloat(document.getElementById("azimuth").value) % 360
        let y = Number.parseFloat(document.getElementById("elevation").value) % 180
        setPosition({'x': isNaN(x) ? azimuth : x, 'y': isNaN(y) ? elevation : y}, true)
    }

    const scrollToBottom = () => {
        let x = document.getElementById("messages")
        x.scrollTo(0, x.scrollHeight)
    }   
    
    const updateAndSaveSettings = (s) => {
        for(let key in s)
            settings[key] = s[key]
        setSettings(settings)
        saveSettings()
    }

    const toggleZoom = () => {
        setZoom(!zoom)
        updateRadialPlot.current()
    }

    return (
        <div className="min-h-screen bg-gray-100 flex flex-row items-start justify-center p-6 flex-wrap gap-x-48 gap-y-8">
            <div className="flex flex-col items-center gap-4">
                <div className="flex w-full justify-between gap-4">
                    <div className="flex flex-col gap-4 shrink-0">
                        <div className="grid grid-cols-3 gap-4">
                            <div></div>
                            <button onClick={() => setPosition({x: 0, y: 10}, false)} className="rounded-full w-12 h-12 flex items-center justify-center cursor-pointer text-white bg-black">
                                <ArrowUp />
                            </button>
                            <div></div>

                            <button onClick={() => setPosition({x: -10, y: 0}, false)} className="rounded-full w-12 h-12 flex items-center justify-center cursor-pointer text-white bg-black">
                                <ArrowLeft />
                            </button>
                            <button onClick={() => initHoming()} className="rounded-full w-12 h-12 flex items-center justify-center cursor-pointer text-white bg-black">
                                <Home />
                            </button>
                            <button onClick={() => setPosition({x: 10, y: 0}, false)} className="rounded-full w-12 h-12 flex items-center justify-center cursor-pointer text-white bg-black">
                                <ArrowRight />
                            </button>

                            <div></div>
                            <button onClick={() => setPosition({x: 0, y: -10}, false)} className="rounded-full w-12 h-12 flex items-center justify-center cursor-pointer text-white bg-black">
                                <ArrowDown />
                            </button>
                            <div></div>
                        </div>

                        <div className="grid grid-cols-3 gap-4">
                            <div></div>
                            <button onClick={() => setPosition({x: directionWithOffset(0), y: elevation}, true)} className="rounded-full w-12 h-12 flex items-center justify-center cursor-pointer text-white bg-black">N</button>
                            <div></div>

                            <button onClick={() => setPosition({x: directionWithOffset(270), y: elevation}, true)} className="rounded-full w-12 h-12 flex items-center justify-center cursor-pointer text-white bg-black">W</button>
                            <div></div>
                            <button onClick={() => setPosition({x: directionWithOffset(90), y: elevation}, true)} className="rounded-full w-12 h-12 flex items-center justify-center cursor-pointer text-white bg-black">E</button>

                            <div></div>
                            <button onClick={() => setPosition({x: directionWithOffset(180), y: elevation}, true)} className="rounded-full w-12 h-12 flex items-center justify-center cursor-pointer text-white bg-black">S</button>
                            <div></div>
                        </div>
                    </div>

                    <div className="flex flex-col gap-4 justify-center">
                        {zoom ? <div className="spacer"></div> : null}
                        <div className={"plot-container" + (zoom ? " zoom" : "")}>
                            <div style={{position: "relative"}}>
                                <RadialPlot setUpdateHandler={x => updateRadialPlot.current = x} settings={settings} target={target} position={{'azimuth': azimuth, 'elevation': elevation}} path={path} trail={trail}/>                                
                                {zoom ? null : <button className="absolute top-1 right-1 cursor-pointer bg-white rounded-sm p-1" onClick={toggleDisplayEditor.current}><PencilRuler className="w-4 h-4"/></button>}
                                {zoom ? <button className="absolute bottom-1 right-1 cursor-pointer bg-white rounded-sm p-1" onClick={toggleZoom}><ZoomOut className="w-4 h-4"/></button> :
                                    <button className="absolute bottom-1 right-1 cursor-pointer bg-white rounded-sm p-1" onClick={toggleZoom}><ZoomIn className="w-4 h-4"/></button>}
                            </div>
                        </div>
                        <Editor enabled={true} settings={settings} setToggleEnable={x => toggleDisplayEditor.current = x} onSettingsChanged={updateAndSaveSettings}/>
                        <div className="flex w-full justify-center gap-1">
                            <span className="w-full text-right">{azimuth}</span>
                            <span>,</span>
                            <span className="w-full">{elevation}</span>
                        </div>
                    </div>
                </div>
                

                <div className="flex w-full gap-2">
                    <input id="azimuth" className="flex-1 border rounded p-2 font-mono text-sm w-full" placeholder="Azimuth" />
                    <input id="elevation" className="flex-1 border rounded p-2 font-mono text-sm w-full" placeholder="Elevation" />
                    <button className="cursor-pointer text-white bg-black rounded-sm p-2" onClick={setManualPosition}>Set</button>
                    <button onClick={getPosition} className="cursor-pointer text-white bg-black rounded-sm p-2">
                        <RefreshCcw className="w-4 h-4" />
                    </button>
                </div>

                <div className="flex flex-wrap gap-2 w-full">
                    <button onClick={toggleMotorHold} className="cursor-pointer text-white bg-black rounded-sm p-2" style={{"width": "calc(50% - 4px)"}}>Toggle Motor Hold</button>
                    <button onClick={stow} className="cursor-pointer text-white bg-black rounded-sm p-2" style={{"width": "calc(50% - 4px)"}}>Stow</button>
                    <button onClick={toggleDisplayConfirmation.current} className="cursor-pointer text-white bg-black rounded-sm p-2" style={{"width": "calc(50% - 4px)"}}>Set North</button>
                    <button onClick={resetController} className="cursor-pointer text-white bg-black rounded-sm p-2" style={{"width": "calc(50% - 4px)"}}>Reset Controller</button>
                </div>
            </div>

            <div className="flex flex-col w-full max-w-lg gap-4">
                <div className="flex gap-2">
                    <select id="ports" className="flex-1 border rounded p-2 font-mono text-sm" value={port} onChange={(e) => setPort(e.target.value)}>
                        {ports.map((x, i) => <option key={i} value={x}>{x}</option>)}
                    </select>                    

                    <select id="baud" className="flex-1 border rounded p-2 font-mono text-sm" value={baudRate} onChange={(e) => setBaudRate(Number(e.target.value))}>
                        {baudRates.map((x, i) => <option key={i} value={x}>{x}</option>)}
                    </select>

                    <button onClick={getPortsAndBaud} className="cursor-pointer text-white bg-black rounded-sm p-2"><RefreshCcw className="w-4 h-4" /></button>
                    <button onClick={updatePortAndBaud} className="cursor-pointer text-white bg-black rounded-sm p-2">Set</button>
                    <button onClick={handleScan} className="cursor-pointer text-white bg-black rounded-sm p-2">Scan</button>
                </div>

                <div className="flex flex-col w-full gap-2">
                    <div className="relative">
                        <div id="messages" className="h-48 bg-black text-green-400 font-mono text-sm overflow-y-auto p-2 rounded mb-2">
                            {messages.map((msg, i) => <div className="w-full" key={i} style={{height: messageHeight}}>{(msg['type'] == "write" ? "> " : "") + msg['data']}</div>)}                        
                        </div>
                        <button onClick={scrollToBottom} className="absolute bottom-4 right-4 sm:right-6 text-black cursor-pointer rounded-md p-1 text-xs" style={{transition: "all 0.25s ease", opacity: displayScroll ? 1 : 0, background: "#c1c1c1"}}>Scroll down</button>
                    </div>

                    <div className="flex gap-2">
                        <input
                            id="command"
                            className="flex-1 border rounded p-2 font-mono text-sm"
                            value={input}
                            onChange={(e) => setInput(e.target.value)}
                            onKeyDown={(e) => e.key === "Enter" && handleSend()}
                            placeholder="Type command..."
                        />
                        <button onClick={handleSend} className="cursor-pointer text-white bg-black rounded-sm p-2">Send</button>
                        <button onClick={() => setMessages([])} className="cursor-pointer text-white bg-black rounded-sm p-2"><Trash /></button>
                    </div>
                </div>
            </div>
            <button onClick={toggleDisplaySettings.current} className="absolute top-4 right-4 cursor-pointer text-white bg-black rounded-sm p-2"><SettingsIcon /></button>
            <Settings settings={settings} setToggleSettings={x => toggleDisplaySettings.current = x} saveSettings={updateAndSaveSettings}/>
            <ConfirmationModal title={"Set North Offset?"} setToggleEnable={x => toggleDisplayConfirmation.current = x} onConfirm={setNorth}/>
        </div>
    );
}
