package com.plcoding.bluetoothchat.presentation

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.filetofragments.Separator
import com.plcoding.bluetoothchat.presentation.components.ChatScreen
import com.plcoding.bluetoothchat.presentation.components.DeviceScreen
import com.plcoding.bluetoothchat.presentation.components.MainScreen
import com.plcoding.bluetoothchat.presentation.components.SenderScreen
import com.plcoding.bluetoothchat.ui.theme.BluetoothChatTheme
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val SELECT_FILE_CODE = 1
    lateinit var separator: Separator
    private var fileName: String = ""

    private val bluetoothManager by lazy {
        applicationContext.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val enableBluetoothLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { /* Not needed */ }

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { perms ->
            val canEnableBluetooth = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                perms[Manifest.permission.BLUETOOTH_CONNECT] == true
            } else true

            if(canEnableBluetooth && !isBluetoothEnabled) {
                enableBluetoothLauncher.launch(
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                )
            }
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                )
            )
        }

        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        val requestCode = 123

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        }

        appContext = applicationContext

        setContent {
            BluetoothChatTheme {
                val viewModel = hiltViewModel<BluetoothViewModel>()
                val state by viewModel.state.collectAsState()
                appState = state

                LaunchedEffect(key1 = state.errorMessage) {
                    state.errorMessage?.let { message ->
                        Toast.makeText(
                            applicationContext,
                            message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                LaunchedEffect(key1 = state.isConnected) {
                    if(state.isConnected) {
                        Toast.makeText(
                            applicationContext,
                            "Вы подключены!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                Surface(
                    color = MaterialTheme.colors.background
                ) {
                    when {
                        state.isStart -> {
                            MainScreen(
                                onSender = viewModel::onSender,
                                onReceiver = viewModel::onReceiver
                            )
                        }
                        state.isConnecting -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator()
                                Text(text = "Подключение...")
                            }
                        }
                        state.isConnected -> {
                            val chunkFiles = File(filesDir, "chunks"
                                ).listFiles { file -> file.name.startsWith("chunk") }
                            //Log.d("MyLog", "countChunks = ${chunkFiles.size}")
                            /*
                            SenderScreen(
                                isSender = state.isSender,
                                chunkFiles = chunkFiles,
                                onSendMessage = viewModel::sendMessage
                                )*/
                            ChatScreen(
                                state = state,
                                onDisconnect = viewModel::disconnectFromDevice,
                                onSendMessage = viewModel::sendMessage,
                                chunkFiles = chunkFiles,
                                fileName = if(state.isSender) fileName else "",
                                onSending = viewModel::changeState,
                                filesDir = this.applicationContext.filesDir
                                )
                        }
                        else -> {
                            if(state.isSender && fileName.isEmpty())
                            {
                                separator = Separator(this.applicationContext.filesDir)
                                separator.deleteRecursive(separator.getFolder())
                                showChooserFile()
                            }
                            Thread.sleep(1000)
                            DeviceScreen(
                                state = state,
                                onStartScan = viewModel::startScan,
                                onStopScan = viewModel::stopScan,
                                onDeviceClick = viewModel::connectToDevice
                            )
                        }
                    }
                }
            }
        }
    }

    companion object {
        lateinit  var appContext: Context
        lateinit var appState: BluetoothUiState
    }

    private fun showChooserFile() {
        val intent = Intent()
            .setType("*/*")
            .setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(Intent.createChooser(intent, "Select a file"), SELECT_FILE_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SELECT_FILE_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                val uri = data.data // The URI with the location of the file
                if (uri != null) {
                    val `is` = this.contentResolver.openInputStream(uri)
                    fileName = getFileName(this, uri)
                    val fileSize: Long = getFileSize(this, uri).toLong()
                    if (`is` != null) {
                        Log.d("MyLog", "fileName = $fileName, fileSize = $fileSize")
                        separator.splitFile(`is`)
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun getFileName(context: Context, uri: Uri): String {
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (cursor.moveToFirst()) {
                    val nameIndex: Int = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    return cursor.getString(nameIndex)
                }
            }
        }

        return uri.path?.substring(uri.path!!.lastIndexOf('/') + 1) ?: ""
    }

    private fun getFileSize(context: Context, uri: Uri): String {
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (cursor.moveToFirst()) {
                    val nameIndex: Int = cursor.getColumnIndex(OpenableColumns.SIZE)
                    return cursor.getString(nameIndex)
                }
            }
        }

        return uri.path?.substring(uri.path!!.lastIndexOf('/') + 1) ?: ""
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            123 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        applicationContext,
                        "Разрешение получено",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Разрешение не получено",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

}