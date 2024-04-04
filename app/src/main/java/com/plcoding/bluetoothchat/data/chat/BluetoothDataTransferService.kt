package com.plcoding.bluetoothchat.data.chat

import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent.getIntent
import android.content.SharedPreferences
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.fragmentstofile.Unifier
import com.plcoding.bluetoothchat.domain.chat.BluetoothMessage
import com.plcoding.bluetoothchat.domain.chat.TransferFailedException
import com.plcoding.bluetoothchat.presentation.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.OutputStream
import kotlin.contracts.contract


class BluetoothDataTransferService(
    private val socket: BluetoothSocket
) {
    private lateinit var unifier: Unifier
    fun listenForIncomingMessages(): Flow<BluetoothMessage> {
        val context = MainActivity.appContext
        val state = MainActivity.appState
        unifier = Unifier(context.applicationContext.filesDir)
        if (!state.isSender)
            unifier.deleteRecursive(unifier.getFolder())
        val folder = unifier.getFolder()
        val filesDir = unifier.getFilesDir()
        var destinationFile = "download.txt"
        return flow {
            if(!socket.isConnected) {
                return@flow
            }
            val buffer = ByteArray(1024)
            while(true) {
                Thread.sleep(10)
                val byteCount = try {
                    socket.inputStream.read(buffer)
                } catch(e: IOException) {
                    throw TransferFailedException()
                }

                Log.d("MyLog", "byteCount = $byteCount")

                /*
                Log.d("MyLog", "buffer = ${buffer.joinToString("")}")
                Log.d("MyLog", "byteCount = $byteCount")

                val ind = buffer.indexOfFirst { it == "f".toByteArray()[0] }
                val numFile = buffer.decodeToString(0, ind)
                val str = buffer.decodeToString(ind + 1, byteCount)
                Log.d("MyLog", "numFile = $numFile")
                Log.d("MyLog", "str = $str")
                */

                val str = buffer.decodeToString(0, byteCount)
                if(str.contains("fileName")) {
                    destinationFile = str.removePrefix("fileName")
                    Log.d("MyLog", "destinationFile = $destinationFile")
                    continue
                }
                //Log.d("MyLog", "str = $str")

                if(str.contains("success")){
                    val inputFile = File(context.filesDir, destinationFile)
                    val outputFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), destinationFile)
                    unifier.mergeFiles(destinationFile, inputFile, outputFile)
                    continue
                }

                val chunkFiles = File(filesDir, "chunks"
                    ).listFiles { file -> file.name.startsWith("chunk") }

                if (!folder.exists()) folder.mkdir()
                File(
                    folder, "chunk${chunkFiles?.size ?: 0}.dat"
                ).outputStream().use { out ->
                    out.write(buffer, 0, byteCount)
                }

                if (chunkFiles != null) {
                    Log.d("MyLog", "countChunks = ${chunkFiles.size + 1}")
                }

                emit(
                    buffer.decodeToString(0, byteCount).toBluetoothMessage(
                        isFromLocalUser = false
                    )
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun sendMessage(bytes: ByteArray): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                socket.outputStream.write(bytes)
            } catch(e: IOException) {
                e.printStackTrace()
                return@withContext false
            }

            true
        }
    }
}