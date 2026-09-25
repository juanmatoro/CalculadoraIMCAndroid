package com.juanmatoro.calculadora_imc_android

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.view.animation.OvershootInterpolator
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.slider.Slider
import kotlin.math.pow
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {


    lateinit var alturaValorTextView: TextView
    lateinit var alturaSlider: Slider
    lateinit var pesoValorTextView: EditText
    lateinit var pesoMenosButon: Button
    lateinit var pesoMasButon: Button
    lateinit var calculateButon: Button
    lateinit var clearButon: Button

    lateinit var resultCardView: View
    lateinit var resultTextView: TextView
    lateinit var categoryTextView: TextView
    lateinit var resultAnimationView: ImageView

    // El peso se guarda en décimas de kg (p.ej. 503 = 50.3 kg) para evitar
    // errores de redondeo al sumar/restar 0.1 repetidamente con Float
    private var pesoDecimas = (PESO_INICIAL * 10).toInt()

    private data class ImcCategoryInfo(
        val text: String,
        val colorRes: Int,
        val iconRes: Int
    )


    // Los OnTouchListener de los botones +/- solo detectan cuándo se
    // suelta el dedo para detener la repetición; nunca consumen el
    // evento (siempre devuelven false), así que el click normal y el de
    // accesibilidad (TalkBack) se siguen procesando exactamente igual
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main) // activiti layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Buscar por ID los componentes en la vista

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

        // Un toque simple ajusta 0.1 kg; mantener pulsado repite el ajuste
        // cada vez más rápido, para poder movernos deprisa por el rango
        pesoMenosButon.setOnClickListener { cambiarPeso(-PESO_PASO_DECIMAS) }
        pesoMasButon.setOnClickListener { cambiarPeso(PESO_PASO_DECIMAS) }
        pesoMenosButon.setOnLongClickListener { iniciarRepeticionPeso(pesoMenosButon, -PESO_PASO_DECIMAS); true }
        pesoMasButon.setOnLongClickListener { iniciarRepeticionPeso(pesoMasButon, PESO_PASO_DECIMAS); true }
        val detenerRepeticionAlSoltar = View.OnTouchListener { boton, event ->
            if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                detenerRepeticionPeso(boton)
            }
            false
        }
        pesoMenosButon.setOnTouchListener(detenerRepeticionAlSoltar)
        pesoMasButon.setOnTouchListener(detenerRepeticionAlSoltar)

        // El número también se puede editar directamente desde el teclado,
        // para saltar rápido a un valor lejano sin tocar +/- muchas veces.
        // Se actualiza pesoDecimas con cada pulsación de tecla (no solo al
        // perder el foco): un MaterialButton no roba el foco del campo al
        // tocarlo (solo lo hacen otros campos de texto), así que si
        // solo confirmáramos el valor al perder el foco, tocar
        // "Calcular IMC" justo después de escribir ignoraría lo escrito
        pesoValorTextView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                parsearPesoEscrito(s.toString())?.let { pesoDecimas = it }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        pesoValorTextView.setOnFocusChangeListener { _, tieneFoco ->
            if (!tieneFoco) confirmarPesoEscrito()
        }
        pesoValorTextView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                confirmarPesoEscrito()
                ocultarTeclado(pesoValorTextView)
                pesoValorTextView.clearFocus()
                true
            } else {
                false
            }
        }


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
            playCategoryAnimation(categoryInfo.iconRes)

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
        resultAnimationView.animate().cancel()
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
        pesoValorTextView.setText("%.1f".format(pesoDecimas / 10.0))
    }

    // Admite tanto "50.5" como "50,5" (teclados en locales que usan coma
    // decimal). Si el texto está vacío o no es un número válido, devuelve
    // null y quien llame mantiene el peso anterior en vez de forzar un
    // valor por defecto que el usuario no ha pedido
    private fun parsearPesoEscrito(texto: String): Int? {
        val valor = texto.replace(',', '.').toDoubleOrNull() ?: return null
        return (valor * 10).roundToInt().coerceIn(PESO_MINIMO_DECIMAS, PESO_MAXIMO_DECIMAS)
    }

    // Al terminar de editar (se pierde el foco o se pulsa "Hecho" en el
    // teclado) se reformatea el número mostrado, p.ej. "8" pasa a "8.0",
    // o se restaura el último valor válido si el campo quedó vacío
    private fun confirmarPesoEscrito() {
        parsearPesoEscrito(pesoValorTextView.text.toString())?.let { pesoDecimas = it }
        updatePesoValor()
    }

    private fun ocultarTeclado(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    // Mantener pulsado +/- repite el ajuste de 0.1 kg, acelerando el
    // intervalo entre repeticiones para poder recorrer rango rápido sin
    // perder la precisión del toque simple
    private fun iniciarRepeticionPeso(boton: View, deltaDecimas: Int) {
        detenerRepeticionPeso(boton)
        val runnable = object : Runnable {
            var intervaloMs = REPETICION_INTERVALO_INICIAL_MS
            override fun run() {
                cambiarPeso(deltaDecimas)
                intervaloMs = (intervaloMs * REPETICION_FACTOR_ACELERACION)
                    .toLong()
                    .coerceAtLeast(REPETICION_INTERVALO_MINIMO_MS)
                boton.postDelayed(this, intervaloMs)
            }
        }
        boton.tag = runnable
        boton.postDelayed(runnable, REPETICION_INTERVALO_INICIAL_MS)
    }

    private fun detenerRepeticionPeso(boton: View) {
        (boton.tag as? Runnable)?.let { boton.removeCallbacks(it) }
        boton.tag = null
    }

    private fun getImcCategory(imc: Double): ImcCategoryInfo {
        return when {
            imc < 18.5 -> ImcCategoryInfo(
                "🍽️ Bajo peso, come más", R.color.imc_bajo_peso, R.drawable.ic_imc_bajo_peso
            )
            imc < 25.0 -> ImcCategoryInfo(
                "✅ Peso saludable, Well done", R.color.imc_saludable, R.drawable.ic_imc_saludable
            )
            imc < 30.0 -> ImcCategoryInfo(
                "⚠️ Sobrepeso, hay que cuidarse!", R.color.imc_sobrepeso, R.drawable.ic_imc_sobrepeso
            )
            else -> ImcCategoryInfo(
                "🚨 Obesidad, Ponte a dieta ya!", R.color.imc_obesidad, R.drawable.ic_imc_obesidad
            )
        }
    }

    // El icono parte invisible y a escala 0 en cada cálculo, para que la
    // entrada con rebote se repita aunque se recalcule sin pulsar "Recalcular"
    private fun playCategoryAnimation(@DrawableRes iconRes: Int) {
        resultAnimationView.animate().cancel()
        resultAnimationView.setImageResource(iconRes)
        resultAnimationView.alpha = 0f
        resultAnimationView.scaleX = 0f
        resultAnimationView.scaleY = 0f
        resultAnimationView.visibility = View.VISIBLE
        resultAnimationView.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(ANIMACION_ICONO_DURACION_MS)
            .setInterpolator(OvershootInterpolator())
            .start()
    }

    companion object {
        private const val ALTURA_POR_DEFECTO = 170f

        private const val PESO_INICIAL = 50f
        private const val PESO_PASO_DECIMAS = 1 // 0.1 kg
        private const val PESO_MINIMO_DECIMAS = 1 // 0.1 kg
        private const val PESO_MAXIMO_DECIMAS = 2000 // 200 kg

        private const val REPETICION_INTERVALO_INICIAL_MS = 350L
        private const val REPETICION_INTERVALO_MINIMO_MS = 40L
        private const val REPETICION_FACTOR_ACELERACION = 0.85

        private const val ANIMACION_ICONO_DURACION_MS = 500L
    }
}