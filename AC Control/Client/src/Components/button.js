import React from 'react'

class Button extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            name: this.getName(),
            action: this.props.button.action,
            color: this.props.button.shape.color
        }
    }

    render() {
        return (
            <div className='button-item' onMouseEnter={() => this.setHighlight(true)} onMouseLeave={() => this.setHighlight(false)}>
                <div className='header'>
                    <input value={this.state.name} onChange={this.setName} onBlur={() => this.setState({name: this.getName()})}/>
                    <button className='close' onClick={this.props.remove}><i className="fa-solid fa-xmark"></i></button>
                </div>
                <div className='body'>
                    <input className="color" value={this.state.color} onInput={this.setColor} type="color"/>
                    <input value={this.state.action} onChange={this.setAction} placeholder='IR data'/>
                </div>
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