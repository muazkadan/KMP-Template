package dev.muazkadan.myapplication.desktopApp

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.TestLogWriter
import co.touchlab.kermit.platformLogWriter
import java.io.File
import java.net.InetAddress
import java.net.ServerSocket
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SingleInstanceControllerTest {
    private val dataDir: File = createTempDirectory("single-instance").toFile()
    private val running = SingleInstanceController(dataDir)

    @AfterTest
    fun tearDown() {
        running.stop()
        dataDir.deleteRecursively()
    }

    @Test
    fun firstLaunchFindsNoInstance() {
        assertFalse(SingleInstanceController(dataDir).notifyExistingInstance())
    }

    @Test
    fun laterLaunchHandsItsMessageToTheRunningInstance() {
        val received = LinkedBlockingQueue<String>()
        running.startListener { received.put(it) }

        assertTrue(SingleInstanceController(dataDir).notifyExistingInstance())
        assertEquals(SingleInstanceController.RESTORE, received.poll(5, TimeUnit.SECONDS))

        assertTrue(SingleInstanceController(dataDir).notifyExistingInstance("open:settings"))
        assertEquals("open:settings", received.poll(5, TimeUnit.SECONDS))
    }

    @Test
    fun portLeftByACrashedInstanceIsCleanedUp() {
        val port = java.net.ServerSocket(0).use { it.localPort }
        File(dataDir, "app.port").writeText(port.toString())
        // Kermit's loggers share one configuration, so a test writer sees every tag's logs
        val logs = TestLogWriter(Severity.Verbose)
        Logger.setLogWriters(logs)

        try {
            assertFalse(SingleInstanceController(dataDir).notifyExistingInstance())
            assertFalse(File(dataDir, "app.port").exists())
            logs.assertLast { tag == "SingleInstance" && message.startsWith("No instance on port $port") }
        } finally {
            Logger.setLogWriters(platformLogWriter())
        }
    }

    @Test
    fun portTakenByAnotherProgramSinceACrashIsNotHandedOver() {
        ServerSocket(0, 1, InetAddress.getLoopbackAddress()).use { foreign ->
            // Reads the message like the app would, then answers as something else
            val worker =
                thread {
                    foreign.accept().use { client ->
                        client.inputStream.bufferedReader().readLine()
                        client.outputStream.write("HTTP/1.1 400 Bad Request\n".toByteArray())
                        client.outputStream.flush()
                    }
                }
            File(dataDir, "app.port").writeText(foreign.localPort.toString())

            assertFalse(SingleInstanceController(dataDir).notifyExistingInstance())
            assertFalse(File(dataDir, "app.port").exists())
            worker.join()
        }
    }

    @Test
    fun stoppedInstanceIsNoLongerFound() {
        running.startListener {}
        running.stop()

        assertFalse(SingleInstanceController(dataDir).notifyExistingInstance())
    }
}
