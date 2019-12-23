package utils

import java.awt.MouseInfo
import java.awt.Point
import kotlin.concurrent.thread

object MouseUtils {
	var delay = 100L

	private var lastLocation = Point(0, 0)
	val listeners = mutableListOf<(Point) -> Unit>()

	val mouseListenThread = thread(start = false) {
		while (true) {
			Thread.sleep(delay)
			val location = getMouseLocation()
			if (lastLocation != location) {
				lastLocation = location
				listeners.forEach { it(location) }
			}
		}
	}

	fun addMouseMotionListener(listener: (location: Point) -> Unit) {
		listeners.add(listener)
		if (!mouseListenThread.isAlive)
			mouseListenThread.start()
	}
}

fun getMouseLocation(): Point = MouseInfo.getPointerInfo().location