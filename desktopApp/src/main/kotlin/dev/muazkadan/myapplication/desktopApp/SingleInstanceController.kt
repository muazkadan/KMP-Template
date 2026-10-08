package dev.muazkadan.myapplication.desktopApp

import java.io.File
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import javax.swing.SwingUtilities
import kotlin.concurrent.thread

/**
 * Keeps the app to one running instance. The first one listens on a loopback port it writes to
 * [dataDir]; a later launch finds the port, hands its message to the running instance and exits.
 *
 * Messages are single lines. [RESTORE] brings the window back; add your own for actions a second
 * launch should trigger, such as a deep link or a notification button that starts the app.
 */
class SingleInstanceController(
    private val dataDir: File,
) {
    private val portFile = File(dataDir, "app.port")
    private var serverSocket: ServerSocket? = null

    /**
     * Hands [message] to an instance that is already running.
     *
     * @return true when one received it, in which case this launch should exit.
     */
    fun notifyExistingInstance(message: String = RESTORE): Boolean {
        require('\n' !in message) { "Messages are single lines" }
        val port = runCatching { portFile.readText().trim().toInt() }.getOrNull() ?: return false

        return try {
            Socket(InetAddress.getLoopbackAddress(), port).use { socket ->
                socket.soTimeout = 2_000
                socket.outputStream.write("$message\n".toByteArray(Charsets.UTF_8))
                socket.outputStream.flush()
            }
            true
        } catch (_: Exception) {
            // Left behind by an instance that didn't exit cleanly
            portFile.delete()
            false
        }
    }

    /** Listens for later launches' messages, calling [onMessage] with each on the AWT event thread. */
    fun startListener(onMessage: (String) -> Unit) {
        dataDir.mkdirs()
        val server =
            try {
                ServerSocket(0, 1, InetAddress.getLoopbackAddress())
            } catch (_: Exception) {
                // The app still runs; later launches just open a second instance
                return
            }
        serverSocket = server
        portFile.writeText(server.localPort.toString())
        portFile.deleteOnExit()

        thread(isDaemon = true, name = "SingleInstanceListener") {
            while (!server.isClosed) {
                try {
                    server.accept().use { client ->
                        client.soTimeout = 2_000
                        val message = client.inputStream.bufferedReader().readLine()
                        if (message != null) SwingUtilities.invokeLater { onMessage(message) }
                    }
                } catch (_: Exception) {
                    // A client that hung up, or the server closing in stop()
                }
            }
        }
    }

    fun stop() {
        runCatching { serverSocket?.close() }
        serverSocket = null
        portFile.delete()
    }

    companion object {
        const val RESTORE = "RESTORE"
    }
}
