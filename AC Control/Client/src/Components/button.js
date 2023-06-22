import React from 'react'

class Button extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            name: this.getName(),
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
                <div>
                    <input className="color" value={this.state.color} onInput={this.setColor} type="color"/>
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

    setHighlight = (state) => {
        this.props.button.shape.highlight = state;
        this.props.update()
    }
}

export default Button