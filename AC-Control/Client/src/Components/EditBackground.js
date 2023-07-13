import React from 'react'
import LoadingSpinner from './Spinners/loading1';
import Button from './button';
import { uuidv4 } from '../Utility.js';
import Menu from './Menu';

class EditBackground extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            uploading: false,
            saving: false,
            error: null
        }
        this.Config = props.Config;
        this.setConfig = props.onConfigChange
        this.Cancel = props.cancel;
        this.Complete = props.complete;
        this.Engine = props.Engine;
    }

    render = () => {
        return (
            <Menu>
                <div className="edit">
                    <div className="btn-container editing">
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
                        <button className='btn cancel' onClick={this.cancel}><i className="fa-solid fa-xmark"></i></button>
                        <button className="btn confirm" onClick={this.complete}>{this.state.saving ? <LoadingSpinner /> : <i className="fa-solid fa-check"></i>}</button>
                    </div>
                    {this.state.error ? <div className='error-message'>{this.state.error}</div> : null}
                    <input className='config-name' value={this.Config.name} onChange={e => this.setConfigName(e.target.value)} placeholder='Display name'/>
                    <input className='config-name' value={this.Config.ir_config} onChange={e => this.setIRConfig(e.target.value)} placeholder='Config remote name'/>
                    <div className="buttons">
                    {
                        this.Config.buttons.map(button => 
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
            </Menu>
        )
    }

    cancel = () => { this.Cancel(); }

    complete = () => {
        this.setState({saving: true});
        this.Complete();
    }

    setConfigName = (name) => {
        this.Config.name = name;
        this.setConfig(this.Config)
    }

    addShape = (type) => {
        if(this.Engine.currentShape)
            this.Engine.shapes.pop();

        const id = uuidv4();
        if(type === "poly")
            this.Engine.currentShape = { 'vertices': [], 'type': 'poly', 'closed': false, 'highlight': false, 'color': '#000000'  };
        else if(type === "ellipse")
            this.Engine.currentShape = { 'x': 0, 'y': 0, r1: 10, r2: 10, 'type': 'ellipse', 'closed': false, 'highlight': false, 'color': '#000000' };

        this.Engine.currentShape['function'] = () => {this.triggerIr(id)};

        this.Engine.shapes.push(this.Engine.currentShape)
        this.Config.buttons.push({ 
            'id': id, 
            'shape': this.Engine.currentShape, 
            'name': '', 
            'action': '',
            'index': this.Config.buttons.length
        })
        this.setConfig(this.Config)
    }
    
    removeButton = (button) => {
        var index = this.Config.buttons.indexOf(button);
        if (index > -1) {
            this.Engine.RemoveShape(button.shape)
            this.Config.buttons.splice(index, 1);
            this.setConfig(this.Config)
        }
    }  
    
    upload = async (files) => {
        if(files && files.length > 0){
            try{
                this.setState({uploading: true})
                var position = await this.Engine.LoadBackground(files[0])
                this.Config.background = { 'file': files[0], 'position': position };
                this.setConfig(this.Config)
            }
            finally{
                this.setState({uploading: false})
            }
        }
    }

    setIRConfig = (name) => {
        this.Config.ir_config = name;
        this.setConfig(this.Config)
    }

}

export default EditBackground