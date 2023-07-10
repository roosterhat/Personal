import React from 'react'
import LoadingSpinner from './Spinners/loading1';
import Button from './button';
import { uuidv4 } from '../Utility';

class EditFrame extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            saving: false
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
            <div className="edit">
                <div className="btn-container editing">
                    <button className="btn" onClick={() => this.rotateImage(-90)}><i className="fa-solid fa-rotate-left"></i></button>
                    <button className="btn" onClick={() => this.rotateImage(90)}><i className="fa-solid fa-rotate-right"></i></button>                                
                    <button className="btn" onClick={this.reset}><i className="fa-solid fa-eraser"></i></button>                                
                    <div className='divider'></div>
                    <button className='btn' onClick={this.cancel}><i className="fa-solid fa-xmark"></i></button>
                    <button className="btn" onClick={this.complete}>{this.state.saving ? <LoadingSpinner /> : <i className="fa-solid fa-check"></i>}</button>
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
                <div className="item-crop">
                    <div className="crop-header">
                        <span>Digits</span>
                        <button className="btn" onClick={this.addDigit}><i className="fa-solid fa-plus"></i></button>
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
                        <span>State Indicators</span>
                        <button className="btn" onClick={this.addState}><i className="fa-solid fa-plus"></i></button>
                    </div>
                    <div className="items">
                        {
                            this.Config.frame.digits ? this.Config.frame.states.map(button => 
                                <Button 
                                    key={button.id} 
                                    button={button} 
                                    update={() => this.Engine.Update()}
                                    remove={() => this.removeButton(button, this.Config.frame.states)}
                                    showAction={false}
                                    showSampler={true}
                                >
                                    <div className="state-properties">
                                        <div className="sample">
                                            <div className="sample-color" style={{background: button.properties.activeColor}}></div>
                                            <button onClick={() => this.sampleColor(button)}><i className="fa-solid fa-eye-dropper"></i></button>
                                        </div>
                                    </div>
                                </Button>
                            ) : null
                        }
                    </div>
                </div>
            </div>
        )
    }

    init = () => {
        this.Engine.shapes = []
        this.Engine.imageEffects.rotate = this.Config.frame.rotate
        var url = `http://${window.location.hostname}:3001/api/frame?${new Date().getTime()}`;
        if(this.Config.frame.position){
            this.Engine.LoadBackground(url, this.Config.frame.position).then(() => {
                this.Engine.shapes.push(...this.Config.frame.digits.map(x => x.shape))
                this.Engine.shapes.push(...this.Config.frame.states.map(x => x.shape))
                this.Engine.shapes.push(this.Config.frame.crop.shape);
                this.Engine.Update()
            })
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
            });
        }
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

        this.Config.frame.states.push({
            shape: shape,
            id: uuidv4(),
            name: '', 
            properties: {},
            index: this.Config.frame.states.length + 1
        });
        this.Engine.shapes.push(shape);
        this.Engine.Update();
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
        this.Engine.Update();
        this.setConfig(this.Config);
    }
}

export default EditFrame