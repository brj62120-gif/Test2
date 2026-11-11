package com.my.keyboard

import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout

class KeyboardService : InputMethodService() {
    private var bangla = false
    private var shift = false
    private var symbols = false
    private lateinit var root: LinearLayout

    private val enRows = listOf(
        "1 2 3 4 5 6 7 8 9 0",
        "q w e r t y u i o p",
        "a s d f g h j k l",
        "z x c v b n m"
    )
    private val bnRows = listOf(
        "ও ঔ এ ঐ অ আ ই ঈ উ ঊ",
        "ক খ গ ঘ ঙ চ ছ জ ঝ ঞ",
        "ট ঠ ড ঢ ণ ত থ দ ধ ন",
        "প ফ ব ভ ম য র ল শ ষ",
        "স হ ড় ঢ় য় ৎ ং ঃ ঁ ্",
        "া ি ী ু ূ ৃ ে ৈ ো ৌ"
    )
    private val symRows = listOf(
        "1 2 3 4 5 6 7 8 9 0",
        "@ # \$ % & - + ( ) /",
        "* \" ' : ; ! ? _ = ~"
    )

    override fun onCreateInputView(): View {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF1E1E1E.toInt())
            setPadding(dp(3), dp(4), dp(3), dp(4))
        }
        render()
        return root
    }

    private fun render() {
        root.removeAllViews()
        val rows = if (symbols) symRows else if (bangla) bnRows else enRows
        val letters = !symbols && !bangla
        for ((i, r) in rows.withIndex()) {
            val line = newLine()
            val lastEn = letters && i == rows.lastIndex
            if (lastEn) {
                line.addView(key(if (shift) "⬆" else "⇧", 1.5f) {
                    shift = !shift
                    render()
                })
            }
            for (k in r.split(" ")) {
                val t = if (shift && letters) k.uppercase() else k
                line.addView(key(t) { type(t) })
            }
            root.addView(line)
        }
        val bottom = newLine()
        bottom.addView(key(if (symbols) "ABC" else "?123", 1.4f) {
            symbols = !symbols
            render()
        })
        bottom.addView(key(if (bangla) "EN" else "বাং", 1.4f) {
            bangla = !bangla
            symbols = false
            shift = false
            render()
        })
        bottom.addView(key(",", 1f) { type(",") })
        bottom.addView(key(if (bangla) "স্পেস" else "space", 3.5f) { type(" ") })
        bottom.addView(key(if (bangla) "।" else ".", 1f) { type(if (bangla) "।" else ".") })
        bottom.addView(key("⌫", 1.4f) { sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL) })
        bottom.addView(key("↵", 1.4f) { enter() })
        root.addView(bottom)
    }

    private fun type(s: String) {
        currentInputConnection?.commitText(s, 1)
        if (shift) {
            shift = false
            render()
        }
    }

    private fun enter() {
        if (!sendDefaultEditorAction(true)) {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
        }
    }

    private fun newLine() = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun key(label: String, weight: Float = 1f, onTap: () -> Unit): Button =
        Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 17f
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFF3A3A3A.toInt())
            minWidth = 0
            minimumWidth = 0
            minHeight = 0
            minimumHeight = 0
            setPadding(0, 0, 0, 0)
            layoutParams = LinearLayout.LayoutParams(0, dp(44), weight).apply {
                setMargins(dp(2), dp(2), dp(2), dp(2))
            }
            setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onTap()
            }
        }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
