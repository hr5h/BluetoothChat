package com.example.fragmentstofile

import java.io.File
import java.lang.Exception
import java.security.MessageDigest

class Unifier(_filesDir: File) {
    private val filesDir: File = _filesDir
    private var folder = File(_filesDir, "chunks")
    fun getFolder() = folder
    fun getFilesDir() = filesDir
    fun mergeFiles(destinationFile: String = "mergeFile.jpg", inputFile: File, outputFile: File) {
        val chunkFiles = File(filesDir, "chunks"
        ).listFiles { file -> file.name.startsWith("chunk") }
        File(filesDir, destinationFile).outputStream().use { out ->
            chunkFiles?.sortedBy { it.nameWithoutExtension.substringAfter("chunk").toInt() }
                ?.forEach { chunkFile -> out.write(chunkFile.readBytes()) }
        }
        try {
            inputFile.copyTo(outputFile, overwrite = true)
        }catch (se: SecurityException){
            println("SecurityException")
        }catch (e: Exception){
            println(e.message)
        }
    }

    fun calculateHash(file: File): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(file.readBytes())
    }

    fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory && fileOrDirectory.exists() && (fileOrDirectory.listFiles()?.size ?: 0) > 0)
            for (child in fileOrDirectory.listFiles()!!) deleteRecursive(child)
        fileOrDirectory.delete()
    }
}