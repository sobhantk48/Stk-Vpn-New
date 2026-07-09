package com.v2ray.app.service

import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.Inflater
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrafficCompressor @Inject constructor() {
    
    private val deflater = Deflater(Deflater.BEST_SPEED)
    private val inflater = Inflater()
    
    fun compress(data: ByteArray): ByteArray {
        deflater.reset()
        deflater.setInput(data)
        deflater.finish()
        
        val outputStream = ByteArrayOutputStream(data.size)
        val buffer = ByteArray(1024)
        while (!deflater.finished()) {
            val count = deflater.deflate(buffer)
            outputStream.write(buffer, 0, count)
        }
        return outputStream.toByteArray()
    }
    
    fun decompress(data: ByteArray): ByteArray {
        inflater.reset()
        inflater.setInput(data)
        
        val outputStream = ByteArrayOutputStream(data.size * 2)
        val buffer = ByteArray(1024)
        while (!inflater.finished()) {
            val count = inflater.inflate(buffer)
            outputStream.write(buffer, 0, count)
        }
        return outputStream.toByteArray()
    }
}
