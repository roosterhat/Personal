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
    }

    render = () => {
        return (
            <div className="edit">
                <div className="btn-container editing">
                    <button className="btn" onClick={() => this.rotateImage(-90)}><i className="fa-solid fa-rotate-left"></i></button>
                    <button className="btn" onClick={() => this.rotateImage(90)}><i className="fa-solid fa-rotate-right"></i></button>                                
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
                        <span>States Lights</span>
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
                                />
                            ) : null
                        }
                    </div>
                </div>
            </div>
        )
    }

    cancel = () => { this.Cancel(); }

    complete = () => {
        this.setState({saving: true})
        this.Complete()
    }

    rotateImage = (degrees) => {
        if(!this.Config.frame.rotate)
            this.Config.frame.rotate = degrees;
        else
            this.Config.frame.rotate += degrees % 360;
        this.Engine.imageEffects.rotate = this.Config.frame.rotate;
        // if(this.Config.frame.crop){
        //     var c = this.Engine.Center(this.Config.frame.crop);
        //     var rads = degrees * Math.PI / 180;
        //     for(var vertex of this.Config.frame.crop.vertices){
        //         var r = this.Engine.Dist(c, vertex);
        //         var a = Math.atan2(c.y - vertex.y, c.y - vertex.x) + rads;
        //         vertex.x = c.x + Math.sin(a) * r;
        //         vertex.y = c.y + Math.cos(a) * r;
        //     }
        // }
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
            index: this.Config.frame.digits.length + 1
        });
        this.Engine.shapes.push(shape);
        this.Engine.Update();
        this.setConfig(this.Config);
    }
}

export default EditFrame