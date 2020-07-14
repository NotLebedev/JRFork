package org.fractals

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO


fun main() {
    val fileOut = File("img.png")
    ImageIO.write(Mandelbrot().image, "png", fileOut)
}

class Mandelbrot {
    private val maximumIterations = 512
    private val width = 800
    private val height = 800
    private val img: BufferedImage

    private val x0: Double = -1.88488933694469 - 0.00000000000024
    private val y0: Double = 0.00000000081387 - 0.00000000000024
    private val x1: Double = -1.88488933694469 + 0.00000000000024
    private val y1: Double = 0.00000000081387 + 0.00000000000024

    val image: BufferedImage
        get() {
            return img
        }

    init {
        val xStep:Double = (x1 - x0) / width
        val yStep:Double = (y1 - y0) / height

        img = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
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
                img.setRGB(x, y, iter or (iter shl 8))
            }
        }
    }
}