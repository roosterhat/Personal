<script>
    import { EventQueue, uuidv4, updateText, request, parseSVG } from '$lib/Utility.js'    

    let {
        dragAction = $bindable(),
        cursor = $bindable(),
        frame,
        elements,
        shapes,
        onElementUpdate,
        onFrameUpdate,
        onClose
    } = $props()

    $effect(async () => {
        console.log("effect")
        window.addEventListener("resize", resize)
        window.addEventListener('mousemove', mouseMove)
        window.addEventListener('mousedown', mouseDown)
        window.addEventListener('mouseup', mouseUp)
        const id = setInterval(eventQueue.processEventQueue, (1/60) * 1000, eventQueue)

        return () => {
            window.removeEventListener("resize", resize)
            window.removeEventListener('mousemove', mouseMove)
            window.removeEventListener('mousedown', mouseDown)
            window.removeEventListener('mouseup', mouseUp)
            clearInterval(id)
        }
    })

    let pos = $state({x: window.innerWidth - 10, y: 60})
    let dragging = false, grabPoint
    const eventQueue = new EventQueue()

    function init() {
        grabPoint = document.getElementById("grab")
    }

    function resize() {
        eventQueue.queueEvent("resize", () => {
            pos = {x: Math.min(window.innerWidth, pos.x), y: Math.min(window.innerHeight, pos.y)}
        })
    }

    function mouseMove(e) {
        eventQueue.queueEvent("mousemove", () => {
            if(dragging) {
                cursor = "grabbing"
                if(!dragAction) {
                    dragAction = (dx, dy) => {
                        pos.x += dx
                        pos.y += dy
                    }
                }
            }
            else if (insideGrabPoint(e)) {
                cursor = "grab"                
            } 
        })
    }

    function mouseDown(e) {
        if(e.button == 0 && insideGrabPoint(e)) {
            dragging = true
            cursor = "grabbing"
        }
    }

    function mouseUp(e) {
        if(e.button == 0) {
            dragging = false
            dragAction = null
            if (insideGrabPoint(e)) {
                cursor = "grab"
            }
        }
    }

    function insideGrabPoint(e) {
        var dims = grabPoint.getBoundingClientRect()
        return e.pageX >= dims.left && e.pageX <= dims.right && e.pageY >= dims.top && e.pageY <= dims.bottom
    }

    function onFontFamilySelect(e) {
        let element = elements[0]
        if(element) {
            element.fontIndex = e.target.value
            element.variantIndex = 0
            updateText(element)
            onElementUpdate(element)
        }
    }

    function onFontVariantSelect(e) {
        let element = elements[0]
        if(element) {
            element.variantIndex = e.target.value
            updateText(element)
            onElementUpdate(element)
        }
    }

    async function onTextChange(e) {
        let element = elements[0]
        if(element) {
            element.text = e.target.value 
            updateText(element)       
            onElementUpdate(element)
        }
    }    

    async function onSelectShape(shape) {
        let response = await fetch(`http://${window.location.host}/shapes/${shape}`)
        if(response.status == 200) {
            let svg = await response.text()
            let element = elements[0]
            element.rootNode = await parseSVG(svg)
            element.imageData = btoa(svg)
            element.shape = shape
        }
    }

    async function onCopy() {
        let newElements = JSON.parse(JSON.stringify(elements, (key, value) => key == "rootNode" ? null : value))
        for(let i in newElements) {
            let element = newElements[i]
            element.id = uuidv4()
            element.rootNode = elements[i].rootNode
            frame.elements.push(element)
        }
        onFrameUpdate(frame)
    }

    function onDelete() {
        frame.elements = frame.elements.filter(x => !elements.includes(x))
        onElementUpdate(null)
    }

    function onGroup() {

    }
</script>

<div class="floating-menu" id="floatingMenu" style="left: {pos.x}px; top: {pos.y}px;" use:init>
    <div class="handle" id="grab" style="cursor: {cursor};">
        <div class="close-cover">
            <button onclick={onClose} class="close"><i class="fa-solid fa-x"></i></button>
        </div>        
        <div class="grab-point">
            <i class="fa-solid fa-border-none"></i>
        </div>
    </div>
    <div class="body">
        {#if elements.length == 1}
            {#if elements[0].type == "text"}
                <div class="text-container">
                    <div class="text-parameters">
                        <select onchange={onFontFamilySelect}>
                            {#each fonts.items as font, index}
                                <option value={index} selected={index == elements[0].fontIndex}>{font.family}</option>
                            {/each}
                        </select>
                        <select onchange={onFontVariantSelect}>
                            {#each fonts.items[elements[0].fontIndex].variants as variant, index}
                                <option value={index} selected={index == elements[0].variantIndex}>{variant}</option>
                            {/each}
                        </select>
                    </div>
                    <textarea onchange={onTextChange}>{elements[0].text}</textarea>
                </div>
            {:else if elements[0].type == "shape"}
                <div class="shapes">
                    {#each shapes as shape}
                        <button class="shape" onclick={() => onSelectShape(shape)}><img src="./shapes/{shape}" /></button>
                    {/each}
                </div>
            {/if}
        {/if}
        <div class="actions">
            {#if elements.length > 1}
                <button onclick={onGroup}><i class="fa-solid fa-object-group"></i></button>
            {/if}
            <button onclick={onCopy}><i class="fa-regular fa-copy"></i></button>
            <button onclick={onDelete}><i class="fa-solid fa-trash-can"></i></button>
        </div>
    </div>
</div>