package org.fractals

import org.notlebedev.Remote
import java.awt.image.BufferedImage
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import javax.imageio.ImageIO


class Mandelbrot : Remote {
    private val maximumIterations = 512
    private val width = 800
    private val height = 800
    private var img: SerializableBufferedImage? = null

    private val x0: Double = -1.88488933694469 - 0.00000000000024
    private val y0: Double = 0.00000000081387 - 0.00000000000024
    private val x1: Double = -1.88488933694469 + 0.00000000000024
    private val y1: Double = 0.00000000081387 + 0.00000000000024

    val image: BufferedImage?
        get() {
            return img?.img
        }

    override fun run() {
        val xStep:Double = (x1 - x0) / width
        val yStep:Double = (y1 - y0) / height

        img = SerializableBufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        for (y: Int in 0 until height) {
            for (x: Int in 0 until width) {
                var zy = 0.0
                var zx = 0.0
                val cX = x * xStep + x0
                val cY = y1 - y * yStep
                var iter = maximumIterations
                while (zx * zx + zy * zy < 4.0 && iter > 0) {
                    zy = (2.0 * zx * zy + cY)
                            .also { zx = zx * zx - zy * zy + cX }
                    iter--
                }
                img!!.setRGB(x, y, iter or (iter shl 8))
            }
        }
    }

    private class SerializableBufferedImage(width: Int, height: Int, imageType: Int) : Serializable {
        @Transient
        var img: BufferedImage = BufferedImage(width, height, imageType)

        fun setRGB(x: Int, y: Int, color: Int) {
            img.setRGB(x, y, color)
        }

        @Throws(IOException::class)
        private fun writeObject(outStream: ObjectOutputStream) {
            outStream.defaultWriteObject()
            ImageIO.write(img, "png", outStream) // png is lossless
        }

        @Throws(IOException::class, ClassNotFoundException::class)
        private fun readObject(inStream: ObjectInputStream) {
            inStream.defaultReadObject()
            img = ImageIO.read(inStream)
        }

    }
}