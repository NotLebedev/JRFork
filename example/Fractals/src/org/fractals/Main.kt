package org.fractals

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO


fun main() {
    val fileOut = File("img.png")
    ImageIO.write(Mandelbrot().image, "png", fileOut)
}


class Mandelbrot {
    private val maximumIterations = 570
    private val zoom = 400.0
    private val width = 800
    private val height = 800
    private val img: BufferedImage

    val image: BufferedImage
        get() {
            return img
        }

    init {
        img = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        for (y in 0 until height) {
            for (x in 0 until width) {
                var zy = 0.0
                var zx = zy
                val cX = (x - 400) / zoom
                val cY = (y - 400) / zoom
                var iter = maximumIterations
                while (zx * zx + zy * zy < 4 && iter > 0) {
                    val tmp = zx * zx - zy * zy + cX
                    zy = 2.0 * zx * zy + cY
                    zx = tmp
                    iter--
                }
                img.setRGB(x, y, iter or (iter shl 8))
            }
        }
    }
}