package dev.muazkadan.myapplication.desktopApp

import co.touchlab.kermit.Logger
import java.io.File
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import javax.swing.SwingUtilities
import kotlin.concurrent.thread

/**
 * Keeps the app to one running instance. The first one listens on a loopback port it writes to
 * [dataDir]; a later launch finds the port, hands its message to the running instance and exits
 * once that instance has answered [ACK]. Without the answer, a port file left by a crash could name
 * a port another program has taken since, and the launch would hand over to it and quit without
 * showing anything.
 *
 * Messages are single lines. [RESTORE] brings the window back; add your own for actions a second
 * launch should trigger, such as a deep link or a notification button that starts the app.
 */
class SingleInstanceController(
    private val dataDir: File,
) {
    private val log = Logger.withTag("SingleInstance")
    private val portFile = File(dataDir, "app.port")
    private var serverSocket: ServerSocket? = null

    /**
     * Hands [message] to an instance that is already running.
     *
     * @return true when one answered that it received it, in which case this launch should exit.
     */
    fun notifyExistingInstance(message: String = RESTORE): Boolean {
        require('\n' !in message) { "Messages are single lines" }
        val port = runCatching { portFile.readText().trim().toInt() }.getOrNull() ?: return false

        return try {
            val answer =
                Socket(InetAddress.getLoopbackAddress(), port).use { socket ->
                    socket.soTimeout = TIMEOUT_MILLIS
                    socket.outputStream.write("$message\n".toByteArray(Charsets.UTF_8))
                    socket.outputStream.flush()
                    socket.inputStream.bufferedReader().readLine()
                }
            check(answer == ACK) { "answered ${answer?.take(40)}" }
            log.i { "Handed $message to the instance running on port $port" }
            true
        } catch (e: Exception) {
            // Left behind by an instance that didn't exit cleanly
            log.d { "No instance on port $port, removing its port file: ${e.message}" }
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
            } catch (e: Exception) {
                // The app still runs; later launches just open a second instance
                log.w(e) { "Could not listen for later launches" }
                return
            }
        serverSocket = server
        portFile.writeText(server.localPort.toString())
        portFile.deleteOnExit()

        thread(isDaemon = true, name = "SingleInstanceListener") {
            while (!server.isClosed) {
                try {
                    server.accept().use { client ->
                        client.soTimeout = TIMEOUT_MILLIS
                        val message = client.inputStream.bufferedReader().readLine()
                        if (message != null) {
                            client.outputStream.write("$ACK\n".toByteArray(Charsets.UTF_8))
                            client.outputStream.flush()
                            SwingUtilities.invokeLater { onMessage(message) }
                        }
                    }
                } catch (e: Exception) {
                    // Closing the server in stop() ends accept() with an exception too
                    if (!server.isClosed) log.w(e) { "Could not read a later launch's message" }
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

        /** The running instance's answer to each message. */
        internal const val ACK = "dev.muazkadan.myapplication:OK"

        private const val TIMEOUT_MILLIS = 2_000
    }
}
