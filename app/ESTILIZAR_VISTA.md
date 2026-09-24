# Cómo darle un aspecto profesional a la Calculadora IMC

Guía práctica para que cada campo (ej. "Altura" + su `EditText`) se comporte como
una **sección visual unificada**, con buena colocación y un look Material Design,
usando lo que ya tienes en el proyecto (`material:1.14.0` y `Theme.Material3.DayNight`).

---

## 1. El problema del layout actual

Ahora mismo cada campo son **dos vistas sueltas** una encima de otra:

```xml
<TextView android:text="Altura:" .../>
<EditText android:id="@+id/heigthEdidtTest" .../>
```

Visualmente no hay nada que las agrupe: no comparten fondo, ni borde, ni
padding común. Si el usuario ve una etiqueta y un input separados, el ojo no
los percibe como "una unidad" — es lo primero que delata un layout no
trabajado.

Hay dos formas de arreglarlo, de menos a más trabajo:

---

## 2. Opción A (recomendada): `TextInputLayout` — la etiqueta y el campo son un único componente

En Material Design, el patrón estándar **no es** `TextView` + `EditText`.
Es un solo componente, `TextInputLayout`, que envuelve un
`TextInputEditText` y le pone la etiqueta como **hint flotante**: aparece
dentro del campo cuando está vacío, y "sube" arriba del campo cuando el
usuario escribe. Esto sustituye tu `TextView` de "Altura:" por completo.

```xml
<com.google.android.material.textfield.TextInputLayout
    android:id="@+id/heightInputLayout"
    style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="Altura (cm)"
    app:startIconDrawable="@drawable/ic_height"
    app:boxCornerRadiusTopStart="12dp"
    app:boxCornerRadiusTopEnd="12dp"
    app:boxCornerRadiusBottomStart="12dp"
    app:boxCornerRadiusBottomEnd="12dp">

    <com.google.android.material.textfield.TextInputEditText
        android:id="@+id/heigthEdidtTest"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:inputType="number" />

</com.google.android.material.textfield.TextInputLayout>
```

Por qué funciona mejor:
- **Una sola "caja" visual** (borde redondeado, o relleno según el `style`) hace evidente dónde empieza y termina el campo — eso ya es "verse como una sección".
- El hint flotante evita el salto visual de tener un `TextView` estático encima.
- `app:startIconDrawable` te deja poner un icono (una regla para altura, una báscula para peso) sin maquetar nada extra.
- Los estilos `Widget.Material3.TextInputLayout.*` (`OutlinedBox`, `FilledBox`) ya vienen con el tema Material3 que tu app usa — no hay que definir nada nuevo.

**En Kotlin no cambia casi nada**: `TextInputEditText` extiende `EditText`,
así que tu `findViewById<EditText>(R.id.heigthEdidtTest)` sigue funcionando
igual. Solo cambias el id que buscas si decides quedarte con el mismo id en
el `TextInputEditText` interno (recomendado, para no tocar `MainActivity.kt`).

---

## 3. Opción B: agrupar visualmente con una `MaterialCardView`

Si prefieres mantener `TextView` + `EditText` tal cual (menos cambios en el
XML), puedes conseguir el efecto de "sección" metiendo cada bloque dentro de
una tarjeta:

```xml
<com.google.android.material.card.MaterialCardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="16dp"
    app:cardCornerRadius="16dp"
    app:cardElevation="2dp"
    app:strokeWidth="0dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Altura"
            android:textAppearance="?attr/textAppearanceLabelLarge"
            android:textColor="?attr/colorOnSurfaceVariant" />

        <EditText
            android:id="@+id/heigthEdidtTest"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="Ej. 175"
            android:inputType="number"
            android:background="@null"
            android:textAppearance="?attr/textAppearanceBodyLarge" />

    </LinearLayout>
</com.google.android.material.card.MaterialCardView>
```

Aquí el fondo blanco (o gris muy claro en modo oscuro), el borde redondeado y
la sombra ligera (`cardElevation`) son lo que hace que el ojo lea "Altura" +
el input como **un solo bloque**, no dos elementos sueltos.

Combina bien con la Opción A: puedes meter el `TextInputLayout` de la
sección 2 dentro de una `MaterialCardView` si quieres aún más separación
entre "Altura" y "Peso".

---

## 4. Colocación: usa `ConstraintLayout`, no `LinearLayout` anidado

Ya tienes `androidx.constraintlayout` como dependencia
(`libs.versions.toml:10`) pero el layout actual usa `LinearLayout` anidado
dos veces. Con `LinearLayout` cada `wrap_content`/`match_parent` obliga a
adivinar el resultado según el orden; con `ConstraintLayout` **declaras
relaciones** (este view va debajo de aquel, con tanto margen) y es más fácil
mantener espaciado consistente sin anidar layouts:

```xml
<androidx.constraintlayout.widget.ConstraintLayout
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="24dp">

    <TextView
        android:id="@+id/titleText"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Calculadora IMC"
        android:textAppearance="?attr/textAppearanceHeadlineSmall"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent" />

    <com.google.android.material.textfield.TextInputLayout
        android:id="@+id/heightInputLayout"
        style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginTop="24dp"
        android:hint="Altura (cm)"
        app:layout_constraintTop_toBottomOf="@id/titleText"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/heigthEdidtTest"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:inputType="number" />
    </com.google.android.material.textfield.TextInputLayout>

    <com.google.android.material.textfield.TextInputLayout
        android:id="@+id/weightInputLayout"
        style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:hint="Peso (kg)"
        app:layout_constraintTop_toBottomOf="@id/heightInputLayout"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/weigthEdidtText"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:inputType="numberDecimal" />
    </com.google.android.material.textfield.TextInputLayout>

    <com.google.android.material.button.MaterialButton
        android:id="@+id/calculateButon"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="24dp"
        android:text="Calcular IMC"
        app:layout_constraintTop_toBottomOf="@id/weightInputLayout"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <TextView
        android:id="@+id/resultTestView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="32dp"
        android:gravity="center"
        android:text="00.00"
        android:textAppearance="?attr/textAppearanceDisplaySmall"
        app:layout_constraintTop_toBottomOf="@id/calculateButon"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

Reglas de colocación que sigue este ejemplo:
- **Un margen consistente** entre secciones (24dp entre bloques grandes, 16dp entre campos relacionados — múltiplos de 8dp, el "grid" estándar de Material).
- `android:layout_width="0dp"` + `app:layout_constraintStart/EndOf="parent"` en vez de `match_parent`: en `ConstraintLayout` es la forma correcta de decir "ocupa todo el ancho disponible entre mis constraints".
- El botón usa `MaterialButton` (de la librería Material) en vez de `Button` — ya trae el estilo relleno de Material3 gratis, sin tener que definir un `background` a mano.

---

## 5. Colores y tipografía: usa el tema, no valores sueltos

Ahora mismo `colors.xml` solo tiene `black`/`white`, y el layout no fija
ningún color de texto — hereda el color por defecto del tema. Dos mejoras:

1. **No hardcodees colores.** Usa los *roles* que ya te da `Theme.Material3.DayNight`:
   - `?attr/colorPrimary` → color de marca (botón, acentos).
   - `?attr/colorOnSurface` → texto principal.
   - `?attr/colorOnSurfaceVariant` → texto secundario (labels, hints).
   - `?attr/colorSurface` → fondo de tarjetas.

   Al usar `?attr/...` en vez de `@color/black`, **el modo oscuro
   (`values-night/themes.xml`) se adapta solo**, sin que tengas que duplicar
   layouts ni colores.

2. **No fijes `textSize` a mano.** Usa las escalas de tipografía de Material3
   (`?attr/textAppearanceHeadlineSmall`, `...TitleMedium`, `...BodyLarge`,
   `...LabelLarge`, `...DisplaySmall` para el resultado grande). Son
   consistentes entre sí y ya respetan accesibilidad (escalan con el ajuste
   de tamaño de fuente del sistema, cosa que un `textSize="24sp"` suelto no
   garantiza tan bien si mezclas valores arbitrarios).

Si en algún momento quieres personalizar la marca (por ejemplo un verde para
"saludable" en el resultado), defínelo como override del tema en
`themes.xml`:

```xml
<style name="Base.Theme.CalculadoraIMCAndroid" parent="Theme.Material3.DayNight.NoActionBar">
    <item name="colorPrimary">@color/imc_primary</item>
    <item name="colorOnPrimary">@color/white</item>
</style>
```

y en `colors.xml` añades `imc_primary`. Así el color se propaga a botón,
iconos, etc. automáticamente en vez de tener que cambiarlo campo por campo.

---

## 6. Resumen: qué hace que un campo "se vea como una sección"

| Técnica | Efecto |
|---|---|
| `TextInputLayout` en vez de `TextView` + `EditText` | Label y campo pasan a ser un único componente visual (hint flotante + caja) |
| `MaterialCardView` envolviendo el bloque | Fondo propio + esquinas redondeadas + sombra ligera = bloque claramente delimitado |
| Márgenes consistentes en grid de 8dp | El espacio entre secciones se percibe intencional, no aleatorio |
| `ConstraintLayout` en vez de `LinearLayout` anidado | Controlas la posición relativa sin depender del orden de los hijos |
| Colores/tipografía del tema (`?attr/...`) en vez de valores fijos | Look coherente y funciona igual en modo claro/oscuro sin trabajo extra |
| `MaterialButton` en vez de `Button` | Estilo Material3 (relleno, elevación, ripple) sin definir nada a mano |

Con la Opción A (TextInputLayout) + colores/tipografía del tema ya notarás
un salto grande de "prototipo" a "app cuidada" sin añadir dependencias
nuevas, porque `material:1.14.0` ya está en tu `build.gradle.kts`.

Cuando quieras, dime "aplícalo" y te lo dejo montado directamente en
`activity_main.xml` (y ajusto `MainActivity.kt` si cambian los ids).
