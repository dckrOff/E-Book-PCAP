package uz.dckroff.pcap.ui.components

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.max
import kotlin.math.min

/**
 * ImageView с поддержкой жестов масштабирования и панорамирования
 */
class ZoomableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val matrix = Matrix()
    private val savedMatrix = Matrix()
    private val matrixValues = FloatArray(9)

    // Константы для режимов касания
    private val NONE = 0
    private val DRAG = 1
    private val ZOOM = 2
    private var mode = NONE

    // Запоминание позиций касания
    private val startPoint = PointF()
    private val midPoint = PointF()
    private var oldDist = 1f

    // Ограничения масштабирования
    private val MAX_SCALE = 5.0f
    private val MIN_SCALE = 0.5f

    private var scaleDetector: ScaleGestureDetector
    private var gestureDetector: GestureDetector

    init {
        scaleType = ScaleType.MATRIX
        
        // Инициализация детекторов жестов
        scaleDetector = ScaleGestureDetector(context, ScaleListener())
        gestureDetector = GestureDetector(context, GestureListener())
        
        // Установка начальной матрицы
        imageMatrix = matrix
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)

        val currentPoint = PointF(event.x, event.y)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                savedMatrix.set(matrix)
                startPoint.set(event.x, event.y)
                mode = DRAG
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                oldDist = spacing(event)
                if (oldDist > 10f) {
                    savedMatrix.set(matrix)
                    midPoint(midPoint, event)
                    mode = ZOOM
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (mode == DRAG) {
                    matrix.set(savedMatrix)
                    matrix.postTranslate(event.x - startPoint.x, event.y - startPoint.y)
                } else if (mode == ZOOM) {
                    val newDist = spacing(event)
                    if (newDist > 10f) {
                        matrix.set(savedMatrix)
                        val scale = newDist / oldDist
                        matrix.postScale(scale, scale, midPoint.x, midPoint.y)
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                mode = NONE
                correctTranslation()
            }
        }

        // Применение матрицы
        imageMatrix = matrix
        return true
    }

    /**
     * Корректировка перемещения для предотвращения выхода за границы изображения
     */
    private fun correctTranslation() {
        matrix.getValues(matrixValues)
        val scale = matrixValues[Matrix.MSCALE_X]
        
        // Ограничение масштаба
        val correctedScale = when {
            scale > MAX_SCALE -> MAX_SCALE / scale
            scale < MIN_SCALE -> MIN_SCALE / scale
            else -> 1.0f
        }
        
        if (correctedScale != 1.0f) {
            matrix.postScale(correctedScale, correctedScale, 
                (width / 2).toFloat(), (height / 2).toFloat())
        }
    }

    /**
     * Вычисление расстояния между двумя точками касания
     */
    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    /**
     * Вычисление средней точки между двумя точками касания
     */
    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point.set(x / 2, y / 2)
    }

    /**
     * Обработчик жестов масштабирования
     */
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            
            // Получаем текущий масштаб
            matrix.getValues(matrixValues)
            val currentScale = matrixValues[Matrix.MSCALE_X]
            
            // Применяем ограничения
            val newScale = min(MAX_SCALE, max(currentScale * scaleFactor, MIN_SCALE))
            val limitedScaleFactor = newScale / currentScale
            
            // Применяем масштабирование
            matrix.postScale(
                limitedScaleFactor, 
                limitedScaleFactor, 
                detector.focusX, 
                detector.focusY
            )
            
            imageMatrix = matrix
            return true
        }
    }

    /**
     * Обработчик жестов (двойное нажатие)
     */
    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            // Получаем текущий масштаб
            matrix.getValues(matrixValues)
            val currentScale = matrixValues[Matrix.MSCALE_X]
            
            // При двойном нажатии либо увеличиваем, либо сбрасываем масштаб
            val targetScale = if (currentScale > 1.5f) 1.0f else 2.5f
            val scaleFactor = targetScale / currentScale
            
            // Анимируем масштабирование к целевому значению
            matrix.postScale(
                scaleFactor, 
                scaleFactor, 
                e.x, 
                e.y
            )
            
            imageMatrix = matrix
            return true
        }
    }
} 