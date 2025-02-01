<script>
    import FloatingMenu from './FloatingMenu.svelte';
    import { SVGDrawer } from '$lib/SVGEngine.js'    
    import { distPoint, EventQueue, convertToOriginalSpace, convertFromOriginalSpace, request } from '$lib/Utility.js'    

    let { 
        onSetSelected = $bindable(),
        frame,
        project,
        settings,
        onFrameUpdate        
    } = $props()

    let dragging = false, dragAction, mousePos = { x: 0, y: 0 }, cursor = $state('default'), selectedElement = $state()
    let canvas, context, startX, startY, _SVGDrawer, offset = 20, elementData = {}, dimensions = {}, rotateRadius = 7
    let shapes = []
    const eventQueue = new EventQueue()
    onSetSelected = e => selectedElement = frame.elements.find(x => x.id == e.id)

    $effect(() => {
        draw()
    })

    $effect(() => {        
        console.log("effect")
        window.addEventListener('resize', resize)
        window.addEventListener('mousemove', mouseMove)
        window.addEventListener('mousedown', mouseDown)
        window.addEventListener('mouseup', mouseUp)
        window.addEventListener('keyup', keyPress)
        updateDimensions()
        const id = setInterval(eventQueue.processEventQueue, (1/30) * 1000, eventQueue)        
        draw()   

        return () => {
            window.removeEventListener('resize', resize)
            window.removeEventListener('mousemove', mouseMove)
            window.removeEventListener('mousedown', mouseDown)
            window.removeEventListener('mouseup', mouseUp)
            window.removeEventListener('keyup', keyPress)
            clearInterval(id)
        }
    })

    async function init(elem) {
        canvas = elem;        
        context = canvas.getContext('2d'); 
        _SVGDrawer = new SVGDrawer(context)
        updateDimensions()    
        // window.addEventListener('resize', resize)
        // window.addEventListener('mousemove', mouseMove)
        // window.addEventListener('mousedown', mouseDown)
        // window.addEventListener('mouseup', mouseUp)
        // window.addEventListener('keyup', keyPress)
        // const id = setInterval(eventQueue.processEventQueue, (1/30) * 1000, eventQueue)
        draw() 
        let response = await request("shapes")
        if(response.status == 200) {
            shapes = await response.json()
        }
    }

    function resize() {
        eventQueue.queueEvent("resize", () => {
            updateDimensions()        
            draw()
        })
    }

    function mouseMove(e) {
        eventQueue.queueEvent("mousemove", () => {
            let clientRect = canvas.getBoundingClientRect()
            let newMousePos = { x: e.pageX - clientRect.left, y: e.pageY - clientRect.top }
            cursor = "default"

            if(selectedElement) {                
                let buffer = 20
                let data = elementData[selectedElement.id]

                if(dragAction) {
                    dragAction(newMousePos.x - mousePos.x, newMousePos.y - mousePos.y)
                    draw()
                }
                else if(!insideFloatingMenu()) {
                    if(inside(data.bounds, newMousePos)) {
                        cursor = dragging ? "grabbing" : "grab"
                        if(dragging) {
                            dragAction = (dx, dy) => {
                                translateElement(dx, dy)
                                onFrameUpdate(frame)
                            }
                        }
                    }

                    if(data.center) {
                        let boundDists = []   
                        for(let i = 0; i < data.bounds.length; i++) {
                            let int = intersection(newMousePos, data.center, data.bounds[i], data.bounds[(i + 1) % data.bounds.length])
                            boundDists.push(distPoint(int, newMousePos))
                        }
                        let p = {x: data.center.x + Math.sin(selectedElement.rotation) * data.radius, y: data.center.y - Math.cos(selectedElement.rotation) * data.radius}

                        if(distPoint(newMousePos, p) <= rotateRadius + 2) {
                            cursor = dragging ? "grabbing" : "grab"
                            if(dragging) {
                                dragAction = (dx, dy) => {
                                    const a = distPoint(data.center, mousePos)
                                    const b = distPoint(p, mousePos)
                                    const angle = Math.atan(b / a)
                                    rotateElement(angle)
                                    // let d = distPoint({x: 0, y:0 }, data.bounds[0])
                                    // let x = -Math.cos(angle) * d
                                    // let y = -Math.sin(angle) * d
                                    // console.log(angle,d)
                                    // translateElement(x, y)                                
                                    onFrameUpdate(frame)
                                }
                            }
                        }                   
                        else if(data.arrows && (inside(data.arrows[0].bounds, newMousePos) || inside(data.arrows[1].bounds, newMousePos))) {
                            console.log("invert")
                            cursor = "pointer"                        
                        }
                        else if(distPoint(newMousePos, data.bounds[0]) <= buffer) {
                            cursor = "nwse-resize"
                            if(dragging) {
                                dragAction = (dx, dy) => {
                                    let [d,s,ts] = calculateDistanceScale(dx, dy, data.bounds[0], data.bounds[1], data.bounds[0])
                                    scaleElementUniform(ts)
                                    translateElement(d * -s * Math.sign(selectedElement.scale.x), d * -s * Math.sign(selectedElement.scale.y))
                                    onFrameUpdate(frame)
                                }
                            }
                        }
                        else if(distPoint(newMousePos, data.bounds[1]) <= buffer) {
                            cursor = "nesw-resize"
                            if(dragging) {
                                dragAction = (dx, dy) => {
                                    let b1 = {x: data.bounds[1].x, y: data.bounds[0].y}
                                    let b2 = {x: data.bounds[0].x, y: data.bounds[1].y}
                                    let [d,s,ts] = calculateDistanceScale(dx, dy, b1, b2, b1)
                                    scaleElementUniform(ts)
                                    translateElement(0, d * -s * Math.sign(selectedElement.scale.y))
                                    onFrameUpdate(frame)
                                }
                            }
                        }
                        else if(distPoint(newMousePos, data.bounds[2]) <= buffer) {
                            cursor = "nwse-resize"
                            if(dragging) {
                                dragAction = (dx, dy) => {
                                    let [d,s,ts] = calculateDistanceScale(dx, dy, data.bounds[0], data.bounds[1], data.bounds[1])
                                    scaleElementUniform(ts)
                                    onFrameUpdate(frame)
                                }
                            }
                        }
                        else if(distPoint(newMousePos, data.bounds[3]) <= buffer) {
                            cursor = "nesw-resize"
                            if(dragging) {
                                dragAction = (dx, dy) => {
                                    let b1 = {x: data.bounds[1].x, y: data.bounds[0].y}
                                    let b2 = {x: data.bounds[0].x, y: data.bounds[1].y}
                                    let [d,s,ts] = calculateDistanceScale(dx, dy, b1, b2, b2)
                                    scaleElementUniform(ts)
                                    translateElement(d * -s * Math.sign(selectedElement.scale.x), 0)
                                    onFrameUpdate(frame)
                                }
                            }
                        }
                        else if(boundDists[0] <= buffer) {
                            cursor = "ns-resize"
                            if(dragging) {
                                dragAction = (dx, dy) => {
                                    scaleElementBounds(0, dy, data.bounds)
                                    onFrameUpdate(frame)
                                }
                            }
                        }
                        else if(boundDists[1] <= buffer) {
                            cursor = "ew-resize"
                            if(dragging) {
                                dragAction = (dx, dy) => {
                                    scaleElementBounds(dx, 0, data.bounds)
                                    onFrameUpdate(frame)
                                }
                            }
                        }
                        else if(boundDists[2] <= buffer) {
                            cursor = "ns-resize"
                            if(dragging) {
                                dragAction = (dx, dy) => {
                                    scaleElementBounds(0, -dy, data.bounds)
                                    translateElement(0, dy * Math.sign(selectedElement.scale.y))
                                    onFrameUpdate(frame)
                                }
                            }
                        }
                        else if(boundDists[3] <= buffer) {
                            cursor = "ew-resize"
                            if(dragging) {
                                dragAction = (dx, dy) => {
                                    scaleElementBounds(-dx, 0, data.bounds)
                                    translateElement(dx * Math.sign(selectedElement.scale.x), 0)
                                    onFrameUpdate(frame)
                                }
                            }
                        }                          
                    }                             
                }
            }

            mousePos = newMousePos
        })
    }    

    function mouseDown(e) {
        if(e.button == 0) {
            dragging = true
            if(selectedElement && inside(elementData[selectedElement.id].bounds, mousePos)) {
                cursor = "grabbing"
            }
        }
    }

    function mouseUp(e) {
        if(e.button == 0) {       
            let data = selectedElement ? elementData[selectedElement.id] : null             

            if(dragAction) {
                if(selectedElement && inside(data.bounds, mousePos, 40)) 
                    cursor = "grab"
                else
                    cursor = "default"
            }
            else if(selectedElement && data.arrows) {
                if(inside(data.arrows[0].bounds, mousePos)) {
                    scaleElement(1, -1)
                    translateElement(0, -Math.sign(selectedElement.scale.y) * (bounds[1].y - bounds[0].y))
                    onFrameUpdate(frame)
                }
                else if(inside(data.arrows[1].bounds, mousePos)) {
                    scaleElement(-1, 1)
                    translateElement(-Math.sign(selectedElement.scale.x) * (bounds[1].x - bounds[0].x), 0)
                    onFrameUpdate(frame)
                }
            }
            else if(!insideFloatingMenu()) {
                selectedElement = null
                cursor = "default"
                for(let element of frame.elements.toSorted((a,b) => a.order - b.order)) {
                    if(inside(elementData[element.id].hull, mousePos)) {
                        selectedElement = element
                        cursor = "grab"
                        break
                    }
                }
            }
            dragging = false 
            dragAction = null
        }
    }

    function keyPress(e) {
        console.log(document.focusElement)
        eventQueue.queueEvent("keypress", () => {
            if(selectedElement && document.activeElement.nodeName == "BODY") {
                switch(e.key) {
                    case "Delete":
                    case "Backspace":
                        frame.elements = frame.elements.filter(x => x.id != selectedElement.id)
                        selectedElement = null
                        onFrameUpdate(frame)
                        break;
                    case "ArrowUp":
                        translateElement(0, -10)
                        break;
                    case "ArrowDown":
                        translateElement(0, 10)
                        break;
                    case "ArrowLeft":
                        translateElement(-10, 0)
                        break;
                    case "ArrowRight":
                        translateElement(10, 0)
                        break;
                }
            }   
        })
    }

    function insideFloatingMenu() {
        let elem = document.getElementById("floatingMenu")
        if(elem) {            
            let clientRect = canvas.getBoundingClientRect()
            let dims = elem.getBoundingClientRect()
            return mousePos.x + clientRect.left >= dims.left && 
                mousePos.x + clientRect.left <= dims.right && 
                mousePos.y + clientRect.top >= dims.top && 
                mousePos.y + clientRect.top <= dims.bottom
        }
        else {
            return false
        }
    }

    function calculateDistanceScale(dx, dy, b1, b2, t) {
        let sl = (b2.y - b1.y) / (b2.x - b1.x)
        let b = (t.y + dy) - (t.x + dx) * (-1/sl)
        let p1 = {x: 10000, y: 10000 * (-1/sl) + b}
        let p2 = {x: -10000, y: -10000 * (-1/sl) + b}
        b = b1.y - b1.x * sl
        let p3 = {x: 10000, y: 10000 * sl + b}
        let p4 = {x: -10000, y: -10000 * sl + b}
        let ip = intersection(p1, p2, p3, p4)
        let d = distPoint(ip, t)
        let bd = distPoint(b1, b2)
        let s = distPoint(ip, t == b1 ? b2 : b1) > bd ? 1 : -1
        let ts = (bd + d * s) / bd
        return [d, s, ts]        
    }

    function translateElement(dx, dy) {
        var p = convertFromOriginalSpace({x: dx, y: dy}, dimensions)
        selectedElement.translation.x += p.x
        selectedElement.translation.y += p.y        
    }

    function scaleElement(dx, dy) {
        selectedElement.scale.x *= dx
        selectedElement.scale.y *= dy
    }

    function scaleElementBounds(dx, dy, bounds) {
        selectedElement.scale.x *= (bounds[1].x - bounds[0].x + dx) / (bounds[1].x - bounds[0].x) 
        selectedElement.scale.y *= (bounds[1].y - bounds[2].y + dy) / (bounds[1].y - bounds[2].y) 
    }

    function scaleElementUniform(s) {
        selectedElement.scale.x *= s 
        selectedElement.scale.y *= s
    }

    function rotateElement(dr) {
        selectedElement.rotation += dr
    }

    function updateDimensions() {
        let dims = canvas.parentElement.getBoundingClientRect()
        canvas.width = dims.width
        canvas.height = dims.height
        dimensions.viewWidth = (canvas.width > canvas.height - offset ? (canvas.height - offset) * settings["aspectRatio"] : canvas.width) - 2
        dimensions.viewHeight = (canvas.height - offset > canvas.width ? canvas.width / settings["aspectRatio"] : canvas.height - offset) - 2
    }

    function draw() {
        context.reset()
        context.clearRect(0, 0, canvas.width, canvas.height)
        context.beginPath();
        context.strokeStyle = '#000'
        context.fillStyle = settings["backgroundColor"]
        context.lineWidth = 2
        context.rect(Math.round((canvas.width - dimensions.viewWidth) / 2), Math.round((canvas.height - dimensions.viewHeight - offset) / 2), dimensions.viewWidth, dimensions.viewHeight)
        context.stroke()
        context.fill()

        if(settings["displayGrid"]) {            
            drawGridLines()
        }

        if(project && frame) {            
            context.font = Math.min(14 * (canvas.width / 500), 14) + "px serif"
            context.strokeStyle = "#000"
            let dims = context.measureText(project.name)
            context.strokeText(project.name, Math.round((canvas.width - dims.width) / 2), Math.round((canvas.height + dimensions.viewHeight + offset / 2) / 2))
            for(let element of frame.elements)
                drawSVG(element)
        }
    }

    function drawGridLines() { 
        context.beginPath();
        context.strokeStyle = settings["gridColor"]
        context.lineWidth = 1

        const midx = Math.round(canvas.width / 2)
        startX = Math.round((canvas.width - dimensions.viewWidth) / 2)
        const midy = Math.round((canvas.height - offset) / 2)
        startY = Math.round((canvas.height - dimensions.viewHeight - offset) / 2)

        context.moveTo(midx, startY)
        context.lineTo(midx, startY + dimensions.viewHeight)
        context.moveTo(startX, midy)
        context.lineTo(startX + dimensions.viewWidth, midy)

        let lineCount = (dimensions.viewWidth > dimensions.viewHeight ? settings["aspectRatio"] * 10 : 10) / 2
        let step = dimensions.viewWidth / lineCount / 2
        for(let i = 0; i < lineCount; i++) {
            context.moveTo(Math.round(midx + step * i), startY)
            context.lineTo(Math.round(midx + step * i), startY + dimensions.viewHeight) 
            context.moveTo(Math.round(midx - step * i), startY)
            context.lineTo(Math.round(midx - step * i), startY + dimensions.viewHeight)           
        }
        lineCount = (dimensions.viewWidth < dimensions.viewHeight ? 1 / settings["aspectRatio"] * 10 : 10) / 2
        step = dimensions.viewHeight / lineCount / 2
        for(let i = 0; i < lineCount; i++) {
            context.moveTo(startX, Math.round(midy + step * i))
            context.lineTo(startX + dimensions.viewWidth, Math.round(midy + step * i))
            context.moveTo(startX, Math.round(midy - step * i))
            context.lineTo(startX + dimensions.viewWidth, Math.round(midy - step * i))
        }
        context.stroke()
    }

    function drawSVG(element) {       
        // let pos = convertFromOriginalSpace(element.translation, dimensions) 
        // let translation = { x : pos.x + Math.round((canvas.width - dimensions.viewWidth) / 2), y: pos.y + Math.round((canvas.height - dimensions.viewHeight - offset) / 2) }
        // let scale = { x: element.scale.x * (dimensions.viewWidth / 100), y: element.scale.y * (dimensions.viewHeight / 100 )}
        let globalTransform = {
            translation: { x : Math.round((canvas.width - dimensions.viewWidth) / 2), y: Math.round((canvas.height - dimensions.viewHeight - offset) / 2) },
            scale: { x: (dimensions.viewWidth / 100), y: (dimensions.viewHeight / 100 )},
            rotation: 0
        }

        context.strokeStyle = settings["traceColor"]  
        context.lineJoin = 'round'
        context.lineWidth = 2
        if(settings["glowEffect"]) {
            context.shadowBlur = settings["glowStrength"];
            context.shadowColor = settings["traceColor"];
        }
        
        let data
        if(element.rootNode) {
            data = _SVGDrawer.drawSVG(element.rootNode, element, globalTransform, dimensions) 
            console.log(data)
        }
        else {
            let size = 60
            data = {
                "bounds": [
                    {x: element.translation.x + (dimensions.viewWidth - size) / 2, y: element.translation.y + (dimensions.viewHeight - size) / 2}, 
                    {x: element.translation.x + (dimensions.viewWidth + size) / 2, y: element.translation.y + (dimensions.viewHeight - size) / 2}, 
                    {x: element.translation.x + (dimensions.viewWidth + size) / 2, y: element.translation.y + (dimensions.viewHeight + size) / 2},
                    {x: element.translation.x + (dimensions.viewWidth - size) / 2, y: element.translation.y + (dimensions.viewHeight + size) / 2}
                ]
            }
        }
        elementData[element.id] = data

        if(selectedElement == element) {
            context.beginPath()
            context.strokeStyle = "#000"
            context.shadowBlur = 0
            context.setLineDash([4, 10]);
            context.moveTo(data.bounds[0].x, data.bounds[0].y)
            for(let i = 0; i < data.bounds.length; i++) {
                let p = data.bounds[(i + 1) % data.bounds.length]
                context.lineTo(p.x, p.y)
            }
            context.stroke()
            context.setLineDash([]);

            if(data.center) {
                context.beginPath()
                context.strokeStyle = "#888"
                context.fillStyle = "#888"
                context.ellipse(data.center.x + Math.sin(element.rotation) * data.radius, data.center.y - Math.cos(element.rotation) * data.radius, rotateRadius, rotateRadius, 0, 0, Math.PI * 2)
                context.ellipse(data.center.x, data.center.y, 2, 2, 0, 0, Math.PI * 2)
                context.fill()
            }

            if(data.arrows) {
                for(let arrow of data.arrows) {
                    context.beginPath()
                    context.strokeStyle = "#888"
                    context.fillStyle = "#888"
                    context.moveTo(arrow.points[0].x, arrow.points[0].y)
                    for(let i = 0; i < arrow.points.length; i++) {
                        let p = arrow.points[(i + 1) % arrow.points.length]
                        context.lineTo(p[0], p[1])
                    }
                    context.fill()
                }  
            }   
        }

        if(settings["showHull"] && data.hull) {
            context.beginPath()
            context.strokeStyle = "#00000088"
            context.moveTo(data.hull[0].x, data.hull[0].y)
            for(let i = 0; i < data.hull.length; i++) {
                let p = data.hull[(i+1) % data.hull.length]
                context.lineTo(p.x, p.y)
            }
            context.stroke()
        }
    }    

    function inside(points, mouse) {
        if(!points) return false
        var count = 0;
        var b_p1 = mouse;
        var b_p2 = {x: 0, y: mouse.y+Math.random()};
        for(var a = 0; a < points.length; a++){
            var a_p1 = points[a];
            var a_p2 = points[(a+1) % points.length];
            if(intersection(a_p1, a_p2, b_p1, b_p2)) {
                count++;    
            }
        }
        return count % 2 == 1;
    }

    function intersection(p1, p2, p3, p4) {
        if ((p1.x === p2.x && p1.y === p2.y) || (p3.x === p4.x && p3.y === p4.y)) {
            return false
        }

        var denom = (p4.y - p3.y)*(p2.x - p1.x) - (p4.x - p3.x)*(p2.y - p1.y);
        if (denom == 0) {
            return false;
        }
        var ua = ((p4.x - p3.x)*(p1.y - p3.y) - (p4.y - p3.y)*(p1.x - p3.x))/denom;
        var ub = ((p2.x - p1.x)*(p1.y - p3.y) - (p2.y - p1.y)*(p1.x - p3.x))/denom;

        if (ua < 0 || ua > 1 || ub < 0 || ub > 1) {
            return false;
        }

        return {
            x: p1.x + ua * (p2.x - p1.x),
            y: p1.y + ua * (p2.y - p1.y)
        };
    }

    function intersectionLine(p1, p2, p3, p4)
    {
        var ua, ub, denom = (p4.y - p3.y)*(p2.x - p1.x) - (p4.x - p3.x)*(p2.x - p1.y);
        if (denom == 0) {
            return null;
        }
        ua = ((p4.x - p3.x)*(p1.y - p3.y) - (p4.y - p3.y)*(p1.x - p3.x))/denom;
        ub = ((p2.x - p1.x)*(p1.y - p3.y) - (p2.y - p1.y)*(p1.x - p3.x))/denom;
        return {
            x: p1.x + ua * (p2.x - p1.x),
            y: p1.y + ua * (p2.y - p1.y)
        };
    }    

    function onElementUpdate(element) {
        if(element) {
            frame.elements = frame.elements.map(x => x.id == element.id ? element : x)
        }        
        selectedElement = element
        onFrameUpdate(frame)
    }
</script>

<div class="canvas-container large" style="cursor: {cursor}">
    <canvas id="osViewer" use:init></canvas>
</div>
{#if selectedElement}
    <FloatingMenu frame={frame} elements={[selectedElement]} shapes={shapes} onFrameUpdate={onFrameUpdate} onElementUpdate={onElementUpdate} onClose={() => selectedElement = null} bind:dragAction={dragAction} bind:cursor={cursor} />
{/if}
