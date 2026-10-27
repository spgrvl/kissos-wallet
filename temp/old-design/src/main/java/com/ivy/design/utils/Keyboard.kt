package com.ivy.design.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout

@Deprecated("Old design system. Use `:ivy-design` and Material3")
fun View.showKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_FORCED)
}

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@SuppressLint("ComposableNaming")
@Composable
fun hideKeyboard() {
    LocalView.current.hideKeyboard()
}

fun View.hideKeyboard() {
    val imm: InputMethodManager =
        context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

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
