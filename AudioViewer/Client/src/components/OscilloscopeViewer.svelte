<script>
    import { SVGDrawer } from '../SVGEngine.js'

    let { 
        data = $bindable(), 
        dimensions = $bindable(),
        aspectRatio = 1, 
        traceColor = '#6aff6a', 
        displayGrid = true, 
        gridColor = '#888', 
        backgroundColor = '#eee'
    } = $props()

    let canvas, context, viewHeight, viewWidth, rootNode, _SVGDrawer, offset = 20;
    const eventQueue = {
        queue: {},
        actionQueued: false
    };
    

    $effect(() => {
        console.log('effect')
        window.addEventListener('resize', resize)
        updateDimensions()
        const id = setInterval(ProcessEventQueue, (1/30) * 1000, eventQueue)
        if(data) {
            var svg = atob(data.imageData)
            rootNode = new DOMParser().parseFromString(svg, 'image/svg+xml')
            const errorNode = rootNode.querySelector("parsererror");
            if(errorNode) {
                console.log("Failed to parse svg")
                data = null
            }
        }
        draw()      

        return () => {
            window.removeEventListener('resize', () => QueueEvent("resize", resize))
            clearInterval(id)
        }
    })

    function init(elem) {
        console.log('init')
        canvas = elem;        
        context = canvas.getContext('2d'); 
        _SVGDrawer = new SVGDrawer(context)
        updateDimensions()       
    }

    function resize() {
        console.log('resize')        
        updateDimensions()        
        draw()
    }

    function updateDimensions() {
        let dims = canvas.parentElement.getBoundingClientRect()
        canvas.width = dims.width
        canvas.height = dims.height
        viewWidth = (canvas.width > canvas.height ? canvas.height * aspectRatio : canvas.width) - 2
        viewHeight = (canvas.height > canvas.width ? canvas.width / aspectRatio : canvas.height) - offset - 2
        dimensions = { width: viewWidth, height: viewHeight }
    }

    function draw() {
        
        console.log("draw: ",canvas.width, canvas.height, aspectRatio, viewWidth, viewHeight)
        console.log($state.snapshot(data))
        context.reset()
        context.clearRect(0, 0, canvas.width, canvas.height)
        context.beginPath();
        context.strokeStyle = '#000'
        context.fillStyle = backgroundColor
        context.lineWidth = 2
        context.rect(Math.round((canvas.width - viewWidth) / 2), Math.round((canvas.height - viewHeight - offset) / 2), viewWidth, viewHeight)
        context.stroke()
        context.fill()

        if(displayGrid) {            
            drawGridLines()
        }

        if(data) {            
            context.font = Math.min(14 * (canvas.width / 500), 14) + "px serif"
            context.strokeStyle = "#000"
            let dims = context.measureText(data.name)
            context.strokeText(data.name, Math.round((canvas.width - dims.width) / 2), Math.round((canvas.height + viewHeight + offset / 2) / 2))
            drawSVG()
        }
    }

    function drawGridLines() { 
        context.beginPath();
        context.strokeStyle = gridColor
        context.lineWidth = 1

        const midx = Math.round(canvas.width / 2)
        const startx = Math.round((canvas.width - viewWidth) / 2)
        const midy = Math.round((canvas.height - offset) / 2)
        const starty = Math.round((canvas.height - viewHeight - offset) / 2)

        context.moveTo(midx, midy)
        context.lineTo(midx, starty + viewHeight)
        context.moveTo(startx, midy)
        context.lineTo(startx + viewWidth, midy)

        for(let i = 0; i < 10; i++) {
            context.moveTo(Math.round(startx + viewWidth / 10 * i), starty)
            context.lineTo(Math.round(startx + viewWidth / 10 * i), starty + viewHeight)
            context.moveTo(startx, Math.round(starty + viewHeight / 10 * i))
            context.lineTo(startx + viewWidth, Math.round(starty + viewHeight / 10 * i))
        }
        context.stroke()
    }

    function drawSVG() {       
        let pos = convertFromOriginalSpace(data, data.originalSize)
        context.translate(pos.x + Math.round((canvas.width - viewWidth) / 2), pos.y + Math.round((canvas.height - viewHeight) / 2) - offset)
        let scale = { x: data.scale.x * (viewWidth / data.originalSize.w), y: data.scale.y * (viewHeight / data.originalSize.h )}
        context.scale(scale.x, scale.y)
        context.rotate(data.rotation)

        context.strokeStyle = traceColor  
        context.lineJoin = 'round'
        context.shadowBlur = 5;
        context.shadowColor = traceColor;

        _SVGDrawer.drawSVG(rootNode, data.rotation, scale, data.originalSize)  
    }    

    function convertFromOriginalSpace(pos, originalSize) {
        return {
            x: pos.x * (originalSize.w / viewWidth),
            y: pos.y * (originalSize.h / viewHeight)
        }
    }

    function convertToOriginalSpace(pos, originalSize) {        
        return {
            x: pos.x * (viewWidth / originalSize.x),
            y: pos.y * (viewHeight / originalSize.y)
        }
    }

    function QueueEvent(event, action) {
        eventQueue.queue[event.type] = {
            time: Date.now(),
            action: action
        }
        eventQueue.actionQueued = true
    }

    function ProcessEventQueue(eventQueue) {
        if(!eventQueue.actionQueued) return;
        eventQueue.actionQueued = false;
        var actions = Object.values(eventQueue.queue);
        for(var k in eventQueue.queue)
            delete eventQueue.queue[k]
        actions.sort((x,y) => x.time - y.time);
        for(var a of actions)
            a.action()
    }

</script>
<div class="canvas-container large">
    <canvas id="osViewer" use:init></canvas>
</div>
