<script>
    import { request } from "$lib/Utility.js"

    let {
        settings = $bindable(),
        onToggle = $bindable(),
        onComplete
    } = $props()
    let originalSettings = {}, currentSettings = $state({}), loading = $state(false), open = $state(false)

    for(let key in settings) {
        originalSettings[key] = settings[key]
        currentSettings[key] = settings[key]
    }

    function init() {
        setTimeout(() => open = true, 1)
        onToggle = toggle
    }

    function setValue(key, value) {
        currentSettings[key] = value
        settings[key] = value
    }

    async function saveSettings() {
        try {
            loading = true
            await request("savesettings", "POST", JSON.stringify(currentSettings), {"Content-Type": "application/json"})
            close(true)
        }
        catch(ex) {
            console.log(ex)
            loading = false
        }        
    }

    function toggle() {
        open = !open
        if(!open)
            close(false)
    }

    function close(saveSettings) {
        open = false
        let newSettings = saveSettings ? currentSettings : originalSettings
        for(let key in newSettings)
            settings[key] = newSettings[key]
        setTimeout(() => { if(!open) onComplete() }, 500)
    }
</script>

<div class="settings {open ? "open" : ""}" use:init>
    <div class="settings-wrapper">
        <div class="title">Settings</div>
        <div class="settings-container">
            <div class="setting">
                <div class="name">Aspect Ratio</div>
                <input type="number" min="0.1" step="0.1" value={currentSettings["aspectRatio"]} onchange={e => setValue("aspectRatio", Math.max(e.target.value, 0.1))}/>
            </div>
            <div class="setting">
                <div class="name">Trace Color</div>
                <input type="color" value={currentSettings["traceColor"]} onchange={e => setValue("traceColor", e.target.value)}/>
            </div>
            <div class="setting">
                <div class="name">Grid Color</div>
                <input type="color" value={currentSettings["gridColor"]} onchange={e => setValue("gridColor", e.target.value)}/>
            </div>
            <div class="setting">
                <div class="name">Background Color</div>
                <input type="color" value={currentSettings["backgroundColor"]} onchange={e => setValue("backgroundColor", e.target.value)}/>
            </div>
            <div class="setting">
                <div class="name">Display Grid</div>
                <div class="switch {currentSettings["displayGrid"] ? "selected" : ""}" onclick={e => setValue("displayGrid", !currentSettings["displayGrid"])}></div>
            </div>
            <div class="setting">
                <div class="name">Glow Effect</div>
                <div class="switch {currentSettings["glowEffect"] ? "selected" : ""}" onclick={e => setValue("glowEffect", !currentSettings["glowEffect"])}></div>
            </div>
            <div class="setting">
                <div class="name">Glow Strength</div>
                <input type="number" min="0" value={currentSettings["glowStrength"]} onchange={e => setValue("glowStrength", Math.round(Math.max(e.target.value, 0)))}/>
            </div>
            <div class="setting">
                <div class="name">Sample Rate</div>
                <input type="number" min="0" value={currentSettings["sampleRate"]} onchange={e => setValue("sampleRate", Math.round(Math.max(e.target.value, 0)))}/>
            </div>
            <div class="setting">
                <div class="name">Kbps</div>
                <input type="number" min="0" value={currentSettings["kbps"]} onchange={e => setValue("kbps", Math.round(Math.max(e.target.value, 0)))}/>
            </div>
            <div class="setting">
                <div class="name">Gain</div>
                <input type="number" min="0" step="0.001" value={currentSettings["gain"]} onchange={e => setValue("gain", Math.max(e.target.value, 0))}/>
            </div>
            <div class="setting">
                <div class="name">Frequency</div>
                <input type="number" min="1" step="1" value={currentSettings["frequency"]} onchange={e => setValue("frequency", Math.round(Math.max(e.target.value, 1)))}/>
            </div>
            <div class="setting">
                <div class="name">Duration (ms)</div>
                <input type="number" min="100" step="1" value={currentSettings["duration"]} onchange={e => setValue("duration", Math.round(Math.max(e.target.value, 100)))}/>
            </div>
            <div class="setting">
                <div class="name">Show Hull</div>
                <div class="switch {currentSettings["showHull"] ? "selected" : ""}" onclick={e => setValue("showHull", !currentSettings["showHull"])}></div>
            </div>
        </div>
        <div class="actions">
            <button class="cancel" onclick={() => close(false)}>Cancel</button>
            <button class="save" onclick={saveSettings}>{#if loading}<img class="spinning" src="loading-spinner-white.svg" />{:else}Save{/if}</button>
        </div>
    </div>
</div>