package com.spgrvl.kissos.legacy.utils

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewTreeObserver
import android.view.Window
import android.view.WindowInsetsController
import android.view.inputmethod.InputMethodManager
import androidx.annotation.FloatRange
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import kotlin.math.roundToInt

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun keyboardVisibleState(): State<Boolean> {
    val keyboardVisible = remember {
        mutableStateOf(false)
    }

    KeyboardListener {
        keyboardVisible.value = it
    }

    return keyboardVisible
}

@Deprecated("Old design system. Use `:ivy-design` and Material3")
fun View.addKeyboardListener(keyboardCallback: (visible: Boolean) -> Unit): () -> Unit {
    var removed = false
    var keyboardVisible: Boolean? = null
    val listener = ViewTreeObserver.OnGlobalLayoutListener {
        val visible = isKeyboardOpen(this)
        // The observer is hit quite often, so only call back when there is a change
        if (visible != keyboardVisible) {
            keyboardVisible = visible
            keyboardCallback(visible)
        }
    }
    // The initial state, as soon as the layout is set
    doOnLayout { if (!removed) listener.onGlobalLayout() }
    viewTreeObserver.addOnGlobalLayoutListener(listener)
    return {
        removed = true
        viewTreeObserver.removeOnGlobalLayoutListener(listener)
    }
}

/**
 * Calls [onChange] when the keyboard shows or hides, for as long as this is composed.
 * Unlike registering with [addKeyboardListener] on every screen start, this doesn't pile up listeners
 * that run on every layout for the rest of the app's life.
 */
@Composable
fun KeyboardListener(onChange: (visible: Boolean) -> Unit) {
    val rootView = LocalView.current
    val latestOnChange by rememberUpdatedState(onChange)
    DisposableEffect(rootView) {
        val remove = rootView.addKeyboardListener { latestOnChange(it) }
        onDispose { remove() }
    }
}

@Deprecated("Old design system. Use `:ivy-design` and Material3")
fun isKeyboardOpen(rootView: View): Boolean {
    return try {
        WindowInsetsCompat.toWindowInsetsCompat(rootView.rootWindowInsets, rootView)
            .isVisible(WindowInsetsCompat.Type.ime())
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

@Deprecated("Old design system. Use `:ivy-design` and Material3")
fun convertDpToPixel(context: Context, dp: Float): Float {
    return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

@Deprecated("Old design system. Use `:ivy-design` and Material3")
fun convertDpToPixel(context: Context, dp: Int): Int {
    return convertDpToPixel(context, dp.toFloat()).roundToInt()
}

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@SuppressLint("ComposableNaming")
@Composable
fun setStatusBarDarkTextCompat(darkText: Boolean) {
    setStatusBarDarkTextCompat(
        view = LocalView.current,
        darkText = darkText
    )
}

@Deprecated("Old design system. Use `:ivy-design` and Material3")
fun setStatusBarDarkTextCompat(view: View, darkText: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        view.windowInsetsController?.setStatusBarDarkText(darkText)
    } else {
        val window = (view.context as Activity).window
        setStatusBarDarkTextOld(window, darkText)
    }
}

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@RequiresApi(Build.VERSION_CODES.R)
fun WindowInsetsController.setStatusBarDarkText(darkText: Boolean) {
    setSystemBarsAppearance(
        if (darkText) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
    )
}

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Suppress("DEPRECATION")
fun setStatusBarDarkTextOld(window: Window, darkText: Boolean) {
    window.decorView.systemUiVisibility = if (darkText) {
        window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // set status bar dark text
    } else {
        window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv() // reset status bar dark text
    }
}

@Deprecated("Old design system. Use `:ivy-design` and Material3")
fun lerp(start: Int, end: Int, @FloatRange(from = 0.0, to = 1.0) fraction: Float): Int {
    return ((start + fraction * (end - start)).roundToInt())
}

@Deprecated("Old design system. Use `:ivy-design` and Material3")
fun lerp(start: Float, end: Float, @FloatRange(from = 0.0, to = 1.0) fraction: Float): Float {
    return (start + fraction * (end - start))
}

@Deprecated("Old design system. Use `:ivy-design` and Material3")
fun lerp(start: Double, end: Double, @FloatRange(from = 0.0, to = 1.0) fraction: Double): Double {
    return (start + fraction * (end - start))
}

@Deprecated("Old design system. Use `:ivy-design` and Material3")
fun colorLerp(start: Color, end: Color, fraction: Float): Color {
    return Color(ArgbEvaluator().evaluate(fraction, start.toArgb(), end.toArgb()) as Int)
}

@Deprecated("Old design system. Use `:ivy-design` and Material3")
fun hideKeyboard(view: View) {
    try {
        val imm: InputMethodManager =
            view.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    } catch (ignore: Exception) {
    }
}
