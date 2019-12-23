package utils

import java.awt.Component
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame
import kotlin.system.exitProcess

/**
 * 作者: Li_ke
 * 日期: 2019/12/23 12:29
 * 作用:
 */
object ViewUtils {
}

fun Component.parentRemove() {
	if (this.parent != null)
		this.parent.remove(this)
}

fun <T : JFrame> T.adjustLocation(): T {
	val jFrame = this
	jFrame.location = ScreenUtils.screenSize.center().apply {
		x -= jFrame.width / 2
		y -= jFrame.height / 2
	}
	return jFrame
}

fun <T : JFrame> T.init(sizeRatio: Double = 0.2): T {

	val jFrame = this

	//关闭时退出程序
	jFrame.addWindowListener(object : WindowAdapter() {
		override fun windowClosing(e: WindowEvent?) {
			super.windowClosing(e)
			exitProcess(0)
		}
	})

	//位置大小 在屏幕中心
	jFrame.size = ScreenUtils.screenSize.copyByScale(sizeRatio)
	jFrame.adjustLocation()

//	jFrame.isUndecorated = true//隐藏窗口标志，使其无法 拖拽、缩小
	return jFrame
}