# Cómo se aplicó Material Design a `activity_main.xml`

Este documento explica, paso a paso, los cambios aplicados al layout para
pasar de componentes de Android "planos" a componentes de **Material
Design**, usando la librería que el proyecto ya tenía como dependencia
(`com.google.android.material:material:1.14.0`, ver
`gradle/libs.versions.toml`) y el tema `Theme.Material3.DayNight` que ya
estaba en `themes.xml`. No se ha añadido ninguna dependencia nueva.

Archivo modificado: `app/src/main/res/layout/activity_main.xml`.
`MainActivity.kt` **no se ha tocado** — los cambios se explican más abajo.

---

## 1. Campo "Altura": de `TextView` + `EditText` a `TextInputLayout`

**Antes:**
```xml
<TextView
    android:text="Altura:"
    android:textStyle="bold"
    android:textSize="24sp"/>

<EditText
    android:id="@+id/heigthEdidtText"
    android:hint="Tu altura en centimetros, tarugo"
    android:inputType="number"/>
```

**Después:**
```xml
<com.google.android.material.textfield.TextInputLayout
    style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="Altura (cm)">

    <com.google.android.material.textfield.TextInputEditText
        android:id="@+id/heigthEdidtText"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:inputType="number"/>

</com.google.android.material.textfield.TextInputLayout>
```

**Qué cambia y por qué:**
- `TextInputLayout` sustituye al `TextView` suelto: el `android:hint` ahora
  vive en el `TextInputLayout` y se muestra como **label flotante** (aparece
  dentro del campo cuando está vacío, y "sube" arriba cuando escribes o el
  campo tiene foco).
- `style="@style/Widget.Material3.TextInputLayout.OutlinedBox"` dibuja el
  campo como una caja con borde redondeado — visualmente delimita dónde
  empieza y termina el campo, cosa que un `EditText` suelto no hace.
- El `EditText` pasa a ser `TextInputEditText`, que es una subclase pensada
  para vivir dentro de un `TextInputLayout`.
- **El `android:id` se mantiene igual** (`heigthEdidtText`) y sigue estando
  en el campo de texto, no en el contenedor. Como `TextInputEditText`
  hereda de `EditText`, en `MainActivity.kt` el código
  `lateinit var heigthEdidtText: EditText` y
  `findViewById(R.id.heigthEdidtText)` **siguen funcionando exactamente
  igual**, sin tocar una línea de Kotlin.
- Se quitó el hint gracioso ("Tu altura en centimetros, tarugo"): con
  `TextInputLayout`, el `hint` ya funciona como etiqueta permanente en vez
  de placeholder que desaparece al escribir, así que ese texto no encajaba
  igual. Si se quiere un placeholder adicional (texto de ejemplo que
  desaparece al escribir, además de la etiqueta fija), se puede añadir con
  `app:placeholderText="Ej. 175"` en el `TextInputLayout`.

## 2. Campo "Peso": mismo patrón

Igual que el de altura, con `android:hint="Peso (kg)"` y
`android:layout_marginTop="16dp"` para separarlo del campo de altura (ver
sección 5, espaciado).

## 3. Botón: de `Button` a `MaterialButton`

**Antes:**
```xml
<Button
    android:id="@+id/calculateButon"
    android:text="Calcular IMC"
    android:layout_gravity="center_horizontal"
    android:layout_marginTop="32dp"/>
```

**Después:**
```xml
<com.google.android.material.button.MaterialButton
    android:id="@+id/calculateButon"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Calcular IMC"
    android:layout_gravity="center_horizontal"
    android:layout_marginTop="24dp"
    app:cornerRadius="24dp"/>
```

**Qué cambia y por qué:**
- `MaterialButton` extiende `Button`, así que en Kotlin
  `lateinit var calculateButon: Button` sigue siendo válido — no hace falta
  cambiar el tipo.
- Al estar dentro de un tema Material3, se pinta automáticamente con
  relleno del color primario del tema, elevación sutil y efecto "ripple" al
  pulsar — nada de esto se define a mano.
- `app:cornerRadius="24dp"` redondea las esquinas del botón (por defecto ya
  vienen algo redondeadas por el tema, pero así se controla explícitamente
  cuánto).

## 4. Resultado: envuelto en `MaterialCardView`

**Antes:** `resultTextView` y `categoryTextView` sueltos, uno debajo del
otro, sin fondo ni delimitación.

**Después:**
```xml
<com.google.android.material.card.MaterialCardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginTop="24dp"
    app:cardCornerRadius="16dp"
    app:cardElevation="2dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">

        <TextView android:id="@+id/resultTextView" .../>
        <TextView android:id="@+id/categoryTextView" .../>

    </LinearLayout>

</com.google.android.material.card.MaterialCardView>
```

**Qué cambia y por qué:**
- `MaterialCardView` añade un fondo propio (color de superficie del tema),
  esquinas redondeadas (`cardCornerRadius`) y una sombra ligera
  (`cardElevation`) — eso hace que el número del IMC y su categoría se lean
  como **un bloque de resultado**, no como texto flotando en la pantalla.
- Los dos `TextView` (`resultTextView`, `categoryTextView`) se meten dentro
  de un `LinearLayout` con `padding="16dp"` para que el texto no toque los
  bordes de la tarjeta.
- Los `id` de ambos `TextView` **no cambian**, así que
  `MainActivity.kt` (`resultTextView.text = ...`,
  `categoryTextView.setTextColor(...)`) sigue funcionando sin
  modificaciones.

## 5. Espaciado consistente

Se homogeneizaron los márgenes siguiendo el grid de 8dp que usa Material
Design (8, 16, 24dp...) en vez de valores sueltos como el `32dp` original:

| Entre... | Margen |
|---|---|
| Campo altura → campo peso | `16dp` |
| Campo peso → botón | `24dp` |
| Botón → tarjeta de resultado | `24dp` |
| Borde de la tarjeta → texto interior | `16dp` (padding) |

---

## Por qué `MainActivity.kt` no necesitó cambios

Los tres componentes de Material usados (`TextInputEditText`,
`MaterialButton`, y los `TextView` dentro de `MaterialCardView`) son
**subclases** de los widgets de Android estándar (`EditText`, `Button`,
`TextView` respectivamente). Android usa el mismo mecanismo de `findViewById`
para cualquier `View`, y como los `id` no cambiaron de nombre ni de tipo
declarado en Kotlin, el "puente" entre el XML y el código sigue intacto.
Esto es justo la ventaja de que Material Components esté construido *sobre*
los widgets base de Android en vez de sustituirlos por algo totalmente
distinto.

## Cómo seguir mejorando el diseño desde aquí

- Cambiar el color de fondo de la `MaterialCardView` según la categoría del
  IMC (reutilizando los colores de `colors.xml`), con
  `cardView.setCardBackgroundColor(...)` en Kotlin.
- Añadir un icono a cada `TextInputLayout` con
  `app:startIconDrawable="@drawable/..."` (por ejemplo un icono de regla
  para altura, de báscula para peso).
- Migrar el `LinearLayout` raíz a `ConstraintLayout` si más adelante se
  necesita un posicionamiento más flexible que "todo en columna".
