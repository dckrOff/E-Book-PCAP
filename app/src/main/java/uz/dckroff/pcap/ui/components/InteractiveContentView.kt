package uz.dckroff.pcap.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import org.json.JSONObject
import timber.log.Timber
import uz.dckroff.pcap.R
import uz.dckroff.pcap.databinding.ViewInteractiveContentBinding

/**
 * Компонент для отображения интерактивного контента с использованием WebView
 * (диаграммы, графики, интерактивные схемы и т.д.)
 */
class InteractiveContentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewInteractiveContentBinding
    private var onInteractionListener: OnInteractionListener? = null

    init {
        binding = ViewInteractiveContentBinding.inflate(LayoutInflater.from(context), this, true)
        
        // Настройка WebView
        with(binding.webView) {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    binding.progressBar.progress = newProgress
                    binding.progressBar.visibility = if (newProgress < 100) View.VISIBLE else View.GONE
                }
            }
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    // Предотвращаем навигацию по внешним ссылкам
                    return true
                }
                
                override fun onPageFinished(view: WebView?, url: String?) {
                    binding.progressBar.visibility = View.GONE
                    // Инъекция JavaScript для обработки взаимодействия
                    view?.evaluateJavascript(
                        "window.Android = {" +
                                "sendEvent: function(eventType, eventData) {" +
                                "  window.InteractiveContent.postMessage(JSON.stringify({type: eventType, data: eventData}));" +
                                "}" +
                            "};", null
                    )
                }
            }
            
            // Настройка JavaScript интерфейса
            addJavascriptInterface(JavaScriptInterface(), "InteractiveContent")
        }
    }

    /**
     * Загружает интерактивный контент
     * @param contentType тип интерактивного контента (chart, diagram, animation)
     * @param contentData JSON-строка с данными для интерактивного контента
     */
    fun loadContent(contentType: String, contentData: String) {
        try {
            binding.progressBar.visibility = View.VISIBLE
            
            when (contentType.lowercase()) {
                "chart" -> loadChart(contentData)
                "diagram" -> loadDiagram(contentData)
                "animation" -> loadAnimation(contentData)
                "interactive_demo" -> loadInteractiveDemo(contentData)
                else -> {
                    Timber.e("Неизвестный тип интерактивного контента: $contentType")
                    binding.progressBar.visibility = View.GONE
                    binding.tvError.visibility = View.VISIBLE
                    binding.tvError.text = "Неподдерживаемый тип контента: $contentType"
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при загрузке интерактивного контента")
            binding.progressBar.visibility = View.GONE
            binding.tvError.visibility = View.VISIBLE
            binding.tvError.text = "Ошибка загрузки: ${e.message}"
        }
    }

    /**
     * Загружает диаграмму
     */
    private fun loadChart(data: String) {
        val jsonData = JSONObject(data)
        val chartType = jsonData.optString("type", "line")
        val chartData = jsonData.optString("data", "{}")
        
        // Подготавливаем HTML для отображения диаграммы с Chart.js
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
                <style>
                    body { margin: 0; padding: 16px; font-family: 'Roboto', sans-serif; }
                    canvas { max-width: 100%; }
                </style>
            </head>
            <body>
                <canvas id="chart"></canvas>
                <script>
                    var ctx = document.getElementById('chart').getContext('2d');
                    var chart = new Chart(ctx, $chartData);
                    
                    // Отправка событий в Android
                    chart.options.onClick = function(e) {
                        var activePoints = chart.getElementsAtEventForMode(e, 'nearest', { intersect: true }, false);
                        if (activePoints && activePoints.length > 0) {
                            var firstPoint = activePoints[0];
                            var label = chart.data.labels[firstPoint.index];
                            var value = chart.data.datasets[firstPoint.datasetIndex].data[firstPoint.index];
                            window.Android.sendEvent('click', {label: label, value: value});
                        }
                    };
                </script>
            </body>
            </html>
        """.trimIndent()
        
        binding.webView.loadDataWithBaseURL(
            "https://local.content/",
            htmlContent,
            "text/html",
            "UTF-8",
            null
        )
    }

    /**
     * Загружает диаграмму с использованием Mermaid.js
     */
    private fun loadDiagram(data: String) {
        val jsonData = JSONObject(data)
        val diagramCode = jsonData.optString("code", "")
        
        // Подготавливаем HTML для отображения диаграммы с Mermaid.js
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <script src="https://cdn.jsdelivr.net/npm/mermaid/dist/mermaid.min.js"></script>
                <style>
                    body { margin: 0; padding: 16px; font-family: 'Roboto', sans-serif; }
                    .mermaid { width: 100%; }
                </style>
            </head>
            <body>
                <div class="mermaid">
                    $diagramCode
                </div>
                <script>
                    mermaid.initialize({
                        startOnLoad: true,
                        theme: 'neutral'
                    });
                    
                    // Отслеживание кликов
                    document.querySelector('.mermaid').addEventListener('click', function(e) {
                        var element = e.target;
                        if (element.tagName.toLowerCase() === 'tspan' && element.parentNode.nodeName.toLowerCase() === 'text') {
                            var nodeId = element.parentNode.id;
                            window.Android.sendEvent('node_click', {id: nodeId, text: element.textContent});
                        }
                    });
                </script>
            </body>
            </html>
        """.trimIndent()
        
        binding.webView.loadDataWithBaseURL(
            "https://local.content/",
            htmlContent,
            "text/html",
            "UTF-8",
            null
        )
    }

    /**
     * Загружает анимацию с использованием Lottie
     */
    private fun loadAnimation(data: String) {
        val jsonData = JSONObject(data)
        val animationData = jsonData.optString("animation", "{}")
        val loop = jsonData.optBoolean("loop", true)
        
        // Подготавливаем HTML для отображения анимации с Lottie
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <script src="https://cdnjs.cloudflare.com/ajax/libs/lottie-web/5.9.6/lottie.min.js"></script>
                <style>
                    body { margin: 0; padding: 0; display: flex; justify-content: center; align-items: center; height: 100vh; }
                    #animation { width: 100%; max-width: 400px; }
                </style>
            </head>
            <body>
                <div id="animation"></div>
                <script>
                    var animation = lottie.loadAnimation({
                        container: document.getElementById('animation'),
                        renderer: 'svg',
                        loop: $loop,
                        autoplay: true,
                        animationData: $animationData
                    });
                    
                    animation.addEventListener('complete', function() {
                        window.Android.sendEvent('animation_complete', {});
                    });
                </script>
            </body>
            </html>
        """.trimIndent()
        
        binding.webView.loadDataWithBaseURL(
            "https://local.content/",
            htmlContent,
            "text/html",
            "UTF-8",
            null
        )
    }

    /**
     * Загружает интерактивную демонстрацию
     */
    private fun loadInteractiveDemo(data: String) {
        val jsonData = JSONObject(data)
        val demoUrl = jsonData.optString("url", "")
        
        if (demoUrl.isNotEmpty()) {
            binding.webView.loadUrl(demoUrl)
        } else {
            val demoHtml = jsonData.optString("html", "")
            if (demoHtml.isNotEmpty()) {
                binding.webView.loadDataWithBaseURL(
                    "https://local.content/",
                    demoHtml,
                    "text/html",
                    "UTF-8",
                    null
                )
            } else {
                Timber.e("Для интерактивной демонстрации не указан URL или HTML")
                binding.progressBar.visibility = View.GONE
                binding.tvError.visibility = View.VISIBLE
                binding.tvError.text = "Ошибка: отсутствуют данные для демонстрации"
            }
        }
    }

    /**
     * JavaScript интерфейс для связи с WebView
     */
    private inner class JavaScriptInterface {
        @android.webkit.JavascriptInterface
        fun postMessage(message: String) {
            try {
                val jsonObject = JSONObject(message)
                val eventType = jsonObject.optString("type", "")
                val eventData = jsonObject.optJSONObject("data")
                
                onInteractionListener?.onInteraction(eventType, eventData.toString())
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при обработке сообщения от JavaScript")
            }
        }
    }

    /**
     * Интерфейс для обработки взаимодействия с интерактивным контентом
     */
    interface OnInteractionListener {
        fun onInteraction(eventType: String, eventData: String)
    }

    /**
     * Устанавливает слушателя событий взаимодействия
     */
    fun setOnInteractionListener(listener: OnInteractionListener) {
        this.onInteractionListener = listener
    }
} 