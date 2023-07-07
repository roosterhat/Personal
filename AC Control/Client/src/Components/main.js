import React from 'react'
import ConfigLoader from './ConfigLoader';
import LoadingSpinner from './Spinners/loading1';
import CanvasEngine from '../canvasEngine';
import EditBackground from './EditBackground';
import EditFrame from './EditFrame';
import { uuidv4 } from '../Utility';
import '../Styles/styles.scss'

class Main extends React.Component {
    Engine = new CanvasEngine();
    Config = null;
    UpdateQueue = []

    constructor(props) {
        super(props);
        this.state = {
            editing: false,
            editingFrame: false,
            uploading: false,
            showConfigSelect: false,
            EditConfig: null,
            error: null,
            loadingFrame: false,
            hasFrame: false
        }
        console.log("Main")
    }

    componentDidMount () {
        console.log("componentDidMount")
        this.Engine.Init();
        this.load("default");
        this.checkHasWebcam();
    }

    componentDidUpdate () {
        while(this.UpdateQueue.length > 0)
            this.UpdateQueue.pop()();
    }

    render () {
        return (
            <div className="container">
                <div className="content-container">
                    { this.state.hasFrame && !this.state.editingFrame ? 
                        <div className="frame-container">
                            <div style={{position: 'relative'}}>
                                <img id="frame" src="http://localhost:3001/api/frame" onLoad={() => { this.setState({loadingFrame: false}); this.Engine.RefreshDimensions() }}/>
                                <div className={"refresh" + (this.state.loadingFrame ? " loading" : "")} onClick={this.refreshFrame}><i className="fa-solid fa-arrows-rotate"></i></div>
                            </div>
                        </div>
                        :
                        null
                    }
                    <div className="canvas-container" id="canvas-container">
                        <LoadingSpinner id="spinner" style={{display: 'none'}}/>
                        <canvas id="canvas"></canvas>
                    </div>  
                </div>                       
                <div className='controls'>
                {this.state.editing ?
                    <EditBackground Engine={this.Engine} Config={this.state.EditConfig} onConfigChange={x => this.setState({EditConfig: x})} complete={this.complete} cancel={this.cancelEdit}/>  
                    :
                (this.state.editingFrame ?
                    <EditFrame Engine={this.Engine} Config={this.state.EditConfig} onConfigChange={x => this.setState({EditConfig: x})} complete={this.complete} cancel={this.cancelEdit}/>
                    :
                    (
                        <div className="btn-container" id="normal">
                            { this.Config ? <button className="btn" onClick={this.editButtons}><i className="fa-solid fa-pen-to-square"></i></button> : null }
                            { this.Config ? <button className="btn" onClick={this.editFrame}><i className="fa-regular fa-object-group"></i></button> : null }
                            <button className="btn" onClick={this.newButtons}><i className="fa-solid fa-plus"></i></button>
                            <button className="btn" onClick={() => this.setState({showConfigSelect: true})}><i className="fa-regular fa-folder-open"></i></button>
                            <button className="btn" onClick={() => {}}><i className="fa-regular fa-clock"></i></button>
                        </div>   
                    )
                    )
                }       
                {this.state.showConfigSelect ? <ConfigLoader select={(config) => this.load(config)} close={() => this.setState({showConfigSelect: false})}/> : null}  
                </div>   
            </div>
        );
    }    

    editFrame = () => {
        var config = JSON.parse(JSON.stringify(this.Config));
        this.setState({editingFrame: true, EditConfig: config})
        this.Engine.shapes = []
        this.Engine.imageEffects = config.frame
        this.Engine.SetEdit(true);
        var url = `http://${window.location.hostname}:3001/api/frame`;
        if(config.frame.position){
            this.Engine.shapes.push(...config.frame.digits.map(x => x.shape))
            this.Engine.shapes.push(...config.frame.states.map(x => x.shape))
            this.Engine.shapes.push(config.frame.crop.shape);
            this.Engine.LoadBackground(url, config.frame.position);
        }
        else {
            this.Engine.LoadBackground(url).then(position => {
                var s = this.Engine.backgroundPosition.scale;
                var shape = { 'vertices': [
                    {x: 0, y: 0, index: 0}, 
                    {x: this.Engine.background.width * s, y: 0, index: 1},
                    {x: this.Engine.background.width * s, y: this.Engine.background.height * s, index: 2},
                    {x: 0, y: this.Engine.background.height * s, index: 3}
                ], 'type': 'poly', 'closed': true, 'highlight': false, 'color': '#000000'  };

                config.frame = {
                    position: position,
                    crop: { shape: shape, id: uuidv4() },
                    digits: [],
                    states: []
                };
                this.Engine.shapes.push(shape);
                this.Engine.Update();
                this.setState({EditConfig: config});
            });
        }
        this.UpdateQueue.push(() => this.Engine.RefreshDimensions())
    }

    editButtons = () => {
        var editConfig = JSON.parse(JSON.stringify(this.Config));
        this.setState({editing: true, EditConfig: editConfig})
        this.Engine.shapes = editConfig.buttons.map(x => x.shape);
        this.Engine.SetEdit(true);
        this.UpdateQueue.push(() => this.Engine.RefreshDimensions())
    }

    newButtons = () => {
        this.setState({editing: true, EditConfig: {
            'name': null,
            'buttons': [],
            'frame': {},
            'background': {},
            'ir_config': null,
            'id': uuidv4()
        }})
        this.Engine.shapes = [];
        this.Engine.LoadBackground(null)
        this.Engine.SetEdit(true);
        this.UpdateQueue.push(() => this.Engine.RefreshDimensions())
    }

    cancelEdit = () => {
        const buttons = JSON.parse(JSON.stringify(this.Config && this.Config.buttons ? this.Config.buttons : []))
        this.setState({editing: false, editingFrame: false, EditConfig: null, error: null})
        this.Engine.shapes = buttons.map(x => { 
            x.shape['function'] = () => this.triggerIr(x.id);
            return x.shape
        });
        this.Engine.imageEffects = {}
        var background = this.Config && this.Config.background ? this.Config.background : null
        this.Engine.LoadBackground((background ? `http://${window.location.hostname}:3001/api/background/${background.file}` : null), background.position)
        this.Engine.SetEdit(false);
        this.UpdateQueue.push(() => this.Engine.RefreshDimensions())
    }
    
    complete = async () => {
        if(!this.state.EditConfig.name || this.state.EditConfig.name.trim().length == 0) {
            this.setState({error: "Enter a valid config name"})
            return;
        }
        else if(!this.state.EditConfig.ir_config || this.state.EditConfig.ir_config.trim().length == 0) {
            this.setState({error: "Enter a valid config file"})
            return;
        }
        else {
            this.Engine.Reset()
            this.setState({saving: true, error: false}) 
            await this.save()
            this.setState({saving: false, editing: false, editingFrame: false}) 
            this.Engine.SetEdit(false);
            this.UpdateQueue.push(() => this.Engine.RefreshDimensions())
        }
    }

    save = async (name) => {
        if(name)
            this.state.EditConfig.name = name
        try{
            if(Object.prototype.toString.call(this.state.EditConfig.background.file) === "[object File]"){
                const response = await fetch(`http://${window.location.hostname}:3001/api/upload`,{
                    method: "POST",
                    headers: { "Content-Type": "image" },
                    body: this.state.EditConfig.background.file
                })
                if(response.status == 200)
                    this.state.EditConfig.background.file = await response.text()
            }
            const response = await fetch(`http://${window.location.hostname}:3001/api/save`,{
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(this.state.EditConfig)
            })
            if(response.status == 200){
                this.setState({showNameInput: false})
                this.Config = JSON.parse(JSON.stringify(this.state.EditConfig))
            }
        }
        catch(ex) {
            console.error(ex);
        }       
    }

    load = async (name) => {
        try{
            this.setState({showConfigSelect: false})
            const response = await fetch(`http://${window.location.hostname}:3001/api/retrieve/${name}`)
            if(response.status == 200){
                this.Config = await response.json();
                const buttons = JSON.parse(JSON.stringify(this.Config.buttons))
                this.Engine.shapes = buttons.map(x => { 
                    x.shape['function'] = () => this.triggerIr(x.id);
                    return x.shape
                });
                this.Engine.LoadBackground(`http://${window.location.hostname}:3001/api/background/${this.Config.background.file}`, this.Config.background.position)
            }
        }
        catch(ex) {
            console.error(ex);
        } 
        finally {
        }
    }

    upload = async (files) => {
        if(files && files.length > 0){
            var position = await this.Engine.LoadBackground(files[0])
            this.state.EditConfig.background = { 'file': files[0], 'position': position };
        }
    }

    setConfig = (name) => {
        this.state.EditConfig.ir_config = name;
        this.setState({EditConfig: this.state.EditConfig})
    }

    triggerIr = async (id) => {
        try {
            const response = await fetch(`http://${window.location.hostname}:3001/api/trigger/${this.Config.id}/${id}`)
            this.refreshFrame();
        }
        catch(ex) {
            console.error(ex);
        } 
    }

    refreshFrame = () => {
        this.setState({loadingFrame: true});
        var elem = document.getElementById("frame");
        elem.src = `api/frame?${new Date().getTime()}`;
    }

    checkHasWebcam = async () => {
        try {
            const response = await fetch(`http://${window.location.hostname}:3001/api/frame`)
            this.setState({hasFrame: response.status == 200})
        }
        catch(ex) {
            console.error(ex);
            this.setState({hasFrame: false})
        }
    }
}

export default Main