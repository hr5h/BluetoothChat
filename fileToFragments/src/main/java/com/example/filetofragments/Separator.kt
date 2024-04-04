package com.example.filetofragments

import java.io.File
import java.io.InputStream
import java.security.MessageDigest

class Separator(_filesDir: File, private val chunkSize: Int = 1024) {

    private var folder = File(_filesDir, "chunks")
    private lateinit var listHashes: Array<Boolean>

    fun getSuccessFiles() = listHashes.count { it }
    fun getCountFiles() = listHashes.size
    fun isSuccess() = listHashes.count { it } == listHashes.size
    fun getFolder() = folder
    fun getListFiles() = listHashes
    fun splitFile(inputStream: InputStream) {
        var chunkNumber = 0
        var bytesRead: Int
        val buffer = ByteArray(chunkSize)
        while (inputStream.read(buffer, 0, chunkSize).also { bytesRead = it } > 0) {
            if (!folder.exists()) folder.mkdir()
            File(
                folder, "chunk$chunkNumber.dat"
            ).outputStream().use { out ->
                out.write(buffer, 0, bytesRead)
            }
            chunkNumber++
        }
        listHashes = Array(chunkNumber) { false }
    }

    private fun calculateHash(file: File): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(file.readBytes())
    }

    fun compHash(n: Int, actualHash: ByteArray): Boolean {
        val file = File(folder, "chunk$n.dat")
        val fileHash = calculateHash(file)
        listHashes[n] = fileHash.contentEquals(actualHash)
        return listHashes[n]
    }

    fun missingFiles(): List<Int> {
        val list = mutableListOf<Int>()
        listHashes.indices.forEach { i ->
            if(!listHashes[i]) list.add(i)
        }
        return list
    }

    fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory)
            for (child in fileOrDirectory.listFiles()!!) deleteRecursive(child)
        fileOrDirectory.delete()
    }
}