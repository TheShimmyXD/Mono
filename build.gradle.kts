// Los plugins se declaran aquí una vez (apply false) y cada módulo aplica los suyos.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
