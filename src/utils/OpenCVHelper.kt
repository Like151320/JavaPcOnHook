package utils

import org.opencv.core.*
import java.awt.image.BufferedImage
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.awt.Rectangle
import java.io.File
import javax.imageio.ImageIO

/**
 * 作者: Li_ke
 * 日期: 2019/12/20 15:30
 * 作用:
 */


/** [Mat]的所有类型，当不知道那个类型才对时，都试试就行了
 * [BufferedImage.TYPE_INT_RGB] + dataBuffer = IntArray 能转 [CvType.CV_32SC1]、[CvType.CV_32SC2]、[CvType.CV_32SC3] 、[CvType.CV_32SC4]
 *
 */
val allCvTypeList by lazy {
	listOf(
		CvType.CV_8UC(1),  // predefined type constants
		CvType.CV_8UC(2),  // predefined type constants
		CvType.CV_8UC(3),  // predefined type constants
		CvType.CV_8UC(4),
		// predefined type constants
		CvType.CV_8SC(1),  // predefined type constants
		CvType.CV_8SC(2),  // predefined type constants
		CvType.CV_8SC(3),  // predefined type constants
		CvType.CV_8SC(4),
		// predefined type constants
		CvType.CV_16UC(1),  // predefined type constants
		CvType.CV_16UC(2),  // predefined type constants
		CvType.CV_16UC(3),  // predefined type constants
		CvType.CV_16UC(4),
		// predefined type constants
		CvType.CV_16SC(1),  // predefined type constants
		CvType.CV_16SC(2),  // predefined type constants
		CvType.CV_16SC(3),  // predefined type constants
		CvType.CV_16SC(4),
		// predefined type constants
		CvType.CV_32SC(1),  // predefined type constants
		CvType.CV_32SC(2),  // predefined type constants
		CvType.CV_32SC(3),  // predefined type constants
		CvType.CV_32SC(4),
		// predefined type constants
		CvType.CV_32FC(1),  // predefined type constants
		CvType.CV_32FC(2),  // predefined type constants
		CvType.CV_32FC(3),  // predefined type constants
		CvType.CV_32FC(4),
		// predefined type constants
		CvType.CV_64FC(1),  // predefined type constants
		CvType.CV_64FC(2),  // predefined type constants
		CvType.CV_64FC(3),  // predefined type constants
		CvType.CV_64FC(4),
		// predefined type constants
		CvType.CV_16FC(1),  // predefined type constants
		CvType.CV_16FC(2),  // predefined type constants
		CvType.CV_16FC(3),  // predefined type constants
		CvType.CV_16FC(4)
	)
}

/** Mat 的 type 转换 */
fun Mat.copyToNewType(newType: Int) {
	val newTypeMat = Mat()
	return this.convertTo(newTypeMat, newType)
}

fun Mat.toBufferedImage(): BufferedImage {
	val fileName = "images/temp${System.currentTimeMillis()}.jpg"
	val file = File(fileName)

	Imgcodecs.imwrite(fileName, this)
	val bufferedImage = ImageIO.read(file)

	file.delete()
	return bufferedImage
}

fun BufferedImage.toMat(): Mat {
	val fileName = "images/temp${System.currentTimeMillis()}.jpg"

	this.writeToFile(fileName)
	val mat = Imgcodecs.imread(fileName)

	File(fileName).delete()
	return mat

	//直接在内存用数据转失败了，保存 Mat 时图片是黑的
//	val mat = Mat(this.width, this.height, this.utils.getCvType())
//	if (raster.dataBuffer is DataBufferInt) {
//		val pixels = (raster.dataBuffer as DataBufferInt).data
//		mat.put(0, 0, pixels)
//
//	} else if (raster.dataBuffer is DataBufferByte) {
//		val pixels = (raster.dataBuffer as DataBufferByte).data
//		mat.put(0, 0, pixels)
//	}
//	return mat
}

fun Rectangle.toRect(): Rect {
	return Rect(this.x, this.y, this.width, this.height)
}

/** 匹配图片。在[sourceImage]中寻找[templateImage]
 * @return 图片位置 or Null
 */
@Deprecated("opencv 不会用")
fun matchImage2(sourceImage: BufferedImage, templateImage: BufferedImage): Rectangle? {
	//OpenCV 图像工具
	System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

	val source = sourceImage.toMat()
	val template = templateImage.toMat()

	//创建于原图相同的大小，储存匹配度
	val result = Mat.zeros(
		source.rows() - template.rows() + 1,
		source.cols() - template.cols() + 1,
		source.type()
	)
	//调用模板匹配方法
	Imgproc.matchTemplate(source, template, result, Imgproc.TM_SQDIFF_NORMED)

	//规格化
	Core.normalize(result, result, 0.0, 1.0, Core.NORM_MINMAX, -1);
	//获得最可能点，MinMaxLocResult是其数据格式，包括了最大、最小点的位置x、y
	val mlr = Core.minMaxLoc(result)

	println("mlr.maxVal = " + mlr.maxVal)
	println("mlr.minVal = " + mlr.minVal)
	//最大匹配点
	val matchLoc: org.opencv.core.Point? = mlr.maxLoc

	if (matchLoc != null) {
		return Rectangle(
			matchLoc.x.toInt(), matchLoc.y.toInt(),
			(template.width()), (template.height())
		)
	} else {
		return null
	}
}

/*
模板匹配 matchTemplate 的原理，就是拿 模板图 在 原图 上每一像素都比较一次，拿到像素差完全相同的值算是匹配成功。
参4是匹配方案，TM_SQDIFF 匹配方案匹配的最佳值为 minLoc、TM_CCORR 方案最佳匹配为 maxLoc、TM_CCOEFF 方案最佳匹配为 maxLoc。
	在匹配方案后加 _NORMED 是把方案的值从真实值替换为比例值 TM_SQDIFF(1-0)，TM_CCORR(1-0)，TM_CCOEFF(1 - -1)  。


归一化（Normalization）
	假设拿到了一群人的体重数据，要查看体重图，就用到了 归一化。如此一来画图时就不用具体的数值，只用比例就能画了。
		A体重程量(1 = 最大，0 = 最小，0.5 = 最大最小值的最中间)
		 	= (某人A体重 - 最小体重) / (最大体重 - 最小体重) 。

标准化（Normalization）
	假设在上文例子中，要查看某人的体中是否平均，就用到了 标准化。计算出体重标准后，就能看这个体重是否正常(平均)了
		A体重平均程度(0 = 最平均，越大 = 越重，越小 = 越轻)
			= (某人A体重 - 平均体重) / 平均体重差
*/

fun BufferedImage.getCvType(): Int {
	val image = this
	if (image.type == BufferedImage.TYPE_INT_RGB /*&& image.raster.dataBuffer is DataBufferInt*/) {
		return CvType.CV_32SC4
	}
	error("utils.getCvType Error")
}

/* 《CVType 解释》 https://my.oschina.net/u/3767256/blog/1794173

格式 ： CV_[bite](U|S|F)C[channels]

[bite] : 比特数，位数。 有 8bite，16bite，32bite，64bite,对应在 Mat 中，每个像素的所占的空间大小，8位即 CV_8

(U|S|F) ：

U : unsigned int , 无符号整形
S : signed int , 有符号整形
F : float , 单精度浮点型,float类型本身即有符号

[channels] : OpenCV 中，图像可以分别为1，2，3，4 通道。

1 通道为灰度图；
2 通道的图像是RGB555和RGB565。2通道图在程序处理中会用到，如傅里叶变换，可能会用到，一个通道为实数，一个通道为虚数，主要是编程方便。RGB555是16位的，2个字节，5+6+5，第一字节的前5位是R，后三位+第二字节是G，第二字节后5位是B，可见对原图像进行压缩了
3 通道为彩色图（RGB）；
4 通道为 RGBA ，是RGB加上一个A通道，也叫alpha通道，表示透明度，PNG图像是一种典型的4通道图像。alpha通道可以赋值0到1，或者0到255，表示透明到不透明
大部分使用场景下，常使用的是1，3，4通道； 2通道不常见
 */