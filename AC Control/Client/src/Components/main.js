import React from 'react'
import Button from './button'
import ConfigLoader from './ConfigLoader';
import LoadingSpinner from './Spinners/loading1';
import CanvasEngine from '../canvasEngine';
import '../Styles/styles.scss'

class Main extends React.Component {
    Engine = new CanvasEngine();
    Config = null;

    constructor(props) {
        super(props);
        this.state = {
            editing: false,
            saving: false,
            uploading: false,
            showConfigSelect: false,
            EditConfig: null,
            error: null,
            loadingFrame: false,
            hasFrame: false,
            buttons: []
        }
        console.log("Main")
    }

    componentDidMount () {
        console.log("componentDidMount")
        this.Engine.Init();
        this.load("default");
        this.checkHasWebcam();
    }

    render () {
        return (
            <div className="container">
                <div className="content-container">
                    { this.state.hasFrame ? 
                        <div className="frame-container">
                            <div style={{position: 'relative'}}>
                                <img id="frame" src="api/frame"/>
                                <div className={"refresh" + (this.loadingFrame ? " loading" : "")} onClick={this.refreshFrame}><i className="fa-solid fa-arrows-rotate"></i></div>
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
                    (
                        <div id="edit">
                            <div className={"btn-container" + (this.state.editing ? " editing" : "")}>
                                <button className="btn" onClick={() => this.addShape('poly')}><i className="fa-solid fa-draw-polygon"></i></button>
                                <button className="btn" onClick={() => this.addShape('ellipse')}><i className="fa-regular fa-circle"></i></button>
                                <button className="btn">
                                    {this.state.uploading ? 
                                        <LoadingSpinner /> 
                                        : 
                                        <div className='file-upload'>
                                            <label htmlFor="upload"><i className="fa-regular fa-image"></i></label>
                                            <input type="file" id="upload" name="upload" accept=".jpg, .jpeg, .png" onChange={e => this.upload(e.target.files)}/>
                                        </div>
                                    }
                                </button>
                                <div className='divider'></div>
                                <button className='btn' onClick={this.cancelEdit}><i className="fa-solid fa-xmark"></i></button>
                                <button className="btn" onClick={this.complete}>{this.state.saving ? <LoadingSpinner /> : <i className="fa-solid fa-check"></i>}</button>
                            </div>
                            {this.state.error ? <div className='error-message'>{this.state.error}</div> : null}
                            <input className='config-name' value={this.state.EditConfig.name} onChange={e => this.setConfigName(e.target.value)} placeholder='Display name'/>
                            <input className='config-name' value={this.state.EditConfig.ir_config} onChange={e => this.setConfig(e.target.value)} placeholder='Config remote name'/>
                            <div className="buttons">
                            {
                                this.state.buttons.map(button => 
                                    <Button 
                                        key={button.id} 
                                        button={button} 
                                        update={() => this.Engine.Update()}
                                        remove={() => this.removeButton(button)}
                                    />
                                )
                            }
                            </div>
                        </div>
                    ) :
                    (
                        <div className="btn-container" id="normal">
                            { this.Config ? <button className="btn" onClick={this.edit}><i className="fa-solid fa-pen-to-square"></i></button> : null }
                            <button className="btn" onClick={this.new}><i className="fa-solid fa-plus"></i></button>
                            <button className="btn" onClick={() => this.setState({showConfigSelect: true})}><i className="fa-regular fa-folder-open"></i></button>
                            <button className="btn" onClick={() => {}}><i className="fa-regular fa-clock"></i></button>
                        </div>   
                    )
                }       
                {this.state.showConfigSelect ? <ConfigLoader select={(config) => this.load(config)} close={() => this.setState({showConfigSelect: false})}/> : null}  
                </div>   
            </div>
        );
    }

    setConfigName = (name) => {
        this.state.EditConfig.name = name;
        this.setState({EditConfig: this.state.EditConfig})
    }

    addShape = (type) => {
        if(this.Engine.currentShape)
            this.Engine.shapes.pop();

        const id = this.uuidv4();
        if(type === "poly")
            this.Engine.currentShape = { 'vertices': [], 'type': 'poly', 'closed': false, 'highlight': false, 'color': '#000000'  };
        else if(type === "ellipse")
            this.Engine.currentShape = { 'x': 0, 'y': 0, r1: 10, r2: 10, 'type': 'ellipse', 'closed': false, 'highlight': false, 'color': '#000000' };

        this.Engine.currentShape['function'] = () => {this.triggerIr(id)};

        this.Engine.shapes.push(this.Engine.currentShape)
        this.state.buttons.push({ 
            'id': id, 
            'shape': this.Engine.currentShape, 
            'name': '', 
            'action': '',
            'index': this.state.buttons.length
        })
        this.setState({buttons: this.state.buttons})
    }
    
    removeButton = (button) => {
        var index = this.state.buttons.indexOf(button);
        if (index > -1) {
            this.Engine.RemoveShape(button.shape)
            this.state.buttons.splice(index, 1);
            this.setState({buttons: this.state.buttons})
        }
    }

    edit = () => {
        this.setState({editing: true, EditConfig: JSON.parse(JSON.stringify(this.Config))})
        this.Engine.SetEdit(true);
    }

    new = () => {
        this.setState({editing: true, buttons: [], EditConfig: {
            'name': null,
            'buttons': [],
            'background': {},
            'ir_config': null,
            'id': this.uuidv4()
        }})
        this.Engine.shapes = [];
        this.Engine.LoadBackground(null)
        this.Engine.SetEdit(true);
    }

    cancelEdit = () => {
        const buttons = JSON.parse(JSON.stringify(this.Config && this.Config.buttons ? this.Config.buttons : []))
        this.setState({editing: false, buttons: buttons, EditConfig: null, error: null})
        this.Engine.shapes = buttons.map(x => { 
            x.shape['function'] = () => this.triggerIr(x.id);
            return x.shape
        });
        var background = this.Config && this.Config.background ? this.Config.background : null
        if(background.file !== this.state.EditConfig.background.file)
            this.Engine.LoadBackground(background.file, background.position)
        this.Engine.SetEdit(false);
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
            this.setState({saving: false, editing: false}) 
            this.Engine.SetEdit(false);
        }
    }
    
    uuidv4 = () => {
        return ([1e7]+-1e3+-4e3+-8e3+-1e11).replace(/[018]/g, c =>
          (c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16)
        );
    }

    save = async (name) => {
        if(name)
            this.state.EditConfig.name = name
        this.state.EditConfig.buttons = this.state.buttons;
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
            this.setState({showConfigSelect: false, uploading: true})
            const response = await fetch(`http://${window.location.hostname}:3001/api/retrieve/${name}`)
            if(response.status == 200){
                this.Config = await response.json();
                const buttons = JSON.parse(JSON.stringify(this.Config.buttons))
                this.setState({buttons: buttons})
                this.Engine.shapes = buttons.map(x => { 
                    x.shape['function'] = () => this.triggerIr(x.id);
                    return x.shape
                });
                this.Engine.LoadBackground(this.Config.background.file, this.Config.background.position)
                this.Engine.Update()
            }
        }
        catch(ex) {
            console.error(ex);
        } 
        finally {
            this.setState({uploading: false})
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
        elem.onload = () => { this.setState({loadingFrame: false}) }
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