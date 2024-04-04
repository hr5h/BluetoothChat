@file:OptIn(ExperimentalComposeUiApi::class)

package com.plcoding.bluetoothchat.presentation.components

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.plcoding.bluetoothchat.presentation.BluetoothUiState
import com.plcoding.bluetoothchat.presentation.MainActivity
import org.intellij.lang.annotations.Identifier
import java.io.File

@Composable
fun ChatScreen(
    state: BluetoothUiState,
    onDisconnect: () -> Unit,
    onSendMessage: (ByteArray) -> Unit,
    chunkFiles: Array<File>?,
    fileName: String,
    onSending: () -> Unit,
    filesDir: File,
) {
    val countFiles: Array<out File>? = File(
        filesDir, "chunks"
    ).listFiles { file -> file.name.startsWith("chunk") }
    var isEnd: Boolean = false
    val message = rememberSaveable {
        mutableStateOf("")
    }
    val keyboardController = LocalSoftwareKeyboardController.current

    if (state.isSender && !state.isSending) {
        onSending()
        val thread = Thread {
            Thread.sleep(1000)
            onSendMessage("fileName".toByteArray() + fileName.toByteArray())
            Thread.sleep(1000)
            for (i in chunkFiles?.indices!!) {
                //Log.d("MyLog", "ind = $i")
                onSendMessage(chunkFiles[i].readBytes())
            }
            Thread.sleep(1000)
            onSendMessage("success".toByteArray())
            isEnd = true
        }
        thread.start()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (state.isSender) "Отправка файлов" else "Получение файлов",
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDisconnect) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Disconnect"
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Text(text = if (!isEnd) if (state.isSender) "Отправление файлов..." else "Получено файлов: ${countFiles?.size ?: 0}" else "")
            }
        }
    }
}

