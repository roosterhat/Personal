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
            buttons: []
        }
        console.log("Main")
    }

    componentDidMount () {
        console.log("componentDidMount")
        this.Engine.Init();
        this.load("default");
    }

    render () {
        return (
            <div className="container">
                <div className="canvas-container">
                    <LoadingSpinner id="spinner" style={{display: 'none'}}/>
                    <canvas id="canvas"></canvas>
                </div>            
                {this.state.editing ?
                    (
                        <div id="edit">
                            <div className="btn-container">
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
                            <input className='config-name' value={this.state.EditConfig.name} onChange={e => this.setConfigName(e.target.value)} placeholder='Config name'/>
                            <div className="buttons">
                                {
                                    this.state.buttons.map(button => 
                                        <Button 
                                            key={button.id} 
                                            button={button} 
                                            update={() => this.Engine.Update()}
                                            remove={() => this.removeButton(button)}
                                        />)
                                }
                            </div>
                        </div>
                    ) :
                    (
                        <div className="btn-container" id="normal">
                            { this.Config ? <button className="btn" onClick={this.edit}><i className="fa-solid fa-pen-to-square"></i></button> : null }
                            <button className="btn" onClick={this.new}><i className="fa-solid fa-plus"></i></button>
                            <button className="btn" onClick={() => this.setState({showConfigSelect: true})}><i className="fa-regular fa-folder-open"></i></button>
                        </div>   
                    )
                }       
                {this.state.showConfigSelect ? <ConfigLoader select={(config) => this.load(config)} close={() => this.setState({showConfigSelect: false})}/> : null}  
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

        if(type === "poly")
            this.Engine.currentShape = { 'vertices': [], 'type': 'poly', 'closed': false, 'highlight': false, 'color': '#000000' };
        else if(type === "ellipse")
            this.Engine.currentShape = { 'x': 0, 'y': 0, r1: 10, r2: 10, 'type': 'ellipse', 'closed': false, 'highlight': false, 'color': '#000000' };

        this.Engine.shapes.push(this.Engine.currentShape)
        this.state.buttons.push({ 
            'id': this.uuidv4(), 
            'shape': this.Engine.currentShape, 
            'name': '', 
            'index': this.state.buttons.length, 
            'function': () => {} 
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
            'background': null,
            'id': this.uuidv4()
        }})
        this.Engine.shapes = [];
        this.Engine.LoadBackground(null)
        this.Engine.SetEdit(true);
    }

    cancelEdit = () => {
        const buttons = JSON.parse(JSON.stringify(this.Config && this.Config.buttons ? this.Config.buttons : []))
        this.setState({editing: false, buttons: buttons, EditConfig: null, error: null})
        this.Engine.shapes = buttons.map(x => x.shape);
        var background = this.Config && this.Config.background ? this.Config.background : null
        if(background !== this.state.EditConfig.background)
            this.Engine.LoadBackground(background)
        this.Engine.SetEdit(false);
    }
    
    complete = async () => {
        if(!this.state.EditConfig.name || this.state.EditConfig.name.trim().length == 0) {
            this.setState({error: "Enter a valid config name"})
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
            if(Object.prototype.toString.call(this.state.EditConfig.background) === "[object File]"){
                const response = await fetch(`http://${window.location.hostname}:3001/upload`,{
                    method: "POST",
                    headers: { "Content-Type": "image" },
                    body: this.state.EditConfig.background
                })
                if(response.status == 200)
                    this.state.EditConfig.background = await response.text()
            }
            const response = await fetch(`http://${window.location.hostname}:3001/save`,{
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
            const response = await fetch(`http://${window.location.hostname}:3001/retrieve/${name}`)
            if(response.status == 200){
                this.Config = await response.json();
                const buttons = JSON.parse(JSON.stringify(this.Config.buttons))
                this.setState({buttons: buttons})
                this.Engine.shapes = buttons.map(x => x.shape);
                this.Engine.LoadBackground(this.Config.background)
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
            this.state.EditConfig.background = files[0];
            this.Engine.LoadBackground(files[0])
        }
    }

    triggerIr = (button) => {

    }
}

export default Main