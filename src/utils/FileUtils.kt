package utils

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * 作者: Li_ke
 * 日期: 2019/12/23 12:30
 * 作用:
 */
object FileUtils {
}

fun File.format(): String {
	val indexPoint = path.lastIndexOf('.')
	return path.substring(indexPoint + 1)
}

fun File.createParent() {
	if (!this.parentFile.exists())
		this.parentFile.mkdirs()
}

/** 图片保存到文件 */
fun BufferedImage.writeToFile(path: String): File {
	val file = File(path)
	file.createParent()
	ImageIO.write(this, file.format(), file)
	return file
}