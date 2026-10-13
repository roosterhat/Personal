package com.example.armcontrol

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.flow.MutableStateFlow

class FrameProcessor() {
    var frameSize: MutableStateFlow<Int> = MutableStateFlow(0)
    var chunkSize: Int = 0
    var buffer: ByteArray = ByteArray(frameSize.value)
    var complete: Boolean = false
    var currentSize: MutableStateFlow<Int> = MutableStateFlow(0)
    var frame: MutableStateFlow<ImageBitmap?> = MutableStateFlow(null)
    var seen: MutableMap<Int, Boolean> = mutableMapOf()
    var MTU: Int = 0

    fun init(_chunkSize: Int, _frameSize: Int, mtu: Int) {
        frameSize.value = _frameSize
        chunkSize = _chunkSize
        buffer = ByteArray(frameSize.value)
        complete = false
        currentSize.value = 0
        frame.value = null
        seen = mutableMapOf()
        MTU = mtu
    }

    fun ingest(data: ByteArray) {
        if(frameSize.value == 0) return

        val b = data[0].toInt() and 0xFF

        val chunkIndex = b and 0x1F
        val subChunkIndex = (b and 0xE0) shr 5
        val offset = (chunkIndex * chunkSize) + (subChunkIndex * (MTU - 4))

        data.copyInto(buffer, offset, 1)

        if(!(seen[b] ?: false)) {
            seen[b] = true
            currentSize.value += data.size - 1
        }
        complete = frameSize.value == currentSize.value
    }

    fun compile() {
        frame.value = BitmapFactory.decodeByteArray(buffer, 0, buffer.size).asImageBitmap()
    }
}