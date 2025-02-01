import WavEncoder from "wav-encoder"
import makerjs from "makerjs"
import { SVGToAudio } from "$lib/SVGEngine.js"

const dataURLPattern = new RegExp('data:(?<type>[^;]+);(?<encoding>[^,]+),(?<data>.+)=', 'g')
const transformPattern = new RegExp('(?<action>\\w+)\\((?<params>[^\\)]+)\\)', 'g')
const pathPattern = new RegExp('(?<type>[a-z])(?<params>[\\s\\d\\.,-]+)?', 'gi')
const numberPattern = new RegExp('(-?\\d+(?:\\.\\d+)?)|(-?(?:\\.\\d+))', 'g')

function delay(ms) {
  return new Promise((resolve, reject) => setTimeout(resolve, ms))
}

function decodeDataURL(dataURL) {
  try {
    console.log(dataURL)
    const match = dataURLPattern.exec(dataURL)
    console.log(match)
    return atob(match.groups['data'])
  }
  catch (ex) {
    console.log(ex)
    return null
  }
}

function request(endpoint, method = "GET", body = null, headers = {}) {
  return new Promise(async (resolve, reject) => {
    try {
      var response = await fetch(`http://${window.location.hostname}:3000/${endpoint}`, {
        method: method,
        headers: headers,
        body: body
      });
      resolve(response);
    }
    catch (ex) {
      reject(ex);
    }
  });
}

async function generateAudio(frame, settings, config) {
  try {
    const rawAudioData = {
      sampleRate: settings["sampleRate"],
      kpbs: settings["kbps"],
      channelData: await new SVGToAudio().generateAudio(frame, settings["frequency"], settings["duration"], settings["gain"], settings["sampleRate"], config)
    };
    console.log(rawAudioData)
    return rawAudioData
  }
  catch (ex) {
    console.log("generateAudio failed: ", ex)
  }
}

function encodeAudio(audioData) {
  return new Promise((resolve, reject) => {
    WavEncoder.encode(audioData).then((buffer, error) => {
      if (error)
        reject(error)
      let audioBlob = new Blob([buffer], { type: "audio/wav" })
      let encodedAudioData = new Uint32Array(buffer)
      resolve([encodedAudioData, audioBlob])
    })
  })
}

async function parseSVG(fileData) {
  var root = new DOMParser().parseFromString(fileData, 'image/svg+xml')
  const error = root.querySelector("parsererror");
  if (error) {
    console.log("Failed to parse svg")
    elementData = null
    return null
  }
  await replaceTextData(root.childNodes[0], root)
  return root
}

async function replaceTextData(root, node) {
  try {
    if (node.nodeName == "text") {
      let [textNode, viewBox] = await getTextNode(node)
      if (!root.attributes["viewBox"])
        root.setAttribute("viewBox", viewBox.join(' '))
      for (let att of node.attributes)
        if (att.name != "style")
          textNode.setAttribute(att.name, att.value)
      node.parentNode.replaceChild(textNode, node)
    }
    else {
      for (let child of node.children) {
        await replaceTextData(root, child)
      }
    }
  }
  catch (ex) {
    console.log(ex)
  }
}

async function getTextNode(node) {
  let [font, fontSize] = await getFont(node)
  const textModel = new makerjs.models.Text(font, node.textContent, fontSize);
  let svg = makerjs.exporter.toSVG(textModel);
  let textNode = new DOMParser().parseFromString(svg, 'image/svg+xml').childNodes[0].childNodes[0]
  let bbox = makerjs.measure.modelExtents(textModel);
  let padding = 0
  let viewBox = [
    bbox.low[0] - padding,
    bbox.low[1] - padding,
    bbox.high[0] + padding,
    bbox.high[1] + padding
  ];

  return [textNode, viewBox]
}

async function getFont(node) {
  let fontFamily = "ABeeZee", fontSize = 12, fontStyle = "regular"
  if (node.attributes["style"]) {
    let rawcss = node.attributes["style"].value.split(";")
    let fontPattern = new RegExp(`((?<size>\\d+px)|(?<style>normal|italic|${Array.from({length: 9}, (x, i) => (i + 1) * 100).join("|")})|(?<family>[\\w-]+)|(?:\"(?<family>[^\"]+)\"))`, "g")
    for (let item of rawcss) {
      let pair = item.split(":")
      let key = pair[0].trim()
      let value = pair[1].trim()
      switch (key) {
        case "font":
          for (let matches of value.matchAll(fontPattern)) {
            for (let type of ["size", "style", "family"]) {
              let match = matches.groups[type]
              if (match) {
                switch (type) {
                  case "size":
                    fontSize = parseInt(match.match("\\d+")[0])
                    break;
                  case "style":
                    fontStyle = match
                    break;
                  case "family":
                    fontFamily = match
                    break;
                }
              }
            }
          }
          break;
        case "font-family":
          fontFamily = value
          break;
        case "font-size":
          fontSize = parseInt(value.match("\\d+")[0])
          break;
        case "font-style":
          fontStyle = value
          break;
      }
    }
  }
  console.log(fontFamily, fontSize, fontStyle)
  fontStyle = fontStyle == "normal" ? "regular" : fontStyle
  let fontGroup = fonts.items.find(x => x.family == fontFamily) || fonts.items[0]
  let url = fontGroup.files[fontStyle] || fontGroup.files["regular"]
  let font = await (new Promise((resolve, reject) =>
    opentype.load(url, (error, result) => {
      if (error)
        reject(error)
      else
        resolve(result)
    }))
  )
  return [font, fontSize]
}

function convertFromOriginalSpace(pos, dimensions) {
  return {
      x: pos.x * (100 / dimensions.viewWidth),
      y: pos.y * (100 / dimensions.viewHeight)
  }
}

function convertToOriginalSpace(pos, dimensions) {        
  return {
      x: pos.x * (dimensions.viewWidth / 100),
      y: pos.y * (dimensions.viewHeight / 100)
  }
}  

async function updateText(element) {
  let parser = new DOMParser()
  let font = fonts.items[element.fontIndex]
  let svg = document.createElementNS("http://www.w3.org/1999/xhtml", "svg")
  let maxViewBox = [Infinity, Infinity, 0, 0]
  let y = 0
  for(let text of element.text.split("\n")) {
    let node = parser.parseFromString(`<text style="font: ${font.variants[element.variantIndex]} ${font.family}">${text}</text>`, 'image/svg+xml').childNodes[0]
    let [textNode, viewBox] = await getTextNode(node)
    textNode.setAttribute("y", y)
    svg.appendChild(textNode)

    let h = viewBox[3] - viewBox[1]
    y += h * 1.1
    maxViewBox[0] = Math.min(maxViewBox[0], viewBox[0])
    maxViewBox[1] = Math.min(maxViewBox[1], viewBox[1])
    maxViewBox[2] = Math.max(maxViewBox[2], viewBox[2])
    maxViewBox[3] = y    
  }
  svg.setAttribute("viewBox", maxViewBox.join(" "))
  element.rootNode = svg
}

class EventQueue {
  constructor() {
    this.eventQueue = {
        queue: {},
        actionQueued: false
    }
  }

  queueEvent(event, action) {
    this.eventQueue.queue[event.type] = {
        time: Date.now(),
        action: action
    }
    this.eventQueue.actionQueued = true
  }

  processEventQueue(self) {
    if(!self.eventQueue.actionQueued) return;
    self.eventQueue.actionQueued = false;
    var actions = Object.values(self.eventQueue.queue);
    for(var k in self.eventQueue.queue)
        delete self.eventQueue.queue[k]
    actions.sort((x,y) => x.time - y.time);
    for(var a of actions)
        a.action()
  }
}

function uuidv4() {
  return ([1e7] + -1e3 + -4e3 + -8e3 + -1e11).replace(/[018]/g, c =>
      (c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16)
  );
}

function dist(x1, y1, x2, y2) {
  return Math.sqrt(Math.pow(y1 - y2, 2) + Math.pow(x1 - x2, 2))
}

function distPoint(p1, p2) {
  return dist(p1.x, p1.y, p2.x, p2.y)
}

function lerp(a, b, p) {
  return a + p * (b - a) 
}

function lerpPoint(p1, p2, p) {
  return { x: lerp(p1.x, p2.x, p), y: lerp(p1.y, p2.y, p)}
}

export { decodeDataURL, request, delay, generateAudio, encodeAudio, parseSVG, convertFromOriginalSpace, convertToOriginalSpace, updateText, EventQueue, uuidv4, dist, distPoint, lerp, lerpPoint, dataURLPattern, transformPattern, pathPattern, numberPattern }