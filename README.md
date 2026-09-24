# Calculadora IMC Android

App Android nativa, escrita en Kotlin, que calcula el Índice de Masa
Corporal (IMC) a partir de la altura y el peso introducidos por el usuario,
y clasifica el resultado según los rangos estándar de la OMS.

## Funcionalidad

- Introduce altura (cm) y peso (kg).
- Calcula el IMC: `peso / altura(m)²`.
- Muestra el resultado con 2 decimales.
- Clasifica el resultado y lo colorea según la categoría:

  | Rango de IMC | Categoría | Color |
  |---|---|---|
  | < 18.5 | Bajo peso | Azul |
  | 18.5 – 24.9 | Peso saludable | Verde |
  | 25 – 29.9 | Sobrepeso | Ámbar |
  | ≥ 30 | Obesidad | Rojo |

- Valida los campos: si están vacíos o contienen un valor no numérico o ≤ 0,
  marca el campo correspondiente con un error en vez de crashear.
- Limpia los campos de altura y peso tras cada cálculo válido.

## Stack técnico

- Kotlin
- Android Views (XML) + `AppCompatActivity`
- Tema `Theme.Material3.DayNight` (Material Components)
- Sin dependencias externas más allá de AndroidX/Material

## Requisitos

- Android Studio (última versión estable)
- `compileSdk` / `targetSdk` 37, `minSdk` 30

## Cómo ejecutarlo

1. Clona el repositorio.
2. Ábrelo en Android Studio y deja que sincronice Gradle.
3. Ejecuta la app (`Run ▶`) en un emulador o dispositivo con Android 11 (API 30) o superior.

O desde línea de comandos:

```bash
./gradlew installDebug
```

## Estructura del proyecto

```
app/src/main/java/.../MainActivity.kt   -> Lógica: validación, cálculo y clasificación del IMC
app/src/main/res/layout/activity_main.xml -> Layout de la pantalla principal
app/src/main/res/values/colors.xml      -> Colores, incluidos los de cada categoría de IMC
```
