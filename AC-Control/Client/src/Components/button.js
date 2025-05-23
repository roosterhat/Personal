import React from 'react'

class Button extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            name: this.props.button.name ? this.getName() : null,
            action: this.props.button.action,
            color: this.props.button.shape ? this.props.button.shape.color : null
        }
        
        this.showName = this.props.showName == null ? true : this.props.showName
        this.showRemove = this.props.showRemove == null ? true : this.props.showRemove
        this.showColor = this.props.showColor == null ? true : this.props.showColor
        this.showAction = this.props.showAction == null ? true : this.props.showAction
    }

    render() {
        return (
            <div className='button-item' onMouseEnter={() => this.setHighlight(true)} onMouseLeave={() => this.setHighlight(false)}>
                { this.showName || this.showRemove ?
                    <div className='header'>
                        { this.showName ? <input value={this.state.name} onChange={this.setName} onBlur={() => this.setState({name: this.getName()})}/> : null }
                        { this.props.Reorder ? 
                            <div className="reorder-container">
                                {this.props.Reorder.index < this.props.Reorder.max - 1 ? <button className="btn reorder down" onClick={() => this.props.Reorder.onReorder(1)}><i class="fa-solid fa-sort-up"></i></button> : <div className="reorder-blank"/>}
                                {this.props.Reorder.index > 0 ? <button className="btn reorder up" onClick={() => this.props.Reorder.onReorder(-1)}><i class="fa-solid fa-sort-up"></i></button> : <div className="reorder-blank"/>}                
                            </div> 
                            : null 
                        }
                        { this.showRemove ? <button className='close' onClick={this.props.remove}><i className="fa-solid fa-xmark"></i></button> : null }
                    </div> : null
                }
                { this.showColor || this.showAction ?
                    <div className='body'>
                        { this.showColor ? <input className="color" value={this.state.color} onInput={this.setColor} type="color"/> : null }
                        { this.showAction ? <input value={this.state.action} onChange={this.setAction} placeholder='IR data'/> : null }
                    </div> : null
                }
                {this.props.children}
            </div>
        );
    }

    getName = () => {
        return this.props.button.name.length > 0 ? this.props.button.name : `Button ${this.props.button.index}`;
    }

    setName = (e) => {
        this.props.button.name = e.target.value;
        this.setState({name: e.target.value})
    }

    setColor = (e) => {
        this.props.button.shape.color = e.target.value;
        this.setState({color: e.target.value})
        this.props.update()
    }

    setAction = (e) => {
        this.props.button.action = e.target.value;
        this.setState({action: e.target.value})
    }

    setHighlight = (state) => {
        this.props.button.shape.highlight = state;
        this.props.update()
    }
}

export default Button