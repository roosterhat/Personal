import React from 'react'
import LoadingSpinner from './Spinners/loading1';

class ConfigLoader extends React.Component {
    constructor(props){
        super(props)
        this.state = {
            loading: false,
            configs: [],
            selected: null
        }        
    }

    componentDidMount() {
        this.list();
    }

    render = () => {
        return (
            <div className='config-loader'>
                <div className='window'>
                    <div className='title'>Load config</div>
                    <div className="list">
                        {this.state.loading ?
                            <div className='loader'><LoadingSpinner /></div>
                            :
                            this.state.configs.map(config => 
                                <div key={config['id']} className={'item' + (this.state.selected == config['id'] ? " selected" : "")} onClick={() => this.select(config['id'])}>
                                    {config['name']}
                                </div>
                            )
                        }
                    </div>
                    <div className='action-container'>
                        <button onClick={this.props.close}>Cancel</button>
                        {this.state.selected ? <button onClick={() => this.props.select(this.state.selected)}>Select</button> : <div/>}      
                    </div>                                  
                </div>
            </div>
        )
    }

    list = async () => {
        try {
            this.setState({loading: true})
            const response = await fetch(`http://${window.location.hostname}:3001/list`)
            if(response.status == 200)
                this.setState({configs: await response.json()})
        }
        catch(ex) {
            console.error(ex);
        } 
        finally { 
            this.setState({loading: false})
        }
    }

    select = (config) => {
        if(this.state.selected === config)
            this.setState({selected: null})
        else
            this.setState({selected: config})
    }
}

export default ConfigLoader