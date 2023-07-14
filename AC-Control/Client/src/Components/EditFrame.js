import React from 'react'
import LoadingSpinner from './Spinners/loading1';
import Button from './button';
import { fetchWithToken, uuidv4 } from '../Utility';
import Menu from './Menu';

class EditFrame extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            saving: false,
            toggle: false
        }

        this.Config = props.Config;
        this.setConfig = props.onConfigChange
        this.Cancel = props.cancel;
        this.Complete = props.complete;
        this.Engine = props.Engine;
        this.init();
    }

    render = () => {
        return (
            <Menu>
                <div className="edit">
                    <div className="btn-container editing">
                        <button className="btn" onClick={() => this.rotateImage(-90)}><i className="fa-solid fa-rotate-left"></i></button>
                        <button className="btn" onClick={() => this.rotateImage(90)}><i className="fa-solid fa-rotate-right"></i></button>                                
                        <button className="btn" onClick={this.reset}><i className="fa-solid fa-eraser"></i></button>                                
                        <div className='divider'></div>
                        <button className='btn cancel' onClick={this.cancel}><i className="fa-solid fa-xmark"></i></button>
                        <button className="btn confirm" onClick={this.complete}>{this.state.saving ? <LoadingSpinner /> : <i className="fa-solid fa-check"></i>}</button>
                    </div>
                    <div className="main-crop">
                        <div className="crop-header">Main Crop</div>
                        <div className="items">
                            {this.Config.frame.crop ? 
                                <Button 
                                    key={this.Config.frame.crop.id} 
                                    button={this.Config.frame.crop} 
                                    update={() => this.Engine.Update()}
                                    showName={false}
                                    showRemove={false}
                                    showAction={false}
                                /> : null 
                            }
                        </div>
                    </div>
                    <div className="item-crop minimal">
                        <div className="crop-header">
                            <div className="header-title">
                                <span>Digits</span>
                                <button className="btn" onClick={this.addDigit}><i className="fa-solid fa-plus"></i></button>
                            </div>                            
                        </div>
                        <div className="items">
                            {
                                this.Config.frame.digits ? this.Config.frame.digits.map(button => 
                                    <Button 
                                        key={button.id} 
                                        button={button} 
                                        update={() => this.Engine.Update()}
                                        remove={() => this.removeButton(button, this.Config.frame.digits)}
                                        showAction={false}
                                    />
                                ) : null
                            }
                        </div>
                    </div>
                    <div className="item-crop">
                        <div className="crop-header">
                            <div className="header-title">
                                <span>State Indicators</span>
                                <button className="btn" onClick={this.addState}><i className="fa-solid fa-plus"></i></button>
                            </div>
                            <div className="header-action">
                                <select name="buttons" id="toggle-select">
                                    {this.Config.buttons.map(x => <option key={x.id} value={x.id}>{x.name}</option>)}
                                </select>
                                <button onClick={this.toggleState}>{this.state.toggle ? <LoadingSpinner id="spinner" /> : "Trigger"}</button>
                            </div>
                        </div>
                        <div className="items">
                            {
                                this.Config.frame.states ? this.Config.frame.states.map((button, index) => 
                                    <Button 
                                        key={button.id} 
                                        button={button} 
                                        update={() => this.Engine.Update()}
                                        remove={() => this.removeButton(button, this.Config.frame.states)}
                                        showAction={false}
                                        showSampler={true}
                                        Reorder={{index: index, max: this.Config.frame.states.length, onReorder: dir => this.reorder(index, dir)}}
                                    >
                                        <div className="state-properties">
                                            <div>
                                                <div>CDT</div>
                                                <input type="number" min="0" max="50" 
                                                    value={button.properties.colorDistanceThreshold} 
                                                    onChange={e => this.updateStateProperty(button, "colorDistanceThreshold", Number(e.target.value))}/>
                                            </div>
                                            <div>
                                                <div>SAP</div>
                                                <input type="number" min="1" max="100" 
                                                    value={button.properties.stateActivationPercentage}
                                                    onChange={e => this.updateStateProperty(button, "stateActivationPercentage", Number(e.target.value))}/>
                                            </div>
                                            <div>
                                                <div>Active Color</div>
                                                <div className="sample">
                                                    <div className="sample-color" style={{background: button.properties.activeColor}}></div>
                                                    <button onClick={() => this.sampleColor(button)}><i className="fa-solid fa-eye-dropper"></i></button>
                                                </div>
                                            </div>
                                        </div>
                                    </Button>
                                ) : null
                            }
                        </div>
                    </div>
                </div>
            </Menu>
        )
    }

    init = async () => {
        this.Engine.shapes = []
        this.Engine.imageEffects.rotate = this.Config.frame.rotate
        var background = null;
        this.Engine.StartBackgroundLoad();
        var response = await fetchWithToken(`api/frame?${new Date().getTime()}`);
        if(response.status == 200){
            var blob = await response.blob()
            background = URL.createObjectURL(blob);
        }
        if(this.Config.frame.position){
            await this.Engine.LoadBackground(background, this.Config.frame.position)
            this.Engine.shapes.push(...this.Config.frame.digits.map(x => x.shape))
            this.Engine.shapes.push(...this.Config.frame.states.map(x => x.shape))
            this.Engine.shapes.push(this.Config.frame.crop.shape);
            this.Engine.Update()
        }
        else {
            var position = await this.Engine.LoadBackground(background)
            var s = this.Engine.backgroundPosition.scale;
            var shape = { 'vertices': [
                {x: 0, y: 0, index: 0}, 
                {x: this.Engine.background.width * s, y: 0, index: 1},
                {x: this.Engine.background.width * s, y: this.Engine.background.height * s, index: 2},
                {x: 0, y: this.Engine.background.height * s, index: 3}
            ], 'type': 'poly', 'closed': true, 'highlight': false, 'color': '#000000'  };

            this.Config.frame = {
                position: position,
                crop: { shape: shape, id: uuidv4() },
                digits: [],
                states: [],
                rotate: 0
            };
            this.Engine.shapes.push(shape);
            this.Engine.Update();
            this.setConfig(this.Config);
        }
    }

    reorder = (index, dir) => {
        var temp = this.Config.frame.states[index + dir];
        this.Config.frame.states[index + dir] = this.Config.frame.states[index]
        this.Config.frame.states[index] = temp
        this.setConfig(this.Config)
    }

    sampleColor = (button) => {
        this.Engine.UserSampleColor().then(x => {
            button.properties.activeColor = x;
            this.setConfig(this.Config)
        })
    }

    cancel = () => { this.Cancel(); }

    complete = () => {
        this.setState({saving: true})
        this.Complete()
    }

    reset = () => {
        this.Config.frame = {}
        this.init();
    }

    rotateImage = (degrees) => {
        this.Config.frame.rotate += degrees % 360;
        this.Engine.imageEffects.rotate = this.Config.frame.rotate;
        this.setConfig(this.Config)
        this.Engine.Update()
    }

    removeButton = (button, list) => {
        var index = list.indexOf(button);
        if (index > -1) {
            this.Engine.RemoveShape(button.shape)
            list.splice(index, 1);
            this.setConfig(this.Config)
        }
    }  
    
    addState = () => {
        var r = 20;
        var shape = { 'x': 0, 'y': 0, r1: r, r2: r, 'type': 'ellipse', 'closed': true, 'highlight': false, 'color': this.Config.frame.crop.shape.color };

        var state = {
            shape: shape,
            id: uuidv4(),
            name: '', 
            properties: { "colorDistanceThreshold": 20, "stateActivationPercentage": 5, "activeColor": null },
            index: this.Config.frame.states.length + 1            
        }

        this.Engine.shapes.push(shape);
        this.Engine.currentDrag = {
            "item": shape, 
            "type": "ellipse", 
            "onDragStop": (e) => {state.properties.activeColor = this.Engine.SampleBackgroundColor(e); this.setConfig(this.Config);}
        };
        this.Engine.dragging = true
        this.Engine.Update();
        this.Config.frame.states.push(state);
        this.setConfig(this.Config);
    }

    addDigit = () => {
        var s = 40;
        var shape = { 'vertices': [
            {x: 0, y: 0},
            {x: 0, y: s},
            {x: s, y: s},
            {x: s, y: 0},
        ], 'type': 'poly', 'closed': true, 'highlight': false, 'color': this.Config.frame.crop.shape.color };

        this.Config.frame.digits.push({
            shape: shape,
            id: uuidv4(),
            name: '', 
            properties: {},
            index: this.Config.frame.digits.length + 1
        });
        this.Engine.shapes.push(shape);
        this.Engine.currentDrag = {"item": shape, "type": "poly"};
        this.Engine.dragging = true
        this.Engine.lastMouse = {x: s/2, y: s/2};
        this.Engine.Update();
        this.setConfig(this.Config);
    }

    updateStateProperty(state, key, value) {
        state.properties[key] = value;
        this.setConfig(this.Config);
    }

    toggleState = () => {
        var elem = document.getElementById("toggle-select")
        if(elem){
            var id = elem.value;
            if(id)
                this.triggerIr(id)
        }
    }

    triggerIr = async (id) => {
        try {
            const response = await fetchWithToken(`api/trigger/${this.Config.id}/${id}`)
            await new Promise(resolve => setTimeout(resolve, this.Settings && this.Settings.frameRefreshDelay ? this.Settings.frameRefreshDelay : 100));
            this.props.refresh();
        }
        catch(ex) {
            console.error(ex);
        } 
    }
}

export default EditFrame