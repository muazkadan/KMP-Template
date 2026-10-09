package dev.muazkadan.myapplication.desktopApp

import java.io.File
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
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

        assertFalse(SingleInstanceController(dataDir).notifyExistingInstance())
        assertFalse(File(dataDir, "app.port").exists())
    }

    @Test
    fun stoppedInstanceIsNoLongerFound() {
        running.startListener {}
        running.stop()

        assertFalse(SingleInstanceController(dataDir).notifyExistingInstance())
    }
}
