package com.example.karaokekotlin.util

object Utils {
     fun msToDuration(ms: Long): String {
        val total = ms / 1000
        val m = total / 60
        val s = total - m * 60
        var mStr = "00:"
        if (m > 9) mStr = "$m:"
        else if (m > 0) mStr = "0$m:"
        var sStr = "00"
        if (s > 9) sStr = "$s"
        else if (s > 0) sStr = "0$s"

        return "$mStr${sStr}"
     }
}

//class SongDialog(
//   private val requireActivity: FragmentActivity
//) {
//   fun init() {
//      val view = requireActivity.layoutInflater.inflate(R.layout.dialog_layout, null)
//      val editText: EditText = view.findViewById(R.id.etSongName)
//      view.findViewById<TextView>(R.id.dialog_title).text = requireActivity.getString(R.string.song_rename)
//      editText.setText(newFavoriteSong.item.snippet.title)
//      MaterialAlertDialogBuilder(requireActivity).apply {
//         setView(view)
//         setNegativeButton(requireActivity.getString(R.string.cancel)) { dialogInterface, _ ->
//            dialogInterface.cancel()
//         }
//         setPositiveButton(requireActivity.getString(R.string.save)) { _, _ ->
//            mainViewModel.updateFavoriteSong(newFavoriteSong.apply { item.snippet.title = editText.text.toString() })
//         }
//         show()
//      }
//   }
//}




//fun rawToWave(rawFile: File, waveFile: File) {
//    val rawData = ByteArray(rawFile.length().toInt())
//    var input: DataInputStream? = null
//    try {
//        input = DataInputStream(FileInputStream(rawFile))
//        input.read(rawData)
//    } finally {
//        input?.close()
//    }
//    var output: DataOutputStream? = null
//    try {
//        output = DataOutputStream(FileOutputStream(waveFile))
//        // WAVE header
//        // see http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
//        writeString(output, "RIFF") // chunk id
//        writeInt(output, 36 + rawData.size) // chunk size
//        writeString(output, "WAVE") // format
//        writeString(output, "fmt ") // subchunk 1 id
//        writeInt(output, 16) // subchunk 1 size
//        writeShort(output, 1.toShort()) // audio format (1 = PCM)
//        writeShort(output, 1.toShort()) // number of channels
//        writeInt(output, 44100) // sample rate
//        writeInt(output, 44100 * 2) // byte rate
//        writeShort(output, 2.toShort()) // block align
//        writeShort(output, 16.toShort()) // bits per sample
//        writeString(output, "data") // subchunk 2 id
//        writeInt(output, rawData.size) // subchunk 2 size
//        // Audio data (conversion big endian -> little endian)
//        val shorts = ShortArray(rawData.size / 2)
//        ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()[shorts]
//        val bytes = ByteBuffer.allocate(shorts.size * 2)
//        for (s in shorts) {
//            bytes.putShort(s)
//        }
//        output.write(fullyReadFileToBytes(rawFile))
//    } catch (e: Exception) {
//        Log.d("TAGggg exp", e.toString())
//    } finally {
//        output?.close()
//    }
//}
//
//fun fullyReadFileToBytes(f: File): ByteArray {
//    val size = f.length().toInt()
//    val bytes = ByteArray(size)
//    val tmpBuff = ByteArray(size)
//    val fis = FileInputStream(f)
//    try {
//        var read = fis.read(bytes, 0, size)
//        if (read < size) {
//            var remain = size - read
//            while (remain > 0) {
//                read = fis.read(tmpBuff, 0, remain)
//                System.arraycopy(tmpBuff, 0, bytes, size - remain, read)
//                remain -= read
//            }
//        }
//    } catch (e: IOException) {
//        throw e
//    } finally {
//        fis.close()
//    }
//    return bytes
//}
//
//private fun writeInt(output: DataOutputStream, value: Int) {
//    try {
//        output.write(value shr 0)
//        output.write(value shr 8)
//        output.write(value shr 16)
//        output.write(value shr 24)
//    } catch (e: Exception) {
//        Log.d("TAGggg exp", e.toString())
//    }
//}
//
//private fun writeShort(output: DataOutputStream, value: Short) {
//    try {
//        output.write(value.toInt() shr 0)
//        output.write(value.toInt() shr 8)
//    } catch (e: Exception) {
//        Log.d("TAGggg writeShort ", e.toString())
//    }
//}
//
//private fun writeString(output: DataOutputStream, value: String) {
//    try {
//        for (i in 0 until value.length) {
//            output.write(value[i].code)
//        }
//    } catch (e: Exception) {
//        Log.d("TAGggg writeString", e.toString())
//    }
//}