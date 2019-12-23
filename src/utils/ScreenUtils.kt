package utils

import java.awt.*
import java.awt.Point
import java.awt.image.BufferedImage


/**
 * Created by Administrator on 2019/12/19.
 */
val toolkit = Toolkit.getDefaultToolkit()
val robot = Robot()

fun Dimension.center(): Point = Point(width / 2, height / 2)

fun Dimension.copyByScale(d: Double): Dimension =
	Dimension((width * d).toInt(), (height * d).toInt())

object ScreenUtils {
	val screenSize = toolkit.screenSize

	fun getNowScreenImage(): BufferedImage = robot.createScreenCapture(Rectangle(screenSize))
}
