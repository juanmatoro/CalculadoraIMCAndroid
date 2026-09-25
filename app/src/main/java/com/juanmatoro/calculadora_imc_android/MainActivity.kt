package com.juanmatoro.calculadora_imc_android

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.slider.Slider
import kotlin.math.pow

class MainActivity : AppCompatActivity() {


    lateinit var alturaValorTextView: TextView
    lateinit var alturaSlider: Slider
    lateinit var pesoValorTextView: TextView
    lateinit var pesoMenosButon: Button
    lateinit var pesoMasButon: Button
    lateinit var calculateButon: Button
    lateinit var clearButon: Button

    lateinit var resultCardView: View
    lateinit var resultTextView: TextView
    lateinit var categoryTextView: TextView
    lateinit var resultAnimationView: LottieAnimationView

    // El peso se guarda en décimas de kg (p.ej. 503 = 50.3 kg) para evitar
    // errores de redondeo al sumar/restar 0.1 repetidamente con Float
    private var pesoDecimas = (PESO_INICIAL * 10).toInt()

    private data class ImcCategoryInfo(
        val text: String,
        val colorRes: Int,
        val animationAsset: String
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main) // activiti layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Buscar por id los componentes en la vista

        alturaValorTextView = findViewById(R.id.alturaValorTextView)
        alturaSlider = findViewById(R.id.alturaSlider)
        pesoValorTextView = findViewById(R.id.pesoValorTextView)
        pesoMenosButon = findViewById(R.id.pesoMenosButon)
        pesoMasButon = findViewById(R.id.pesoMasButon)
        calculateButon = findViewById(R.id.calculateButon)
        clearButon = findViewById(R.id.clearButon)
        resultCardView = findViewById(R.id.resultCardView)
        resultTextView = findViewById(R.id.resultTextView)
        categoryTextView = findViewById(R.id.categoryTextView)
        resultAnimationView = findViewById(R.id.resultAnimationView)

        updateAlturaValor(alturaSlider.value)
        alturaSlider.addOnChangeListener { _, value, _ -> updateAlturaValor(value) }

        updatePesoValor()
        pesoMenosButon.setOnClickListener { cambiarPeso(-PESO_PASO_DECIMAS) }
        pesoMasButon.setOnClickListener { cambiarPeso(PESO_PASO_DECIMAS) }


        // Dar funcionalidad a los componentes

        calculateButon.setOnClickListener {

            Log.d("MainActivity", "Boton pulsado")

            // Tanto el slider de altura como el selector de peso siempre
            // entregan un valor válido dentro de su rango, así que ya no
            // hace falta validación manual
            val height = alturaSlider.value.toDouble()
            val weigth = pesoDecimas / 10.0

            val heightInMeters = height / 100

            val result = weigth / heightInMeters.pow(2)

            Log.d("MainActivity", "Resultado: $result")

            resultTextView.text = "%.2f".format(result)

            val categoryInfo = getImcCategory(result)
            categoryTextView.text = categoryInfo.text
            categoryTextView.setTextColor(ContextCompat.getColor(this, categoryInfo.colorRes))
            playCategoryAnimation(categoryInfo.animationAsset)

            resultCardView.visibility = View.VISIBLE
        }

        clearButon.setOnClickListener {
            resetForm()
        }

    }

    private fun resetForm() {
        alturaSlider.value = ALTURA_POR_DEFECTO
        pesoDecimas = (PESO_INICIAL * 10).toInt()
        updatePesoValor()
        resultTextView.text = getString(R.string.resultado_placeholder)
        categoryTextView.text = ""
        resultAnimationView.cancelAnimation()
        resultAnimationView.visibility = View.GONE
        resultCardView.visibility = View.GONE
    }

    private fun updateAlturaValor(value: Float) {
        alturaValorTextView.text = value.toInt().toString()
    }

    private fun cambiarPeso(deltaDecimas: Int) {
        pesoDecimas = (pesoDecimas + deltaDecimas).coerceIn(PESO_MINIMO_DECIMAS, PESO_MAXIMO_DECIMAS)
        updatePesoValor()
    }

    private fun updatePesoValor() {
        pesoValorTextView.text = "%.1f".format(pesoDecimas / 10.0)
    }

    private fun getImcCategory(imc: Double): ImcCategoryInfo {
        return when {
            imc < 18.5 -> ImcCategoryInfo(
                "🍽️ Bajo peso, come más", R.color.imc_bajo_peso, "lottie/bajo_peso.json"
            )
            imc < 25.0 -> ImcCategoryInfo(
                "✅ Peso saludable, Well done", R.color.imc_saludable, "lottie/saludable.json"
            )
            imc < 30.0 -> ImcCategoryInfo(
                "⚠️ Sobrepeso, hay que cuidarse!", R.color.imc_sobrepeso, "lottie/sobrepeso.json"
            )
            else -> ImcCategoryInfo(
                "🚨 Obesidad, Ponte a dieta ya!", R.color.imc_obesidad, "lottie/obesidad.json"
            )
        }
    }

    // Cada categoría tiene su propio archivo .json en assets/lottie/; si el archivo
    // aún no existe (por ejemplo, todavía no se ha añadido la animación real) se
    // captura el fallo para no crashear y simplemente se oculta la animación.
    private fun playCategoryAnimation(assetPath: String) {
        resultAnimationView.setFailureListener { error ->
            Log.w("MainActivity", "No se pudo cargar la animación $assetPath", error)
            resultAnimationView.visibility = View.GONE
        }
        resultAnimationView.setAnimation(assetPath)
        resultAnimationView.visibility = View.VISIBLE
        resultAnimationView.playAnimation()
    }

    companion object {
        private const val ALTURA_POR_DEFECTO = 170f

        private const val PESO_INICIAL = 50f
        private const val PESO_PASO_DECIMAS = 1 // 0.1 kg
        private const val PESO_MINIMO_DECIMAS = 1 // 0.1 kg
        private const val PESO_MAXIMO_DECIMAS = 2000 // 200 kg
    }
}