package xyz.malkki.neostumbler.crashlog

import junit.framework.TestCase.assertEquals
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.writeText
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FileCrashLogManagerTest {
    @get:Rule val temporaryFolder = TemporaryFolder()

    @Test
    fun `Test crash log`() = runTest {
        val dir = temporaryFolder.newFolder().toPath()

        dir.resolve("crash_2.txt").writeText("another crash!!!")
        dir.resolve("crash_1.txt").writeText("crash!!!")

        val crashLogManager: CrashLogManager = FileCrashLogManager(dir)

        val entries = crashLogManager.getEntries().firstOrNull()

        assertEquals(2, entries?.size)

        assertEquals("crash!!!", crashLogManager.getLogsForEntry(entries!!.first()))

        crashLogManager.deleteEntry(entries.first())

        assertEquals(1, crashLogManager.getEntries().firstOrNull()?.size)

        crashLogManager.clearEntries()

        assertEquals(0, crashLogManager.getEntries().firstOrNull()?.size)

        assertEquals(0, dir.listDirectoryEntries().size)
    }
}
