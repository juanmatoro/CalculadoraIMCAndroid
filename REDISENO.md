# Rediseño de la pantalla principal (rama `rediseño`)

Este documento explica, cambio a cambio, todo lo que se ha modificado
en `activity_main.xml`, `MainActivity.kt` y `strings.xml` durante el
rediseño de la pantalla principal, y el porqué de cada decisión.
Complementa a [`DISENO_MATERIAL.md`](DISENO_MATERIAL.md), que cubre el
paso anterior (aplicar Material Design a los componentes originales).

No se ha añadido ninguna dependencia nueva: todo se construye con
componentes que el proyecto ya usaba (`MaterialCardView`,
`MaterialButton`, `Slider`, `LottieAnimationView`).

---

## 1. Altura: de campo de texto a `Slider`

**Antes:** un `TextInputLayout` + `TextInputEditText` con
`inputType="number"`, validado a mano en Kotlin (si estaba vacío o
era ≤ 0, se marcaba error y no se calculaba).

**Después (vista, `activity_main.xml`):**
```xml
<com.google.android.material.slider.Slider
    android:id="@+id/alturaSlider"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:stepSize="1"
    android:value="160"
    android:valueFrom="100"
    android:valueTo="250"
    app:labelBehavior="floating"
    app:tickVisible="false" />
```

**Qué cambia y por qué:**
- Un `Slider` de Material acota el valor automáticamente entre
  `valueFrom` (100) y `valueTo` (250) — es físicamente imposible que
  el usuario introduzca una altura fuera de rango o no numérica.
- `stepSize="1"` hace que el valor salte de cm en cm (un entero),
  que es la precisión que tiene sentido para una altura humana.
- `app:tickVisible="false"` oculta los puntitos que Material dibuja
  por defecto en cada paso cuando hay `stepSize` (las "tick marks");
  visualmente son ruido si no aportan información nueva, pero el
  valor sigue saltando de 1 en 1 igual.
- `app:labelBehavior="floating"` muestra una burbuja con el valor
  mientras se arrastra el thumb, como feedback inmediato.

**Lógica (`MainActivity.kt`):**
```kotlin
lateinit var alturaValorTextView: TextView
lateinit var alturaSlider: Slider
...
updateAlturaValor(alturaSlider.value)
alturaSlider.addOnChangeListener { _, value, _ -> updateAlturaValor(value) }
...
private fun updateAlturaValor(value: Float) {
    alturaValorTextView.text = value.toInt().toString()
}
```
- `addOnChangeListener` se dispara cada vez que el usuario mueve el
  slider; actualiza en tiempo real el número grande que se muestra
  encima (ver sección 3).
- Como el slider **siempre** entrega un valor válido, se pudo borrar
  por completo la validación manual de altura (`if (height == null
  || height <= 0.0) { ... }`) que existía en el botón "Calcular IMC".

---

## 2. Peso: de campo de texto a selector `−` / `+`

**Antes:** otro `TextInputLayout` + `TextInputEditText` con
`inputType="numberDecimal"`, validado igual que la altura.

**Después (vista):** dos `MaterialButton` cuadrados (48dp,
`insetLeft/Top/Right/Bottom="0dp"` y `minWidth/minHeight="0dp"` para
que no hereden el padding/tamaño mínimo por defecto de un botón de
Material) a los lados de un `TextView` grande con el valor:

```xml
<com.google.android.material.button.MaterialButton
    android:id="@+id/pesoMenosButon"
    android:layout_width="48dp"
    android:layout_height="48dp"
    android:text="@string/peso_menos"
    app:cornerRadius="14dp" />

<TextView
    android:id="@+id/pesoValorTextView"
    android:textSize="48sp"
    android:textStyle="bold"
    tools:text="50.0" />

<com.google.android.material.button.MaterialButton
    android:id="@+id/pesoMasButon"
    android:layout_width="48dp"
    android:layout_height="48dp"
    android:text="@string/peso_mas"
    app:cornerRadius="14dp" />
```

**Por qué un stepper y no un slider:** el peso necesita precisión de
0.1 kg en un rango de hasta 200 kg (2000 pasos); un slider con esa
granularidad sería casi imposible de ajustar con el dedo. Un botón
+/- con paso fijo es el patrón estándar para este caso.

**Lógica — el truco de guardar "décimas" en vez de un `Float`:**
```kotlin
// El peso se guarda en décimas de kg (p.ej. 503 = 50.3 kg) para evitar
// errores de redondeo al sumar/restar 0.1 repetidamente con Float
private var pesoDecimas = (PESO_INICIAL * 10).toInt()

private fun cambiarPeso(deltaDecimas: Int) {
    pesoDecimas = (pesoDecimas + deltaDecimas).coerceIn(PESO_MINIMO_DECIMAS, PESO_MAXIMO_DECIMAS)
    updatePesoValor()
}

private fun updatePesoValor() {
    pesoValorTextView.text = "%.1f".format(pesoDecimas / 10.0)
}
```
- **Por qué no sumar `0.1f` directamente:** los `Float`/`Double` no
  pueden representar 0.1 de forma exacta en binario. Sumar `0.1f`
  muchas veces acumula un error de redondeo minúsculo pero real (por
  ejemplo, tras 3 sumas puede dar `50.30000001` en vez de `50.3`).
  Guardando el valor como un `Int` que representa **décimas de kg**
  (50.0 kg → `500`), sumar y restar es una operación entera exacta;
  solo se convierte a decimal (`/ 10.0`) al mostrarlo en pantalla.
- `coerceIn(PESO_MINIMO_DECIMAS, PESO_MAXIMO_DECIMAS)` es lo que
  impone el rango 0.1–200 kg pedido: cada toque en +/- suma o resta
  `PESO_PASO_DECIMAS` (1 décima = 0.1 kg) y lo recorta al rango.
- Como el peso también queda siempre acotado, se eliminó igualmente
  su validación manual (`if (weigth == null || weigth <= 0.0)`).
- Las constantes viven en el `companion object`, al final de la
  clase, en vez de como "números mágicos" sueltos en el código:

```kotlin
companion object {
    private const val ALTURA_POR_DEFECTO = 170f
    private const val PESO_INICIAL = 50f
    private const val PESO_PASO_DECIMAS = 1     // 0.1 kg
    private const val PESO_MINIMO_DECIMAS = 1   // 0.1 kg
    private const val PESO_MAXIMO_DECIMAS = 2000 // 200 kg
}
```

---

## 3. Tarjetas "Altura" y "Peso": título + valor grande + control

Ambas tarjetas comparten la misma estructura visual, envueltas en
`MaterialCardView` (igual que la tarjeta de resultado que ya existía):

```
┌─────────────────────────┐
│ Título (16sp, negrita)  │
│                         │
│      Valor (48sp)       │  <- o "− valor +" en el caso del peso
│                         │
│     [control: slider]   │  <- o "kg" en el caso del peso
└─────────────────────────┘
```

**Por qué el mismo tamaño de texto en ambas:** para que la pantalla
se lea como un conjunto coherente — el usuario reconoce el mismo
patrón visual (título pequeño arriba, dato grande en el centro) en
las dos tarjetas en vez de que cada una tenga su propio estilo.

El título de "Peso" se alineó a la izquierda (`android:textAlignment
="viewStart"`) para que coincida con el de "Altura", que por defecto
ya se alinea a la izquierda (al no tener `textAlignment` y tener
`layout_width="wrap_content"`).

---

## 4. Título de pantalla y reorganización de los botones

**Vista:** se añadió un `TextView` de 24sp antes de la primera
tarjeta:
```xml
<TextView
    android:id="@+id/tituloPantallaTextView"
    android:text="@string/titulo_pantalla"
    android:textAlignment="center"
    android:textSize="24sp"
    android:textStyle="bold" />
```
y el bloque con los botones "Calcular IMC" / "Recalcular" se movió
de posición: antes estaba entre la tarjeta de Peso y la de
Resultado; ahora está **después** de la tarjeta de Resultado, al
final de la pantalla.

**Por qué:** con el título arriba, la jerarquía visual queda
"qué es esto → datos de entrada → resultado → acciones", que es el
orden natural en el que se usa el formulario (leer el título, rellenar
altura y peso, ver el resultado, y solo entonces decidir si repetir
el cálculo).

Este cambio es puramente de `activity_main.xml`; no tocó
`MainActivity.kt`, porque los `id` de los botones no cambiaron, solo
su posición en el árbol de vistas.

---

## 5. Botón "Borrar" → "Recalcular"

Cambio de un único string en `strings.xml`:
```xml
<string name="recalcular">Recalcular</string>
```
en vez de `borrar`, y la referencia en el layout
(`android:text="@string/recalcular"`). La función a la que llama el
botón (`resetForm()`) no cambió: sigue reseteando altura, peso,
resultado, categoría y animación a sus valores iniciales. Es un
cambio de **etiqueta**, no de comportamiento.

---

## 6. Tarjeta de resultado oculta hasta que se calcula

**Antes:** la tarjeta de resultado era visible desde el arranque,
mostrando el placeholder `00.00` y sin categoría.

**Vista — estado inicial oculto:**
```xml
<com.google.android.material.card.MaterialCardView
    android:id="@+id/resultCardView"
    android:visibility="gone"
    ...>
```
`gone` (y no `invisible`) para que además de no verse, no reserve
espacio en el layout — así el botón "Calcular IMC" queda justo debajo
de la tarjeta de Peso la primera vez que se abre la app.

**Lógica:**
```kotlin
lateinit var resultCardView: View
...
resultCardView = findViewById(R.id.resultCardView)
...
// dentro de calculateButon.setOnClickListener, al final:
resultCardView.visibility = View.VISIBLE
...
// dentro de resetForm():
resultCardView.visibility = View.GONE
```
- Se muestra (`VISIBLE`) justo después de calcular y pintar el
  resultado, para que aparezca ya con los datos correctos (nunca se
  ve "en blanco" un instante).
- Se vuelve a ocultar (`GONE`) en `resetForm()`, que es lo que
  ejecuta el botón "Recalcular" — así cada vez que se reinicia el
  formulario, el resultado desaparece hasta el siguiente cálculo,
  igual que al abrir la app por primera vez.
- El tipo de la variable es `View` (no `MaterialCardView`) porque
  solo se necesita controlar su visibilidad; no hace falta el tipo
  concreto para eso, así que se usa el tipo más genérico posible.

---

## 7. Distribuir las tarjetas uniformemente en vertical, sin scroll

**Objetivo:** que el título, las tres tarjetas y la fila de botones
no queden todos amontonados arriba (dejando un hueco vacío debajo en
pantallas grandes), sino repartidos a lo largo de toda la altura
disponible — y que esto funcione en cualquier tamaño de pantalla
**sin** que la pantalla necesite scroll.

**La técnica: espaciadores (`View`) con `layout_weight`.** Es el
equivalente, en un `LinearLayout` clásico de Android, al
`justify-content: space-between` de CSS Flexbox.

**Antes:** el contenedor vertical medía `wrap_content` (solo lo alto
que necesitaba su contenido) y cada tarjeta tenía un margen fijo fijo
(`layout_marginTop="16dp"` o `24dp`) respecto a la anterior. El
conjunto quedaba pegado arriba y, si la pantalla era más alta que el
contenido, el resto quedaba vacío.

**Después (vista, `activity_main.xml`):**

1. El contenedor que envuelve todo pasa de `wrap_content` a
   `match_parent`, para que ocupe toda la pantalla disponible (el
   alto real, descontando la barra de estado/navegación, que ya
   gestiona el `ViewCompat.setOnApplyWindowInsetsListener` existente
   en `MainActivity.kt`):
   ```xml
   <LinearLayout
       android:layout_width="match_parent"
       android:layout_height="match_parent"
       android:orientation="vertical"
       android:padding="16dp">
   ```

2. Se quitan los márgenes fijos entre elementos (`layout_marginTop`
   en el título, la tarjeta de Peso, la de Resultado y la fila de
   botones) y, en su lugar, se intercala un `View` vacío entre cada
   par de elementos:
   ```xml
   <View
       android:layout_width="match_parent"
       android:layout_height="0dp"
       android:layout_weight="1" />
   ```
   Uno va: entre el título y la tarjeta de Altura, entre Altura y
   Peso, entre Peso y Resultado, y entre Resultado y los botones — 4
   espaciadores en total.

**Por qué funciona:** en un `LinearLayout`, cuando un elemento tiene
`layout_height="0dp"` + `layout_weight="1"`, Android primero mide
todos los elementos **sin** peso (el título, las tarjetas, los
botones) según su contenido real (`wrap_content`), y **todo el
espacio que sobra** en el contenedor se reparte entre los elementos
con peso. Como los 4 espaciadores tienen el mismo peso (`1`), cada
uno recibe exactamente la misma porción de espacio libre → los
huecos entre título/tarjetas/botones quedan iguales, sin importar si
la pantalla es de un móvil pequeño o una tablet grande.

**Por qué no hace falta `ScrollView`:** un `ScrollView` sería
necesario si el contenido pudiera ser *más alto* que la pantalla.
Aquí se ha optado por lo contrario: dejar que el contenido ocupe
*como mucho* el alto de la pantalla (`match_parent`) y que sea el
espacio sobrante el que se reparta, nunca al revés. En un dispositivo
extremadamente pequeño donde ni siquiera el contenido "comprimido"
(tarjetas + botones, sin huecos) cupiera entero, los espaciadores
simplemente se quedarían a `0dp` — el contenido no haría scroll, pero
tampoco se generaría uno artificialmente; es la solución más simple
que cumple "sin scroll en cualquier dispositivo" para los tamaños de
pantalla reales de un teléfono Android.

**Por qué la tarjeta de resultado oculta (sección 6) no rompe el
reparto:** cuando `resultCardView` está en `GONE`, Android la trata
como si no existiera a efectos de layout — los dos espaciadores que
la rodean simplemente se combinan en uno solo más grande, así que
altura/peso/botones se siguen repartiendo bien tanto si el resultado
está visible como si no.

---

## Verificación

Para cada cambio de este documento se comprobó:
- `./gradlew assembleDebug` — compila sin errores.
- `./gradlew testDebugUnitTest` — los tests unitarios existentes
  siguen pasando.
- `./gradlew lint` — sin avisos nuevos relevantes (se corrigieron los
  que sí lo eran, como `HardcodedText` en los textos de los botones
  −/+).
- Instalación y prueba manual en un emulador Android (capturas de
  pantalla y toques simulados con `adb`), verificando el flujo
  completo: mover el slider, tocar +/-, calcular, ver el resultado
  aparecer, y recalcular para volver al estado inicial.
