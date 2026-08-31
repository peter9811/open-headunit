package com.andrerinas.openheadunit.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class LogExporterClipboardTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testSmallFileNotTruncated() {
        val file = tempFolder.newFile("small_log.txt")
        val content = "Log content line 1\nLog content line 2"
        file.writeText(content)

        val result = LogExporter.getClipboardLogText(file, maxBytes = 100 * 1024)
        assertEquals(content, result)
    }

    @Test
    fun testLargeFileTruncatedToTail() {
        val file = tempFolder.newFile("large_log.txt")
        val sb = StringBuilder()
        // Generate a log file larger than 10 KB
        for (i in 1..500) {
            sb.append("Log line $i: Some detailed application log statement\n")
        }
        file.writeText(sb.toString())

        val maxBytes = 2000L // 2 KB threshold for test
        val result = LogExporter.getClipboardLogText(file, maxBytes = maxBytes)

        assertTrue(result.startsWith("[Log truncated: showing last"))
        assertTrue(result.contains("Log line 500:"))
        // Ensure earlier lines were truncated
        assertTrue(!result.contains("Log line 1:"))
    }

    @Test
    fun testTruncatedFileCleansPartialFirstLine() {
        val file = tempFolder.newFile("partial_line_log.txt")
        file.writeText("Line 1000\nLine 2000\nLine 3000\nLine 4000\n")

        val maxBytes = 22L
        val result = LogExporter.getClipboardLogText(file, maxBytes = maxBytes)

        assertTrue(result.startsWith("[Log truncated: showing last"))
        // Partial line before first newline after seek should be dropped
        assertTrue(result.contains("Line 4000"))
        assertTrue(!result.contains("Line 1000"))
    }

    @Test
    fun testEmptyFileReturnsEmptyString() {
        val file = tempFolder.newFile("empty_log.txt")
        val result = LogExporter.getClipboardLogText(file)
        assertEquals("", result)
    }

    @Test
    fun testNonExistentFileReturnsEmptyString() {
        val file = File(tempFolder.root, "non_existent.txt")
        val result = LogExporter.getClipboardLogText(file)
        assertEquals("", result)
    }
}
