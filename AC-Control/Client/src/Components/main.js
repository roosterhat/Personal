import React from 'react'
import ConfigLoader from './ConfigLoader';
import LoadingSpinner from './Spinners/loading1';
import CanvasEngine from '../canvasEngine';
import EditBackground from './EditBackground';
import EditFrame from './EditFrame';
import Login from './Login';
import Settings from './Settings';
import EditActions from './EditActions';
import { uuidv4, fetchWithToken, getCookie } from '../Utility';
import '../Styles/styles.scss'
import Schedules from './EditSchedules';


class Main extends React.Component {
    Engine = new CanvasEngine();
    Config = null;
    Settings = null;
    UpdateQueue = []

    constructor(props) {
        super(props);
        this.state = {
            editRemote: false,
            editFrame: false,
            editSettings: false,
            editActions: false,
            editSchedules: false,
            uploading: false,
            showConfigSelect: false,
            EditConfig: null,
            EditSetting: null,
            error: null,
            loadingFrame: false,
            loadingState: false,
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
        if(this.state.checkingLogin) {
            return (
                <div className="loading-container">
                    <LoadingSpinner id="spinner"/>
                </div>
            );
        }
        else if(this.state.loggedIn){
            return (
                <div className="container">
                    <div className="content-container">
                        { !this.state.editFrame && !this.state.editRemote && (this.state.hasFrame || this.state.currentState) ?
                            <div className="content-header">
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
                                            this.state.currentState.states.map(x => 
                                                <div className="state">
                                                    <div className="active-color" style={{background: x.active ? x.properties.activeColor : "#505050"}}></div>
                                                    <div className="name">{x.name}</div>
                                                </div>
                                            )
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
        if(this.state.editRemote) {
            return (<EditBackground Engine={this.Engine} Config={this.state.EditConfig} onConfigChange={x => this.setState({EditConfig: x})} complete={this.complete} cancel={this.cancelEdit}/>);
        }
        else if (this.state.editFrame) {
            return (<EditFrame Engine={this.Engine} Config={this.state.EditConfig} onConfigChange={x => this.setState({EditConfig: x})} complete={this.complete} cancel={this.cancelEdit} refresh={this.refreshEditFrame}/>);
        }
        else if (this.state.editActions) {
            return (<EditActions Config={this.state.EditConfig} onConfigChange={x => this.setState({EditConfig: x})} complete={this.completeActions} cancel={this.cancelEdit}/>)
        }
        else if (this.state.editSchedules) {
            return (<Schedules Config={this.state.EditConfig} onConfigChange={x => this.setState({EditConfig: x})} complete={this.completeSchedules} cancel={this.cancelEdit}/>)
        }
        else if (this.state.editSettings) {
            return (<Settings Settings={this.state.EditSetting} Config={this.state.EditConfig} refresh={this.refreshFrameAndState} complete={this.completeSettings} cancel={this.cancelEdit} />)
        }
        else {
            return (
                <div className="btn-container vertical" id="normal">
                    <button className="btn" onClick={this.newButtons}><i className="fa-solid fa-plus"></i></button>
                    { this.Config ? <button className="btn" onClick={this.editButtons}><i className="fa-solid fa-gamepad"></i></button> : null }
                    { this.Config ? <button className="btn" onClick={this.editFrame}><i className="fa-regular fa-object-group"></i></button> : null }
                    { this.Config ? <button className="btn" onClick={this.editActions}><i className="fa-solid fa-shapes"></i></button> : null }
                    { this.Config ? <button className="btn" onClick={this.editSchedules}><i className="fa-regular fa-clock"></i></button> : null }
                    <button className="btn" onClick={() => this.setState({showConfigSelect: true})}><i className="fa-regular fa-folder-open"></i></button>
                    { this.Settings ? <button className="btn" onClick={this.editSettings}><i className="fa-solid fa-gear"></i></button> : null }
                </div>
            )
        }
    }

    init = async () => {
        this.setState({loggedIn: true, checkingLogin: false})     
        this.UpdateQueue.push(async () => {   
            this.Engine.Init();
            await this.getSettings();
            await this.load("default")
            if(await this.checkHasWebcam()) {
                this.UpdateQueue.push(this.refreshFrame)       
                this.refreshState();     
            }
        })
    }

    editSchedules = () => {
        this.checkAuthorized(false)
        var config = JSON.parse(JSON.stringify(this.Config));
        this.setState({editSchedules: true, EditConfig: config})
    }

    completeSchedules = async () => {
        await this.save();
        this.setState({editSchedules: false, EditConfig: null})
        this.switchToMainView();
    }

    editActions = () => {
        this.checkAuthorized(false)
        var config = JSON.parse(JSON.stringify(this.Config));
        this.setState({editActions: true, EditConfig: config})
    }

    completeActions = async () => {
        await this.save();
        this.setState({editActions: false, EditConfig: null})
        this.switchToMainView();
    }

    editFrame = () => {
        this.checkAuthorized(false)
        var config = JSON.parse(JSON.stringify(this.Config));
        this.setState({editFrame: true, EditConfig: config})
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
        this.setState({editRemote: true, EditConfig: editConfig})
        this.Engine.shapes = editConfig.buttons.map(x => x.shape);
        this.Engine.SetEdit(true);
        this.UpdateQueue.push(() => this.Engine.RefreshDimensions())
    }

    editSettings = () => {
        var settings = JSON.parse(JSON.stringify(this.Settings));
        var editConfig = JSON.parse(JSON.stringify(this.Config));
        this.setState({editSettings: true, EditSetting: settings, EditConfig: editConfig});
    }

    completeSettings = async (settings) => {
        this.Settings = settings
        await this.saveSettings();
        await this.save();
        this.setState({editSettings: false, EditSetting: null})
        this.switchToMainView();
    }

    newButtons = () => {
        this.checkAuthorized(false)
        this.setState({editRemote: true, EditConfig: {
            'name': null,
            'buttons': [],
            'frame': {},
            'actions': {},
            'schedules': [],
            'background': {},
            'ir_config': null,
            'id': uuidv4()
        }})
        this.Engine.shapes = [];
        this.Engine.LoadBackground(null)
        this.Engine.SetEdit(true);
        this.UpdateQueue.push(() => this.Engine.RefreshDimensions())
    }

    cancelEdit = () => {
        this.setState({editRemote: false, editFrame: false, editActions: false, editSchedules: false, editSettings: false, EditConfig: null, error: null, EditSetting: null})
        this.switchToMainView();
    }
    
    complete = async () => {
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
            this.setState({saving: false, editRemote: false, editFrame: false}) 
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
                if(!this.state.editRemote)
                    this.switchToMainView();               
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
            var response = await fetchWithToken(`api/state/${this.Config.id}`)
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
            this.setState({hasFrame: response.status == 200})
            return response.status == 200;
        }
        catch(ex) {
            console.error(ex);
            this.setState({hasFrame: false})
            return false;
        }
    }

    switchToMainView = () => {
        const buttons = JSON.parse(JSON.stringify(this.Config && this.Config.buttons ? this.Config.buttons : []))
        const background = this.Config && this.Config.background ? this.Config.background : null
        this.Engine.SetEdit(false);
        this.Engine.imageEffects = {}
        this.Engine.shapes = buttons.map(x => { 
            x.shape['function'] = () => this.triggerIr(x.id);
            return x.shape
        });
        if(background) {
            this.Engine.StartBackgroundLoad();
            try{
                fetchWithToken(`api/background/${background.file}`).then(async response => {
                    if(response.status == 200 && !this.state.editRemote){
                        var blob = await response.blob()
                        await this.Engine.LoadBackground(URL.createObjectURL(blob), background.position)
                        this.UpdateQueue.push(() => this.Engine.RefreshDimensions())
                    }
                })
            }
            catch {}
        }
        if(this.state.hasFrame)
            this.UpdateQueue.push(this.refreshFrameAndState)
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
}

export default Main