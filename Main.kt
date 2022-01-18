package cryptography

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.Exception
import kotlin.experimental.xor

const val EOM = "003"

fun hideMessage() {
    val (inFile, outFile, msg) = hideData()
    val image: BufferedImage = try {
        val imageFile = File(inFile)
        ImageIO.read(imageFile)
    } catch (e: Exception) {
        println("Can't read input file!")
        return
    }
    val newFile = File(outFile)
    try {
        ImageIO.write(hideInImage(image, msg), "png", newFile)
    } catch (e: Exception) {
        println(e.message)
        return
    }
    println("Message saved in $outFile image.")
}

fun hideData(): Triple<String, String, ByteArray> {
    println("Input image file:")
    val inFile = readLine()!!
    println("Output image file:")
    val outFile = readLine()!!
    println("Message to hide:")
    val msg = readLine()!!.encodeToByteArray()
    println("Password:")
    val password = readLine()!!.encodeToByteArray()
    val encrypted = crypt(msg, password) + EOM.map { it.toString().toInt().toByte() }
    return Triple(inFile, outFile, encrypted)
}

fun hideInImage(image: BufferedImage, msg: ByteArray): BufferedImage {
    if (msg.size * Byte.SIZE_BITS > image.width * image.height) {
        throw Exception("The input image is not large enough to hold this message.")
    }
    val newImage: BufferedImage = image
    var pos = 0
    for (byte in msg) {
        for (bit in bitList(byte)) {
            val col = pos % image.width
            val row = pos / image.width
            val c = image.getRGB(col, row)
            val color = if (bit == 1) c or 1 else (c.inv() or 1).inv()
            newImage.setRGB(col, row, color )
            pos++
        }
    }
    return newImage
}

fun bitList(byte: Byte): List<Int> {
    val bits = emptyList<Int>().toMutableList()
    for (index in 0 until Byte.SIZE_BITS) {
        bits.add(byte.toInt() shr index and 1)
    }
    return bits.reversed()
}

fun showMessage() {
    println("Input image file:")
    val filename = readLine()!!
    println("Password:")
    val password = readLine()!!.encodeToByteArray()
    val image: BufferedImage = try {
        val imageFile = File(filename)
        ImageIO.read(imageFile)
    } catch (e: Exception) {
        println("Can't read input file!")
        return
    }
    println("Message:")
    println(crypt(decodeImage(image), password).map { it.toInt().toChar() }.joinToString(""))
}

fun decodeImage(image: BufferedImage): ByteArray {
    val byteMsg = emptyList<Byte>().toMutableList()
    var pos = 0
    while (!endOfMessage(byteMsg)) {
        val bits = emptyList<Int>().toMutableList()
        for (index in 0 until Byte.SIZE_BITS) {
            val col = pos % image.width
            val row = pos / image.width
            bits.add(image.getRGB(col, row) and 1)
            pos++
        }
        byteMsg.add(bitListToByte(bits))
    }
    return byteMsg.subList(0, byteMsg.size - EOM.length).toByteArray()
}

fun bitListToByte(bits: MutableList<Int>): Byte {
    var newByte: Int = 0
    for (bit in bits) {
        newByte = newByte shl 1
        newByte += bit
    }
    return newByte.toByte()
}

fun endOfMessage(msg: MutableList<Byte>): Boolean {
    if (msg.size >= EOM.length) {
        val end = EOM.map { it.toString().toInt().toByte() }
        return msg.reversed().subList(0, end.size) == end.reversed()
    } else {
        return false
    }
}

fun crypt(msg: ByteArray, password: ByteArray): ByteArray {
    val crypted: MutableList<Byte> = emptyList<Byte>().toMutableList()
    if (password.isNotEmpty()) {
        for (index in 0 until msg.size) {
            crypted.add(msg[index] xor password[index % password.size])
        }
        return crypted.toByteArray()
    } else {
        return msg
    }
}

fun main() {
    while (true) {
        println("\nTask (hide, show, exit):")
        when (val task = readLine()!!) {
            "hide" -> hideMessage()
            "show" -> showMessage()
            "exit" -> break
            else -> println("Wrong task: $task")
        }
    }
    println("Bye!")
}