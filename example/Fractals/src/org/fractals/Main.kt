package org.fractals

import org.notlebedev.RemoteThread
import org.notlebedev.introspection.ObjectIntrospection
import org.notlebedev.networking.SocketMasterConnection
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

    val mandelbrots:Array<Mandelbrot> = Array(threadCount, init = {
        Mandelbrot(centerX - radius + (2 * radius / threadCount) * it,
            centerY - radius,
            centerX - radius + (2 * radius / threadCount) * (it + 1),
            centerY + radius,
            height = imageHeight, width = imageWidth / threadCount)
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
    for(i in 0 until threadCount) {
        val mandelbrot = mandelbrots[i]
        val fileOut1 = File("img${i}.png")
        mandelbrot.image?.let { ImageIO.write(it, "png", fileOut1) }
                ?: throw IllegalStateException("Expected to be not null")
    }
}