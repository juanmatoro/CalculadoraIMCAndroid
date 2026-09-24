package com.juanmatoro.calculadora_imc_android

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.pow

class MainActivity : AppCompatActivity() {


    lateinit var heigthEdidtText: EditText
    lateinit var weigthEdidtText: EditText
    lateinit var calculateButon: Button

    lateinit var resultTextView: TextView
    lateinit var categoryTextView: TextView


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

        heigthEdidtText = findViewById(R.id.heigthEdidtText)
        weigthEdidtText = findViewById(R.id.weigthEdidtText)
        calculateButon = findViewById(R.id.calculateButon)
        resultTextView = findViewById(R.id.resultTextView)
        categoryTextView = findViewById(R.id.categoryTextView)


        // Dar funcionalidad a los componentes

        calculateButon.setOnClickListener {

            Log.d("MainActivity", "Boton pulsado")

            // toDoubleOrNull() devuelve null en vez de lanzar excepción si el campo
            // está vacío o no es un número, así evitamos el crash
            val height = heigthEdidtText.text.toString().toDoubleOrNull()
            val weigth = weigthEdidtText.text.toString().toDoubleOrNull()

            if (height == null || height <= 0.0) {
                heigthEdidtText.error = "Introduce una altura válida"
                return@setOnClickListener
            }

            if (weigth == null || weigth <= 0.0) {
                weigthEdidtText.error = "Introduce un peso válido"
                return@setOnClickListener
            }

            val heightInMeters = height / 100

            val result = weigth / heightInMeters.pow(2)

            Log.d("MainActivity", "Resultado: $result")

            resultTextView.text = "%.2f".format(result)

            val (category, categoryColorRes) = getImcCategory(result)
            categoryTextView.text = category
            categoryTextView.setTextColor(ContextCompat.getColor(this, categoryColorRes))

            clearFields()
        }


    }

    private fun clearFields() {
        heigthEdidtText.text.clear()
        weigthEdidtText.text.clear()
    }

    private fun getImcCategory(imc: Double): Pair<String, Int> {
        return when {
            imc < 18.5 -> "Bajo peso, come más" to R.color.imc_bajo_peso
            imc < 25.0 -> "Peso saludable, Well done" to R.color.imc_saludable
            imc < 30.0 -> "Sobrepeso, hay que cuidarse!" to R.color.imc_sobrepeso
            else -> "Obesidad, Ponte a dieta ya!" to R.color.imc_obesidad
        }
    }
}