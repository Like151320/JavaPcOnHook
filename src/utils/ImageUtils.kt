package utils

import java.awt.Rectangle
import java.awt.image.BufferedImage

/**
 * 作者: Li_ke
 * 日期: 2019/12/23 12:24
 * 作用:
 */
object ImageUtils {

	/** 在图[sourceImage]中寻找图[templateImage] */
	fun findImage(sourceImage: BufferedImage, templateImage: BufferedImage): Rectangle? {
		check(sourceImage.width > templateImage.width) { "模板宽度过大" }
		check(sourceImage.height > templateImage.height) { "模板高度过大" }

		for (x in 0 until sourceImage.width - templateImage.width + 1) {
			for (y in 0 until sourceImage.height - templateImage.height + 1) {
				//从左上角像素开始匹配图片
				if (sourceImage.getRGB(x, y) == templateImage.getRGB(0, 0)) {
					val matchImage =
						sourceImage.getSubimage(x, y, templateImage.width, templateImage.height)
					if (matchImage(matchImage, templateImage)) {
						return Rectangle(x, y, templateImage.width, templateImage.height)
					}
				}
			}
		}

		return null
	}

	/** 检测两张图片是否形同 */
	fun matchImage(image1: BufferedImage, image2: BufferedImage): Boolean {
		for (x in 0 until image1.width) {
			for (y in 0 until image1.height) {
				println("匹配 $x - $y")
				if (image1.getRGB(x, y) != image2.getRGB(x, y)) return false
			}
		}
		return true
	}
}