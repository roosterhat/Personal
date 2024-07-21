import React from 'react'

class Switch extends React.Component {
    constructor(props) {
        super(props);
    }

    render = () => {
        return (
            <div class={"switch " + (this.props.State ? "selected" : "")} onClick={() => this.props.OnSelect(!this.props.State)}>
            </div>
        )
    }
}

export default Switch