package org.fractals

import org.notlebedev.Remote
import java.awt.image.BufferedImage
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import javax.imageio.ImageIO
import kotlin.math.roundToInt

class Mandelbrot(private val x0: Double,
                 private val y0: Double,
                 private val x1: Double,
                 private val y1: Double,
                 private val width: Int = 800,
                 private val height: Int = 800,
                 private val maximumIterations: Int = 512) : Remote {
    private var img: SerializableBufferedImage? = null

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
                img!!.setRGB(x, y, rgbToInt(iter.toDouble() / maximumIterations,
                        iter.toDouble() / maximumIterations,
                        iter.toDouble() / maximumIterations))
            }
        }
    }

    fun rgbToInt(Red: Double, Green: Double, Blue: Double): Int {
        var R = (255 * Red).roundToInt()
        var G = (255 * Green).roundToInt()
        var B = (255 * Blue).roundToInt()

        R = R shl 16 and 0x00FF0000
        G = G shl 8 and 0x0000FF00
        B = B and 0x000000FF

        return -0x1000000 or R or G or B
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