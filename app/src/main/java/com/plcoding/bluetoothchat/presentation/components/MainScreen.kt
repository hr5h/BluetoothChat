package com.plcoding.bluetoothchat.presentation.components

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.filetofragments.Separator
import com.plcoding.bluetoothchat.domain.chat.BluetoothDevice
import com.plcoding.bluetoothchat.presentation.BluetoothUiState
import java.io.File

@Composable
fun MainScreen(
    onSender: () -> Unit,
    onReceiver: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(onClick = onSender,
                modifier = Modifier
                    .height(144.dp)) {
                Text(text = "Отправить", fontSize = 20.sp)
            }
            Button(onClick = onReceiver,
                modifier = Modifier
                    .height(144.dp)) {
                Text(text = "Получить", fontSize = 20.sp)
            }
        }
    }
}