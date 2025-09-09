import React, { useState, useEffect, useRef } from "react";
import { ArrowUp, ArrowDown, ArrowLeft, ArrowRight, Home, RefreshCcw, Trash, Settings, X } from "lucide-react";

export default function SatelliteTracker() {
    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState("");
    const [azimuth, setAzimuth] = useState(0);
    const [elevation, setElevation] = useState(0);
    const [port, setPort] = useState("");
    const [baudRate, setBaudRate] = useState(9600);
    const [baudRates, setBaudRates] = useState([]);
    const [ports, setPorts] = useState([]);
    const [displaySettings, setDisplaySettings] = useState(false);
    const [displayScroll, setDisplayScroll] = useState(false);

    const first = useRef(true)
    const commandIndex = useRef(-1)
    const commands = useRef([])
    const settings = useRef({})
    const newMessageCount = useRef(0)

    const maxMessages = 1000
    const origin = window.location.origin     
    
    useEffect(() => {
        console.log("componentDidMount")
        initEventListeners()            
        getPortsAndBaud()
        getSettings()
        getPosition()

        return () => { }
    }, [])

    useEffect(() => {
        let socket = window.io(origin);
        socket.on('connect', () => {
            console.log('Connected to server')
        });

        socket.on('disconnect', () => {
            console.log('Disconnected from server')
        });

        socket.on('message', (json) => {
            const data = JSON.parse(json)
            switch(data["type"]) {
                case "read":
                case "write":
                    setMessages((prev) => [...prev.slice(0, Math.min(prev.length, maxMessages)), data])
                    newMessageCount.current++
                    break;
                case "position":
                    setAzimuth(data["data"]["x"])
                    setElevation(data["data"]["y"])
                    break
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
        const x = document.getElementById("messages")
        if(messages.length < maxMessages && x.scrollHeight - (x.scrollTop + x.clientHeight) <= newMessageCount.current * 20 + 10) {
            newMessageCount.current = 0
            x.scrollTo(0, x.scrollHeight)
            setDisplayScroll(false)
        }
        else {
            setDisplayScroll(true)
        }

        return () => {}
    }, [messages])

    const initEventListeners = () => {
        let command = document.getElementById('command')
        command.addEventListener('keydown', handleKeyboardEvent)

        let messages = document.getElementById("messages")
        messages.addEventListener("scroll", e => {
            setDisplayScroll(e.target.scrollHeight - (e.target.clientHeight + e.target.scrollTop) > 100)
        })
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

    const getSettings = async () => {
        try {
            let response = await fetch(`${origin}/api/get/settings`);
            if(response.status == 200)
                settings.current = await response.json()
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
        settings.current["offset"] = azimuth
        await saveSettings()  
    }

    const saveSettings = async () => {
        try {
            let response = await fetch(`${origin}/api/set/settings`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(settings.current)
            });
            setDisplaySettings(false)
        }
        catch(e) {
            console.log(e)
        }
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
        return (dir + settings.current["offset"]) % 360
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

    return (
        <div className="min-h-screen bg-gray-100 flex flex-row items-start justify-center p-6 flex-wrap gap-48">
            <div className="flex flex-col items-center gap-4">
                <div className="flex w-full justify-between">
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

                <div className="flex w-full justify-center gap-1">
                    <span className="w-full text-right">{azimuth}</span>
                    <span>,</span>
                    <span className="w-full">{elevation}</span>
                </div>

                <div className="flex w-full gap-2">
                    <input id="azimuth" className="flex-1 border rounded p-2 font-mono text-sm w-full" placeholder="Azimuth" />
                    <input id="elevation" className="flex-1 border rounded p-2 font-mono text-sm w-full" placeholder="Elevation" />
                    <button className="cursor-pointer text-white bg-black rounded-sm p-2" onClick={setManualPosition}>Set</button>
                    <button onClick={getPosition} className="cursor-pointer text-white bg-black rounded-sm p-2">
                        <RefreshCcw className="w-4 h-4" />
                    </button>
                </div>

                <div className="flex gap-2">
                    <button onClick={toggleMotorHold} className="cursor-pointer text-white bg-black rounded-sm p-2">Toggle Motor Hold</button>
                    <button onClick={stow} className="cursor-pointer text-white bg-black rounded-sm p-2">Stow</button>
                    <button onClick={setNorth} className="cursor-pointer text-white bg-black rounded-sm p-2">Set North</button>
                    <button onClick={resetController} className="cursor-pointer text-white bg-black rounded-sm p-2">Reset Controller</button>
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
                            {messages.map((msg, i) => <div className="w-full" key={i}>{(msg['type'] == "write" ? "> " : "") + msg['data']}</div>)}                        
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
            <button onClick={() => setDisplaySettings(!displaySettings)} className="absolute top-4 right-4 cursor-pointer text-white bg-black rounded-sm p-2"><Settings /></button>
            <div className="fixed top-0 h-full w-60 right-0 flex flex-col p-4 gap-4 bg-white" style={{transform: `translateX(${displaySettings ? "0" : "100%"})`, transition: "all 0.5s ease"}}>
                <div className="flex justify-between items-center">
                    <h2>Settings</h2>
                    <button onClick={() => setDisplaySettings(!displaySettings)} className="cursor-pointer text-white bg-black rounded-sm p-1 bg-red-500"><X /></button>
                </div>
                <div className="flex flex-col gap-4 h-full">
                    <div className="flex justify-between items-center gap-2">
                        <span>North Offset</span>
                        <input className="flex-1 border rounded p-2 font-mono text-sm w-full h-6" placeholder={settings.current["offset"]} onChange={e => settings.current["offset"] = Number(e.target.value)} />
                    </div>
                    <div className="flex justify-between items-center gap-2">
                        <span>Idle Timeout (s)</span>
                        <input className="flex-1 border rounded p-2 font-mono text-sm w-full h-6" placeholder={settings.current["idleTimeout"]} onChange={e => settings.current["idleTimeout"] = Number(e.target.value)} />
                    </div>
                    <div className="flex justify-between items-center gap-2">
                        <span>Stow Position</span>
                        <input className="flex-1 border rounded p-2 font-mono text-sm w-full h-6" placeholder={settings.current["stowPosition"]} onChange={e => settings.current["stowPosition"] = Number(e.target.value)} />
                    </div>
                </div>
                <button onClick={saveSettings} className="cursor-pointer bg-green-500 text-white rounded-md p-1">Save</button>
            </div>
        </div>
    );
}
