import utils.ImageUtils
import utils.ScreenUtils
import utils.parentRemove
import java.awt.Color
import java.awt.FlowLayout
import java.awt.Point
import java.awt.image.BufferedImage
import javax.print.attribute.IntegerSyntax
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * 作者: Li_ke
 * 日期: 2019/12/23 14:14
 * 作用:
 */
class LogicItem {
	lateinit var jDelayTextField: JTextField
	lateinit var jPanel: JPanel
	val termsList = mutableListOf<LogicTerms>()
	val action = LogicAction()
}

/** 判断条件 */
interface LogicTerms {
	val guide: String
	fun checkTerms(): Boolean
}

class FindImageLogicTerms : LogicTerms {
	override val guide: String get() = "在屏幕中寻找图片"
	var reverse = false //反转判断结果

	var image: BufferedImage? = null
	var useLocation = false

	val imageJLabel = JLabel()
	val jPointXTextField = JTextField("0000")
	val jPointYTextField = JTextField("0000")

	fun getPoint() = Point(
		jPointXTextField.text.toInt(),
		jPointYTextField.text.toInt()
	)

	override fun checkTerms(): Boolean {
		val location = getPoint()
		val result =
			if (image != null && !useLocation) {
				ImageUtils.findImage(ScreenUtils.getNowScreenImage(), image!!) != null
			} else if (image != null && useLocation) {
				println("匹配图$location ${image!!.width},${image!!.height}")
				val subimage = ScreenUtils.getNowScreenImage()
					.getSubimage(location.x, location.y, image!!.width, image!!.height)
				ImageUtils.matchImage(subimage, image!!)
			} else false

		println("图片匹配:" + result)
		return if (reverse) !result else result
	}

	fun initJPanel(jPanel: JPanel, focusImageCallback: (FindImageLogicTerms) -> Unit) {
		imageJLabel.parentRemove()
		jPointXTextField.parentRemove()
		jPointYTextField.parentRemove()

		jPanel.layout = FlowLayout()
		jPanel.apply {
			add(JLabel("在"))
			add(JButton("全部").also { jButton ->
				if (useLocation) {
					jButton.text = "位置:"
					jPointXTextField.isVisible = true
					jPointYTextField.isVisible = true
				} else {
					jButton.text = "全部"
					jPointXTextField.isVisible = false
					jPointYTextField.isVisible = false
				}
				jButton.addActionListener {
					if (jButton.text == "全部") {
						jButton.text = "位置:"
						useLocation = true
						jPointXTextField.isVisible = true
						jPointYTextField.isVisible = true
					} else {
						jButton.text = "全部"
						useLocation = false
						jPointXTextField.isVisible = false
						jPointYTextField.isVisible = false
					}
				}
			})
			add(jPointXTextField)
			add(jPointYTextField)
			add(JLabel("匹配图片"))
			add(imageJLabel)
			add(JButton("编辑图片").apply {
				focusImageCallback(this@FindImageLogicTerms)
			})
			add(JButton("存在").also { jButton ->
				if (reverse) {
					jButton.text = "不存在"
				} else {
					jButton.text = "存在"
				}
				jButton.addActionListener {
					if (jButton.text == "存在") {
						jButton.text = "不存在"
						reverse = true
					} else {
						jButton.text = "存在"
						reverse = false
					}
				}
			})
		}

	}
}

class CheckRGBLogicTerms : LogicTerms {
	override val guide: String get() = "检测屏幕像素点"
	var reverse = false

	val jPointXTextField = JTextField("0000")
	val jPointYTextField = JTextField("0000")

	val jPointRTextField = JTextField("000")
	val jPointGTextField = JTextField("000")
	val jPointBTextField = JTextField("000")

	fun getRGB() = Color(
		jPointRTextField.text.toInt(),
		jPointGTextField.text.toInt(),
		jPointBTextField.text.toInt()
	)

	fun getPoint() = Point(
		jPointXTextField.text.toInt(),
		jPointYTextField.text.toInt()
	)

	override fun checkTerms(): Boolean {
		val point = getPoint()
		val result = ScreenUtils.getNowScreenImage().getRGB(point.x, point.y) == getRGB().rgb

		return if (reverse) !result else result
	}

	fun initJPanel(jPanel: JPanel) {
		jPointXTextField.parentRemove()
		jPointYTextField.parentRemove()
		jPointRTextField.parentRemove()
		jPointGTextField.parentRemove()
		jPointBTextField.parentRemove()

		jPanel.layout = FlowLayout()
		jPanel.apply {
			add(JLabel("检查位置"))
			add(jPointXTextField)
			add(jPointYTextField)
			add(JLabel("是否是颜色"))
			add(jPointRTextField)
			add(jPointGTextField)
			add(jPointBTextField)
			add(JButton("是").also { jButton ->
				if (reverse) {
					jButton.text = "不是"
				} else {
					jButton.text = "是"
				}
				jButton.addActionListener {
					if (jButton.text == "是") {
						jButton.text = "不是"
						reverse = true
					} else {
						jButton.text = "是"
						reverse = false
					}
				}
			})
		}
	}
}

/** 执行动作 */
class LogicAction {

	lateinit var jMouseYTextField: JTextField
	lateinit var jMouseXTextField: JTextField

	fun getPoint() = Point(
		Integer.parseInt(jMouseXTextField.text),
		Integer.parseInt(jMouseYTextField.text)
	)
}