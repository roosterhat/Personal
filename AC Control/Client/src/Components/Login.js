import React from 'react'
import LoadingSpinner from './Spinners/loading1';
import { setCookie } from '../Utility';

class Login extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            loggingIn: false,
            errorMessage: null
        }
    }

    render = () => {
        return (
            <div className="login">
                <div className="field-container">
                    <input id="password" type="password" placeholder="Password"/>
                    <button onClick={this.login}>{this.state.loggingIn ? <LoadingSpinner id="spinner" /> : "Login"}</button>
                </div>
                <div className="error">{this.state.errorMessage}</div>
            </div>
        );
    }

    login = async () => {
        try{
            this.setState({loggingIn: true, errorMessage: null})
            var elem = document.getElementById("password")
            if(elem) {
                const response = await fetch(`https://${window.location.hostname}:3001/api/login`,{
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({"password": elem.value})
                })
                if(response.status == 200) {
                    var token = await response.text();
                    setCookie("ac_token", token)
                    this.props.onLoginSuccess()
                }
                else {
                    this.setState({errorMessage: "Failed to login"})
                }
            }
        }
        catch(ex){
            console.log(ex)
            this.setState({errorMessage: "Failed to login"})
        }
        finally {
            this.setState({loggingIn: false})
        }
    }
}

export default Login