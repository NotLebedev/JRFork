package org.fractals

import org.notlebedev.RemoteThread
import org.notlebedev.introspection.ObjectIntrospection
import org.notlebedev.networking.MasterConnection
import org.notlebedev.networking.SocketMasterConnection
import java.io.File
import java.net.InetAddress
import javax.imageio.ImageIO


fun main() {
    val fileOut = File("img.png")
    val mandelbrot = Mandelbrot(-1.88488933694469 - 0.00000000000024,
            0.00000000081387 - 0.00000000000024,
            -1.88488933694469 + 0.00000000000024,
            0.00000000081387 + 0.00000000000024)

    val connection: MasterConnection = SocketMasterConnection(InetAddress.getLocalHost(), 4040, 8081)
    val remoteThread = RemoteThread(connection, mandelbrot)

    remoteThread.start()
    remoteThread.setInaccessibleModulePolicy(ObjectIntrospection.InaccessibleModulePolicy.WARN)

    println("Thread is running remotely")

    remoteThread.join()
    if (!remoteThread.isSuccessful) {
        remoteThread.exception.printStackTrace()
        return
    }

    mandelbrot.image?.let { ImageIO.write(it, "png", fileOut) }
            ?: throw IllegalStateException("Expected to be not null")
}