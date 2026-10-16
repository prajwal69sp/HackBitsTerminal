package com.hackbits.terminal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val Bg = Color(0xFF101410)
private val Panel = Color(0xFF1B241D)
private val Green = Color(0xFF75F08A)
private val TextColor = Color(0xFFE1E9E1)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HackBitsApp() }
    }
}

@Composable
private fun HackBitsApp() {
    var page by remember { mutableStateOf("Terminal") }
    var command by remember { mutableStateOf("") }
    var running by remember { mutableStateOf(false) }
    val lines = remember {
        mutableStateListOf(
            "Hack Bits Terminal v0.1.0",
            "Android app sandbox shell — not Debian yet.",
            "Type 'help' to see starter commands."
        )
    }
    val scope = rememberCoroutineScope()

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Green, background = Bg, surface = Panel,
        onBackground = TextColor, onSurface = TextColor
    )) {
        Column(
            Modifier.fillMaxSize().background(Bg).padding(14.dp)
        ) {
            Text("HACK BITS", color = Green, fontSize = 23.sp, fontWeight = FontWeight.Bold)
            Text("TERMINAL  •  v0.1.0", color = TextColor.copy(alpha = .65f), fontSize = 12.sp)
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Terminal", "Linux", "Packages").forEach { item ->
                    FilterChip(
                        selected = page == item,
                        onClick = { page = item },
                        label = { Text(item) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            when (page) {
                "Terminal" -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth()
                            .background(Panel, RoundedCornerShape(12.dp)).padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        items(lines) { line ->
                            Text(line, color = if (line.startsWith("$")) Green else TextColor,
                                fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BasicTextField(
                            value = command,
                            onValueChange = { command = it },
                            modifier = Modifier.weight(1f).background(Panel, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = TextColor, fontFamily = FontFamily.Monospace, fontSize = 14.sp
                            ),
                            decorationBox = { inner ->
                                if (command.isEmpty()) Text("Enter command…", color = TextColor.copy(alpha = .45f))
                                inner()
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                        Button(enabled = !running, onClick = {
                            val input = command.trim()
                            if (input.isNotEmpty()) {
                                command = ""
                                lines.add("$ $input")
                                running = true
                                scope.launch {
                                    val result = withContext(Dispatchers.IO) { ShellRunner.run(input) }
                                    lines.addAll(result)
                                    running = false
                                }
                            }
                        }) { Text(if (running) "…" else "Run") }
                    }
                }
                "Linux" -> {
                    InfoCard(
                        title = "Linux environment",
                        body = "The first starter build runs Android's app-sandbox shell. A real Debian environment needs an ARM64 root filesystem plus a compatible PRoot runtime and process/terminal integration. That backend is intentionally not faked in this starter."
                    )
                    Spacer(Modifier.height(10.dp))
                    InfoCard(
                        title = "Recommended next step",
                        body = "Prototype Debian in Termux with proot-distro first. Then integrate a legally compatible PRoot/bootstrap implementation into this app, with checksum verification, storage limits, cancellation, and recovery."
                    )
                }
                else -> {
                    InfoCard(
                        title = "Package manager",
                        body = "Package actions become available after a Linux backend is installed. Android's shell does not include apt. Do not run apt commands here and assume they install Linux packages."
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Planned actions", color = Green, fontWeight = FontWeight.Bold)
                    listOf("Update package lists", "Search packages", "Install package", "Remove package").forEach {
                        Text("• $it", color = TextColor, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Use only on systems and networks you are authorized to test.",
                color = TextColor.copy(alpha = .55f), fontSize = 11.sp)
        }
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(12.dp)).padding(14.dp)) {
        Text(title, color = Green, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(6.dp))
        Text(body, color = TextColor, fontSize = 13.sp, lineHeight = 19.sp)
    }
}

/**
 * Safe starter command runner.
 *
 * It runs a command through the Android app's own /system/bin/sh. This is NOT
 * a PTY and does not provide job control, interactive programs, or a Linux rootfs.
 * Do not add root execution or concatenate untrusted input into privileged commands.
 */
private object ShellRunner {
    fun run(command: String): List<String> {
        if (command == "help") {
            return listOf(
                "Starter commands: help, pwd, whoami, id, uname -a, ls, date",
                "Other commands are passed to Android's app-sandbox shell.",
                "This is not a full interactive terminal emulator."
            )
        }
        return try {
            val allowed = setOf("pwd", "whoami", "id", "uname -a", "ls", "date", "getprop ro.build.version.release")
            if (command !in allowed) {
                return listOf("Blocked in starter build. Allowed: ${allowed.joinToString()}", "No arbitrary command executed.")
            }
            val process = ProcessBuilder("/system/bin/sh", "-c", command)
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().use { it.readText() }
            val code = process.waitFor()
            (output.lines().filter { it.isNotBlank() } + "exit code: $code").ifEmpty { listOf("(no output)", "exit code: $code") }
        } catch (e: Exception) {
            listOf("Command error: ${e.javaClass.simpleName}: ${e.message ?: "unknown error"}")
        }
    }
}
