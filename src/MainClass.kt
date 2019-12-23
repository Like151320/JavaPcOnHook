import com.melloware.jintellitype.JIntellitype
import utils.*
import java.awt.FlowLayout
import java.awt.GridLayout
import java.awt.Point
import java.awt.event.InputEvent
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.swing.*
import kotlin.system.exitProcess

/**
 * 作者: Li_ke
 * 日期: 2019/12/23 14:08
 * 作用:
 */

object MainClass {
	private lateinit var jFrame: JFrame
	val startLocation = Point() //截图起点
	var focusItem: FindImageLogicTerms? = null //截图目标item
	var pool: ExecutorService? = null
	var isRunning = false

	lateinit var mouseLocationLabel: JLabel //追踪鼠标位置
	lateinit var newLogicButton: JButton
	lateinit var groupPanel: JPanel

	val logicList = mutableListOf<LogicItem>()

	fun mainStart() {

		jFrame = createJFrame()

		//监听键盘，实现 快捷键 功能
		createHotKey()

		//监听鼠标位置
		MouseUtils.addMouseMotionListener {
			mouseLocationLabel.text = String.format("%04d,%04d", it.x, it.y)
		}

	}

	/** 处理快捷键事件 */
	object HotKeyEvent {

		/** 截图起点 */
		fun onStartLocation(start: Point) {
			startLocation.location = start
		}

		/** 截图终点 */
		fun onEndLocation(end: Point) {
			if (focusItem == null) {
				return
			}

			//保存图片
			val image = ScreenUtils.getNowScreenImage().getSubimage(
				startLocation.x,
				startLocation.y,
				end.x - startLocation.x,
				end.y - startLocation.y
			)
			focusItem!!.image = image
			focusItem!!.imageJLabel.icon = ImageIcon(image)
		}

		/** 关闭程序 */
		fun onExit() {
			exitProcess(0)
		}

		/** 逻辑开始 */
		fun onStartLogic() {
			isRunning = true
			jFrame.title = "运行中"
			pool = Executors.newCachedThreadPool()

			for (item in logicList) {
				val delay = Integer.parseInt(item.jDelayTextField.text).toLong()
				val mousePoint = item.action.getPoint()
				val termsList = item.termsList

				pool!!.execute {
					while (true) {
						val checkSuccess: Boolean = termsList.all { it.checkTerms() }
						println("check - $checkSuccess")

						if (checkSuccess) { //TODO 多线程有临界值风险
							//TODO 动作太单调
							val lastLocation = getMouseLocation()
							robot.mouseMove(mousePoint.x, mousePoint.y)
							robot.mousePress(InputEvent.BUTTON1_MASK)
							robot.mouseRelease(InputEvent.BUTTON1_MASK)
							robot.mouseMove(lastLocation.x, lastLocation.y)
						}

						Thread.sleep(delay)
					}
				}
			}
		}

		/** 逻辑停止 */
		fun onStopLogic() {
			isRunning = false
			jFrame.title = "未运行"
			pool?.shutdownNow()
		}
	}

	/** 处理按钮事件 */
	object ButtonEvent {

		/** 添加新逻辑 */
		fun onNewLogic() {
			val logicItem = LogicItem()
			val logicPanel = createLogicPlant(logicItem)

			logicList.add(logicItem)

			groupPanel.remove(newLogicButton)
			groupPanel.add(logicPanel)
			groupPanel.add(newLogicButton)
			jFrame.validate()
		}

		/** 删除某条逻辑 */
		fun onDeleteLogic(item: LogicItem, jPanel: JPanel) {
			logicList.remove(item)

			groupPanel.remove(jPanel)
			jFrame.validate()
		}

		/** 编辑判断条件 */
		fun onEditTerms(item: LogicItem) {
			val termsJFrame = JFrame()
			termsJFrame.layout = GridLayout(0, 1)
			termsJFrame.size = ScreenUtils.screenSize.copyByScale(0.3)
			termsJFrame.adjustLocation()

			for (terms in item.termsList) {
				val jPanel = JPanel()
				when (terms) {
					is FindImageLogicTerms -> {
						terms.initJPanel(jPanel) {
							focusItem = it
						}
					}
					is CheckRGBLogicTerms -> {
						terms.initJPanel(jPanel)
					}
				}
				termsJFrame.add(jPanel)
			}

			val newTermsJPanel = JPanel(GridLayout(1, 0))
			val newFindImageTermsJButton = JButton("添加图片匹配条件")
			val newCheckRGBTermsJButton = JButton("添加像素匹配条件")
			newTermsJPanel.add(newFindImageTermsJButton)
			newTermsJPanel.add(newCheckRGBTermsJButton)
			newFindImageTermsJButton.addActionListener {
				ButtonEvent.onNewFindImageTerms(item, termsJFrame, newTermsJPanel)
			}
			newCheckRGBTermsJButton.addActionListener {
				ButtonEvent.onNewCheckRGBTerms(item, termsJFrame, newTermsJPanel)
			}

			termsJFrame.add(newTermsJPanel)
			termsJFrame.isVisible = true
		}

		private fun onNewCheckRGBTerms(
			item: LogicItem, termsJFrame: JFrame, newTermsJPanel: JPanel
		) {

			val jPanel = JPanel()
			val terms = CheckRGBLogicTerms()
			terms.initJPanel(jPanel)
			item.termsList.add(terms)

			termsJFrame.remove(newTermsJPanel)
			termsJFrame.add(jPanel)
			termsJFrame.add(newTermsJPanel)
			termsJFrame.validate()
		}

		private fun onNewFindImageTerms(
			item: LogicItem, termsJFrame: JFrame, newTermsJPanel: JPanel
		) {
			val jPanel = JPanel()
			val terms = FindImageLogicTerms()
			terms.initJPanel(jPanel) { focusItem = it }
			item.termsList.add(terms)

			termsJFrame.remove(newTermsJPanel)
			termsJFrame.add(jPanel)
			termsJFrame.add(newTermsJPanel)
			termsJFrame.validate()
		}
	}

	private fun createHotKey() {
		//第一步：注册热键，第一个参数表示该热键的标识，第二个参数表示组合键，如果没有则为0，第三个参数为定义的主要热键
		//com/melloware/jintellitype/JIntellitype.dll
		JIntellitype.getInstance().registerHotKey('起'.toInt(), "ALT+A")
		JIntellitype.getInstance().registerHotKey('终'.toInt(), "ALT+S")
		JIntellitype.getInstance().registerHotKey('开'.toInt(), "ALT+Z")
		JIntellitype.getInstance().registerHotKey('停'.toInt(), "ALT+X")
		JIntellitype.getInstance().registerHotKey('退'.toInt(), "ALT+E")

		//第二步：添加热键监听器
		JIntellitype.getInstance().addHotKeyListener {
			when (it) {
				'起'.toInt() -> {
					HotKeyEvent.onStartLocation(getMouseLocation())
				}
				'终'.toInt() -> {
					//截图并保存
					HotKeyEvent.onEndLocation(getMouseLocation())
				}
				'开'.toInt() -> {
					HotKeyEvent.onStartLogic()
				}
				'停'.toInt() -> {
					HotKeyEvent.onStopLogic()
				}
				'退'.toInt() -> {
					HotKeyEvent.onExit()
				}
				else -> {
				}
			}
		}
	}

	private fun createJFrame(): JFrame {
		val jFrame = JFrame().init(0.5)
		jFrame.layout = FlowLayout()

		mouseLocationLabel = JLabel() //实时显示鼠标位置
		groupPanel = JPanel(GridLayout(0, 1))
		newLogicButton = JButton("添加逻辑")

		groupPanel.add(mouseLocationLabel)
		groupPanel.add(newLogicButton)

		newLogicButton.addActionListener {
			ButtonEvent.onNewLogic()
		}

		jFrame.add(groupPanel)
		jFrame.isVisible = true

		return jFrame
	}

	private fun createLogicPlant(item: LogicItem): JPanel {

		item.jDelayTextField = JTextField("0100")
		item.action.jMouseXTextField = JTextField("0000")
		item.action.jMouseYTextField = JTextField("0000")

		item.jPanel = JPanel(FlowLayout()).apply {
			add(JLabel("每隔"))
			add(item.jDelayTextField)
			add(JLabel("毫秒，判断"))
			add(JButton("编辑").apply {
				addActionListener {
					ButtonEvent.onEditTerms(item)
				}
			})
			add(JLabel("成功则控制鼠标点击"))
			add(item.action.jMouseXTextField)
			add(JLabel(","))
			add(item.action.jMouseYTextField)
			add(JButton("删除").apply {
				addActionListener {
					ButtonEvent.onDeleteLogic(item, item.jPanel)
				}
			})
		}
		return item.jPanel
	}
}


fun main(args: Array<String>) {
	MainClass.mainStart()
}