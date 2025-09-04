import React, { useState, useEffect, useRef } from "react";
import { ArrowUp, ArrowDown, ArrowLeft, ArrowRight, Home, RefreshCcw, Trash } from "lucide-react";

export default function SatelliteTracker() {
    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState("");
    const [azimuth, setAzimuth] = useState(0);
    const [elevation, setElevation] = useState(0);
    const [port, setPort] = useState("");
    const [baudRate, setBaudRate] = useState(9600);
    const [baudRates, setBaudRates] = useState([]);
    const [ports, setPorts] = useState([]);
    const [settings, setSettings] = useState({})

    const first = useRef(true)
    const commandIndex = useRef(-1)
    const commands = useRef([])

    const origin = "http://192.168.1.181:3001" //window.location.origin     
    
    useEffect(() => {
        console.log("componentDidMount")
        initEventListeners()            
        getPortsAndBaud()
        getSettings()
        getPosition()

        return () => { }
    }, [])

    useEffect(() => {
        var socket = window.io(origin);
        socket.on('connect', () => {
            console.log('Connected to server');
        });

        socket.on('disconnect', () => {
            console.log('Disconnected from server');
        });

        socket.on('message', (json) => {
            const data = JSON.parse(json)
            console.log('socket message: ' + json);
            switch(data["type"]) {
                case "read":
                case "write":
                    console.log(data['data'])
                    setMessages((prev) => [...prev, data]);
                    console.log(messages)
                    break;
            }       
        });

        return () => {
            if(socket)
                socket.close()
        }
    }, [])

    useEffect(() => {
        const x = document.getElementById("messages")
        x.scrollTo(0, x.scrollHeight)
    }, [messages])

    const initEventListeners = () => {
        var command = document.getElementById('command')
        command.addEventListener('keydown', handleKeyboardEvent)
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
        commands.current = [cmd, ...commands.current]
        commandIndex.current = -1
        var response = await fetch(`${origin}/api/send/command`, {
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
            var response = await fetch(`${origin}/api/scan/baud`);
            if(response.status == 200)
                await getSettings()
        }
        catch(e) {
            console.log(e)
        }
    };    

    const getSettings = async () => {
        try {
            var response = await fetch(`${origin}/api/get/settings`);
            if(response.status == 200)
                setSettings(await response.json())
        }
        catch(e) {
            console.log(e)
        }
    }

    const setPosition = async (coords, absolute) => {
        try {
            var response = await fetch(`${origin}/api/set/position`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ "coordinates": coords, "absolute": absolute })
            });
            if(response.status == 200)
                await getPosition()
        }
        catch(e) {
            console.log(e)
        }
    }

    const getPosition = async () => {
        try {
            var response = await fetch(`${origin}/api/get/position`);
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
            var response = await fetch(`${origin}/api/home`);
            if(response.status == 200)
                await getPosition()
        }
        catch(e) {
            console.log(e)
        }
    }

    const toggleMotorHold = async () => {
        try {
            var response = await fetch(`${origin}/api/toggle/motors`);
        }
        catch(e) {
            console.log(e)
        }
    }

    const stow = async () => {
        await setPosition({x: 0, y: 0}, true)
        await toggleMotorHold()
    }

    const updatePortAndBaud = async () => {
        try {
            var response = await fetch(`${origin}/api/set/port`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ "port": port }),

            });
        }
        catch(e) {
            console.log(e)
        }

        try {
            var response = await fetch(`${origin}/api/set/baud`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ "baud": baudRate })
            });                
        }
        catch(e) {
            console.log(e)
        }

        await getPosition()
    }

    const getPortsAndBaud = async () => {
        try {
            var response = await fetch(`${origin}/api/get/ports`);
            if(response.status == 200) {
                var body = await response.json()
                setPorts(body)
                if (body.length > 0 && body.indexOf(port) == -1)
                    setPort(body[0])
            }
        }
        catch(e) {
            console.log(e)
        }

        try{
            var response = await fetch(`${origin}/api/get/baud`);
            if(response.status == 200) {
                var body = await response.json()
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

    }

    return (
        <div className="min-h-screen bg-gray-100 flex flex-row items-start justify-center p-6 space-x-12 flex-wrap gap-48">
            <div className="flex flex-col items-center space-y-6 m-0">
                <div className="flex space-x-12 w-full justify-between">
                    <div className="grid grid-cols-3 gap-4 m-0">
                        <div></div>
                        <button onClick={() => setPosition({x: 0, y: 10}, false)} className="rounded-full w-12 h-12 flex items-center justify-center cursor-pointer text-white bg-black">
                            <ArrowUp />
                        </button>
                        <div></div>

                        <button onClick={() => setPosition({x: 10, y: 0}, false)} className="rounded-full w-12 h-12 flex items-center justify-center cursor-pointer text-white bg-black">
                            <ArrowLeft />
                        </button>
                        <button onClick={() => initHoming()} className="rounded-full w-12 h-12 flex items-center justify-center cursor-pointer text-white bg-black">
                            <Home />
                        </button>
                        <button onClick={() => setPosition({x: -10, y: 0}, false)} className="rounded-full w-12 h-12 flex items-center justify-center cursor-pointer text-white bg-black">
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
                        <button onClick={() => setPosition({x: 0, y: elevation}, true)} className="rounded-full w-12 h-12 flex items-center justify-center cursor-pointer text-white bg-black">N</button>
                        <div></div>

                        <button onClick={() => setPosition({x: 270, y: elevation}, true)} className="rounded-full w-12 h-12 flex items-center justify-center cursor-pointer text-white bg-black">W</button>
                        <div></div>
                        <button onClick={() => setPosition({x: 90, y: elevation}, true)} className="rounded-full w-12 h-12 flex items-center justify-center cursor-pointer text-white bg-black">E</button>

                        <div></div>
                        <button onClick={() => setPosition({x: 180, y: elevation}, true)} className="rounded-full w-12 h-12 flex items-center justify-center cursor-pointer text-white bg-black">S</button>
                        <div></div>
                    </div>
                </div>

                <div className="flex space-x-2 w-full">
                    <input
                        id="azimuth"
                        className="flex-1 border rounded p-2 font-mono text-sm w-full"
                        value={azimuth}
                        onChange={(e) => setAzimuth(Number(e.target.value) % 360)}
                        placeholder="Azimuth"
                    />
                    <input
                        id="elevation"
                        className="flex-1 border rounded p-2 font-mono text-sm w-full"
                        value={elevation}
                        onChange={(e) => setElevation(Number(e.target.value) % 180)}
                        placeholder="Elevation"
                    />
                    <button className="cursor-pointer text-white bg-black rounded-sm p-2" onClick={() => setPosition({x: azimuth, y: elevation}, true)}>Set</button>
                    <button onClick={getPosition} className="cursor-pointer text-white bg-black rounded-sm p-2">
                        <RefreshCcw className="w-4 h-4" />
                    </button>
                </div>

                <div className="flex space-x-2">
                    <button onClick={toggleMotorHold} className="cursor-pointer text-white bg-black rounded-sm p-2">Toggle Motor Hold</button>
                    <button onClick={stow} className="cursor-pointer text-white bg-black rounded-sm p-2">Stow</button>
                    <button onClick={setNorth} className="cursor-pointer text-white bg-black rounded-sm p-2">Set North</button>
                </div>
            </div>

            <div className="flex flex-col space-y-6 w-full max-w-lg">
                <div className="flex space-x-2">
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

                <div className="w-full">
                    <div id="messages" className="h-48 bg-black text-green-400 font-mono text-sm overflow-y-auto p-2 rounded mb-2">
                        {messages.map((msg, i) => <div className="w-full" key={i}>{(msg['type'] == "write" ? "> " : "") + msg['data']}</div>)}
                    </div>

                    <div className="flex space-x-2">
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
        </div>
    );
}
