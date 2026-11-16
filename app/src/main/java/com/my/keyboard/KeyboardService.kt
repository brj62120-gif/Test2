package com.my.keyboard

import android.animation.ValueAnimator
import android.content.ClipboardManager
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.SoundEffectConstants
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView

class KeyboardService : InputMethodService() {
    private val MP = LinearLayout.LayoutParams.MATCH_PARENT
    private val WC = LinearLayout.LayoutParams.WRAP_CONTENT

    private var lang = 0 // 0 = English, 1 = বাংলা (Samsung layout অথবা Avro)
    private var shift = false
    private var caps = false
    private var shiftManual = false // ইউজার নিজে shift চাপলে auto-capitalization থেমে থাকে
    private var lastShiftTap = 0L
    private var mode = 0 // 0 = অক্ষর, 1 = symbol 1/2, 2 = symbol 2/2
    private var bnPage = 0
    private var bnCtx = false // true = ব্যঞ্জনের পর, কার দেখানো হচ্ছে
    private var backHeld = false // ব্যাকস্পেস চেপে ধরা থাকলে লেআউট আঁকা বন্ধ রাখা হয়
    private var dark = false
    // 0 = none, 1 = emoji, 2 = clipboard, 3 = text editing, 4 = settings, 5 = more, 6 = emoji search, 7 = modes
    private var panel = 0
    private var emojiCat = 1
    private var emojiSlide = 0
    private var emojiRipple = false
    private var selecting = false
    private var barMode = 0 // 0 = auto (tools), 1 = tools, 2 = suggestions
    private var barManual = false
    private var lastShowSug = false
    private var barFresh = true
    private var panelSug: List<String> = emptyList()
    private var sugExpanded = false // suggestion bar এর "..." চাপলে বড় suggestion + Auto replace প্যানেল

    private var themeMode = 0
    private var heightSet = 1
    private var haptic = true
    private var sound = false
    private var suggestOn = true
    private var kbMode = 0 // 0 = Standard, 1 = Left hand, 2 = Right hand, 3 = Split, 4 = Compact
    private var clipOn = true
    private var clipExpire = 0 // 0 = never, 1 = 1 day, 2 = 7 days, 3 = 30 days
    private var autoReplace = false
    private var autoCap = true
    private var previewOn = true
    private var translit = false // true = বাংলা মানে Avro phonetic (QWERTY দিয়ে)
    private var avroDigits = true

    private lateinit var host: FrameLayout
    private lateinit var overlay: FrameLayout
    private lateinit var root: LinearLayout
    private var sugBox: LinearLayout? = null
    private var leftIcon: IconView? = null
    private var spaceKey: View? = null
    private var previewView: TextView? = null
    private var langPopup: TextView? = null
    private var toastView: TextView? = null
    private val uiHandler = Handler(Looper.getMainLooper())
    private val hidePreviewRunnable = Runnable { previewView?.visibility = View.GONE }
    private val hideLangRunnable = Runnable {
        val lp = langPopup
        if (lp != null) {
            lp.animate().alpha(0f).setDuration(150).withEndAction { lp.visibility = View.GONE }.start()
        }
    }
    private val hideToastRunnable = Runnable {
        val tv = toastView
        if (tv != null) {
            tv.animate().alpha(0f).setDuration(200).withEndAction { tv.visibility = View.GONE }.start()
        }
    }
    private val emojiCache = HashMap<Int, List<String>>()
    private val learned = HashMap<String, Int>()
    private var learnedLoaded = false
    private val bigrams = HashMap<String, Int>()
    private var bigramsLoaded = false

    // letter keys that show a preview bubble (used for finger sliding between keys)
    private class PreviewInfo(val label: String, val onTap: () -> Unit)

    private val previewKeys = ArrayList<View>()
    private val curKeyFor = HashMap<View, View?>()

    // emoji search
    private val searchQ = StringBuilder()
    private var searchCard: LinearLayout? = null
    private var searchTv: TextView? = null
    private var searchClear: View? = null
    private var searchIndex: List<Pair<String, String>>? = null

    private class Clip(var text: String, var locked: Boolean, val time: Long)

    private val clips = ArrayList<Clip>()
    private var menuClip: Clip? = null
    private var clipFilter = 0 // 0 = All, 1 = Locked, 2 = Links, 3 = Numbers
    private var selMode = false
    private val selected = HashSet<Clip>()
    private var confirmAll = false

    // ---- Avro composing state ----
    private val avroBuf = StringBuilder()
    private var lastAvroTime = 0L

    // ---- Toolbar items (Samsung style: Touch and hold to reorder) ----
    private class Tool(val id: String, val kind: Int, val label: String)

    private val allTools = listOf(
        Tool("emoji", 1, "Emoji"),
        Tool("edit", 3, "Text editing"),
        Tool("clip", 2, "Clipboard"),
        Tool("modes", 19, "Modes"),
        Tool("settings", 31, "Settings"),
        Tool("translit", 32, "Transliterate")
    )
    private var tbBar = mutableListOf("emoji", "edit", "clip") // top bar এ যা দেখাবে
    private var tbPanel = mutableListOf("modes", "settings", "translit") // "..." প্যানেলে যা থাকবে
    private val maxBarTools = 6
    private var moreArea: LinearLayout? = null
    private val moreTiles = ArrayList<Pair<View, String>>()
    private var ghost: IconView? = null
    private var dragSrc: View? = null

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

    // ================= Icons (drawn by hand) =================
    private inner class IconView(var kind: Int) : View(this@KeyboardService) {
        var tint = 0
        var cutColor = 0
        private val p = Paint(Paint.ANTI_ALIAS_FLAG)
        private val q = Paint(Paint.ANTI_ALIAS_FLAG)

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val u = minOf(width, height) / 40f
            val cx = width / 2f
            val cy = height / 2f
            p.reset()
            p.isAntiAlias = true
            p.style = Paint.Style.FILL
            p.color = if (tint != 0) tint else if (dark) 0xFFB8B8B8.toInt() else 0xFF6B6B6B.toInt()
            q.reset()
            q.isAntiAlias = true
            q.color = if (cutColor != 0) cutColor else bgColor()
            when (kind) {
                0, 20 -> { // circular arrows with T (tools mode) or smiley (suggestion mode)
                    p.style = Paint.Style.STROKE
                    p.strokeWidth = 2f * u
                    val r = 10f * u
                    canvas.drawArc(RectF(cx - r, cy - r, cx + r, cy + r), 205f, 110f, false, p)
                    canvas.drawArc(RectF(cx - r, cy - r, cx + r, cy + r), 25f, 110f, false, p)
                    p.style = Paint.Style.FILL
                    for (start in floatArrayOf(205f, 25f)) {
                        val e = Math.toRadians((start + 110f).toDouble())
                        val nx = Math.cos(e).toFloat()
                        val ny = Math.sin(e).toFloat()
                        val ex = cx + r * nx
                        val ey = cy + r * ny
                        val path = Path()
                        path.moveTo(ex + nx * 3.2f * u, ey + ny * 3.2f * u)
                        path.lineTo(ex - nx * 3.2f * u, ey - ny * 3.2f * u)
                        path.lineTo(ex - ny * 4.5f * u, ey + nx * 4.5f * u)
                        path.close()
                        canvas.drawPath(path, p)
                    }
                    if (kind == 0) {
                        p.textAlign = Paint.Align.CENTER
                        p.textSize = 12f * u
                        p.isFakeBoldText = true
                        canvas.drawText("T", cx, cy + 4.2f * u, p)
                    } else {
                        canvas.drawCircle(cx - 2.8f * u, cy - 1.6f * u, 1.2f * u, p)
                        canvas.drawCircle(cx + 2.8f * u, cy - 1.6f * u, 1.2f * u, p)
                        p.style = Paint.Style.STROKE
                        p.strokeWidth = 1.5f * u
                        p.strokeCap = Paint.Cap.ROUND
                        canvas.drawArc(
                            RectF(cx - 4.2f * u, cy - 4.2f * u, cx + 4.2f * u, cy + 4.8f * u),
                            25f, 130f, false, p
                        )
                    }
                }
                1 -> { // smiley
                    canvas.drawCircle(cx, cy, 11f * u, p)
                    q.style = Paint.Style.FILL
                    canvas.drawCircle(cx - 4f * u, cy - 3f * u, 1.7f * u, q)
                    canvas.drawCircle(cx + 4f * u, cy - 3f * u, 1.7f * u, q)
                    q.style = Paint.Style.STROKE
                    q.strokeWidth = 1.8f * u
                    q.strokeCap = Paint.Cap.ROUND
                    canvas.drawArc(
                        RectF(cx - 5.5f * u, cy - 5f * u, cx + 5.5f * u, cy + 6f * u),
                        20f, 140f, false, q
                    )
                }
                2 -> { // clipboard
                    canvas.drawRoundRect(
                        RectF(cx - 8f * u, cy - 9f * u, cx + 8f * u, cy + 11f * u),
                        2f * u, 2f * u, p
                    )
                    canvas.drawRoundRect(
                        RectF(cx - 4.5f * u, cy - 12f * u, cx + 4.5f * u, cy - 6f * u),
                        1.5f * u, 1.5f * u, p
                    )
                    q.style = Paint.Style.STROKE
                    q.strokeWidth = 1.6f * u
                    q.strokeCap = Paint.Cap.ROUND
                    canvas.drawLine(cx - 4.5f * u, cy - 1f * u, cx + 4.5f * u, cy - 1f * u, q)
                    canvas.drawLine(cx - 4.5f * u, cy + 3f * u, cx + 4.5f * u, cy + 3f * u, q)
                    canvas.drawLine(cx - 4.5f * u, cy + 7f * u, cx + 1.5f * u, cy + 7f * u, q)
                }
                3 -> { // four arrows
                    val path = Path()
                    path.moveTo(cx, cy - 11f * u)
                    path.lineTo(cx - 4f * u, cy - 6f * u)
                    path.lineTo(cx + 4f * u, cy - 6f * u)
                    path.close()
                    path.moveTo(cx, cy + 11f * u)
                    path.lineTo(cx - 4f * u, cy + 6f * u)
                    path.lineTo(cx + 4f * u, cy + 6f * u)
                    path.close()
                    path.moveTo(cx - 11f * u, cy)
                    path.lineTo(cx - 6f * u, cy - 4f * u)
                    path.lineTo(cx - 6f * u, cy + 4f * u)
                    path.close()
                    path.moveTo(cx + 11f * u, cy)
                    path.lineTo(cx + 6f * u, cy - 4f * u)
                    path.lineTo(cx + 6f * u, cy + 4f * u)
                    path.close()
                    canvas.drawPath(path, p)
                }
                4 -> { // three dots
                    canvas.drawCircle(cx - 8f * u, cy, 2.2f * u, p)
                    canvas.drawCircle(cx, cy, 2.2f * u, p)
                    canvas.drawCircle(cx + 8f * u, cy, 2.2f * u, p)
                }
                10 -> { // clock
                    canvas.drawCircle(cx, cy, 11f * u, p)
                    q.style = Paint.Style.STROKE
                    q.strokeWidth = 2f * u
                    q.strokeCap = Paint.Cap.ROUND
                    canvas.drawLine(cx, cy, cx, cy - 6f * u, q)
                    canvas.drawLine(cx, cy, cx + 4.5f * u, cy + 2.5f * u, q)
                }
                11 -> { // person
                    canvas.drawCircle(cx, cy - 5f * u, 4.2f * u, p)
                    canvas.drawArc(
                        RectF(cx - 8.5f * u, cy + 1f * u, cx + 8.5f * u, cy + 17f * u),
                        180f, 180f, true, p
                    )
                }
                12 -> { // paw
                    canvas.drawOval(RectF(cx - 5.5f * u, cy + 0.5f * u, cx + 5.5f * u, cy + 10f * u), p)
                    canvas.drawCircle(cx - 7.5f * u, cy - 1.5f * u, 2.4f * u, p)
                    canvas.drawCircle(cx - 2.8f * u, cy - 6.5f * u, 2.6f * u, p)
                    canvas.drawCircle(cx + 2.8f * u, cy - 6.5f * u, 2.6f * u, p)
                    canvas.drawCircle(cx + 7.5f * u, cy - 1.5f * u, 2.4f * u, p)
                }
                13 -> { // burger
                    canvas.drawArc(
                        RectF(cx - 10f * u, cy - 11f * u, cx + 10f * u, cy + 1f * u),
                        180f, 180f, true, p
                    )
                    canvas.drawRoundRect(
                        RectF(cx - 10.5f * u, cy - 3.5f * u, cx + 10.5f * u, cy - 0.5f * u),
                        1.5f * u, 1.5f * u, p
                    )
                    canvas.drawRoundRect(
                        RectF(cx - 10f * u, cy + 1.5f * u, cx + 10f * u, cy + 7f * u),
                        2.5f * u, 2.5f * u, p
                    )
                }
                14 -> { // ball
                    canvas.drawCircle(cx, cy, 11f * u, p)
                    q.style = Paint.Style.STROKE
                    q.strokeWidth = 1.4f * u
                    canvas.drawLine(cx, cy - 11f * u, cx, cy + 11f * u, q)
                    canvas.drawLine(cx - 11f * u, cy, cx + 11f * u, cy, q)
                    canvas.drawArc(
                        RectF(cx - 15f * u, cy - 8f * u, cx - 1f * u, cy + 8f * u),
                        -60f, 120f, false, q
                    )
                    canvas.drawArc(
                        RectF(cx + 1f * u, cy - 8f * u, cx + 15f * u, cy + 8f * u),
                        120f, 120f, false, q
                    )
                }
                15 -> { // house
                    val path = Path()
                    path.moveTo(cx, cy - 11f * u)
                    path.lineTo(cx + 11f * u, cy - 1f * u)
                    path.lineTo(cx + 8f * u, cy - 1f * u)
                    path.lineTo(cx + 8f * u, cy + 10f * u)
                    path.lineTo(cx - 8f * u, cy + 10f * u)
                    path.lineTo(cx - 8f * u, cy - 1f * u)
                    path.lineTo(cx - 11f * u, cy - 1f * u)
                    path.close()
                    canvas.drawPath(path, p)
                    q.style = Paint.Style.FILL
                    canvas.drawRect(RectF(cx - 2.5f * u, cy + 3f * u, cx + 2.5f * u, cy + 10f * u), q)
                }
                16 -> { // light bulb
                    canvas.drawCircle(cx, cy - 3f * u, 7.5f * u, p)
                    canvas.drawRoundRect(
                        RectF(cx - 3.5f * u, cy + 5.5f * u, cx + 3.5f * u, cy + 10.5f * u),
                        1.5f * u, 1.5f * u, p
                    )
                }
                17 -> { // heart
                    val path = Path()
                    path.moveTo(cx, cy + 9f * u)
                    path.cubicTo(cx - 14f * u, cy - 1f * u, cx - 8f * u, cy - 11f * u, cx, cy - 4f * u)
                    path.cubicTo(cx + 8f * u, cy - 11f * u, cx + 14f * u, cy - 1f * u, cx, cy + 9f * u)
                    path.close()
                    canvas.drawPath(path, p)
                }
                18 -> { // flag
                    canvas.drawRect(RectF(cx - 8f * u, cy - 11f * u, cx - 6f * u, cy + 11f * u), p)
                    val path = Path()
                    path.moveTo(cx - 6f * u, cy - 10f * u)
                    path.lineTo(cx + 10f * u, cy - 5.5f * u)
                    path.lineTo(cx - 6f * u, cy - 1f * u)
                    path.close()
                    canvas.drawPath(path, p)
                }
                19 -> { // keyboard
                    canvas.drawRoundRect(
                        RectF(cx - 11f * u, cy - 7f * u, cx + 11f * u, cy + 7f * u),
                        2f * u, 2f * u, p
                    )
                    q.style = Paint.Style.FILL
                    for (i in 0..4) {
                        val x = cx - 7f * u + i * 3.5f * u
                        canvas.drawCircle(x, cy - 3.5f * u, 1f * u, q)
                        canvas.drawCircle(x, cy, 1f * u, q)
                    }
                    q.style = Paint.Style.STROKE
                    q.strokeWidth = 1.6f * u
                    q.strokeCap = Paint.Cap.ROUND
                    canvas.drawLine(cx - 5f * u, cy + 4f * u, cx + 5f * u, cy + 4f * u, q)
                }
                21 -> { // magnifier
                    p.style = Paint.Style.STROKE
                    p.strokeWidth = 2.4f * u
                    p.strokeCap = Paint.Cap.ROUND
                    canvas.drawCircle(cx - 2f * u, cy - 2f * u, 7f * u, p)
                    canvas.drawLine(cx + 3f * u, cy + 3f * u, cx + 9f * u, cy + 9f * u, p)
                }
                22 -> { // chevron left
                    p.style = Paint.Style.STROKE
                    p.strokeWidth = 2.4f * u
                    p.strokeCap = Paint.Cap.ROUND
                    p.strokeJoin = Paint.Join.ROUND
                    val path = Path()
                    path.moveTo(cx + 3f * u, cy - 7f * u)
                    path.lineTo(cx - 4f * u, cy)
                    path.lineTo(cx + 3f * u, cy + 7f * u)
                    canvas.drawPath(path, p)
                }
                31 -> { // gear (Settings)
                    p.style = Paint.Style.STROKE
                    p.strokeCap = Paint.Cap.ROUND
                    p.strokeWidth = 2.8f * u
                    canvas.drawCircle(cx, cy, 6.2f * u, p)
                    p.strokeWidth = 3.4f * u
                    for (a in 0 until 8) {
                        val ang = a * Math.PI / 4
                        val dx = Math.cos(ang).toFloat()
                        val dy = Math.sin(ang).toFloat()
                        canvas.drawLine(
                            cx + dx * 9f * u, cy + dy * 9f * u,
                            cx + dx * 10.5f * u, cy + dy * 10.5f * u, p
                        )
                    }
                }
                32 -> { // transliterate: two tiles "অ" and "A"
                    canvas.drawRoundRect(
                        RectF(cx - 11f * u, cy - 11f * u, cx + 3f * u, cy + 3f * u),
                        2f * u, 2f * u, p
                    )
                    q.style = Paint.Style.FILL
                    canvas.drawRoundRect(
                        RectF(cx - 4.5f * u, cy - 4.5f * u, cx + 12.5f * u, cy + 12.5f * u),
                        3f * u, 3f * u, q
                    )
                    canvas.drawRoundRect(
                        RectF(cx - 3f * u, cy - 3f * u, cx + 11f * u, cy + 11f * u),
                        2f * u, 2f * u, p
                    )
                    q.textAlign = Paint.Align.CENTER
                    q.isFakeBoldText = true
                    q.textSize = 9f * u
                    canvas.drawText("অ", cx - 4f * u, cy - 0.5f * u, q)
                    canvas.drawText("A", cx + 4f * u, cy + 7.5f * u, q)
                }
                else -> { // chevron down
                    p.style = Paint.Style.STROKE
                    p.strokeWidth = 2.4f * u
                    p.strokeCap = Paint.Cap.ROUND
                    p.strokeJoin = Paint.Join.ROUND
                    val path = Path()
                    path.moveTo(cx - 7f * u, cy - 3f * u)
                    path.lineTo(cx, cy + 4f * u)
                    path.lineTo(cx + 7f * u, cy - 3f * u)
                    canvas.drawPath(path, p)
                }
            }
        }
    }

    private fun feedback(v: View) {
        if (haptic) v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        if (sound) v.playSoundEffect(SoundEffectConstants.CLICK)
    }

    private fun fnIcon(kind: Int, weight: Float, onTap: () -> Unit): View {
        val v = IconView(kind)
        v.background = bg(fnBg())
        v.layoutParams = LinearLayout.LayoutParams(0, dp(keyH()), weight).apply {
            setMargins(dp(2), dp(3), dp(2), dp(3))
        }
        v.setOnClickListener {
            feedback(it)
            onTap()
        }
        return v
    }

    private fun ovalBg(): StateListDrawable {
        val pressed = GradientDrawable()
        pressed.shape = GradientDrawable.OVAL
        pressed.setColor(0x44707070)
        val normal = GradientDrawable()
        normal.shape = GradientDrawable.OVAL
        normal.setColor(0)
        val sl = StateListDrawable()
        sl.addState(intArrayOf(android.R.attr.state_pressed), pressed)
        sl.addState(intArrayOf(), normal)
        return sl
    }

    // ================= Popups (key preview, language, toast) =================
    // Make sure a popup view lives inside the CURRENT overlay (Android can rebuild the keyboard view).
    private fun attach(tv: TextView) {
        val par = tv.parent
        if (par !== overlay) {
            if (par is ViewGroup) par.removeView(tv)
            overlay.addView(tv)
        }
        tv.bringToFront()
    }

    private fun relPos(v: View): IntArray {
        val a = IntArray(2)
        val b = IntArray(2)
        v.getLocationInWindow(a)
        host.getLocationInWindow(b)
        return intArrayOf(a[0] - b[0], a[1] - b[1])
    }

    private fun popupBg(radius: Int) = GradientDrawable().apply {
        cornerRadius = dp(radius).toFloat()
        setColor(keyBg())
    }

    private fun showPreview(v: View, label: String) {
        if (!::host.isInitialized) return
        uiHandler.removeCallbacks(hidePreviewRunnable)
        var pv = previewView
        if (pv == null) {
            pv = TextView(this)
            pv.gravity = Gravity.CENTER
            pv.textSize = 28f
            pv.elevation = dp(6).toFloat()
            previewView = pv
        }
        attach(pv)
        val pos = relPos(v)
        val w = maxOf((v.width * 1.25f).toInt(), dp(44))
        var h = (v.height * 1.5f).toInt()
        val room = pos[1] + dp(8)
        if (h > room) h = maxOf(room, v.height)
        var x = pos[0] + (v.width - w) / 2
        if (x > host.width - w) x = host.width - w
        if (x < 0) x = 0
        var y = pos[1] - h + dp(6)
        if (y < 0) y = 0
        pv.layoutParams = FrameLayout.LayoutParams(w, h, Gravity.TOP or Gravity.START)
        pv.translationX = x.toFloat()
        pv.translationY = y.toFloat()
        pv.background = popupBg(10)
        pv.setTextColor(textCol())
        pv.text = label
        pv.scaleX = 0.88f
        pv.scaleY = 0.88f
        pv.alpha = 1f
        pv.visibility = View.VISIBLE
        pv.animate().scaleX(1f).scaleY(1f).setDuration(60).start()
    }

    private fun hidePreview() {
        uiHandler.removeCallbacks(hidePreviewRunnable)
        uiHandler.postDelayed(hidePreviewRunnable, 90)
    }

    // ---- tap typing with a preview bubble; sliding the finger moves to the key under it ----
    private fun keyAt(x: Float, y: Float): View? {
        val loc = IntArray(2)
        for (k in previewKeys) {
            k.getLocationOnScreen(loc)
            if (x >= loc[0] && x < loc[0] + k.width && y >= loc[1] && y < loc[1] + k.height) return k
        }
        return null
    }

    private fun infoOf(v: View): PreviewInfo? = v.tag as? PreviewInfo

    private fun handlePreviewTouch(v: View, e: MotionEvent) {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                curKeyFor[v] = v
                v.isPressed = true
                val info = infoOf(v)
                if (info != null && previewOn) showPreview(v, info.label)
            }
            MotionEvent.ACTION_MOVE -> {
                val cur = curKeyFor[v]
                val under = keyAt(e.rawX, e.rawY)
                if (under != null && under !== cur) {
                    cur?.isPressed = false
                    under.isPressed = true
                    curKeyFor[v] = under
                    val info = infoOf(under)
                    if (info != null && previewOn) showPreview(under, info.label)
                }
            }
            MotionEvent.ACTION_UP -> {
                val cur = curKeyFor.remove(v)
                cur?.isPressed = false
                hidePreview()
                if (cur != null) {
                    val info = infoOf(cur)
                    if (info != null) {
                        feedback(cur)
                        info.onTap()
                    }
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                val cur = curKeyFor.remove(v)
                cur?.isPressed = false
                hidePreview()
            }
        }
    }

    private fun langName(l: Int) =
        if (l == 1) (if (translit) "বাংলা (Avro)" else "বাংলা") else "English (US)"

    private fun showLangPopup(anchor: View, dir: Int, label: String) {
        if (!::host.isInitialized) return
        uiHandler.removeCallbacks(hideLangRunnable)
        var lp = langPopup
        if (lp == null) {
            lp = TextView(this)
            lp.gravity = Gravity.CENTER
            lp.textSize = 15f
            lp.elevation = dp(6).toFloat()
            langPopup = lp
        }
        attach(lp)
        val w = dp(160)
        val h = dp(46)
        val pos = relPos(anchor)
        var x = pos[0] + (anchor.width - w) / 2
        if (x > host.width - w) x = host.width - w
        if (x < 0) x = 0
        val y = maxOf(pos[1] - h - dp(2), 0)
        lp.animate().cancel()
        lp.layoutParams = FrameLayout.LayoutParams(w, h, Gravity.TOP or Gravity.START)
        lp.translationX = x.toFloat() + dir * dp(30)
        lp.translationY = y.toFloat()
        lp.background = popupBg(14)
        lp.setTextColor(textCol())
        lp.text = "◂   " + label + "   ▸"
        lp.alpha = 0f
        lp.visibility = View.VISIBLE
        lp.animate().alpha(1f).translationX(x.toFloat()).setDuration(130).start()
    }

    private fun hideLangPopup() {
        uiHandler.removeCallbacks(hideLangRunnable)
        langPopup?.animate()?.cancel()
        langPopup?.visibility = View.GONE
    }

    private fun flashLangPopup() {
        uiHandler.postDelayed({
            val sp = spaceKey
            if (sp != null && sp.width > 0) {
                showLangPopup(sp, 0, langName(lang))
                uiHandler.removeCallbacks(hideLangRunnable)
                uiHandler.postDelayed(hideLangRunnable, 650)
            }
        }, 70)
    }

    private fun showToast(msg: String) {
        if (!::host.isInitialized) return
        var tv = toastView
        if (tv == null) {
            tv = TextView(this)
            tv.textSize = 13f
            tv.setTextColor(0xFFFFFFFF.toInt())
            tv.gravity = Gravity.CENTER
            tv.setPadding(dp(14), dp(10), dp(14), dp(10))
            toastView = tv
        }
        attach(tv)
        val bgd = GradientDrawable()
        bgd.cornerRadius = dp(18).toFloat()
        bgd.setColor(0xEE333333.toInt())
        tv.background = bgd
        tv.text = msg
        val lpar = FrameLayout.LayoutParams(WC, WC, Gravity.TOP or Gravity.CENTER_HORIZONTAL)
        lpar.topMargin = dp(10)
        lpar.leftMargin = dp(16)
        lpar.rightMargin = dp(16)
        tv.layoutParams = lpar
        tv.animate().cancel()
        tv.alpha = 0f
        tv.visibility = View.VISIBLE
        tv.animate().alpha(1f).setDuration(150).start()
        uiHandler.removeCallbacks(hideToastRunnable)
        uiHandler.postDelayed(hideToastRunnable, 2600)
    }

    // ================= Dictionary & Suggestions =================
    private val enWords = "the be to of and a in that have it for not on with he as you do at this but his by from they we say her she or an will my one all would there their what so up out if about who get which go me when make can like time no just him know take people into year your good some could them see other than then now look only come its over think also back after use two how our work first well way even new want because any these give day most us is are was were been has had did does am hello hi thanks thank please sorry okay yes where why tomorrow today tonight morning night friend family home love happy birthday welcome great nice really very much more still should going doing coming working message phone call tell send share meeting office school food water money problem question answer help need understand remember forget something anything everything nothing someone everyone yesterday thinking wonderful important different information beautiful together possible between another without through before always never maybe probably however actually definitely especially awesome congratulations assalamualaikum inshallah alhamdulillah mashallah bangladesh dhaka internet facebook whatsapp youtube google"
    private val bnWords = "আমি তুমি আপনি সে আমরা তোমরা তারা এই ওই কি কী কেন কোথায় কখন কেমন কত কে কাকে না হ্যাঁ ভালো খারাপ আছি আছো আছেন আছে করছি করছো করছেন করে করা করব করবো হবে হয় হয়েছে হচ্ছে যাব যাবো যাচ্ছি আসব আসবো আসছি বলো বলছি বলুন ধন্যবাদ দুঃখিত অনেক একটু এখন তখন আজ কাল আগামীকাল গতকাল সকাল দুপুর বিকাল সন্ধ্যা রাত বন্ধু ভাই বোন মা বাবা বাড়ি স্কুল কলেজ অফিস কাজ টাকা খাবার পানি ভাত চা সালাম শুভ জন্মদিন শুভেচ্ছা স্বাগতম ঠিক থাকো থাকি দেখা তাহলে কিন্তু এবং অথবা যদি তবে তাই কারণ সবাই সব কিছু কোনো একটা একটি দুটি তিনটি আমার তোমার আপনার তার আমাদের তোমাদের তাদের বাংলা বাংলাদেশ ঢাকা ভালোবাসা ভালোবাসি মন মানুষ সময় দিন বছর মাস সপ্তাহ প্রশ্ন উত্তর সমস্যা সাহায্য দরকার চাই পারি পারো পারেন জানি জানো জানেন বুঝি বুঝতে শুনছি দেখছি খাচ্ছি খেয়েছো ঘুমাচ্ছি পড়ছি লিখছি কথা ফোন মেসেজ খবর নতুন পুরনো বড় ছোট"
    private val bnWords2 = "তোমাকে তোমার তোমাদের আপনাকে আমাকে তাকে তোমাকে ভালোবাসি ভালোবাসো ভালোবাসা কেমন আছো কেমন আছেন কোথায় যাচ্ছো যাচ্ছেন কী করছো কী করছেন কি খবর হ্যালো ঈদ মোবারক ঈদের শুভেচ্ছা রমজান নামাজ দোয়া আল্লাহ ইনশাআল্লাহ আলহামদুলিল্লাহ মাশাআল্লাহ আসসালামুআলাইকুম ওয়ালাইকুম স্বপ্ন জীবন পৃথিবী আকাশ বৃষ্টি রোদ গরম ঠান্ডা বাজার দোকান গাড়ি বাস ট্রেন রাস্তা শহর গ্রাম দেশ বিদেশ ভাষা শিক্ষা শিক্ষক ছাত্র ছাত্রী পরীক্ষা ফলাফল চাকরি ব্যবসা অফিস মিটিং কাজের জন্য কারণে সাথে মধ্যে পর্যন্ত থেকে হইতে দিয়ে নিয়ে নিয়েছি দিয়েছি গিয়েছি এসেছি বলেছি শুনেছি দেখেছি করেছি খেয়েছি ঘুমিয়েছি পড়েছি লিখেছি বুঝেছি জেনেছি পেয়েছি হারিয়েছি তোমরা আপনারা তারা এরা ওরা কারা কোন কখনো কখনও সবসময় প্রায় খুব বেশি কম আরও আর অন্য অন্যান্য প্রথম দ্বিতীয় শেষ আবার আবারও দয়া করে অবশ্যই অবশ্য হয়তো সম্ভবত নিশ্চয়ই নিশ্চিত সত্যি মিথ্যা সুন্দর চমৎকার দারুণ অসাধারণ মজা আনন্দ দুঃখ কষ্ট ভয় রাগ হাসি কান্না প্রেম বিয়ে সংসার পরিবার সন্তান ছেলে মেয়ে স্বামী স্ত্রী দাদা দাদি নানা নানি চাচা চাচি মামা মামি খালা ফুফু শুভরাত্রি শুভসকাল শুভদিন শুভ নববর্ষ"
    private val dict by lazy { (enWords + " " + bnWords + " " + bnWords2).split(" ").filter { it.isNotEmpty() } }
    private val dictSet by lazy { HashSet(dict) }
    private val enList by lazy { enWords.split(" ").filter { it.isNotEmpty() } }
    private val bnList by lazy { (bnWords + " " + bnWords2).split(" ").filter { it.isNotEmpty() }.distinct() }

    private fun isWordChar(c: Char) = c.isLetter() || c in '\u0980'..'\u09FF' || c == '\''

    // ব্যঞ্জনবর্ণের পর কার বসে: ক-হ, নুক্তা, ড় ঢ় য় (precomposed)
    private fun isBnCons(c: Char) =
        c in '\u0995'..'\u09B9' || c == '\u09BC' || c == '\u09DC' || c == '\u09DD' || c == '\u09DF'

    private fun afterConsonant(): Boolean {
        val b = currentInputConnection?.getTextBeforeCursor(1, 0) ?: return false
        if (b.isEmpty()) return false
        return isBnCons(b[0])
    }

    private fun isPassword(): Boolean {
        val t = currentInputEditorInfo?.inputType ?: return false
        val cls = t and InputType.TYPE_MASK_CLASS
        val v = t and InputType.TYPE_MASK_VARIATION
        return (cls == InputType.TYPE_CLASS_TEXT && (
            v == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                v == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                v == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD)) ||
            (cls == InputType.TYPE_CLASS_NUMBER && v == InputType.TYPE_NUMBER_VARIATION_PASSWORD)
    }

    private fun loadLearned() {
        if (learnedLoaded) return
        learnedLoaded = true
        val s = getSharedPreferences("kb", MODE_PRIVATE).getString("words", "") ?: ""
        for (line in s.split("\n")) {
            val i = line.lastIndexOf(':')
            if (i > 0) learned[line.substring(0, i)] = line.substring(i + 1).toIntOrNull() ?: 1
        }
    }

    private fun learn(word: String) {
        if (isPassword()) return
        loadLearned()
        val w = word.lowercase()
        if (w.length < 2 || w.any { it.isDigit() }) return
        learned[w] = (learned[w] ?: 0) + 1
        if (learned.size > 600) {
            val keep = learned.entries.sortedByDescending { it.value }.take(400)
            learned.clear()
            for (e in keep) learned[e.key] = e.value
        }
        getSharedPreferences("kb", MODE_PRIVATE).edit()
            .putString("words", learned.entries.joinToString("\n") { it.key + ":" + it.value })
            .apply()
    }

    private fun loadBigrams() {
        if (bigramsLoaded) return
        bigramsLoaded = true
        val s = getSharedPreferences("kb", MODE_PRIVATE).getString("bigrams", "") ?: ""
        for (line in s.split("\n")) {
            val i = line.lastIndexOf(':')
            if (i > 0) bigrams[line.substring(0, i)] = line.substring(i + 1).toIntOrNull() ?: 1
        }
    }

    private fun learnBigram(prev: String, next: String) {
        if (isPassword() || prev.isEmpty() || next.isEmpty()) return
        loadBigrams()
        val k = prev.lowercase() + " " + next.lowercase()
        bigrams[k] = (bigrams[k] ?: 0) + 1
        if (bigrams.size > 800) {
            val keep = bigrams.entries.sortedByDescending { it.value }.take(500)
            bigrams.clear()
            for (e in keep) bigrams[e.key] = e.value
        }
        getSharedPreferences("kb", MODE_PRIVATE).edit()
            .putString("bigrams", bigrams.entries.joinToString("\n") { it.key + ":" + it.value })
            .apply()
    }

    private fun nextWords(prev: String, n: Int = 3): List<String> {
        if (prev.isEmpty()) return emptyList()
        loadBigrams()
        val pre = prev.lowercase() + " "
        return bigrams.entries
            .filter { it.key.startsWith(pre) }
            .sortedByDescending { it.value }
            .map { it.key.substring(pre.length) }
            .take(n)
    }

    private fun currentWord(): String {
        val before = currentInputConnection?.getTextBeforeCursor(30, 0) ?: return ""
        var i = before.length
        while (i > 0 && isWordChar(before[i - 1])) i--
        return before.substring(i)
    }

    private fun previousWord(): String {
        val before = currentInputConnection?.getTextBeforeCursor(60, 0) ?: return ""
        var i = before.length
        while (i > 0 && isWordChar(before[i - 1])) i--
        var j = i
        while (j > 0 && before[j - 1] == ' ') j--
        if (j == i) return ""
        var k = j
        while (k > 0 && isWordChar(before[k - 1])) k--
        return before.substring(k, j)
    }

    private fun learnCurrent() {
        val w = currentWord()
        if (w.isEmpty()) return
        if (w.length >= 2) learn(w)
        learnBigram(previousWord(), w)
    }

    private fun fieldEmpty(): Boolean {
        val ic = currentInputConnection ?: return true
        val b = ic.getTextBeforeCursor(1, 0)
        val a = ic.getTextAfterCursor(1, 0)
        return (b == null || b.isEmpty()) && (a == null || a.isEmpty())
    }

    private fun matches(p: String): List<String> {
        loadLearned()
        val cap = p[0].isUpperCase()
        val lp = p.lowercase()
        val out = LinkedHashSet<String>()
        for (e in learned.entries.sortedByDescending { it.value }) {
            if (e.key.startsWith(lp) && e.key != lp) out.add(e.key)
        }
        for (w in dict) {
            if (w.startsWith(lp) && w != lp) out.add(w)
        }
        return out.map { if (cap) it.replaceFirstChar { c -> c.uppercase() } else it }
    }

    private fun starters(): List<String> =
        if (lang == 1) listOf("আমি", "তুমি", "কেমন") else listOf("I", "The", "Hi")

    // Avro: ধ্বনিগতভাবে কাছাকাছি শব্দ খোঁজার জন্য বানানকে সহজ রূপে নামানো হয়
    // (ো বাদ, ী→ি, ূ→ু, ণ→ন, শ/ষ→স, হসন্ত বাদ ইত্যাদি)
    private fun bnNorm(s: String): String {
        val sb = StringBuilder()
        for (c in s) {
            when (c) {
                '\u09CB', '\u09CD', '\u0981', '\u09BC' -> {}
                '\u09C0' -> sb.append('\u09BF')
                '\u09C2' -> sb.append('\u09C1')
                '\u0988' -> sb.append('\u0987')
                '\u098A' -> sb.append('\u0989')
                '\u09A3' -> sb.append('\u09A8')
                '\u09B6', '\u09B7' -> sb.append('\u09B8')
                '\u09DF' -> sb.append('\u09AF')
                '\u0993' -> sb.append('\u0985')
                '\u09CE' -> sb.append('\u09A4')
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    private val bnIndex: HashMap<String, String> by lazy {
        val m = HashMap<String, String>()
        for (w in bnList) {
            val n = bnNorm(w)
            if (!m.containsKey(n)) m[n] = w
        }
        m
    }

    private fun bnFuzzy(conv: String): List<String> {
        loadLearned()
        val nc = bnNorm(conv)
        if (nc.isEmpty()) return emptyList()
        val pool = ArrayList<String>()
        for (e in learned.entries.sortedByDescending { it.value }) pool.add(e.key)
        pool.addAll(bnList)
        val out = LinkedHashSet<String>()
        for (w in pool) {
            if (w != conv && w.isNotEmpty() && w[0] in '\u0980'..'\u09FF' && bnNorm(w).startsWith(nc)) out.add(w)
        }
        return out.sortedBy { bnNorm(it).length }
    }

    private fun candidates(n: Int = 3): List<String> {
        if (isPassword()) return emptyList()
        if (avroOn() && avroBuf.isNotEmpty()) {
            val conv = avroConvert(avroBuf.toString())
            val out = LinkedHashSet<String>()
            out.add(conv)
            out.addAll(bnFuzzy(conv))
            return out.take(n)
        }
        val w = currentWord()
        if (w.isEmpty()) {
            val out = LinkedHashSet<String>(nextWords(previousWord(), n))
            for (s in starters()) out.add(s)
            return out.take(n)
        }
        return matches(w).take(n)
    }

    // ---------- Auto replace ----------
    private val fixMap = mapOf(
        "i" to "I", "im" to "I'm", "ive" to "I've", "dont" to "don't", "cant" to "can't",
        "wont" to "won't", "didnt" to "didn't", "doesnt" to "doesn't", "isnt" to "isn't",
        "wasnt" to "wasn't", "couldnt" to "couldn't", "shouldnt" to "shouldn't",
        "wouldnt" to "wouldn't", "arent" to "aren't", "havent" to "haven't", "hasnt" to "hasn't",
        "youre" to "you're", "thats" to "that's", "whats" to "what's", "theyre" to "they're",
        "teh" to "the", "adn" to "and", "taht" to "that", "thier" to "their", "recieve" to "receive"
    )

    // একটি অক্ষর অদলবদল (teh→the) অথবা ৫+ অক্ষরের শব্দে একটি অক্ষর বাদ/বেশি
    private fun closeEdit(a: String, b: String): Boolean {
        if (a == b) return false
        if (a.length == b.length) {
            var i = 0
            while (i < a.length && a[i] == b[i]) i++
            if (i + 1 < a.length && a[i] == b[i + 1] && a[i + 1] == b[i]) {
                return a.substring(i + 2) == b.substring(i + 2)
            }
            return false
        }
        if (Math.abs(a.length - b.length) == 1 && minOf(a.length, b.length) >= 4) {
            val s = if (a.length < b.length) a else b
            val l = if (a.length < b.length) b else a
            var i = 0
            while (i < s.length && s[i] == l[i]) i++
            return s.substring(i) == l.substring(i + 1)
        }
        return false
    }

    private fun engFix(w: String): String? {
        if (w.isEmpty()) return null
        val lw = w.lowercase()
        val m = fixMap[lw]
        if (m != null) return if (w[0].isUpperCase() && !m[0].isUpperCase()) m.replaceFirstChar { it.uppercase() } else m
        if (lw.length < 3 || !lw.all { it in 'a'..'z' }) return null
        loadLearned()
        if (dictSet.contains(lw) || learned.containsKey(lw)) return null
        var best: String? = null
        for (d in enList) {
            if (d.length >= 3 && closeEdit(lw, d)) {
                best = d
                break
            }
        }
        val b = best ?: return null
        return if (w[0].isUpperCase()) b.replaceFirstChar { it.uppercase() } else b
    }

    private fun tryAutoReplace() {
        if (!autoReplace || isPassword() || lang != 0) return
        val w = currentWord()
        val fix = engFix(w) ?: return
        val ic = currentInputConnection ?: return
        ic.deleteSurroundingText(w.length, 0)
        ic.commitText(fix, 1)
    }

    private fun avroFix(w: String): String? {
        if (dictSet.contains(w)) return null
        loadLearned()
        if (learned.containsKey(w)) return null
        val r = bnIndex[bnNorm(w)] ?: return null
        return if (r != w) r else null
    }

    // ================= Avro Phonetic engine =================
    // একটি token = [key, kind, ...]
    //  C = ব্যঞ্জন (আগের ব্যঞ্জনের সাথে নিজে থেকেই হসন্ত দিয়ে যুক্ত হয়), V = স্বর (ব্যঞ্জনের পর কার)
    //  S = চিহ্ন, বাকিগুলো বিশেষ নিয়ম
    private val avroTokens: List<Array<String>> by lazy {
        val l = ArrayList<Array<String>>()
        fun c(k: String, v: String) { l.add(arrayOf(k, "C", v)) }
        fun v(k: String, ind: String, kar: String) { l.add(arrayOf(k, "V", ind, kar)) }
        fun s(k: String, x: String) { l.add(arrayOf(k, "S", x)) }
        fun sp(k: String, t: String) { l.add(arrayOf(k, t)) }
        c("kkh", "ক্ষ"); c("kh", "খ"); c("k", "ক"); c("q", "ক"); c("x", "ক্স")
        c("GG", "জ্ঞ"); c("gh", "ঘ"); c("g", "গ")
        c("Ng", "ঙ"); c("NG", "ঞ")
        c("ngkh", "ঙ্খ"); c("ngk", "ঙ্ক"); c("nggh", "ঙ্ঘ"); c("ngg", "ঙ্গ")
        c("njh", "ঞ্ঝ"); c("nj", "ঞ্জ"); c("nch", "ঞ্ছ"); c("nc", "ঞ্চ")
        c("chh", "ছ"); c("ch", "ছ"); c("c", "চ"); c("jh", "ঝ"); c("j", "জ")
        c("Th", "ঠ"); c("T", "ট"); c("Dh", "ঢ"); c("D", "ড"); c("N", "ণ")
        c("th", "থ"); c("t", "ত"); c("dh", "ধ"); c("d", "দ"); c("n", "ন")
        c("ph", "ফ"); c("p", "প"); c("f", "ফ"); c("bh", "ভ"); c("b", "ব"); c("v", "ভ"); c("m", "ম")
        c("z", "য"); c("r", "র"); c("l", "ল")
        c("sh", "শ"); c("Sh", "ষ"); c("S", "শ"); c("s", "স"); c("h", "হ")
        c("Rh", "\u09DD"); c("R", "\u09DC")
        v("rri", "ঋ", "ৃ"); v("OI", "ঐ", "ৈ"); v("OU", "ঔ", "ৌ")
        v("aa", "আ", "া"); v("a", "আ", "া")
        v("ii", "ঈ", "ী"); v("ee", "ঈ", "ী"); v("I", "ঈ", "ী"); v("i", "ই", "ি")
        v("oo", "ঊ", "ূ"); v("uu", "ঊ", "ূ"); v("U", "ঊ", "ূ"); v("u", "উ", "ু")
        v("e", "এ", "ে"); v("O", "ও", "ো")
        s("t``", "\u09CE"); s("ng", "ং"); s(":", "ঃ"); s("^", "ঁ")
        sp("o", "o"); sp("y", "y"); sp("w", "w"); sp("Z", "Z"); sp("rr", "rr"); sp("`", "brk")
        l.sortByDescending { it[0].length }
        l
    }

    private fun avroConvert(src: String): String {
        var str = src
        val out = StringBuilder()
        var brk = false // true = আগের ব্যঞ্জন "বন্ধ" (যেমন অন্তর্নিহিত 'অ' এর পর) — যুক্ত হবে না, স্বর স্বাধীন হবে
        var i = 0
        while (i < str.length) {
            var tok: Array<String>? = null
            for (t in avroTokens) {
                if (str.startsWith(t[0], i)) {
                    tok = t
                    break
                }
            }
            if (tok == null) {
                val ch = str[i]
                if (ch.lowercaseChar() != ch) { // বড় হাতের অক্ষর যার আলাদা নিয়ম নেই → ছোট হাতেরটা
                    str = str.substring(0, i) + ch.lowercaseChar() + str.substring(i + 1)
                    continue
                }
                out.append(ch)
                brk = true
                i++
                continue
            }
            val k = tok[0]
            val lc = !brk && out.isNotEmpty() && isBnCons(out[out.length - 1])
            when (tok[1]) {
                "C" -> {
                    if (lc) out.append('\u09CD')
                    out.append(tok[2])
                    brk = false
                }
                "V" -> {
                    out.append(if (lc) tok[3] else tok[2])
                    brk = false
                }
                "S" -> {
                    out.append(tok[2])
                    brk = false
                }
                "o" -> {
                    if (lc) {
                        if (i + k.length >= str.length) {
                            out.append('\u09CB') // শব্দের শেষে ব্যঞ্জন+o = ো (ভালো)
                            brk = false
                        } else {
                            brk = true // মাঝে 'o' = অন্তর্নিহিত অ (কলম)
                        }
                    } else {
                        out.append(if (out.isEmpty()) '\u0985' else '\u0993')
                        brk = false
                    }
                }
                "y" -> {
                    out.append(if (lc) "\u09CD\u09AF" else "\u09DF")
                    brk = false
                }
                "w" -> {
                    out.append(if (lc) "\u09CD\u09AC" else "\u0993")
                    brk = false
                }
                "Z" -> {
                    out.append(if (lc) "\u09CD\u09AF" else "\u09AF")
                    brk = false
                }
                "rr" -> {
                    out.append("\u09B0\u09CD") // র্ (রেফ)
                    brk = false
                }
                "brk" -> brk = true
            }
            i += k.length
        }
        return out.toString()
    }

    private fun avroOn() = translit && lang == 1

    private fun flushAvro() {
        if (avroBuf.isNotEmpty()) {
            currentInputConnection?.finishComposingText()
            avroBuf.setLength(0)
        }
    }

    private fun finishAvro(replaceFix: Boolean) {
        if (avroBuf.isEmpty()) return
        var w = avroConvert(avroBuf.toString())
        val prev = previousWord()
        if (replaceFix && autoReplace) {
            val f = avroFix(w)
            if (f != null) w = f
        }
        currentInputConnection?.commitText(w, 1)
        avroBuf.setLength(0)
        learn(w)
        learnBigram(prev, w)
    }

    private fun avroType(ic: InputConnection, s: String) {
        if (s.length == 1) {
            val c = s[0]
            val isLetter = c in 'a'..'z' || c in 'A'..'Z' || c == '`'
            val isMark = (c == ':' || c == '^') && avroBuf.isNotEmpty()
            if (isLetter || isMark) {
                avroBuf.append(s)
                lastAvroTime = SystemClock.uptimeMillis()
                ic.setComposingText(avroConvert(avroBuf.toString()), 1)
                afterType()
                return
            }
        }
        if (avroBuf.isNotEmpty()) finishAvro(true)
        var out = s
        if (s.length == 1) {
            val ch = s[0]
            val b = ic.getTextBeforeCursor(1, 0)
            val bc = if (b != null && b.isNotEmpty()) b[0] else ' '
            if (ch in '0'..'9' && avroDigits) {
                out = ('\u09E6' + (ch - '0')).toString()
            } else if (ch == '.') {
                if (bc == '\u0964') {
                    ic.deleteSurroundingText(1, 0)
                    out = "."
                } else if (bc in '\u09E6'..'\u09EF' || bc in '0'..'9') {
                    out = "."
                } else {
                    out = "\u0964" // ।
                }
            } else if (ch == '$') {
                out = "\u09F3" // ৳
            }
        }
        ic.commitText(out, 1)
        afterType()
    }

    // Top bar rule:
    //  - keyboard: tools when the field is empty, word suggestions once you type
    //  - panels (emoji, clipboard, text editing...): tools stay until you tap T;
    //    after T: emoji panel -> emoji suggestions, other panels -> word suggestions
    private fun refreshSuggestions() {
        val box = sugBox ?: return
        if (barMode == 2 && !barManual && panel == 0 && fieldEmpty()) barMode = 0
        val showSug = if (panel != 0) barMode == 2 else (suggestOn && barMode == 2)
        if (panel == 1 && panelSug.isEmpty()) panelSug = defaultEmojis().take(7)
        box.removeAllViews()
        if (!showSug) {
            for (id in tbBar) box.addView(toolIcon(id))
        } else if (panel == 1) {
            for (e in panelSug) {
                box.addView(TextView(this).apply {
                    text = e
                    textSize = 22f
                    gravity = Gravity.CENTER
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
                    setOnClickListener {
                        feedback(it)
                        currentInputConnection?.commitText(e, 1)
                        addRecent(e)
                    }
                })
            }
        } else {
            val list = if (suggestOn) candidates(3) else emptyList()
            for ((i, s) in list.withIndex()) {
                if (i > 0) {
                    box.addView(View(this).apply {
                        layoutParams = LinearLayout.LayoutParams(dp(1), dp(20))
                        setBackgroundColor(0x44888888)
                    })
                }
                box.addView(TextView(this).apply {
                    text = s
                    textSize = 16f
                    maxLines = 1
                    gravity = Gravity.CENTER
                    setTextColor(textCol())
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
                    setOnClickListener { pick(s) }
                })
            }
        }
        val target = if (showSug) 20 else 0
        val ic = leftIcon
        if (ic != null && ic.kind != target) {
            ic.kind = target
            ic.invalidate()
        }
        if (barFresh) {
            barFresh = false
            lastShowSug = showSug
        } else if (showSug != lastShowSug) {
            lastShowSug = showSug
            box.animate().cancel()
            box.alpha = 0f
            box.animate().alpha(1f).setDuration(180).start()
        }
    }

    // T button: only flips the top bar (tools <-> suggestions). Open panels stay open.
    private fun onBarToggle() {
        val icon = leftIcon ?: return
        val box = sugBox ?: return
        val showingSug = lastShowSug
        val wasExpanded = sugExpanded
        sugExpanded = false
        icon.animate().cancel()
        icon.rotation = 0f
        icon.animate().rotation(360f).scaleX(1.2f).scaleY(1.2f).setDuration(260)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                icon.rotation = 0f
                icon.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }
            .start()
        box.animate().cancel()
        box.animate().alpha(0f).setDuration(130).withEndAction {
            barMode = if (showingSug) 1 else 2
            barManual = true
            if (wasExpanded) {
                render()
            } else {
                refreshSuggestions()
                box.animate().alpha(1f).setDuration(180).start()
            }
        }.start()
    }

    // Opening a panel keeps the tools row visible (barMode = 1) until you tap T.
    private fun openPanel(p: Int) {
        flushAvro()
        sugExpanded = false
        if (panel == p) {
            closePanel()
        } else {
            panel = p
            barMode = 1
            barManual = true
            panelSug = defaultEmojis().take(7)
            render()
        }
    }

    private fun closePanel() {
        panel = 0
        sugExpanded = false
        barMode = 0
        barManual = false
        render()
    }

    private fun pick(s: String) {
        val ic = currentInputConnection ?: return
        val wasExpanded = sugExpanded
        sugExpanded = false
        if (avroOn() && avroBuf.isNotEmpty()) {
            val prev = previousWord()
            ic.commitText(s + " ", 1)
            avroBuf.setLength(0)
            learn(s)
            learnBigram(prev, s)
        } else {
            val prev = previousWord()
            val w = currentWord()
            ic.deleteSurroundingText(w.length, 0)
            ic.commitText(s + " ", 1)
            learn(s)
            learnBigram(prev, s)
        }
        if (wasExpanded) render() else refreshSuggestions()
    }

    // ================= Toolbar tools =================
    private fun toolById(id: String): Tool? = allTools.firstOrNull { it.id == id }

    private fun toolActive(id: String) = when (id) {
        "translit" -> translit
        "emoji" -> panel == 1
        "clip" -> panel == 2
        "edit" -> panel == 3
        "settings" -> panel == 4
        "modes" -> panel == 7
        else -> false
    }

    private fun runTool(id: String) {
        when (id) {
            "emoji" -> openPanel(1)
            "clip" -> openPanel(2)
            "edit" -> openPanel(3)
            "settings" -> openPanel(4)
            "modes" -> openPanel(7)
            "translit" -> toggleTranslit()
        }
    }

    private fun toggleTranslit() {
        flushAvro()
        translit = !translit
        putPref("translit", if (translit) 1 else 0)
        if (translit) lang = 1
        mode = 0
        shift = false
        caps = false
        closePanel()
        showToast(
            if (translit) "Avro transliteration ON — ইংরেজি অক্ষরে বাংলা লিখুন (ami → আমি)"
            else "Transliteration OFF — Samsung বাংলা লেআউট"
        )
    }

    private fun loadTools() {
        val p = getSharedPreferences("kb", MODE_PRIVATE)
        fun valid(s: String?, def: String): List<String> =
            (s ?: def).split(",").filter { id -> allTools.any { it.id == id } }.distinct()
        val b = valid(p.getString("tb_bar", null), "emoji,edit,clip").toMutableList()
        while (b.size > maxBarTools) b.removeAt(b.size - 1)
        val pn = valid(p.getString("tb_panel", null), "modes,settings,translit").filter { it !in b }.toMutableList()
        tbBar = b
        tbPanel = pn
        for (t in allTools) if (t.id !in tbBar && t.id !in tbPanel) tbPanel.add(t.id)
    }

    private fun saveTools() {
        getSharedPreferences("kb", MODE_PRIVATE).edit()
            .putString("tb_bar", tbBar.joinToString(","))
            .putString("tb_panel", tbPanel.joinToString(","))
            .apply()
    }

    private fun toolIcon(id: String): View {
        val t = toolById(id) ?: return View(this)
        val v = IconView(t.kind)
        v.tint = if (toolActive(id)) 0xFF2F7CF6.toInt() else 0
        v.background = bg(0)
        v.layoutParams = LinearLayout.LayoutParams(0, dp(40), 1f).apply {
            setMargins(dp(2), dp(3), dp(2), dp(3))
        }
        toolTouch(v, id)
        return v
    }

    private fun toolTile(id: String): View {
        val t = toolById(id) ?: return View(this)
        val tile = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, MP, 1f)
            background = bg(0)
        }
        val ic = IconView(t.kind)
        ic.tint = if (toolActive(id)) 0xFF2F7CF6.toInt() else 0
        ic.layoutParams = LinearLayout.LayoutParams(dp(34), dp(34))
        tile.addView(ic)
        tile.addView(TextView(this).apply {
            text = t.label
            textSize = 12f
            maxLines = 1
            gravity = Gravity.CENTER
            setTextColor(textCol())
            setPadding(0, dp(2), 0, 0)
        })
        toolTouch(tile, id)
        return tile
    }

    // tap = কাজ করবে; "..." প্যানেল খোলা থাকলে চেপে ধরে টানলে (drag) toolbar ↔ প্যানেলে সরানো / সাজানো যায়
    private fun toolTouch(v: View, id: String) {
        var sx = 0f
        var sy = 0f
        var dragging = false
        var moved = false
        val longPress = Runnable {
            if (panel == 5 && !moved) {
                dragging = true
                v.parent?.requestDisallowInterceptTouchEvent(true)
                startDrag(v, id)
            }
        }
        v.setOnTouchListener { _, e ->
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    sx = e.rawX
                    sy = e.rawY
                    dragging = false
                    moved = false
                    v.isPressed = true
                    uiHandler.postDelayed(longPress, 380)
                }
                MotionEvent.ACTION_MOVE -> {
                    if (dragging) {
                        moveGhost(e.rawX, e.rawY)
                    } else if (Math.hypot((e.rawX - sx).toDouble(), (e.rawY - sy).toDouble()) > dp(8)) {
                        moved = true
                        uiHandler.removeCallbacks(longPress)
                    }
                }
                MotionEvent.ACTION_UP -> {
                    v.isPressed = false
                    uiHandler.removeCallbacks(longPress)
                    if (dragging) {
                        dragging = false
                        endDrag(id, e.rawX, e.rawY)
                    } else if (!moved) {
                        feedback(v)
                        runTool(id)
                    }
                }
                MotionEvent.ACTION_CANCEL -> {
                    v.isPressed = false
                    uiHandler.removeCallbacks(longPress)
                    if (dragging) {
                        dragging = false
                        cancelDrag()
                    }
                }
            }
            true
        }
    }

    private fun startDrag(v: View, id: String) {
        val t = toolById(id) ?: return
        val g = IconView(t.kind)
        g.tint = 0xFF2F7CF6.toInt()
        g.cutColor = keyBg()
        g.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(keyBg())
        }
        g.elevation = dp(8).toFloat()
        g.layoutParams = FrameLayout.LayoutParams(dp(52), dp(52), Gravity.TOP or Gravity.START)
        overlay.addView(g)
        ghost = g
        dragSrc = v
        v.alpha = 0.25f
        v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    private fun moveGhost(rx: Float, ry: Float) {
        val g = ghost ?: return
        val b = IntArray(2)
        host.getLocationOnScreen(b)
        g.translationX = rx - b[0] - dp(26)
        g.translationY = ry - b[1] - dp(26) - dp(24)
    }

    private fun cancelDrag() {
        ghost?.let { overlay.removeView(it) }
        ghost = null
        dragSrc?.alpha = 1f
        dragSrc = null
    }

    private fun hit(v: View, rx: Float, ry: Float, pad: Int): Boolean {
        val l = IntArray(2)
        v.getLocationOnScreen(l)
        return rx >= l[0] - pad && rx <= l[0] + v.width + pad && ry >= l[1] - pad && ry <= l[1] + v.height + pad
    }

    private fun endDrag(id: String, rx: Float, ry: Float) {
        val src = dragSrc
        ghost?.let { overlay.removeView(it) }
        ghost = null
        dragSrc?.alpha = 1f
        dragSrc = null
        val bar = sugBox
        val area = moreArea
        val inBar = bar != null && hit(bar, rx, ry, dp(14))
        val inArea = !inBar && area != null && hit(area, rx, ry, 0)
        if (!inBar && !inArea) return
        val oldBar = tbBar.toMutableList()
        val oldPanel = tbPanel.toMutableList()
        tbBar.remove(id)
        tbPanel.remove(id)
        if (inBar && bar != null) {
            if (tbBar.size >= maxBarTools) {
                tbBar = oldBar
                tbPanel = oldPanel
                showToast("Toolbar full — আগে একটা সরান")
                return
            }
            var idx = 0
            val loc = IntArray(2)
            for (i in 0 until bar.childCount) {
                val c = bar.getChildAt(i)
                if (c === src) continue
                c.getLocationOnScreen(loc)
                if (loc[0] + c.width / 2f < rx) idx++
            }
            tbBar.add(idx.coerceIn(0, tbBar.size), id)
        } else {
            var idx = 0
            val loc = IntArray(2)
            val half = dp(38)
            for ((tv, tid) in moreTiles) {
                if (tid == id) continue
                tv.getLocationOnScreen(loc)
                val cy = loc[1] + tv.height / 2f
                val cx = loc[0] + tv.width / 2f
                if (cy < ry - half || (Math.abs(cy - ry) <= half && cx < rx)) idx++
            }
            tbPanel.add(idx.coerceIn(0, tbPanel.size), id)
        }
        saveTools()
        render()
    }

    // ================= Clipboard data =================
    private fun saveClips() {
        getSharedPreferences("kb", MODE_PRIVATE).edit()
            .putString(
                "clips3",
                clips.joinToString("\u001F") {
                    (if (it.locked) "L" else "U") + it.time.toString() + "\u001E" + it.text
                }
            )
            .apply()
    }

    private fun pruneClips() {
        val days = when (clipExpire) {
            1 -> 1L
            2 -> 7L
            3 -> 30L
            else -> 0L
        }
        if (days == 0L) return
        val limit = System.currentTimeMillis() - days * 86400000L
        if (clips.removeAll { !it.locked && it.time < limit }) saveClips()
    }

    private fun recordClip() {
        if (!clipOn) return
        try {
            val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val c = cm.primaryClip ?: return
            if (c.itemCount == 0) return
            if (Build.VERSION.SDK_INT >= 24 &&
                c.description?.extras?.getBoolean("android.content.extra.IS_SENSITIVE", false) == true
            ) return
            val t = c.getItemAt(0).coerceToText(this)?.toString() ?: return
            if (t.isBlank() || t.length > 2000) return
            val now = System.currentTimeMillis()
            val existing = clips.firstOrNull { it.text == t }
            if (existing != null) {
                if (!existing.locked) {
                    clips.remove(existing)
                    clips.add(0, Clip(t, false, now))
                }
            } else {
                clips.add(0, Clip(t, false, now))
                while (clips.count { !it.locked } > 50) {
                    clips.removeAt(clips.indexOfLast { !it.locked })
                }
            }
            pruneClips()
            saveClips()
        } catch (e: Exception) {
        }
    }

    private fun clipKind(t: String): Int {
        val s = t.trim()
        if (s.startsWith("http://") || s.startsWith("https://") ||
            s.startsWith("www.") || s.contains("://")
        ) return 1
        if (s.length in 3..20 && s.any { it.isDigit() } &&
            s.all { it.isDigit() || it == ' ' || it == '+' || it == '-' || it == '(' || it == ')' || it == '.' || it == ',' }
        ) return 2
        return 0
    }

    private fun ago(t: Long): String {
        val m = (System.currentTimeMillis() - t) / 60000L
        return when {
            m < 1 -> "now"
            m < 60 -> m.toString() + "m"
            m < 1440 -> (m / 60).toString() + "h"
            else -> (m / 1440).toString() + "d"
        }
    }

    // ================= Emoji data =================
    private val emojiRanges: List<List<IntRange>> = listOf(
        listOf(0x1F600..0x1F64F, 0x1F910..0x1F917, 0x1F920..0x1F92F, 0x1F970..0x1F97F, 0x1FAE0..0x1FAE8),
        listOf(
            0x1F440..0x1F487, 0x1F90C..0x1F90C, 0x1F90F..0x1F90F, 0x1F918..0x1F91F,
            0x1F930..0x1F939, 0x1F9B0..0x1F9BF, 0x1F9D0..0x1F9DF, 0x1F6B6..0x1F6B6,
            0x1F6C0..0x1F6C0, 0x1FAC0..0x1FAC5, 0x1FAF0..0x1FAF8
        ),
        listOf(
            0x1F300..0x1F32C, 0x1F330..0x1F335, 0x1F337..0x1F344, 0x1F400..0x1F43F,
            0x1F980..0x1F9AF, 0x1FAB0..0x1FABF
        ),
        listOf(
            0x1F32D..0x1F32F, 0x1F345..0x1F37F, 0x1F950..0x1F96F, 0x1F9C0..0x1F9CB,
            0x1FAD0..0x1FADB, 0x2615..0x2615
        ),
        listOf(
            0x1F380..0x1F393, 0x1F3A0..0x1F3D3, 0x1F3F8..0x1F3F8, 0x1F93A..0x1F94F,
            0x26BD..0x26BE, 0x26F3..0x26F3, 0x26F8..0x26F8
        ),
        listOf(
            0x1F3D4..0x1F3F0, 0x1F680..0x1F6C5, 0x1F6D0..0x1F6D7, 0x1F6E0..0x1F6EC,
            0x1F6F0..0x1F6FC, 0x26EA..0x26EA, 0x26F2..0x26F2, 0x26F4..0x26F5,
            0x26FA..0x26FA, 0x26FD..0x26FD, 0x2708..0x2708
        ),
        listOf(
            0x1F4A1..0x1F4FD, 0x1F4FF..0x1F4FF, 0x1F507..0x1F517, 0x1F526..0x1F52E,
            0x1F9E7..0x1F9FF, 0x1FA70..0x1FAAF, 0x231A..0x231B, 0x23F0..0x23F3, 0x260E..0x260E
        ),
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

    // ================= Emoji search data =================
    private val aliasData = listOf(
        "😂🤣" to "lol laugh haha funny lmao joy",
        "❤️😍🥰😘💕💖💗💓" to "love heart like crush",
        "😢😭" to "sad cry crying upset tears",
        "😀😊😁😄😃" to "happy smile smiling glad",
        "😠😡🤬" to "angry mad rage furious",
        "👌👍" to "ok okay good fine nice yes",
        "🙏" to "thanks thank pray please namaste hope",
        "👎❌🙅" to "no bad wrong dislike",
        "👋" to "hi hello hey bye wave",
        "🎉🥳🎊" to "party celebrate congrats congratulations",
        "🔥" to "fire hot lit",
        "😎" to "cool sunglasses",
        "😴💤🌙" to "sleep night tired sleepy",
        "🎂🎈🎁" to "birthday cake gift",
        "💰💵" to "money cash rich dollar",
        "🍔🍕🍟" to "food eat hungry",
        "😘💋" to "kiss",
        "😱😨" to "scared fear shock",
        "🤔" to "think thinking hmm",
        "😉" to "wink"
    )

    private val aliasMap: HashMap<Int, String> by lazy {
        val m = HashMap<Int, String>()
        for ((g, t) in aliasData) {
            var i = 0
            while (i < g.length) {
                val cp = g.codePointAt(i)
                i += Character.charCount(cp)
                if (cp != 0xFE0F) m[cp] = (m[cp] ?: "") + " " + t
            }
        }
        m
    }

    private val popularEmoji = listOf(
        "😂", "❤️", "😘", "🤣", "😍", "😊", "😁", "😭", "😉", "😅", "🤗", "🙄", "😋", "🤔"
    )

    private fun buildSearchIndex(): List<Pair<String, String>> {
        val out = ArrayList<Pair<String, String>>()
        for (cat in 1..8) {
            for (e in emojiList(cat)) {
                val cp = e.codePointAt(0)
                val name = if (Build.VERSION.SDK_INT >= 24) (Character.getName(cp) ?: "") else ""
                out.add(Pair(e, name.lowercase() + " " + (aliasMap[cp] ?: "")))
            }
        }
        for (e in emojiList(9)) {
            val c1 = (e.codePointAt(0) - 0x1F1E6 + 'A'.code).toChar()
            val c2 = (e.codePointAt(2) - 0x1F1E6 + 'A'.code).toChar()
            val code = "" + c1 + c2
            val country = java.util.Locale("", code).getDisplayCountry(java.util.Locale.ENGLISH).lowercase()
            out.add(Pair(e, "flag " + country + " " + code.lowercase()))
        }
        return out
    }

    private fun defaultEmojis(): List<String> {
        val out = LinkedHashSet<String>()
        out.addAll(loadRecent())
        out.addAll(popularEmoji)
        return out.take(14)
    }

    private fun searchResults(q: String): List<String> {
        val query = q.trim()
        if (query.isEmpty()) return defaultEmojis()
        val idx = searchIndex ?: buildSearchIndex().also { searchIndex = it }
        val toks = query.lowercase().split(" ").filter { it.isNotEmpty() }
        val out = ArrayList<String>()
        for ((e, txt) in idx) {
            val words = txt.split(' ', '-')
            if (toks.all { t -> words.any { w -> w.startsWith(t) } }) {
                out.add(e)
                if (out.size >= 14) break
            }
        }
        return out
    }

    // ================= Colors and sizes =================
    private fun bgColor() = if (dark) 0xFF1B1B1B.toInt() else 0xFFDADADA.toInt()
    private fun barColor() = if (dark) 0xFF262626.toInt() else 0xFFD0D0D0.toInt()
    private fun keyBg() = if (dark) 0xFF3C3C3C.toInt() else 0xFFFFFFFF.toInt()
    private fun fnBg() = if (dark) 0xFF2A2A2A.toInt() else 0xFFEBEBEB.toInt()
    private fun pressBg() = if (dark) 0xFF5A5A5A.toInt() else 0xFFB5B5B5.toInt()
    private fun textCol() = if (dark) 0xFFF2F2F2.toInt() else 0xFF1A1A1A.toInt()

    private fun baseKeyH() = when (heightSet) {
        0 -> 42
        2 -> 54
        else -> 48
    }

    // বাংলা (Samsung) অক্ষর কিবোর্ডে ৬ সারি, ইংরেজি/Avro/সিম্বলে ৫ সারি।
    // তাই ইংরেজি/সিম্বলের প্রতিটি সারি একটু লম্বা, যাতে মোট উচ্চতা বাংলার সমান হয়।
    private fun keyH(): Int {
        val b = baseKeyH()
        val letterPanel = panel == 0 || panel == 6
        val bangla = lang == 1 && !translit && mode == 0
        return if (letterPanel && !bangla) Math.round((b + 6) * 6f / 5f) - 6 else b
    }

    private fun putPref(k: String, v: Int) {
        getSharedPreferences("kb", MODE_PRIVATE).edit().putInt(k, v).apply()
    }

    // ================= Lifecycle =================
    override fun onCreate() {
        super.onCreate()
        val p = getSharedPreferences("kb", MODE_PRIVATE)
        themeMode = p.getInt("theme", 0)
        heightSet = p.getInt("height", 1)
        haptic = p.getInt("haptic", 1) == 1
        sound = p.getInt("sound", 0) == 1
        suggestOn = p.getInt("suggest", 1) == 1
        kbMode = p.getInt("kbmode", p.getInt("onehand", 0))
        clipOn = p.getInt("cliphist", 1) == 1
        clipExpire = p.getInt("clipexp", 0)
        autoReplace = p.getInt("autorep", 0) == 1
        autoCap = p.getInt("autocap", 1) == 1
        previewOn = p.getInt("preview", 1) == 1
        translit = p.getInt("translit", 0) == 1
        avroDigits = p.getInt("avrodigits", 1) == 1
        loadTools()

        val saved3 = p.getString("clips3", "") ?: ""
        if (saved3.isNotEmpty()) {
            for (s in saved3.split("\u001F")) {
                val sep = s.indexOf('\u001E')
                if (s.length > 1 && sep > 1) {
                    val tm = s.substring(1, sep).toLongOrNull() ?: System.currentTimeMillis()
                    clips.add(Clip(s.substring(sep + 1), s[0] == 'L', tm))
                }
            }
        } else {
            val old = p.getString("clips2", "") ?: ""
            for (s in old.split("\u001F")) {
                if (s.length > 1) clips.add(Clip(s.substring(1), s[0] == 'L', System.currentTimeMillis()))
            }
        }
        pruneClips()

        try {
            val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            cm.addPrimaryClipChangedListener { recordClip() }
        } catch (e: Exception) {
        }
    }

    override fun onCreateInputView(): View {
        // Android may build the keyboard view again: forget popups that belonged to the old one.
        previewView = null
        langPopup = null
        toastView = null
        ghost = null
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        overlay = FrameLayout(this)
        host = FrameLayout(this)
        host.addView(root, FrameLayout.LayoutParams(MP, WC))
        host.addView(overlay, FrameLayout.LayoutParams(MP, MP))
        render()
        return host
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        if (::root.isInitialized) {
            panel = 0
            selecting = false
            sugExpanded = false
            barMode = 0
            barManual = false
            avroBuf.setLength(0)
            shiftManual = false
            searchQ.setLength(0)
            if (autoCap && lang == 0 && !caps) {
                val m = currentInputConnection?.getCursorCapsMode(info?.inputType ?: 0) ?: 0
                shift = m != 0
            }
            render()
            host.animate().cancel()
            if (restarting) {
                host.alpha = 1f
                host.translationY = 0f
            } else {
                host.alpha = 0f
                host.translationY = dp(26).toFloat()
                host.animate().alpha(1f).translationY(0f).setDuration(190)
                    .setInterpolator(DecelerateInterpolator()).start()
            }
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        avroBuf.setLength(0)
        super.onFinishInputView(finishingInput)
    }

    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int, newSelStart: Int, newSelEnd: Int,
        candidatesStart: Int, candidatesEnd: Int
    ) {
        super.onUpdateSelection(
            oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd
        )
        if (::root.isInitialized) {
            // Avro: কার্সর অন্য জায়গায় সরে গেলে composing বন্ধ (দ্রুত টাইপের সময় পুরনো আপডেট উপেক্ষা)
            if (avroBuf.isNotEmpty() && SystemClock.uptimeMillis() - lastAvroTime > 300 &&
                (candidatesStart < 0 || newSelStart != candidatesEnd || newSelEnd != candidatesEnd)
            ) {
                avroBuf.setLength(0)
                currentInputConnection?.finishComposingText()
            }
            refreshSuggestions()
            // ইংরেজি: বাক্যের শুরুতে নিজে থেকে বড় হাতের অক্ষর
            if (autoCap && lang == 0 && !caps && !shiftManual && panel == 0 && !backHeld && !sugExpanded) {
                val m = currentInputConnection
                    ?.getCursorCapsMode(currentInputEditorInfo?.inputType ?: 0) ?: 0
                val want = m != 0
                if (want != shift) {
                    shift = want
                    render()
                }
            }
            // বাংলা: ব্যঞ্জনের আগে/পরে বদলালে বাম দিকের স্বর/কার অংশ আবার আঁকা হবে
            if (!backHeld) syncBnContext()
        }
    }

    // ================= UI =================
    private fun render() {
        dark = when (themeMode) {
            1 -> false
            2 -> true
            else -> (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
        }
        previewView?.visibility = View.GONE
        hideLangPopup()
        previewKeys.clear()
        curKeyFor.clear()
        moreTiles.clear()
        moreArea = null
        if (panel != 0) sugExpanded = false
        val w = resources.displayMetrics.widthPixels
        val side = (w * 0.22f).toInt()
        val cside = (w * 0.09f).toInt()
        var padL = dp(4)
        var padR = dp(4)
        when (kbMode) {
            1 -> padR += side
            2 -> padL += side
            4 -> {
                padL += cside
                padR += cside
            }
        }
        root.setPadding(padL, dp(6), padR, dp(10))
        root.setBackgroundColor(bgColor())
        root.removeAllViews()
        sugBox = null
        leftIcon = null
        spaceKey = null
        searchCard = null
        searchTv = null
        searchClear = null
        if (panel != 2) {
            menuClip = null
            confirmAll = false
            selMode = false
            selected.clear()
        }
        if (panel != 3) selecting = false
        when (panel) {
            1 -> { root.addView(toolbarRow()); renderEmoji(); return }
            2 -> { root.addView(toolbarRow()); renderClipboard(); return }
            3 -> { root.addView(toolbarRow()); renderTextEdit(); return }
            4 -> { root.addView(toolbarRow()); renderSettings(); return }
            5 -> { root.addView(toolbarRow()); renderMore(); return }
            6 -> { renderSearch(); return }
            7 -> { root.addView(toolbarRow()); renderModes(); return }
        }
        root.addView(toolbarRow())
        if (sugExpanded) {
            renderSugExpanded()
            return
        }
        val start = root.childCount
        when {
            mode != 0 -> renderSymbols()
            lang == 1 && !translit -> renderBangla()
            else -> renderEnglish()
        }
        if (kbMode == 3) splitRows(start)
        root.addView(bottomRow())
    }

    // Split mode: প্রতিটি সারির মাঝখানে ফাঁকা জায়গা
    private fun splitRows(start: Int) {
        for (i in start until root.childCount) {
            val line = root.getChildAt(i) as? LinearLayout ?: continue
            val n = line.childCount
            if (n >= 4) line.addView(spacer(0.9f), n / 2)
        }
    }

    private fun toolbarRow(): LinearLayout {
        val line = newLine()
        val left = IconView(0)
        left.background = bg(0)
        left.layoutParams = LinearLayout.LayoutParams(0, dp(40), 1f).apply {
            setMargins(dp(2), dp(3), dp(2), dp(3))
        }
        left.setOnClickListener {
            feedback(it)
            onBarToggle()
        }
        leftIcon = left
        line.addView(left)
        line.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(1), dp(22)).apply {
                setMargins(dp(1), 0, dp(1), 0)
            }
            setBackgroundColor(0x55888888)
        })
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, dp(40), 7f)
        }
        sugBox = box
        line.addView(box)

        // "..." : tools মোডে → More প্যানেল (Modes/Settings/Transliterate...),
        //          suggestion মোডে → আরও suggestion + Auto replace
        val dots = IconView(4)
        dots.background = if (panel == 5 || sugExpanded) {
            GradientDrawable().apply {
                cornerRadius = dp(20).toFloat()
                setColor(0x55808080)
            }
        } else bg(0)
        dots.layoutParams = LinearLayout.LayoutParams(0, dp(40), 1f).apply {
            setMargins(dp(2), dp(3), dp(2), dp(3))
        }
        dots.setOnClickListener {
            feedback(it)
            if (lastShowSug && panel == 0) {
                sugExpanded = !sugExpanded
                render()
            } else {
                openPanel(5)
            }
        }
        line.addView(dots)
        barFresh = true
        refreshSuggestions()
        return line
    }

    private fun panelHeader(
        title: String,
        rightLabel: String? = null,
        rightColor: Int = 0xFF2F7CF6.toInt(),
        onRight: () -> Unit = {}
    ): LinearLayout {
        val head = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(MP, dp(44))
            setPadding(dp(8), 0, dp(8), 0)
        }
        head.addView(TextView(this).apply {
            text = title
            textSize = 17f
            setTextColor(textCol())
        })
        val chev = IconView(5)
        chev.layoutParams = LinearLayout.LayoutParams(dp(40), dp(40))
        chev.setOnClickListener { closePanel() }
        head.addView(chev)
        head.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(0, 1, 1f) })
        if (rightLabel != null) {
            head.addView(TextView(this).apply {
                text = rightLabel
                textSize = 16f
                setTextColor(rightColor)
                setPadding(dp(8), dp(8), dp(8), dp(8))
                setOnClickListener { onRight() }
            })
        }
        return head
    }

    private fun hint(t: String) = TextView(this).apply {
        text = t
        textSize = 14f
        gravity = Gravity.CENTER
        setTextColor(textCol())
        setPadding(0, dp(50), 0, 0)
    }

    private fun scrollBox(h: Int = 200) = ScrollView(this).apply {
        layoutParams = LinearLayout.LayoutParams(MP, dp(h))
        isVerticalScrollBarEnabled = false
    }

    // ---------- Suggestion bar "..." : বড় suggestion গ্রিড + Auto replace ----------
    private fun renderSugExpanded() {
        val h = 6 * (baseKeyH() + 6)
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = popupBg(16)
            layoutParams = LinearLayout.LayoutParams(MP, dp(h)).apply {
                setMargins(dp(2), dp(2), dp(2), 0)
            }
            setPadding(dp(4), dp(4), dp(4), dp(6))
        }
        val list = candidates(12).drop(3)
        if (list.isEmpty()) {
            card.addView(TextView(this).apply {
                text = "No more suggestions"
                textSize = 14f
                gravity = Gravity.CENTER
                setTextColor(0xFF8A8A8A.toInt())
                setPadding(0, dp(20), 0, dp(20))
            })
        }
        for (chunk in list.chunked(3)) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(MP, dp(50))
            }
            for (s in chunk) {
                row.addView(TextView(this).apply {
                    text = s
                    textSize = 17f
                    maxLines = 1
                    gravity = Gravity.CENTER
                    setTextColor(textCol())
                    background = bg(0)
                    layoutParams = LinearLayout.LayoutParams(0, MP, 1f)
                    setOnClickListener {
                        feedback(it)
                        pick(s)
                    }
                })
            }
            repeat(3 - chunk.size) {
                row.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(0, 1, 1f) })
            }
            card.addView(row)
        }
        card.addView(View(this).apply { layoutParams = LinearLayout.LayoutParams(0, 0, 1f) })
        val ar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = GradientDrawable().apply {
                cornerRadius = dp(22).toFloat()
                setColor(fnBg())
            }
            setPadding(dp(18), 0, dp(14), 0)
            layoutParams = LinearLayout.LayoutParams(MP, dp(48))
        }
        ar.addView(TextView(this).apply {
            text = "Auto replace"
            textSize = 16f
            setTextColor(textCol())
            layoutParams = LinearLayout.LayoutParams(0, WC, 1f)
        })
        val sw = Switch(this)
        sw.isChecked = autoReplace
        sw.setOnCheckedChangeListener { _, c ->
            autoReplace = c
            putPref("autorep", if (c) 1 else 0)
        }
        ar.addView(sw)
        card.addView(ar)
        root.addView(card)
    }

    // ---------- Emoji ----------
    private fun renderEmoji() {
        val items = if (emojiCat == 0) loadRecent() else emojiList(emojiCat)
        val scroll = scrollBox(188)
        scroll.setBackgroundColor(if (dark) 0xFF2A2A2A.toInt() else 0xFFF0F0F0.toInt())
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        if (items.isEmpty()) col.addView(hint("No recent emojis"))
        for (chunk in items.chunked(7)) {
            val line = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            for (e in chunk) line.addView(emojiCell(e))
            repeat(7 - chunk.size) { line.addView(spacer(1f, 52)) }
            col.addView(line)
        }
        scroll.addView(col)
        root.addView(scroll)
        val slide = emojiSlide
        emojiSlide = 0
        if (slide != 0) {
            scroll.translationX = slide * dp(70).toFloat()
            scroll.alpha = 0.2f
            scroll.animate().translationX(0f).alpha(1f).setDuration(150)
                .setInterpolator(DecelerateInterpolator()).start()
        }
        root.addView(emojiBar())
    }

    private fun emojiBarIcon(kind: Int, sel: Boolean, w: Int, onTap: () -> Unit): View {
        val v = IconView(kind)
        v.tint = if (sel) 0xFF2F7CF6.toInt() else 0
        v.cutColor = barColor()
        if (sel && emojiRipple) {
            val d = GradientDrawable()
            d.shape = GradientDrawable.OVAL
            d.setColor(0x55707070)
            v.background = d
            val anim = ValueAnimator.ofInt(255, 0)
            anim.duration = 420
            anim.addUpdateListener { d.alpha = it.animatedValue as Int }
            anim.start()
        } else {
            v.background = ovalBg()
        }
        v.layoutParams = LinearLayout.LayoutParams(dp(w), dp(46))
        v.setOnClickListener {
            feedback(it)
            onTap()
        }
        return v
    }

    private fun emojiBar(): LinearLayout {
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(MP, dp(46))
            setBackgroundColor(barColor())
        }
        bar.addView(emojiBarIcon(19, false, 46) { closePanel() })
        bar.addView(emojiBarIcon(21, false, 40) { enterSearch() })
        bar.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(1), dp(22))
            setBackgroundColor(0x55888888)
        })
        val hs = HorizontalScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, dp(46), 1f)
            isHorizontalScrollBarEnabled = false
        }
        val inner = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = FrameLayout.LayoutParams(WC, dp(46))
        }
        val kinds = listOf(10, 1, 11, 12, 13, 14, 15, 16, 17, 18)
        for ((i, k) in kinds.withIndex()) {
            inner.addView(emojiBarIcon(k, i == emojiCat, 44) {
                emojiSlide = if (i > emojiCat) 1 else if (i < emojiCat) -1 else 0
                emojiRipple = true
                emojiCat = i
                render()
            })
        }
        emojiRipple = false
        hs.addView(inner)
        bar.addView(hs)
        val bk = key("⌫", 1f, flat = true, repeat = true, onHold = { backspaceHold(it) }) { backspace() }
        bk.layoutParams = LinearLayout.LayoutParams(dp(54), dp(46))
        bar.addView(bk)
        return bar
    }

    private fun emojiCell(e: String) = TextView(this).apply {
        text = e
        textSize = 28f
        gravity = Gravity.CENTER
        layoutParams = LinearLayout.LayoutParams(0, dp(52), 1f)
        setOnClickListener {
            feedback(it)
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

    // ---------- Emoji search ----------
    private fun enterSearch() {
        flushAvro()
        searchQ.setLength(0)
        val switched = lang != 0
        lang = 0
        mode = 0
        shift = false
        caps = false
        if (searchIndex == null) searchIndex = buildSearchIndex()
        panel = 6
        render()
        if (switched) {
            showToast("Keyboard automatically changed to a language that supports emoji searches.")
        }
    }

    private fun exitSearch() {
        searchQ.setLength(0)
        panel = 1
        barMode = 1
        barManual = true
        render()
    }

    private fun renderSearch() {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = popupBg(14)
            setPadding(dp(6), dp(6), dp(6), dp(6))
            layoutParams = LinearLayout.LayoutParams(MP, dp(112)).apply {
                setMargins(dp(2), dp(2), dp(2), dp(6))
            }
        }
        searchCard = card
        root.addView(card)

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = GradientDrawable().apply {
                cornerRadius = dp(10).toFloat()
                setColor(fnBg())
            }
            layoutParams = LinearLayout.LayoutParams(MP, dp(44)).apply {
                setMargins(dp(2), 0, dp(2), dp(6))
            }
        }
        val back = IconView(22)
        back.layoutParams = LinearLayout.LayoutParams(dp(40), dp(40))
        back.setOnClickListener {
            feedback(it)
            exitSearch()
        }
        bar.addView(back)
        val tv = TextView(this).apply {
            textSize = 16f
            setTextColor(textCol())
            maxLines = 1
            layoutParams = LinearLayout.LayoutParams(0, WC, 1f)
        }
        searchTv = tv
        bar.addView(tv)
        val clear = TextView(this).apply {
            text = "✕"
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(textCol())
            setPadding(dp(14), dp(8), dp(14), dp(8))
            setOnClickListener {
                feedback(it)
                searchQ.setLength(0)
                updateSearchUi()
            }
        }
        searchClear = clear
        bar.addView(clear)
        root.addView(bar)

        if (mode != 0) renderSymbols() else renderEnglish()
        root.addView(bottomRow())
        updateSearchUi()
    }

    private fun searchText(): CharSequence {
        val q = searchQ.toString()
        if (q.isEmpty()) {
            val s = SpannableString("|Search emojis")
            s.setSpan(ForegroundColorSpan(0xFF2F7CF6.toInt()), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            s.setSpan(ForegroundColorSpan(0xFF8A8A8A.toInt()), 1, s.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            return s
        }
        val s = SpannableString(q + "|")
        s.setSpan(ForegroundColorSpan(0xFF2F7CF6.toInt()), q.length, s.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return s
    }

    private fun updateSearchUi() {
        val card = searchCard ?: return
        card.removeAllViews()
        val res = searchResults(searchQ.toString())
        if (res.isEmpty()) {
            card.addView(TextView(this).apply {
                text = "No emojis found"
                textSize = 14f
                gravity = Gravity.CENTER
                setTextColor(0xFF8A8A8A.toInt())
            })
        } else {
            for (chunk in res.chunked(7)) {
                val line = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(MP, WC)
                }
                for (e in chunk) line.addView(searchCell(e))
                repeat(7 - chunk.size) { line.addView(spacer(1f, 46)) }
                card.addView(line)
            }
        }
        searchTv?.text = searchText()
        searchClear?.visibility = if (searchQ.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun searchCell(e: String) = TextView(this).apply {
        text = e
        textSize = 26f
        gravity = Gravity.CENTER
        layoutParams = LinearLayout.LayoutParams(0, dp(46), 1f)
        setOnClickListener {
            feedback(it)
            currentInputConnection?.commitText(e, 1)
            addRecent(e)
        }
    }

    private fun searchType(s: String) {
        if (s == " ") {
            if (searchQ.isNotEmpty() && searchQ[searchQ.length - 1] != ' ') searchQ.append(' ')
        } else if (searchQ.length < 40) {
            searchQ.append(s.lowercase())
        }
        if (shift && !caps) {
            shift = false
            render()
        } else {
            updateSearchUi()
        }
    }

    // ---------- Clipboard ----------
    private fun deleteAllTap() {
        if (confirmAll) {
            clips.removeAll { !it.locked }
            saveClips()
            confirmAll = false
            render()
        } else {
            confirmAll = true
            render()
            uiHandler.postDelayed({
                if (confirmAll) {
                    confirmAll = false
                    if (panel == 2) render()
                }
            }, 3000)
        }
    }

    private fun pasteSelected() {
        val text = clips.filter { it in selected }.reversed().joinToString("\n") { it.text }
        if (text.isNotEmpty()) currentInputConnection?.commitText(text, 1)
        selMode = false
        selected.clear()
        render()
    }

    private fun lockSelected() {
        val items = clips.filter { it in selected }
        val allLocked = items.isNotEmpty() && items.all { it.locked }
        for (c in items) c.locked = !allLocked
        saveClips()
        selMode = false
        selected.clear()
        render()
    }

    private fun deleteSelected() {
        clips.removeAll { it in selected }
        saveClips()
        selMode = false
        selected.clear()
        render()
    }

    private fun renderClipboard() {
        recordClip()
        if (selMode) {
            root.addView(TextView(this).apply {
                text = selected.size.toString() + " selected"
                textSize = 17f
                setTextColor(textCol())
                setPadding(dp(10), dp(10), 0, dp(4))
            })
            val bar = newLine()
            bar.addView(key("Done", 1f, fn = true, hDp = 38, sp = 13f) {
                selMode = false
                selected.clear()
                render()
            })
            bar.addView(key("Paste", 1f, fn = true, hDp = 38, sp = 13f) { pasteSelected() })
            bar.addView(key("Lock", 1f, fn = true, hDp = 38, sp = 13f) { lockSelected() })
            bar.addView(key("Delete", 1f, fn = true, hDp = 38, sp = 13f) { deleteSelected() })
            root.addView(bar)
        } else {
            root.addView(
                panelHeader(
                    "Clipboard",
                    if (confirmAll) "Confirm?" else "Delete all",
                    if (confirmAll) 0xFFD93025.toInt() else 0xFF2F7CF6.toInt()
                ) { deleteAllTap() }
            )
            val chips = newLine()
            val names = listOf("All", "Locked", "Links", "Numbers")
            for ((i, n) in names.withIndex()) {
                chips.addView(key(n, 1f, fn = i != clipFilter, accent = i == clipFilter, hDp = 34, sp = 13f) {
                    clipFilter = i
                    render()
                })
            }
            root.addView(chips)
        }

        val base = clips.sortedByDescending { it.locked }
        val shown = when (clipFilter) {
            1 -> base.filter { it.locked }
            2 -> base.filter { clipKind(it.text) == 1 }
            3 -> base.filter { clipKind(it.text) == 2 }
            else -> base
        }

        val frame = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(MP, dp(172))
        }
        val scroll = ScrollView(this).apply {
            layoutParams = FrameLayout.LayoutParams(MP, MP)
            isVerticalScrollBarEnabled = false
        }
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        if (shown.isEmpty()) {
            col.addView(hint(if (clipOn) "Nothing here yet" else "Clipboard history is off"))
        }
        for (chunk in shown.chunked(3)) {
            val line = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            for (c in chunk) line.addView(clipCard(c))
            repeat(3 - chunk.size) {
                line.addView(View(this).apply {
      
