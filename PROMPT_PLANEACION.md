# Prompt de planeación — Proyecto Mono

*2026-10-06 · Carpeta: `0_SP_Codes/Project_Mono`*

Este texto es el encargo original, con las palabras del autor. Sirve para volver a planear desde cero en cualquier conversación: se pega tal cual y se pide «arma el plan». El plan vigente está en `PLAN_MONO.md`.

---

## Contexto

> «Tengo android studio, un xiaomi redmi note 13 pro y una idea. me gustaria crear un juego como el tio rico o el monopoly pero para celulares. lo nuevo aca es la personalizacion de las locaciones, cosas como nombres, precios, etc se pueden editar, las reglas igual, hasta el tamaño del mapa, el caso es que te doy estos dos juegos de mesa populares para que extraigas las reglas basicas y crees un motor de logica basado en ellos. y la otra funcionalidad nueva es que si un amigo se instala la app, pueda conectarse por bt conmigo y jugar conmigo» (autor, 2026-10-06)

Los reglamentos: «tienes los dos en mi carpeta de Descargas» (`Monopoly(Spanish).pdf`, 8 pp., y `tio_rico.pdf`, «Tío Rico de Lujo», 1 página; los dos son escaneos sin texto).

## Qué quiero

1. Un juego de propiedades para celular, como el Tío Rico o el Monopoly.
2. Un motor de lógica basado en las reglas básicas extraídas de los dos reglamentos.
3. Personalización: nombres y precios de las locaciones, las reglas y el tamaño del mapa se pueden editar.
4. Que un amigo con la app instalada se conecte por Bluetooth y juegue conmigo.

## Restricciones

- Pila: Android Studio, Kotlin + Jetpack Compose (elegida por el autor). Teléfono de pruebas: Xiaomi Redmi Note 13 Pro.
- Duración: meses (largo). Primero el juego local en un teléfono (varias personas pasándose el teléfono); el Bluetooth después.
- 2-6 jugadores; tablero en anillo de N casillas.
- Sin un segundo Android a mano para probar el Bluetooth; conoce poco Kotlin y Android: que se expliquen las decisiones.
- Nada sensible; los reglamentos son de solo lectura.

## Qué debe entregar la planeación

1. Un **nombre clave**.
2. **Visión, alcance y un hito** que diga «ya funciona».
3. **Plan de trabajo por fases**, con casillas e ID por tarea y un criterio *Terminado* comprobable en cada una.
4. **La skill del agente** y **la skill del observador**, adaptadas de los proyectos anteriores (Azorian (../../0_Python_Codes/Ogata_lib) como hermano).
5. Las **decisiones técnicas** iniciales, las **reglas del proyecto**, los **riesgos** y el **primer paso exacto**.
6. Las **preguntas que solo el autor puede responder**, marcadas como tales.
