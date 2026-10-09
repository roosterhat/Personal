package com.example.armcontrol

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Dictionary

class FrameProcessor() {
    var frameSize: MutableStateFlow<Int> = MutableStateFlow(0)
    var buffer: ByteArray = ByteArray(frameSize.value)
    var complete: Boolean = false
    var currentSize: MutableStateFlow<Int> = MutableStateFlow(0)
    var frame: MutableStateFlow<ImageBitmap?> = MutableStateFlow(null)
    var seen: MutableMap<Int, Boolean> = mutableMapOf()
    var MTU: Int = 0

    fun init(size: Int, mtu: Int) {
        frameSize.value = size
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

        val chunkIndex = b and 0x3F
        val subChunkIndex = (b and 0xC0) shr 6
        val offset = (chunkIndex * (128 - 4) * 4) + (subChunkIndex * (MTU - 4))

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