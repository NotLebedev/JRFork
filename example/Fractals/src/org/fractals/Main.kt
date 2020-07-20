package org.fractals

import org.notlebedev.RemoteThread
import org.notlebedev.introspection.ObjectIntrospection
import org.notlebedev.networking.SocketMasterConnection
import java.awt.image.BufferedImage
import java.io.File
import java.net.InetAddress
import javax.imageio.ImageIO

fun main() {
    val imageWidth = 1024
    val imageHeight = 1024

    val centerX = -1.88488933694469
    val centerY = 0.00000000081387
    val radius = 0.00000000000024

    val threadCount = 4

    val maxIterations = 1024
    val redGradient: (Int) -> Double = {iter: Int ->
        val x = iter.toDouble() / maxIterations
        val y = -7.1 * x * x + 5 * x
        if(y > 0) y else 0.0}
    val greenGradient: (Int) -> Double = {iter: Int ->
        val x = iter.toDouble() / maxIterations
        val y = 2.1 * x * x - 2.4 * x + 0.5
        if(y > 0) y else 0.0}
    val blueGradient: (Int) -> Double = {iter: Int ->
        iter.toDouble() / maxIterations}

    val mandelbrots:Array<Mandelbrot> = Array(threadCount, init = {
        Mandelbrot(centerX - radius + (2 * radius / threadCount) * it,
            centerY - radius,
            centerX - radius + (2 * radius / threadCount) * (it + 1),
            centerY + radius,
            height = imageHeight, width = imageWidth / threadCount +
                (if(imageWidth % threadCount > it)  1 else 0),
            maximumIterations = maxIterations,
            colorRed = redGradient,
            colorGreen = greenGradient,
            colorBlue = blueGradient)
    })

    val threads:Array<RemoteThread> = Array(threadCount, init = {
        RemoteThread(SocketMasterConnection(InetAddress.getLocalHost(), 4040 + it, 8081 + it),
            mandelbrots[it])
                .apply { this.setInaccessibleModulePolicy(ObjectIntrospection.InaccessibleModulePolicy.INFO) }
                .apply { this.setInspectAnnotations(false) }
    })

    threads.forEach { it.start() }

    println("Thread is running remotely")

    threads.forEach { it.join() }
    for(thread in threads) {
        if (!thread.isSuccessful) {
            thread.exception.printStackTrace()
            return
        }
    }

    val result = BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB)
    val graphics = result.graphics
    var pastedWidth = 0

    for(i in 0 until threadCount) {
        val image:BufferedImage = mandelbrots[i].image!!
        graphics.drawImage(image, pastedWidth, 0, null)
        pastedWidth += image.width
    }
    graphics.dispose()

    val fileOut = File("img.png")
    ImageIO.write(result, "png", fileOut)
}