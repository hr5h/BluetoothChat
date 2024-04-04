package com.plcoding.bluetoothchat.presentation.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.io.File
import kotlin.reflect.KFunction1

@Composable
fun SenderScreen(
    isSender: Boolean = false,
    chunkFiles: Array<File>?,
    onSendMessage: (ByteArray) -> Unit,
) {
    if(isSender) {
        for(i in chunkFiles?.indices!!) {
            Log.d("MyLog", "ind = $i")
            onSendMessage(chunkFiles[i].readBytes())
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                text = if(isSender) "Отправка файлов..." else "Получение файлов..."
            )
        }
    }
}