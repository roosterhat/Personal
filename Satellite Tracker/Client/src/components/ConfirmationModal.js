import React from 'react'

class ConfirmationModal extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            enabled: false,
        }

        this.props.setToggleEnable(this.toggleEnabled)
    }

    onAction = (action) => {
        if(action)
            action()
        this.setState({enabled: false})
    }

    toggleEnabled = () => {
        this.setState({enabled: !this.state.enabled})
    }

    render = () => {
        return (
            this.state.enabled ? 
                <div className='background'>
                    <div class="modal">
                        <div className='title'>{this.props.title}</div>
                        <div className='actions'>
                            <button className='confirm' onClick={() => this.onAction(this.props.onConfirm)}>Confirm</button>
                            <button className='cancel' onClick={() => this.onAction(this.props.onCancel)}>Cancel</button>
                        </div>
                    </div>
                </div>
            : null
        )
    }
}

export default ConfirmationModal