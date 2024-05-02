import React from 'react'
import ConfigLoader from './ConfigLoader';
import LoadingSpinner from './Spinners/loading1';
import CanvasEngine from '../canvasEngine';
import EditBackground from './EditBackground';
import EditFrame from './EditFrame';
import Login from './Login';
import Settings from './Settings';
import EditActions from './EditActions';
import Schedules from './EditSchedules';
import Macros from './EditMacros';
import { uuidv4, fetchWithToken, getCookie } from '../Utility';
import '../Styles/styles.scss'
import Control from './Control';



class Main extends React.Component {
    Engine = new CanvasEngine();
    Config = null;
    Settings = null;
    UpdateQueue = [];

    constructor(props) {
        super(props);
        this.state = {
            view: "main",
            showMacros: false,
            uploading: false,
            hideHeaders: false,
            showConfigSelect: false,
            EditConfig: null,
            EditSetting: null,
            error: null,
            loadingFrame: false,
            loadingState: false,
            loadingConfig: false,
            triggeringMacro: false,
            hasFrame: false,
            loggedIn: false,
            checkingLogin: true,
            currentState: null
        }
    }

    async componentDidMount () {
        var token = getCookie("ac_token")
        if(token && await this.checkAuthorized()){
            this.init()
        }
        else {
            this.setState({checkingLogin: false})
        }
    }

    componentDidUpdate () {
        while(this.UpdateQueue.length > 0)
            this.UpdateQueue.pop()();
    }

    render () {
        if(this.state.checkingLogin || this.state.loadingConfig) {
            return (
                <div className="loading-container">
                    <LoadingSpinner id="spinner"/>
                </div>
            );
        }
        else if(this.Settings && this.Settings.UIView == "control") {
            return <Control Config={this.state.Config} Settings={this.Settings} />
        }
        else if(this.state.loggedIn){
            return (
                <div className="container">
                    <div className="content-container">
                        {(this.state.hasFrame || this.state.currentState) ?
                            <div className="content-header" style={{display: this.state.hideHeaders ? "none" : "flex"}}>
                                { this.state.hasFrame ? 
                                    <div className="frame-container">
                                        <img id="frame" onLoad={() => { this.setState({loadingFrame: false}); this.Engine.RefreshDimensions() }}/>
                                        <div className={"refresh" + (this.state.loadingFrame ? " loading" : "")} onClick={this.refreshFrameAndState}><i className="fa-solid fa-arrows-rotate"></i></div>
                                    </div>
                                    : null
                                }
                                { this.state.currentState ?
                                    <div className="state-container">
                                        { this.state.loadingState ?
                                            <LoadingSpinner id="spinner"/>
                                            :
                                            <div>
                                                {this.state.currentState.states.map(x => 
                                                    <div className="state" key={x.name}>
                                                        <div className="active-color" style={{background: x.active ? x.properties.activeColor : "#505050"}}></div>
                                                        <div className="name">{x.name}</div>
                                                    </div>
                                                )}
                                                <div className="state-container-footer">
                                                    <div>{this.getTemperature()}</div>
                                                    <div>{this.getHumidity()}</div>
                                                </div>
                                            </div>
                                        }
                                    </div>
                                    : null
                                }
                            </div>  
                            : null
                        }
                        
                        <div className="canvas-container" id="canvas-container">
                            { this.state.editFrame ? 
                                <div className="editframe-refresh">
                                    <div className={"refresh" + (this.state.loadingFrame ? " loading" : "")} onClick={this.refreshEditFrame}><i className="fa-solid fa-arrows-rotate"></i></div>
                                </div> : null 
                            }
                            <LoadingSpinner id="spinner" style={{display: 'none'}}/>
                            <canvas id="canvas"></canvas>
                        </div>  
                        {this.Config && this.Config.macros.length > 0 && !this.state.editFrame && !this.state.editRemote? 
                            <div className={"btn-container macros-container " + (this.state.showMacros ? "show" : "hidden")}>
                                {this.Config.macros.map(x => 
                                    <button className="btn" onClick={() => this.triggerMacro(x)} key={x.name}>
                                        {this.state.triggeringMacro ? 
                                            <LoadingSpinner id="spinner"/>
                                            : <i className={x.icon}></i>
                                        }                                        
                                    </button>    
                                )}
                            </div>
                            : null
                        }
                    </div>                     
                    <div className='controls'>
                        {this.renderMenus()}       
                        {this.state.showConfigSelect ? <ConfigLoader select={(config) => this.load(config)} close={() => this.setState({showConfigSelect: false})}/> : null}  
                    </div>   
                </div>
            );
        }
        else {
            return (<Login onLoginSuccess={this.init} />);
        }
    }    

    renderMenus = () => {
        if(this.state.view == "remote") {
            return (<EditBackground Engine={this.Engine} Config={this.state.EditConfig} onConfigChange={x => this.setState({EditConfig: x})} complete={this.validateAndComplete} cancel={() => this.cancelEdit(true)}/>);
        }
        else if (this.state.view == "frame") {
            return (<EditFrame Engine={this.Engine} Config={this.state.EditConfig} onConfigChange={x => this.setState({EditConfig: x})} complete={this.validateAndComplete} cancel={() => this.cancelEdit(true)} refresh={this.refreshEditFrame}/>);
        }
        else if (this.state.view == "actions") {
            return (<EditActions Config={this.state.EditConfig} onConfigChange={x => this.setState({EditConfig: x})} complete={this.completeView} cancel={() => this.cancelEdit()}/>)
        }
        else if (this.state.view == "schedules") {
            return (<Schedules Config={this.state.EditConfig} Settings={this.state.EditSetting} onConfigChange={x => this.setState({EditConfig: x})} complete={this.completeView} cancel={() => this.cancelEdit()}/>)
        }
        else if (this.state.view == "macros") {
            return (<Macros Config={this.state.EditConfig} Settings={this.state.EditSetting} onConfigChange={x => this.setState({EditConfig: x})} complete={this.completeView} cancel={() => this.cancelEdit()}/>)
        }
        else if (this.state.view == "settings") {
            return (<Settings Settings={this.state.EditSetting} Config={this.state.EditConfig} refresh={this.refreshFrameAndState} complete={this.completeSettings} cancel={() => this.cancelEdit()} />)
        }
        else {
            return (
                <div className="btn-container vertical" id="normal">
                    <button className="btn" onClick={this.newButtons}><i className="fa-solid fa-plus"></i></button>
                    { this.Config ? <button className="btn" onClick={this.editButtons}><i className="fa-solid fa-gamepad"></i></button> : null }
                    { this.Config ? <button className="btn" onClick={this.editFrame}><i className="fa-regular fa-object-group"></i></button> : null }
                    { this.Config ? <button className="btn" onClick={() => this.switchTo("actions")}><i className="fa-solid fa-shapes"></i></button> : null }
                    { this.Config ? <button className="btn" onClick={() => this.switchTo("schedules")}><i className="fa-regular fa-clock"></i></button> : null }
                    { this.Config ? <button className="btn" onClick={() => this.switchTo("macros")}><i className="fa-solid fa-clapperboard"></i></button> : null }
                    <button className="btn" onClick={() => this.setState({showConfigSelect: true})}><i className="fa-regular fa-folder-open"></i></button>
                    { this.Settings ? <button className="btn" onClick={() => this.switchTo("settings")}><i className="fa-solid fa-gear"></i></button> : null }
                    <div className="spacer"></div>
                    { this.Settings && this.Config && this.Config.macros.length > 0 ? <button className={"btn macros " + (this.state.showMacros ? "retract" : "expand")} onClick={() => this.setState({showMacros: !this.state.showMacros})}><i className="fa-solid fa-angles-right"></i></button> : null }
                </div>
            )
        }
    }

    init = async () => {
        this.setState({loggedIn: true, checkingLogin: false})     
        this.UpdateQueue.push(async () => {   
            this.setState({loadingConfig: true});
            await this.getSettings();
            await this.load("default");            
            this.setState({loadingConfig: false});         
            if(this.Settings.UIView == "main")   
                this.UpdateQueue.push(this.setupCanvasAndFrame)
        })
    }

    setupCanvasAndFrame = async () => {
        this.Engine.Init();
        await this.checkHasWebcam();
        this.switchToMainView();
    }

    switchTo = (view) => {
        this.checkAuthorized(false)
        var settings = JSON.parse(JSON.stringify(this.Settings));
        var config = JSON.parse(JSON.stringify(this.Config));
        this.setState({view: view, EditConfig: config, EditSetting: settings})
    }

    completeView = async () => {
        await this.save();
        this.switchToMainView();
    }

    editFrame = () => {
        this.checkAuthorized(false)
        var config = JSON.parse(JSON.stringify(this.Config));
        this.setState({view: "frame", hideHeaders: true, EditConfig: config})
        this.Engine.SetEdit(true);
        this.UpdateQueue.push(() => this.Engine.RefreshDimensions())
    }

    refreshEditFrame = async () => {
        try{
            this.setState({loadingFrame: true})
            var response = await fetchWithToken(`api/frame?${new Date().getTime()}`);
            if(response.status == 200){
                var blob = await response.blob()
                var background = URL.createObjectURL(blob);
                await this.Engine.LoadBackground(background, this.Config.frame.position)
            }            
        }
        finally {
            this.setState({loadingFrame: false})
        }
    }

    editButtons = () => {
        this.checkAuthorized(false)
        var editConfig = JSON.parse(JSON.stringify(this.Config));
        this.setState({view: "remote", hideHeaders: true, EditConfig: editConfig})
        this.Engine.shapes = editConfig.buttons.map(x => x.shape);
        this.Engine.SetEdit(true);
        this.UpdateQueue.push(() => this.Engine.RefreshDimensions())
    }

    completeSettings = async (settings) => {
        this.Settings = settings
        await this.saveSettings();
        await this.save();
        this.switchToMainView();
    }

    newButtons = () => {
        this.checkAuthorized(false)
        this.setState({editRemote: true, hideHeaders: true, EditConfig: {
            'name': null,
            'buttons': [],
            'frame': {},
            'actions': {},
            'schedules': [],
            'macros': [],
            'background': {},
            'ir_config': null,
            'id': uuidv4()
        }})
        this.Engine.shapes = [];
        this.Engine.LoadBackground(null)
        this.Engine.SetEdit(true);
        this.UpdateQueue.push(() => this.Engine.RefreshDimensions())
    }

    cancelEdit = (refresh = false) => {
        this.switchToMainView(refresh);
    }
    
    validateAndComplete = async () => {
        if(!this.state.EditConfig.name || this.state.EditConfig.name.trim().length == 0) {
            this.setState({error: "Enter a valid config name"})
            return;
        }
        else if(!this.state.EditConfig.ir_config || this.state.EditConfig.ir_config.trim().length == 0) {
            this.setState({error: "Enter a valid config file"})
            return;
        }
        else {
            this.Engine.Reset()
            this.setState({saving: true, error: false}) 
            await this.save()
            this.switchToMainView();            
        }
    }

    save = async (name) => {
        if(name)
            this.state.EditConfig.name = name
        try{
            if(Object.prototype.toString.call(this.state.EditConfig.background.file) === "[object File]"){
                const response = await fetchWithToken("api/upload", "POST", this.state.EditConfig.background.file, { "Content-Type": "image" })
                if(response.status == 200)
                    this.state.EditConfig.background.file = await response.text()
            }
            const response = await fetchWithToken(`api/save`, "POST", JSON.stringify(this.state.EditConfig), { "Content-Type": "application/json" });
            if(response.status == 200){
                this.setState({showNameInput: false})
                this.Config = JSON.parse(JSON.stringify(this.state.EditConfig))
            }
        }
        catch(ex) {
            console.error(ex);
        }       
    }

    load = async (name) => {
        try{
            this.setState({showConfigSelect: false})
            const response = await fetchWithToken(`api/retrieve/${name}`)
            if(response.status == 200){
                this.Config = await response.json();          
            }
        }
        catch(ex) {
            console.error(ex);
        } 
        finally {
        }
    }

    upload = async (files) => {
        if(files && files.length > 0){
            var position = await this.Engine.LoadBackground(files[0])
            this.state.EditConfig.background = { 'file': files[0], 'position': position };
        }
    }

    getSettings = async() => {
        try {
            const response = await fetchWithToken(`api/settings`);
            if(response.status == 200)
                this.Settings = await response.json();
        }
        catch(ex) {
            console.error(ex);
        }
    }

    saveSettings = async() => {
        try {
            const response = await fetchWithToken(`api/settings`, "POST", JSON.stringify(this.Settings), { "Content-Type": "application/json" });
        }
        catch(ex) {
            console.error(ex);
        }
    }

    triggerIr = async (id) => {
        try {
            const response = await fetchWithToken(`api/trigger/${this.Config.id}/${id}`)
            await new Promise(resolve => setTimeout(resolve, this.Settings && this.Settings.frameRefreshDelay ? this.Settings.frameRefreshDelay : 100));
            this.refreshFrameAndState();
        }
        catch(ex) {
            console.error(ex);
        } 
    }

    triggerMacro = async (macro) => {
        if(this.state.triggeringMacro) return;

        try{
            this.setState({triggeringMacro: true})
            var body = JSON.stringify(macro.state)
            var response = await fetchWithToken(`api/setstate/${this.Config.id}`, "POST", body, {"Content-Type": "application/json"})
            this.refreshFrameAndState();
        }
        finally {
            this.setState({triggeringMacro: false})
        }
    }

    refreshFrameAndState = () => {
        this.refreshFrame();
        this.refreshState();
    }

    refreshFrame = async () => {
        this.setState({loadingFrame: true});
        var elem = document.getElementById("frame");
        if(elem){
            try{
                var response = await fetchWithToken(`api/frame/${this.Config ? this.Config.id : ""}?${new Date().getTime()}`)
                if(response.status == 200){
                    var blob = await response.blob()
                    elem.src = URL.createObjectURL(blob)
                }
            }
            catch {}
        }
    }

    refreshState = async () => {
        if(!(this.Config.frame && this.Config.frame.states && this.Config.frame.states.length > 0)) return;
        try {
            this.setState({loadingState: true});
            var response = await fetchWithToken(`api/state/${this.Config.id}/states`)
            if(response.status == 200){
                this.setState({currentState: await response.json()})
            }
        }
        finally {
            this.setState({loadingState: false});
        }
    }

    checkHasWebcam = async () => {
        try {
            const response = await fetchWithToken(`api/frame`)
            this.state.hasFrame = response.status == 200
            this.setState({hasFrame: this.state.hasFrame})
            return response.status == 200;
        }
        catch(ex) {
            console.error(ex);
            this.setState({hasFrame: false})
            return false;
        }
    }

    switchToMainView = (refresh = true) => {
        var menu = document.getElementById("menu");
        if(menu)
            menu.classList.add("closed")

        setTimeout(() => this.setState({view: "main", error: null, EditSetting: null, EditConfig: null}), 450)
        const buttons = JSON.parse(JSON.stringify(this.Config && this.Config.buttons ? this.Config.buttons : []))
        const background = this.Config && this.Config.background ? this.Config.background : null
        this.Engine.SetEdit(false);
        this.Engine.imageEffects = {}
        this.Engine.shapes = buttons.map(x => { 
            x.shape['function'] = () => this.triggerIr(x.id);
            return x.shape
        });
        if(refresh && background) {
            this.Engine.StartBackgroundLoad();
            try{
                fetchWithToken(`api/background/${background.file}`).then(async response => {
                    if(response.status == 200){
                        var blob = await response.blob()
                        await this.Engine.LoadBackground(URL.createObjectURL(blob), background.position)
                        this.UpdateQueue.push(() => this.Engine.RefreshDimensions())
                    }
                })
            }
            catch {}
        }
        
        if(refresh && this.state.hasFrame)
            this.UpdateQueue.push(this.refreshFrameAndState)

        this.setState({hideHeaders: false})            
    }

    checkAuthorized = async(allowUnauthorized = true) => {
        try{
            const response = await fetchWithToken(`api/test/authorize`, "GET", null, {}, allowUnauthorized)
            return response.status == 200
        }
        catch {
            return false;
        }
    }

    getTemperature = () => {
        if(!this.state.currentState.humidity)
            return "-º" + this.Settings.temperatureUnit
        else if(this.Settings.temperatureUnit == "C")
            return this.state.currentState.temperature + "ºC"
        else 
            return (this.state.currentState.temperature * (9 / 5) + 32) + "ºF"
    }

    getHumidity = () => {
        if(this.state.currentState.humidity)
            return this.state.currentState.humidity + "%"
        else
            return "-%"
    }
}

export default Main