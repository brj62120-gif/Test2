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
import android.text.InputType
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
    private var lang = 0 // 0 = English, 1 = বাংলা (Samsung), 2 = Avro
    private var shift = false
    private var caps = false
    private var lastShiftTap = 0L
    private var mode = 0 // 0 = অক্ষর, 1 = symbol 1/2, 2 = symbol 2/2
    private var bnPage = 0
    private var dark = false
    private var emojiMode = false
    private var emojiCat = 1
    private lateinit var root: LinearLayout
    private var sugBox: LinearLayout? = null
    private val uiHandler = Handler(Looper.getMainLooper())
    private val emojiCache = HashMap<Int, List<String>>()
    private val avro = StringBuilder()
    private val learned = HashMap<String, Int>()
    private var learnedLoaded = false

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

    // ================= Avro =================
    private class Tok(val p: String, val a: String, val b: String, val t: Int)

    private val avroToks: List<Tok> = run {
        val l = ArrayList<Tok>()
        fun c(p: String, a: String) { l.add(Tok(p, a, "", 0)) }
        fun v(p: String, a: String, b: String) { l.add(Tok(p, a, b, 1)) }
        c("ngkh", "ঙ্খ"); c("nggh", "ঙ্ঘ"); c("ngk", "ঙ্ক"); c("ngg", "ঙ্গ")
        c("ksh", "ক্ষ"); c("kSh", "ক্ষ"); c("gg", "জ্ঞ")
        c("chh", "ছ"); c("ch", "চ"); c("kh", "খ"); c("gh", "ঘ"); c("jh", "ঝ")
        c("Th", "ঠ"); c("Dh", "ঢ"); c("th", "থ"); c("dh", "ধ"); c("ph", "ফ"); c("bh", "ভ")
        c("sh", "শ"); c("Sh", "ষ"); c("Rh", "ঢ়"); c("Ng", "ঙ")
        c("k", "ক"); c("g", "গ"); c("c", "চ"); c("j", "জ"); c("T", "ট"); c("D", "ড")
        c("N", "ণ"); c("t", "ত"); c("d", "দ"); c("n", "ন"); c("p", "প"); c("f", "ফ")
        c("b", "ব"); c("v", "ভ"); c("m", "ম"); c("z", "য"); c("r", "র"); c("l", "ল")
        c("S", "শ"); c("s", "স"); c("h", "হ"); c("R", "ড়"); c("q", "ক"); c("x", "ক্স")
        l.add(Tok("ng", "ং", "", 2))
        l.add(Tok("y", "", "", 3))
        l.add(Tok("w", "", "", 4))
        v("rri", "ঋ", "ৃ"); v("oi", "ঐ", "ৈ"); v("ou", "ঔ", "ৌ"); v("OI", "ঐ", "ৈ"); v("OU", "ঔ", "ৌ")
        v("ii", "ঈ", "ী"); v("ee", "ঈ", "ী"); v("uu", "ঊ", "ূ"); v("oo", "উ", "ু"); v("aa", "আ", "া")
        v("a", "আ", "া"); v("i", "ই", "ি"); v("I", "ঈ", "ী"); v("u", "উ", "ু"); v("U", "ঊ", "ূ")
        v("e", "এ", "ে"); v("O", "ও", "ো"); v("o", "অ", "")
        l.sortedByDescending { it.p.length }
    }

    private val avroAuto = mapOf(
        "bhalo" to "ভালো", "bhalobashi" to "ভালোবাসি", "tomake" to "তোমাকে",
        "tomar" to "তোমার", "tomra" to "তোমরা", "tomader" to "তোমাদের",
        "kothay" to "কোথায়", "dhonnobad" to "ধন্যবাদ", "dhonnobaad" to "ধন্যবাদ",
        "achi" to "আছি", "acho" to "আছো", "achen" to "আছেন", "ache" to "আছে",
        "korchi" to "করছি", "korcho" to "করছো", "korchen" to "করছেন", "bolo" to "বলো"
    )

    private fun avroConvert(src: String): String {
        avroAuto[src]?.let { return it }
        val out = StringBuilder()
        var s = src
        var afterCons = false
        var i = 0
        while (i < s.length) {
            var tok: Tok? = null
            for (k in avroToks) {
                if (s.startsWith(k.p, i)) {
                    tok = k
                    break
                }
            }
            if (tok == null) {
                val c = s[i]
                if (c.isUpperCase() && c.lowercaseChar() != c) {
                    s = s.substring(0, i) + c.lowercaseChar() + s.substring(i + 1)
                    continue
                }
                out.append(c)
                afterCons = false
                i++
                continue
            }
            when (tok.t) {
                0 -> {
                    if (afterCons) out.append('্')
                    out.append(tok.a)
                    afterCons = true
                }
                1 -> {
                    out.append(if (afterCons) tok.b else tok.a)
                    afterCons = false
                }
                2 -> {
                    out.append(tok.a)
                    afterCons = false
                }
                3 -> {
                    out.append(if (afterCons) "্য" else "য়")
                    afterCons = true
                }
                else -> {
                    if (afterCons) {
                        out.append("্ব")
                    } else {
                        out.append("ও")
                    }
                }
            }
            i += tok.p.length
        }
        return out.toString()
    }

    private fun isLatin(s: String) = s.length == 1 && (s[0] in 'a'..'z' || s[0] in 'A'..'Z')

    private fun flushAvro() {
        if (avro.isEmpty()) return
        currentInputConnection?.commitText(avroConvert(avro.toString()), 1)
        avro.setLength(0)
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

    private fun currentWord(): String {
        if (avro.isNotEmpty()) return avroConvert(avro.toString())
        val before = currentInputConnection?.getTextBeforeCursor(30, 0) ?: return ""
        var i = before.length
        while (i > 0 && isWordChar(before[i - 1])) i--
        return before.substring(i)
    }

    private fun learnCurrent() {
        val w = currentWord()
        if (w.length >= 2) learn(w)
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

    private fun candidates(): List<String> {
        if (isPassword()) return emptyList()
        val w = currentWord()
        if (w.isEmpty()) return emptyList()
        val limit = if (avro.isNotEmpty()) 2 else 3
        val res = ArrayList<String>(matches(w).take(limit))
        if (avro.isNotEmpty()) res.add(avro.toString())
        return res
    }

    private fun refreshSuggestions() {
        val box = sugBox ?: return
        box.removeAllViews()
        if (emojiMode) return
        val list = candidates()
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

    private fun pick(s: String) {
        val ic = currentInputConnection ?: return
        if (avro.isNotEmpty()) {
            avro.setLength(0)
            ic.commitText(s + " ", 1)
        } else {
            val w = currentWord()
            ic.deleteSurroundingText(w.length, 0)
            ic.commitText(s + " ", 1)
        }
        learn(s)
        refreshSuggestions()
    }

    // ================= Emoji =================
    private val emojiTabs = listOf("🕘", "😀", "👍", "🐶", "🍔", "⚽", "🚗", "💡", "❤️", "🏳️")

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

    // ================= রং =================
    private fun bgColor() = if (dark) 0xFF1B1B1B.toInt() else 0xFFDADADA.toInt()
    private fun keyBg() = if (dark) 0xFF3C3C3C.toInt() else 0xFFFFFFFF.toInt()
    private fun fnBg() = if (dark) 0xFF2A2A2A.toInt() else 0xFFEBEBEB.toInt()
    private fun pressBg() = if (dark) 0xFF5A5A5A.toInt() else 0xFFC4C4C4.toInt()
    private fun textCol() = if (dark) 0xFFF2F2F2.toInt() else 0xFF1A1A1A.toInt()

    // ================= Lifecycle =================
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
        avro.setLength(0)
        if (::root.isInitialized) {
            emojiMode = false
            render()
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        flushAvro()
        super.onFinishInputView(finishingInput)
    }

    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int, newSelStart: Int, newSelEnd: Int,
        candidatesStart: Int, candidatesEnd: Int
    ) {
        super.onUpdateSelection(
            oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd
        )
        if (avro.isNotEmpty() && (newSelStart != candidatesEnd || newSelEnd != candidatesEnd)) {
            avro.setLength(0)
            currentInputConnection?.finishComposingText()
        }
        if (::root.isInitialized) refreshSuggestions()
    }

    // ================= UI =================
    private fun render() {
        dark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        root.setBackgroundColor(bgColor())
        root.removeAllViews()
        if (emojiMode) {
            sugBox = null
            renderEmoji()
            return
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
        line.addView(key("😊", 1f, flat = true, hDp = 40, sp = 22f) {
            flushAvro()
            emojiMode = true
            render()
        })
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, dp(40), 9f)
        }
        sugBox = box
        line.addView(box)
        refreshSuggestions()
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
            flushAvro()
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
        line.addView(key(",") { type(",") })
        val spaceLabel = when (lang) {
            1 -> "◂  বাংলা  ▸"
            2 -> "◂  Avro  ▸"
            else -> "◂  English (US)  ▸"
        }
        line.addView(key(spaceLabel, 5f, onLong = { toggleLang() }) { type(" ") })
        line.addView(key(if (lang == 0) "." else "।") { type(if (lang == 1) "।" else ".") })
        line.addView(key("↵", 1.5f, fn = true) { enter() })
        return line
    }

    private fun toggleLang() {
        flushAvro()
        lang = (lang + 1) % 3
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
            line.addView(key(label) { type(commit) })
        }
    }

    // ================= টাইপ করা =================
    private fun isBoundary(s: String) =
        s.isNotEmpty() && !(s[0].isLetter() || s[0] in '\u0980'..'\u09FF')

    private fun type(s: String) {
        val ic = currentInputConnection ?: return
        if (lang == 2 && isLatin(s)) {
            avro.append(s)
            ic.setComposingText(avroConvert(avro.toString()), 1)
        } else {
            if (isBoundary(s)) learnCurrent()
            flushAvro()
            ic.commitText(if (lang == 2 && s == ".") "।" else s, 1)
        }
        if (shift && !caps) {
            shift = false
            render()
        } else {
            refreshSuggestions()
        }
    }

    private fun backspace() {
        val ic = currentInputConnection ?: return
        if (avro.isNotEmpty()) {
            avro.setLength(avro.length - 1)
            if (avro.isEmpty()) {
                ic.setComposingText("", 1)
                ic.finishComposingText()
            } else {
                ic.setComposingText(avroConvert(avro.toString()), 1)
            }
        } else {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
        }
        refreshSuggestions()
    }

    private fun enter() {
        learnCurrent()
        flushAvro()
        if (!sendDefaultEditorAction(true)) {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
        }
        refreshSuggestions()
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
