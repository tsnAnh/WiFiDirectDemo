package dev.tsnanh.android.wifidirectdemo

import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.net.ServerSocket

class MainViewModel : ViewModel() {
    init {
        // viewModelScope.launch(Dispatchers.IO) {
        //     val serverSocket = ServerSocket(8888)
        //     while (true) {
        //         serverSocket.accept().let {
        //             it.getInputStream().use { `is` ->
        //                 val file = File.createTempFile("test", "txt", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))
        //                 if (file.exists().not()) file.createNewFile()
        //                 file.writeBytes(`is`.readBytes())
        //             }
        //         }
        //     }
        // }
    }
}
