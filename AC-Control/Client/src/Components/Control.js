import React from 'react'
import LoadingSpinner from './Spinners/loading1';
import { fetchWithToken } from '../Utility';

class Control extends React.Component {
    constructor(props){
        super(props)
        this.state = {

        }        
    }

    render = () => {
        if(this.props.loading) {
            return (
                <div className="loading-container">
                    <LoadingSpinner id="spinner"/>
                </div>
            );
        }
        else {
            return (
                <div>
                    {this.renderTemperatureRing()}
                </div>
            );
        }
    }

    renderTemperatureRing = () => {
        return (
            <div>
                <div>
                    <div></div>
                    <div></div>
                </div>
                <div>74</div>
            </div>
        )
    }
}

export default Control