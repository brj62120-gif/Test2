package com.my.keyboard

import android.content.res.Configuration
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class KeyboardService : InputMethodService() {
    private var bangla = false
    private var shift = false
    private var caps = false
    private var lastShiftTap = 0L
    private var mode = 0 // 0 = অক্ষর, 1 = symbol 1/2, 2 = symbol 2/2
    private var bnPage = 0
    private var dark = false
    private var emojiMode = false
    private var emojiCat = 1
    private lateinit var root: LinearLayout
    private val uiHandler = Handler(Looper.getMainLooper())
    private val emojiCache = HashMap<Int, List<String>>()

    private val enRows = listOf(
        "1 2 3 4 5 6 7 8 9 0",
        "q w e r t y u i o p",
        "a s d f g h j k l",
        "z x c v b n m"
    )

    private val bnLeft = listOf(
        listOf("অ আ ই ঈ", "উ ঊ ঋ এ ঐ", "ও ঔ অং|ং অঃ|ঃ অঁ|ঁ"),
        listOf("্ া ি ী", "ু ূ ৃ ে ৈ", "ো ৌ ং ঃ ঁ"),
        listOf("ড় ঢ় য় ৎ", "স্ক ঞ্জ জ্ঞ স্ক্র ক্ট", "ক্ত ক্র ক্ল ক্ষ গ্ধ"),
        listOf("হ্ম ত্র অ্যা ক্ষ", "শ্র র্ ঙ্ক ঙ্গ ঞ্চ", "্ব ্য ্ম ্ন ্র")
    )
    private val bnRight = listOf(
        "ক খ গ ঘ ঙ",
        "চ ছ জ ঝ ঞ",
        "ট ঠ ড ঢ ণ",
        "ত থ দ ধ ন প ফ ব ভ ম",
        "য র ল শ ষ স হ ৎ"
    )

    private val enSym = listOf(
        listOf("1 2 3 4 5 6 7 8 9 0", "+ × ÷ = / _ € £ ¥ ₩", "! @ # \$ % ^ & * ( )", "- ' \" : ; , ?"),
        listOf("1 2 3 4 5 6 7 8 9 0", "` ~ \\ | < > { } [ ]", "° • ○ ● □ ■ ♤ ♡ ◇ ♧", "☆ ▪ ¤ 《 》 ¡ ¿")
    )
    private val bnSym = listOf(
        listOf("1 2 3 4 5 6 7 8 9 0", "+ × ÷ = / _ € £ ₹ ₩", "! @ # \$ % ^ & * ( )", "- ' \" : ; , ?"),
        listOf("১ ২ ৩ ৪ ৫ ৬ ৭ ৮ ৯ ০", "৲ ৳ \\ | < > { } [ ]", "ঽ ৺ ° । ॥ ‘ ’ “ ” …", "ZWNJ|\u200C ZWJ|\u200D ¤ ॐ + ~ `")
    )

    // ---------- Emoji ----------
    private val emojiTabs = listOf("🕘", "😀", "👍", "🐶", "🍔", "⚽", "🚗", "💡", "❤️", "🏳️")

    private val emojiRanges: List<List<IntRange>> = listOf(
        // 1 মুখ
        listOf(0x1F600..0x1F64F, 0x1F910..0x1F917, 0x1F920..0x1F92F, 0x1F970..0x1F97F, 0x1FAE0..0x1FAE8),
        // 2 হাত ও মানুষ
        listOf(
            0x1F440..0x1F487, 0x1F90C..0x1F90C, 0x1F90F..0x1F90F, 0x1F918..0x1F91F,
            0x1F930..0x1F939, 0x1F9B0..0x1F9BF, 0x1F9D0..0x1F9DF, 0x1F6B6..0x1F6B6,
            0x1F6C0..0x1F6C0, 0x1FAC0..0x1FAC5, 0x1FAF0..0x1FAF8
        ),
        // 3 প্রাণী ও প্রকৃতি
        listOf(
            0x1F300..0x1F32C, 0x1F330..0x1F335, 0x1F337..0x1F344, 0x1F400..0x1F43F,
            0x1F980..0x1F9AF, 0x1FAB0..0x1FABF
        ),
        // 4 খাবার
        listOf(
            0x1F32D..0x1F32F, 0x1F345..0x1F37F, 0x1F950..0x1F96F, 0x1F9C0..0x1F9CB,
            0x1FAD0..0x1FADB, 0x2615..0x2615
        ),
        // 5 খেলা ও উৎসব
        listOf(
            0x1F380..0x1F393, 0x1F3A0..0x1F3D3, 0x1F3F8..0x1F3F8, 0x1F93A..0x1F94F,
            0x26BD..0x26BE, 0x26F3..0x26F3, 0x26F8..0x26F8
        ),
        // 6 ভ্রমণ ও স্থান
        listOf(
            0x1F3D4..0x1F3F0, 0x1F680..0x1F6C5, 0x1F6D0..0x1F6D7, 0x1F6E0..0x1F6EC,
            0x1F6F0..0x1F6FC, 0x26EA..0x26EA, 0x26F2..0x26F2, 0x26F4..0x26F5,
            0x26FA..0x26FA, 0x26FD..0x26FD, 0x2708..0x2708
        ),
        // 7 জিনিসপত্র
        listOf(
            0x1F4A1..0x1F4FD, 0x1F4FF..0x1F4FF, 0x1F507..0x1F517, 0x1F526..0x1F52E,
            0x1F9E7..0x1F9FF, 0x1FA70..0x1FAAF, 0x231A..0x231B, 0x23F0..0x23F3, 0x260E..0x260E
        ),
        // 8 হার্ট ও চিহ্ন
        listOf(
            0x1F493..0x1F49F, 0x1F90D..0x1F90E, 0x1F500..0x1F506, 0x1F518..0x1F53D,
            0x1F7E0..0x1F7EB, 0x1F191..0x1F19A, 0x2600..0x26FF, 0x2700..0x27BF,
            0x2B05..0x2B07, 0x2B1B..0x2B1C, 0x2B50..0x2B50, 0x2B55..0x2B55,
            0x2934..0x2935, 0x203C..0x203C, 0x2049..0x2049, 0x2122..0x2122,
            0x2139..0x2139, 0x2194..0x2199, 0x21A9..0x21AA, 0x24C2..0x24C2,
            0x25AA..0x25AB, 0x25B6..0x25B6, 0x25C0..0x25C0, 0x25FB..0x25FE
        )
    )

    private val flagCodes = (
        "BD IN PK NP LK BT MV AF CN JP KR KP TW HK SG MY TH VN ID PH MM KH LA MN " +
        "US CA MX BR AR CL CO PE VE UY PY BO EC CU DO JM GB IE FR DE IT ES PT NL BE " +
        "CH AT SE NO DK FI IS PL CZ SK HU RO BG GR TR RU UA BY RS HR SI BA AL MK ME " +
        "LT LV EE CY MT LU SA AE QA KW BH OM YE IQ IR IL PS JO LB SY EG LY TN DZ MA " +
        "SD ET KE TZ UG NG GH ZA CD CM SN CI ZW ZM AO MZ MG AU NZ FJ PG UN EU"
        ).split(" ")

    private fun has(p: Paint, s: String) = Build.VERSION.SDK_INT < 23 || p.hasGlyph(s)

    private fun buildEmojis(ranges: List<IntRange>): List<String> {
        val p = Paint()
        val out = LinkedHashSet<String>()
        for (r in ranges) {
            for (cp in r) {
                val s = String(Character.toChars(cp))
                if (cp < 0x10000) {
                    val v = s + "\uFE0F"
                    if (has(p, v)) out.add(v) else if (has(p, s)) out.add(s)
                } else if (has(p, s)) {
                    out.add(s)
                }
            }
        }
        return out.toList()
    }

    private fun buildFlags(): List<String> {
        val p = Paint()
        return flagCodes.map { c ->
            String(Character.toChars(0x1F1E6 + (c[0] - 'A'))) +
                String(Character.toChars(0x1F1E6 + (c[1] - 'A')))
        }.filter { has(p, it) }
    }

    private fun emojiList(cat: Int): List<String> =
        emojiCache.getOrPut(cat) {
            if (cat == 9) buildFlags() else buildEmojis(emojiRanges[cat - 1])
        }

    // ---------- রং ----------
    private fun bgColor() = if (dark) 0xFF1B1B1B.toInt() else 0xFFDADADA.toInt()
    private fun keyBg() = if (dark) 0xFF3C3C3C.toInt() else 0xFFFFFFFF.toInt()
    private fun fnBg() = if (dark) 0xFF2A2A2A.toInt() else 0xFFEBEBEB.toInt()
    private fun pressBg() = if (dark) 0xFF5A5A5A.toInt() else 0xFFC4C4C4.toInt()
    private fun textCol() = if (dark) 0xFFF2F2F2.toInt() else 0xFF1A1A1A.toInt()

    override fun onCreateInputView(): View {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(4), dp(6), dp(4), dp(10))
        }
        render()
        return root
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        if (emojiMode && ::root.isInitialized) {
            emojiMode = false
            render()
        }
    }

    private fun render() {
        dark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        root.setBackgroundColor(bgColor())
        root.removeAllViews()
        if (emojiMode) {
            renderEmoji()
            return
        }
        root.addView(toolbarRow())
        when {
            mode != 0 -> renderSymbols()
            bangla -> renderBangla()
            else -> renderEnglish()
        }
        root.addView(bottomRow())
    }

    private fun toolbarRow(): LinearLayout {
        val line = newLine()
        line.addView(key("😊", 1f, flat = true, hDp = 40, sp = 22f) {
            emojiMode = true
            render()
        })
        line.addView(spacer(8f, 40))
        return line
    }

    private fun renderEmoji() {
        val tabs = newLine()
        for ((i, ic) in emojiTabs.withIndex()) {
            tabs.addView(key(ic, 1f, fn = i != emojiCat, hDp = 40, sp = 17f) {
                emojiCat = i
                render()
            })
        }
        root.addView(tabs)

        val items = if (emojiCat == 0) loadRecent() else emojiList(emojiCat)
        val scroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(200)
            )
            isVerticalScrollBarEnabled = false
        }
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        if (items.isEmpty()) {
            col.addView(TextView(this).apply {
                text = "এখনো কোনো emoji ব্যবহার করা হয়নি"
                setTextColor(textCol())
                textSize = 14f
                gravity = Gravity.CENTER
                setPadding(0, dp(60), 0, 0)
            })
        }
        for (chunk in items.chunked(8)) {
            val line = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            for (e in chunk) line.addView(emojiCell(e))
            repeat(8 - chunk.size) { line.addView(spacer(1f, 44)) }
            col.addView(line)
        }
        scroll.addView(col)
        root.addView(scroll)

        val b = newLine()
        b.addView(key("ABC", 1.5f, fn = true) {
            emojiMode = false
            render()
        })
        b.addView(key("space", 5f) { type(" ") })
        b.addView(backKey(1.5f))
        b.addView(key("↵", 1.5f, fn = true) { enter() })
        root.addView(b)
    }

    private fun emojiCell(e: String) = TextView(this).apply {
        text = e
        textSize = 24f
        gravity = Gravity.CENTER
        layoutParams = LinearLayout.LayoutParams(0, dp(44), 1f)
        setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            currentInputConnection?.commitText(e, 1)
            addRecent(e)
        }
    }

    private fun loadRecent(): List<String> {
        val s = getSharedPreferences("kb", MODE_PRIVATE).getString("recent", "") ?: ""
        return if (s.isBlank()) emptyList() else s.split(" ")
    }

    private fun addRecent(e: String) {
        val list = loadRecent().toMutableList()
        list.remove(e)
        list.add(0, e)
        getSharedPreferences("kb", MODE_PRIVATE).edit()
            .putString("recent", list.take(32).joinToString(" "))
            .apply()
    }

    // ---------- অক্ষরের keyboard ----------
    private fun renderEnglish() {
        for ((i, r) in enRows.withIndex()) {
            val line = newLine()
            if (i == 2) line.addView(spacer(0.5f))
            if (i == 3) line.addView(shiftKey())
            for (k in r.split(" ")) {
                val t = if (shift && i > 0) k.uppercase() else k
                line.addView(key(t) { type(t) })
            }
            if (i == 2) line.addView(spacer(0.5f))
            if (i == 3) line.addView(backKey(1.5f))
            root.addView(line)
        }
    }

    private fun renderBangla() {
        val left = bnLeft[bnPage]
        var line = newLine()
        line.addView(key("০-|", fn = true) {
            bnPage = (bnPage + 1) % bnLeft.size
            render()
        })
        addKeys(line, left[0], true)
        addKeys(line, bnRight[0])
        root.addView(line)

        for (i in 1..2) {
            line = newLine()
            addKeys(line, left[i], true)
            addKeys(line, bnRight[i])
            root.addView(line)
        }

        line = newLine()
        addKeys(line, bnRight[3])
        root.addView(line)

        line = newLine()
        addKeys(line, bnRight[4])
        line.addView(backKey(2f))
        root.addView(line)
    }

    private fun renderSymbols() {
        val rows = (if (bangla) bnSym else enSym)[mode - 1]
        for (i in 0..2) {
            val line = newLine()
            addKeys(line, rows[i])
            root.addView(line)
        }
        val line = newLine()
        line.addView(key(if (mode == 1) "1/2" else "2/2", 1.5f, fn = true) {
            mode = if (mode == 1) 2 else 1
            render()
        })
        addKeys(line, rows[3])
        line.addView(backKey(1.5f))
        root.addView(line)
    }

    private fun bottomRow(): LinearLayout {
        val line = newLine()
        if (mode != 0) {
            line.addView(key(if (bangla) "কখগ" else "ABC", 1.5f, fn = true) {
                mode = 0
                render()
            })
        } else {
            line.addView(key("!#1", 1.5f, fn = true) {
                mode = 1
                render()
            })
        }
        line.addView(key(",") { type(",") })
        line.addView(
            key(if (bangla) "◂  বাংলা  ▸" else "◂  English (US)  ▸", 5f, onLong = { toggleLang() }) {
                type(" ")
            }
        )
        line.addView(key(if (bangla) "।" else ".") { type(if (bangla) "।" else ".") })
        line.addView(key("↵", 1.5f, fn = true) { enter() })
        return line
    }

    private fun toggleLang() {
        bangla = !bangla
        mode = 0
        shift = false
        caps = false
        render()
    }

    private fun shiftKey(): Button =
        key(if (caps) "⇪" else "⇧", 1.5f, fn = true, accent = shift) {
            val now = SystemClock.uptimeMillis()
            when {
                caps -> {
                    caps = false
                    shift = false
                }
                now - lastShiftTap < 400 -> {
                    caps = true
                    shift = true
                }
                else -> shift = !shift
            }
            lastShiftTap = now
            render()
        }

    private fun backKey(w: Float): Button =
        key("⌫", w, fn = true, repeat = true) { sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL) }

    private fun addKeys(line: LinearLayout, spec: String, marks: Boolean = false) {
        for (tok in spec.split(" ")) {
            val i = tok.indexOf('|')
            val commit = if (i > 0) tok.substring(i + 1) else tok
            var label = if (i > 0) tok.substring(0, i) else tok
            if (marks && i <= 0 && label[0] in "ািীুূৃেৈোৌ্ংঃঁ") label = "◌" + label
            line.addView(key(label) { type(commit) })
        }
    }

    private fun type(s: String) {
        currentInputConnection?.commitText(s, 1)
        if (shift && !caps) {
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

    private fun spacer(w: Float, hDp: Int = 48) = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(0, dp(hDp), w)
    }

    private fun bg(color: Int): StateListDrawable {
        fun shape(c: Int) = GradientDrawable().apply {
            cornerRadius = dp(8).toFloat()
            setColor(c)
        }
        return StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), shape(pressBg()))
            addState(intArrayOf(), shape(color))
        }
    }

    private fun key(
        label: String,
        weight: Float = 1f,
        fn: Boolean = false,
        accent: Boolean = false,
        repeat: Boolean = false,
        flat: Boolean = false,
        hDp: Int = 48,
        sp: Float = 0f,
        onLong: (() -> Unit)? = null,
        onTap: () -> Unit
    ): Button = Button(this).apply {
        text = label
        isAllCaps = false
        textSize = if (sp > 0f) sp else when {
            label.length <= 1 -> 20f
            label.length <= 3 -> 17f
            else -> 14f
        }
        setTextColor(if (accent) 0xFF2F7CF6.toInt() else textCol())
        background = bg(if (flat) bgColor() else if (fn) fnBg() else keyBg())
        stateListAnimator = null
        minWidth = 0
        minimumWidth = 0
        minHeight = 0
        minimumHeight = 0
        setPadding(0, 0, 0, 0)
        layoutParams = LinearLayout.LayoutParams(0, dp(hDp), weight).apply {
            setMargins(dp(2), dp(3), dp(2), dp(3))
        }
        if (repeat) {
            val r = object : Runnable {
                override fun run() {
                    onTap()
                    uiHandler.postDelayed(this, 45)
                }
            }
            setOnTouchListener { v, e ->
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        v.isPressed = true
                        v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onTap()
                        uiHandler.postDelayed(r, 400)
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        v.isPressed = false
                        uiHandler.removeCallbacks(r)
                    }
                }
                true
            }
        } else {
            setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onTap()
            }
            if (onLong != null) {
                setOnLongClickListener {
                    onLong()
                    true
                }
            }
        }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
