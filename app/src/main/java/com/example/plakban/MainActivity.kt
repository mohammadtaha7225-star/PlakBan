package com.example.plakban

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import org.json.JSONArray

class MainActivity : Activity() {
    private lateinit var first: EditText
    private lateinit var letter: EditText
    private lateinit var middle: EditText
    private lateinit var code: EditText
    private lateinit var list: LinearLayout
    private val items = mutableListOf<String>()
    private val prefs by lazy { getSharedPreferences("plates", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(245,247,250)
        window.navigationBarColor = Color.rgb(245,247,250)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(12))
            setBackgroundColor(Color.rgb(245,247,250))
        }
        root.addView(TextView(this).apply {
            text = "پلاک‌بان"; textSize = 25f; gravity = Gravity.CENTER
            setTextColor(Color.rgb(25,45,65)); typeface = Typeface.DEFAULT_BOLD
        }, LinearLayout.LayoutParams(-1, dp(48)))
        root.addView(TextView(this).apply {
            text = "شماره و حرف پلاک را وارد کنید"; textSize = 14f
            gravity = Gravity.CENTER; setTextColor(Color.DKGRAY)
        }, LinearLayout.LayoutParams(-1, dp(32)))

        val plate = FrameLayout(this)
        plate.background = GradientDrawable().apply {
            setColor(Color.WHITE); setStroke(dp(3), Color.BLACK); cornerRadius = dp(14).toFloat()
        }
        plate.addView(PlateArt(this), FrameLayout.LayoutParams(-1, -1))
        first = edit(2)
        letter = edit(1, true)
        middle = edit(3)
        code = edit(2)
        fun place(v: View, left: Float, width: Float, top: Float = .16f, height: Float = .70f) {
            val lp = FrameLayout.LayoutParams(-1, -1)
            lp.width = 0; lp.height = 0
            plate.addView(v, lp)
            v.tag = floatArrayOf(left, width, top, height)
        }
        place(first, .13f, .16f)
        place(letter, .33f, .11f)
        place(middle, .47f, .29f)
        place(code, .80f, .16f, .34f, .55f)
        plate.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            for (v in listOf(first, letter, middle, code)) {
                val a = v.tag as FloatArray
                val w = plate.width; val h = plate.height
                v.layout((w*a[0]).toInt(), (h*a[2]).toInt(),
                    (w*(a[0]+a[1])).toInt(), (h*(a[2]+a[3])).toInt())
            }
        }
        root.addView(plate, LinearLayout.LayoutParams(-1, dp(112)))

        val save = TextView(this).apply {
            text = "✓"; textSize = 26f; gravity = Gravity.CENTER
            setTextColor(Color.WHITE); background = rounded(Color.rgb(25,145,85), 14)
            setOnClickListener { savePlate() }
        }
        root.addView(save, LinearLayout.LayoutParams(dp(64), dp(52)).apply {
            gravity = Gravity.CENTER; topMargin = dp(12); bottomMargin = dp(8)
        })
        root.addView(TextView(this).apply {
            text = "پلاک‌های ذخیره‌شده"; textSize = 18f
            typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
            setTextColor(Color.rgb(35,50,65))
        }, LinearLayout.LayoutParams(-1, dp(42)))
        val scroll = ScrollView(this)
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        load(); render()
    }

    private fun edit(max: Int, isLetter: Boolean = false) = EditText(this).apply {
        inputType = if (isLetter) InputType.TYPE_CLASS_TEXT else InputType.TYPE_CLASS_NUMBER
        filters = if (isLetter) arrayOf(InputFilter.LengthFilter(max), InputFilter.AllCaps())
                  else arrayOf(InputFilter.LengthFilter(max))
        textSize = 23f; gravity = Gravity.CENTER; setTextColor(Color.BLACK)
        setHintTextColor(Color.LTGRAY); hint = if (isLetter) "ب" else "•"
        setPadding(0,0,0,0); background = null; isSingleLine = true
        textDirection = View.TEXT_DIRECTION_LTR
    }

    private fun savePlate() {
        val a=first.text.toString().trim(); val l=letter.text.toString().trim()
        val b=middle.text.toString().trim(); val c=code.text.toString().trim()
        if (a.length!=2 || l.isBlank() || b.length!=3 || c.length!=2) {
            Toast.makeText(this,"همه قسمت‌های پلاک را کامل کنید",Toast.LENGTH_SHORT).show(); return
        }
        items.add(0,"$a $l $b | ایران $c"); persist(); render()
        first.text.clear(); letter.text.clear(); middle.text.clear(); code.text.clear()
        Toast.makeText(this,"پلاک ذخیره شد",Toast.LENGTH_SHORT).show()
    }
    private fun load() {
        val arr=try { JSONArray(prefs.getString("items","[]")) } catch (_:Exception) { JSONArray() }
        for(i in 0 until arr.length()) items.add(arr.optString(i))
    }
    private fun persist() {
        val arr=JSONArray(); items.forEach { arr.put(it) }
        prefs.edit().putString("items",arr.toString()).apply()
    }
    private fun render() {
        list.removeAllViews()
        if(items.isEmpty()) {
            list.addView(TextView(this).apply {
                text="هنوز پلاکی ذخیره نشده است"; gravity=Gravity.CENTER
                setTextColor(Color.GRAY); setPadding(0,dp(24),0,dp(24))
            }); return
        }
        items.forEachIndexed { index, value ->
            val row=LinearLayout(this).apply {
                orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL
                background=rounded(Color.WHITE,12); setPadding(dp(12),dp(8),dp(12),dp(8))
            }
            row.addView(TextView(this).apply {
                text=value; textSize=17f; setTextColor(Color.DKGRAY); gravity=Gravity.CENTER_VERTICAL
            },LinearLayout.LayoutParams(0,dp(48),1f))
            row.addView(TextView(this).apply {
                text="حذف"; setTextColor(Color.rgb(190,45,45)); setPadding(dp(10),0,dp(10),0)
                setOnClickListener {
                    AlertDialog.Builder(this@MainActivity).setMessage("این پلاک حذف شود؟")
                        .setPositiveButton("حذف") { _,_ -> items.removeAt(index); persist(); render() }
                        .setNegativeButton("لغو",null).show()
                }
            })
            list.addView(row,LinearLayout.LayoutParams(-1,-2).apply { bottomMargin=dp(8) })
        }
    }
    private fun rounded(color:Int,r:Int)=GradientDrawable().apply { setColor(color); cornerRadius=dp(r).toFloat() }
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()

    private inner class PlateArt(context: android.content.Context): View(context) {
        private val p=Paint(3)
        override fun onDraw(c:Canvas) {
            super.onDraw(c)
            val w=width.toFloat(); val h=height.toFloat()
            p.color=Color.WHITE; c.drawRect(0f,0f,w,h,p)
            p.color=Color.rgb(20,67,145); c.drawRoundRect(0f,0f,w*.105f,h,dp(8).toFloat(),dp(8).toFloat(),p)
            p.color=Color.WHITE; p.textAlign=Paint.Align.CENTER; p.typeface=Typeface.DEFAULT_BOLD
            p.textSize=h*.16f; c.drawText("IR",w*.052f,h*.43f,p)
            p.textSize=h*.12f; c.drawText("IRAN",w*.052f,h*.59f,p)
            p.color=Color.BLACK; p.strokeWidth=dp(2).toFloat()
            c.drawLine(w*.11f,h*.12f,w*.11f,h*.88f,p)
            c.drawLine(w*.78f,h*.12f,w*.78f,h*.88f,p)
            p.textSize=h*.16f; p.textAlign=Paint.Align.CENTER
            c.drawText("ایران",w*.88f,h*.40f,p)
            p.textSize=h*.13f; c.drawText("IRAN",w*.88f,h*.60f,p)
            p.color=Color.DKGRAY; p.textSize=h*.07f
            c.drawText("پلاک خودرو",w*.50f,h*.94f,p)
        }
    }
}
