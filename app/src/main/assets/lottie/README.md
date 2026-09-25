# Animaciones Lottie de la Calculadora IMC

Coloca aquí los 4 archivos `.json` de Lottie (descargados de
[LottieFiles](https://lottiefiles.com), o de la fuente que elijas),
**renombrados exactamente así** para que `MainActivity.kt` los encuentre:

| Archivo esperado | Categoría | Se muestra cuando... |
|---|---|---|
| `bajo_peso.json` | Bajo peso | IMC < 18.5 |
| `saludable.json` | Peso saludable | 18.5 ≤ IMC < 25 |
| `sobrepeso.json` | Sobrepeso | 25 ≤ IMC < 30 |
| `obesidad.json` | Obesidad | IMC ≥ 30 |

Mientras un archivo no exista, la app no crashea: simplemente no muestra
ninguna animación para esa categoría (el fallo se captura en
`playCategoryAnimation()` en `MainActivity.kt`).

Animaciones sugeridas encontradas en LottieFiles (revisa tú la licencia de
cada una antes de descargarla):

- Saludable → ["Check mark - Success animation" de Victor Winnhed](https://lottiefiles.com/free-animation/check-mark-success-animation-mXuKRByCaH)
- Sobrepeso → colección ["Warning"](https://lottiefiles.com/free-animations/warning)
- Obesidad → ["Siren Alert" de Rooman Akhlaq](https://lottiefiles.com/animation/siren-alert-11853211)
- Bajo peso → ["Healthy food for diet & fitness" de Haikal](https://lottiefiles.com/free-animation/healthy-food-for-diet-fitness-09b8bqFvrl) (o cualquier otra que encaje mejor)
