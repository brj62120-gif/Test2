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
import android.widget.Button
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class KeyboardService : InputMethodService() {
    private val MP = LinearLayout.LayoutParams.MATCH_PARENT
    private val WC = LinearLayout.LayoutParams.WRAP_CONTENT

    private var lang = 0 // 0 = English, 1 = বাংলা (Samsung)
    private var shift = false
    private var caps = false
    private var lastShiftTap = 0L
    private var mode = 0 // 0 = অক্ষর, 1 = symbol 1/2, 2 = symbol 2/2
    private var bnPage = 0
    private var dark = false
    // 0 = none, 1 = emoji, 2 = clipboard, 3 = text editing, 4 = settings, 5 = more, 6 = emoji search
    private var panel = 0
    private var emojiCat = 1
    private var emojiSlide = 0
    private var emojiRipple = false
    private var selecting = false
    private var barMode = 0 // 0 = auto (tools), 1 = tools, 2 = suggestions
    private var barManual = false
    private var lastShowSug = false
    private var barFresh = true

    private var themeMode = 0
    private var heightSet = 1
    private var haptic = true
    private var sound = false
    private var suggestOn = true
    private var oneHand = 0
    private var clipOn = true
    private var clipExpire = 0 // 0 = never, 1 = 1 day, 2 = 7 days, 3 = 30 days

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

    private fun iconKey(kind: Int, weight: Float = 1f, onTap: () -> Unit): View {
        val v = IconView(kind)
        v.background = bg(0)
        v.layoutParams = LinearLayout.LayoutParams(0, dp(40), weight).apply {
            setMargins(dp(2), dp(3), dp(2), dp(3))
        }
        v.setOnClickListener {
            feedback(it)
            onTap()
        }
        return v
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
            pv.textSize = 26f
            pv.elevation = dp(6).toFloat()
            previewView = pv
        }
        attach(pv)
        val w = v.width + dp(8)
        val h = v.height + dp(8)
        val pos = relPos(v)
        var x = pos[0] + (v.width - w) / 2
        if (x > host.width - w) x = host.width - w
        if (x < 0) x = 0
        var y = pos[1] - h + dp(2)
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

    private fun langName(l: Int) = if (l == 1) "বাংলা" else "English (US)"

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

    // ================= Suggestions =================
    private val enWords = "the be to of and a in that have it for not on with he as you do at this but his by from they we say her she or an will my one all would there their what so up out if about who get which go me when make can like time no just him know take people into year your good some could them see other than then now look only come its over think also back after use two how our work first well way even new want because any these give day most us is are was were been has had did does am hello hi thanks thank please sorry okay yes where why tomorrow today tonight morning night friend family home love happy birthday welcome great nice really very much more still should going doing coming working message phone call tell send share meeting office school food water money problem question answer help need understand remember forget something anything everything nothing someone everyone yesterday thinking wonderful important different information beautiful together possible between another without through before always never maybe probably however actually definitely especially awesome congratulations assalamualaikum inshallah alhamdulillah mashallah bangladesh dhaka internet facebook whatsapp youtube google"
    private val bnWords = "আমি তুমি আপনি সে আমরা তোমরা তারা এই ওই কি কী কেন কোথায় কখন কেমন কত কে কাকে না হ্যাঁ ভালো খারাপ আছি আছো আছেন আছে করছি করছো করছেন করে করা করব করবো হবে হয় হয়েছে হচ্ছে যাব যাবো যাচ্ছি আসব আসবো আসছি বলো বলছি বলুন ধন্যবাদ দুঃখিত অনেক একটু এখন তখন আজ কাল আগামীকাল গতকাল সকাল দুপুর বিকাল সন্ধ্যা রাত বন্ধু ভাই বোন মা বাবা বাড়ি স্কুল কলেজ অফিস কাজ টাকা খাবার পানি ভাত চা সালাম শুভ জন্মদিন শুভেচ্ছা স্বাগতম ঠিক থাকো থাকি দেখা তাহলে কিন্তু এবং অথবা যদি তবে তাই কারণ সবাই সব কিছু কোনো একটা একটি দুটি তিনটি আমার তোমার আপনার তার আমাদের তোমাদের তাদের বাংলা বাংলাদেশ ঢাকা ভালোবাসা ভালোবাসি মন মানুষ সময় দিন বছর মাস সপ্তাহ প্রশ্ন উত্তর সমস্যা সাহায্য দরকার চাই পারি পারো পারেন জানি জানো জানেন বুঝি বুঝতে শুনছি দেখছি খাচ্ছি খেয়েছো ঘুমাচ্ছি পড়ছি লিখছি কথা ফোন মেসেজ খবর নতুন পুরনো বড় ছোট"
    private val dict by lazy { (enWords + " " + bnWords).split(" ").filter { it.isNotEmpty() } }

    private fun isWordChar(c: Char) = c.isLetter() || c in '\u0980'..'\u09FF' || c == '\''

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

    private fun nextWords(prev: String): List<String> {
        if (prev.isEmpty()) return emptyList()
        loadBigrams()
        val pre = prev.lowercase() + " "
        return bigrams.entries
            .filter { it.key.startsWith(pre) }
            .sortedByDescending { it.value }
            .map { it.key.substring(pre.length) }
            .take(3)
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

    private fun candidates(): List<String> {
        if (isPassword()) return emptyList()
        val w = currentWord()
        if (w.isEmpty()) {
            val n = nextWords(previousWord())
            return if (n.isNotEmpty()) n else starters()
        }
        return matches(w).take(3)
    }

    private fun refreshSuggestions() {
        val box = sugBox ?: return
        if (barMode == 2 && !barManual && panel == 0 && fieldEmpty()) barMode = 0
        val showSug = panel == 0 && suggestOn && barMode == 2
        val list = if (showSug) candidates() else emptyList()
        box.removeAllViews()
        if (!showSug) {
            box.addView(iconKey(1) {
                panel = if (panel == 1) 0 else 1
                render()
            })
            box.addView(iconKey(3) {
                panel = if (panel == 3) 0 else 3
                render()
            })
            box.addView(iconKey(2) {
                panel = if (panel == 2) 0 else 2
                render()
            })
        } else {
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

    private fun onBarToggle() {
        if (panel != 0) {
            panel = 0
            render()
            return
        }
        val icon = leftIcon ?: return
        val box = sugBox ?: return
        val showingSug = lastShowSug
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
            refreshSuggestions()
            box.animate().alpha(1f).setDuration(180).start()
        }.start()
    }

    private fun pick(s: String) {
        val ic = currentInputConnection ?: return
        val prev = previousWord()
        val w = currentWord()
        ic.deleteSurroundingText(w.length, 0)
        ic.commitText(s + " ", 1)
        learn(s)
        learnBigram(prev, s)
        refreshSuggestions()
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

    private fun keyH() = when (heightSet) {
        0 -> 42
        2 -> 54
        else -> 48
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
        oneHand = p.getInt("onehand", 0)
        clipOn = p.getInt("cliphist", 1) == 1
        clipExpire = p.getInt("clipexp", 0)

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
            barMode = 0
            barManual = false
            searchQ.setLength(0)
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

    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int, newSelStart: Int, newSelEnd: Int,
        candidatesStart: Int, candidatesEnd: Int
    ) {
        super.onUpdateSelection(
            oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd
        )
        if (::root.isInitialized) refreshSuggestions()
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
        val side = if (oneHand == 0) 0 else (resources.displayMetrics.widthPixels * 0.22f).toInt()
        root.setPadding(
            dp(4) + (if (oneHand == 2) side else 0), dp(6),
            dp(4) + (if (oneHand == 1) side else 0), dp(10)
        )
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
        }
        root.addView(toolbarRow())
        when {
            mode != 0 -> renderSymbols()
            lang == 1 -> renderBangla()
            else -> renderEnglish()
        }
        root.addView(bottomRow())
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
        line.addView(iconKey(4, 1f) {
            panel = if (panel == 5) 0 else 5
            render()
        })
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
        chev.setOnClickListener {
            panel = 0
            render()
        }
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
        bar.addView(emojiBarIcon(19, false, 46) {
            panel = 0
            render()
        })
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
        val bk = key("⌫", 1f, flat = true, repeat = true) { backspace() }
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
                    layoutParams = LinearLayout.LayoutParams(0, dp(10), 1f)
                })
            }
            col.addView(line)
        }
        scroll.addView(col)
        frame.addView(scroll)
        val m = menuClip
        if (m != null) frame.addView(clipMenuView(m))
        root.addView(frame)
    }

    private fun clipCard(c: Clip): View {
        val isSel = c in selected
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(7), dp(8), dp(5))
            background = GradientDrawable().apply {
                cornerRadius = dp(8).toFloat()
                if (isSel) {
                    setStroke(dp(2), 0xFF34A853.toInt())
                } else if (c.locked) {
                    setStroke(dp(2), 0xFF2F7CF6.toInt())
                }
                setColor(keyBg())
            }
            layoutParams = LinearLayout.LayoutParams(0, dp(92), 1f).apply {
                setMargins(dp(3), dp(3), dp(3), dp(3))
            }
        }
        card.addView(TextView(this).apply {
            text = c.text
            textSize = 13f
            maxLines = 4
            ellipsize = TextUtils.TruncateAt.END
            setTextColor(textCol())
            layoutParams = LinearLayout.LayoutParams(MP, 0, 1f)
        })
        val kind = clipKind(c.text)
        card.addView(TextView(this).apply {
            text = (if (isSel) "✓ " else if (c.locked) "Locked · " else "") +
                ago(c.time) +
                (if (kind == 1) " · Link" else if (kind == 2) " · Number" else "")
            textSize = 10f
            maxLines = 1
            setTextColor(0xFF8A8A8A.toInt())
        })
        card.setOnClickListener {
            feedback(it)
            if (selMode) {
                if (c in selected) selected.remove(c) else selected.add(c)
                render()
            } else {
                currentInputConnection?.commitText(c.text, 1)
            }
        }
        card.setOnLongClickListener {
            if (!selMode) {
                menuClip = c
                render()
            }
            true
        }
        return card
    }

    private fun clipMenuView(c: Clip): View {
        val dim = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(MP, MP)
            setBackgroundColor(0x55000000)
            setOnClickListener {
                menuClip = null
                render()
            }
        }
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                cornerRadius = dp(18).toFloat()
                setColor(keyBg())
            }
            layoutParams = FrameLayout.LayoutParams(dp(260), WC, Gravity.CENTER)
            setPadding(dp(6), dp(4), dp(6), dp(4))
        }
        card.addView(menuItem("Delete from clipboard") {
            clips.remove(c)
            menuClip = null
            saveClips()
            render()
        })
        card.addView(menuItem(if (c.locked) "Unlock from clipboard" else "Lock to clipboard") {
            c.locked = !c.locked
            menuClip = null
            saveClips()
            render()
        })
        card.addView(menuItem("Select") {
            selMode = true
            selected.clear()
            selected.add(c)
            menuClip = null
            render()
        })
        dim.addView(card)
        return dim
    }

    private fun menuItem(label: String, onTap: () -> Unit) = TextView(this).apply {
        text = label
        textSize = 16f
        setTextColor(textCol())
        setPadding(dp(18), dp(14), dp(18), dp(14))
        setOnClickListener { onTap() }
    }

    // ---------- Text editing ----------
    private fun sendArrow(code: Int) {
        val ic = currentInputConnection ?: return
        val now = SystemClock.uptimeMillis()
        val meta = if (selecting) KeyEvent.META_SHIFT_ON else 0
        ic.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, code, 0, meta))
        ic.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_UP, code, 0, meta))
    }

    private fun menuAction(id: Int) {
        currentInputConnection?.performContextMenuAction(id)
    }

    private fun renderTextEdit() {
        val top = newLine()
        top.addView(key("⇤", 1f, fn = true) { sendArrow(KeyEvent.KEYCODE_MOVE_HOME) })
        top.addView(key("Select all", 4f, fn = true, sp = 15f) { menuAction(android.R.id.selectAll) })
        top.addView(key("⇥", 1f, fn = true) { sendArrow(KeyEvent.KEYCODE_MOVE_END) })
        top.addView(key("✕", 1.5f, flat = true) {
            panel = 0
            selecting = false
            render()
        })
        root.addView(top)

        val body = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(MP, WC)
        }
        val left = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, WC, 6f)
        }
        val up = newLine()
        up.addView(spacer(1.5f))
        up.addView(key("↑", 1f, fn = true, repeat = true) { sendArrow(KeyEvent.KEYCODE_DPAD_UP) })
        up.addView(spacer(1.5f))
        left.addView(up)

        val mid = newLine()
        mid.addView(key("←", 1f, fn = true, repeat = true) { sendArrow(KeyEvent.KEYCODE_DPAD_LEFT) })
        mid.addView(key("Select", 2f, fn = !selecting, accent = selecting, sp = 14f) {
            selecting = !selecting
            render()
        })
        mid.addView(key("→", 1f, fn = true, repeat = true) { sendArrow(KeyEvent.KEYCODE_DPAD_RIGHT) })
        left.addView(mid)

        val down = newLine()
        down.addView(spacer(1.5f))
        down.addView(key("↓", 1f, fn = true, repeat = true) { sendArrow(KeyEvent.KEYCODE_DPAD_DOWN) })
        down.addView(spacer(1.5f))
        left.addView(down)

        val right = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, WC, 3f)
        }
        val r1 = newLine()
        r1.addView(key("Cut", 1f, fn = true, sp = 14f) { menuAction(android.R.id.cut) })
        right.addView(r1)
        val r2 = newLine()
        r2.addView(key("Copy", 1f, fn = true, sp = 14f) { menuAction(android.R.id.copy) })
        right.addView(r2)
        val r3 = newLine()
        r3.addView(key("Paste", 1f, fn = true, sp = 14f) { menuAction(android.R.id.paste) })
        right.addView(r3)
        val r4 = newLine()
        r4.addView(backKey(1f))
        right.addView(r4)

        body.addView(left)
        body.addView(right)
        root.addView(body)
    }

    // ---------- More ----------
    private fun renderMore() {
        root.addView(panelHeader("More"))
        val themeNames = listOf("Auto", "Light", "Dark")
        val heightNames = listOf("Small", "Medium", "Large")
        val handNames = listOf("Off", "Left", "Right")

        fun tile(label: String, onTap: () -> Unit): Button =
            key(label, 1f, fn = true, hDp = 64, sp = 14f, onTap = onTap)

        val r1 = newLine()
        r1.addView(tile("Settings") {
            panel = 4
            render()
        })
        r1.addView(tile("One-handed\n" + handNames[oneHand]) {
            oneHand = (oneHand + 1) % 3
            putPref("onehand", oneHand)
            render()
        })
        r1.addView(tile("Theme\n" + themeNames[themeMode]) {
            themeMode = (themeMode + 1) % 3
            putPref("theme", themeMode)
            render()
        })
        root.addView(r1)

        val r2 = newLine()
        r2.addView(tile("Key height\n" + heightNames[heightSet]) {
            heightSet = (heightSet + 1) % 3
            putPref("height", heightSet)
            render()
        })
        r2.addView(tile("Vibration\n" + (if (haptic) "On" else "Off")) {
            haptic = !haptic
            putPref("haptic", if (haptic) 1 else 0)
            render()
        })
        r2.addView(tile("Key sound\n" + (if (sound) "On" else "Off")) {
            sound = !sound
            putPref("sound", if (sound) 1 else 0)
            render()
        })
        root.addView(r2)
    }

    // ---------- Settings ----------
    private fun renderSettings() {
        root.addView(panelHeader("Settings"))

        val scroll = scrollBox()
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        fun row(title: String, opts: List<String>, sel: Int, pick: (Int) -> Unit) {
            col.addView(TextView(this).apply {
                text = title
                textSize = 13f
                setTextColor(textCol())
                setPadding(dp(6), dp(8), 0, 0)
            })
            val line = newLine()
            for ((i, o) in opts.withIndex()) {
                line.addView(key(o, 1f, fn = i != sel, accent = i == sel, hDp = 38, sp = 14f) { pick(i) })
            }
            col.addView(line)
        }

        row("Theme", listOf("Auto", "Light", "Dark"), themeMode) {
            themeMode = it
            putPref("theme", it)
            render()
        }
        row("Key height", listOf("Small", "Medium", "Large"), heightSet) {
            heightSet = it
            putPref("height", it)
            render()
        }
        row("Vibration", listOf("On", "Off"), if (haptic) 0 else 1) {
            haptic = it == 0
            putPref("haptic", if (haptic) 1 else 0)
            render()
        }
        row("Key sound", listOf("On", "Off"), if (sound) 0 else 1) {
            sound = it == 0
            putPref("sound", if (sound) 1 else 0)
            render()
        }
        row("Word suggestions", listOf("On", "Off"), if (suggestOn) 0 else 1) {
            suggestOn = it == 0
            putPref("suggest", if (suggestOn) 1 else 0)
            render()
        }
        row("One-handed mode", listOf("Off", "Left", "Right"), oneHand) {
            oneHand = it
            putPref("onehand", it)
            render()
        }
        row("Clipboard history", listOf("On", "Off"), if (clipOn) 0 else 1) {
            clipOn = it == 0
            putPref("cliphist", if (clipOn) 1 else 0)
            render()
        }
        row("Auto-delete clips", listOf("Never", "1 day", "7 days", "30 days"), clipExpire) {
            clipExpire = it
            putPref("clipexp", it)
            pruneClips()
            render()
        }

        col.addView(TextView(this).apply {
            text = "Clear data"
            textSize = 13f
            setTextColor(textCol())
            setPadding(dp(6), dp(8), 0, 0)
        })
        val reset = newLine()
        reset.addView(key("Clear learned words", 1f, fn = true, hDp = 38, sp = 13f) {
            learned.clear()
            learnedLoaded = true
            bigrams.clear()
            bigramsLoaded = true
            getSharedPreferences("kb", MODE_PRIVATE).edit().remove("words").remove("bigrams").apply()
            render()
        })
        reset.addView(key("Clear clipboard", 1f, fn = true, hDp = 38, sp = 13f) {
            clips.clear()
            saveClips()
            render()
        })
        col.addView(reset)

        scroll.addView(col)
        root.addView(scroll)
    }

    // ---------- Letter keyboards ----------
    private fun renderEnglish() {
        for ((i, r) in enRows.withIndex()) {
            val line = newLine()
            if (i == 2) line.addView(spacer(0.5f))
            if (i == 3) line.addView(shiftKey())
            for (k in r.split(" ")) {
                val t = if (shift && i > 0) k.uppercase() else k
                line.addView(key(t, preview = true) { type(t) })
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
        val rows = (if (lang == 1) bnSym else enSym)[mode - 1]
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
            line.addView(key(if (lang == 1) "কখগ" else "ABC", 1.5f, fn = true) {
                mode = 0
                render()
            })
        } else {
            line.addView(key("!#1", 1.5f, fn = true) {
                mode = 1
                render()
            })
        }
        line.addView(key(",", preview = true) { type(",") })
        val spaceLabel = "◂  " + langName(lang) + "  ▸"
        val sk = key(spaceLabel, 5f, onSwipe = { toggleLang() }) { type(" ") }
        spaceKey = sk
        line.addView(sk)
        line.addView(key(if (lang == 0) "." else "।", preview = true) { type(if (lang == 0) "." else "।") })
        if (panel == 6) {
            line.addView(fnIcon(21, 1.5f) { })
        } else {
            line.addView(key("↵", 1.5f, fn = true) { enter() })
        }
        return line
    }

    private fun toggleLang() {
        if (panel == 6) return
        lang = if (lang == 0) 1 else 0
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
        key("⌫", w, fn = true, repeat = true) { backspace() }

    private fun addKeys(line: LinearLayout, spec: String, marks: Boolean = false) {
        for (tok in spec.split(" ")) {
            val i = tok.indexOf('|')
            val commit = if (i > 0) tok.substring(i + 1) else tok
            var label = if (i > 0) tok.substring(0, i) else tok
            if (marks && i <= 0 && label[0] in "ািীুূৃেৈোৌ্ংঃঁ") label = "◌" + label
            line.addView(key(label, preview = true) { type(commit) })
        }
    }

    // ================= Typing =================
    private fun isBoundary(s: String) =
        s.isNotEmpty() && !(s[0].isLetter() || s[0] in '\u0980'..'\u09FF')

    private fun type(s: String) {
        if (panel == 6) {
            searchType(s)
            return
        }
        val ic = currentInputConnection ?: return
        if (isBoundary(s)) learnCurrent()
        ic.commitText(s, 1)
        barMode = 2
        barManual = false
        if (shift && !caps) {
            shift = false
            render()
        } else {
            refreshSuggestions()
        }
    }

    private fun backspace() {
        if (panel == 6) {
            if (searchQ.isNotEmpty()) searchQ.setLength(searchQ.length - 1)
            updateSearchUi()
            return
        }
        sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
        refreshSuggestions()
    }

    private fun enter() {
        learnCurrent()
        if (!sendDefaultEditorAction(true)) {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
        }
        refreshSuggestions()
    }

    private fun newLine() = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        layoutParams = LinearLayout.LayoutParams(MP, WC)
    }

    private fun spacer(w: Float, hDp: Int = 0) = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(0, dp(if (hDp > 0) hDp else keyH()), w)
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
        preview: Boolean = false,
        hDp: Int = 0,
        sp: Float = 0f,
        onSwipe: ((Int) -> Unit)? = null,
        onTap: () -> Unit
    ): Button = Button(this).apply {
        val h = if (hDp > 0) hDp else keyH()
        text = label
        isAllCaps = false
        textSize = if (sp > 0f) sp else when {
            label.length <= 1 -> 20f
            label.length <= 3 -> 17f
            else -> 14f
        }
        setTextColor(if (accent) 0xFF2F7CF6.toInt() else textCol())
        background = bg(if (flat) 0 else if (fn) fnBg() else keyBg())
        stateListAnimator = null
        minWidth = 0
        minimumWidth = 0
        minHeight = 0
        minimumHeight = 0
        setPadding(0, 0, 0, 0)
        layoutParams = LinearLayout.LayoutParams(0, dp(h), weight).apply {
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
                        feedback(v)
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
        } else if (onSwipe != null) {
            var downX = 0f
            var swiped = false
            var dir = 1
            setOnTouchListener { v, e ->
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = e.x
                        swiped = false
                        v.isPressed = true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val far = Math.abs(e.x - downX) > dp(14)
                        if (far && !swiped) {
                            swiped = true
                            dir = if (e.x > downX) 1 else -1
                            if (panel != 6) showLangPopup(v, dir, langName(1 - lang))
                        } else if (!far && swiped) {
                            swiped = false
                            hideLangPopup()
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        v.isPressed = false
                        if (swiped) {
                            hideLangPopup()
                            feedback(v)
                            onSwipe(dir)
                            if (panel != 6) flashLangPopup()
                        } else {
                            feedback(v)
                            onTap()
                        }
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        v.isPressed = false
                        hideLangPopup()
                    }
                }
                true
            }
        } else {
            setOnClickListener {
                feedback(it)
                onTap()
            }
            if (preview) {
                setOnTouchListener { v, e ->
                    when (e.action) {
                        MotionEvent.ACTION_DOWN -> showPreview(v, label)
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> hidePreview()
                    }
                    false
                }
            }
        }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
