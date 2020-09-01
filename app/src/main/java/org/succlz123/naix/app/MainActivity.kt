package org.succlz123.naix.app

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import org.succlz123.naix.lib.Naix
import org.succlz123.naix.lib.NaixFull
import org.succlz123.naix.lib.NaixOption
import org.succlz123.naix.lib.NaixPrinter

class MainActivity : Activity() {
    lateinit var content: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        content = findViewById(R.id.content)

        NaixOption.set { className, methodName, msg ->
            Log.i("Naix ", msg)
            content.append("\n")
            content.append(msg)
        }

        func1("123", "test", 1.2)
        func2()
        func3()
        func4("321", "tset", 2.1)
        func5()
    }

    @NaixFull
    fun func1(a: String?, b: String?, c: Double): String {
        return "test"
    }

    @NaixFull
    fun func2(): Int {
        return 1
    }

    @NaixFull
    fun func3(): Double? {
        return 1.1
    }

    @NaixFull
    fun func4(a: String?, b: String?, c: Double): String {
        if (a == "3") {
            val xx = "cc"
            xx.split("33")
        }
        val list = arrayListOf<String>()
        funcTest(list)
        val list111 = arrayListOf<String>()
        list111.add(b.orEmpty())
        funcTest(list111)
        val list1113 = arrayListOf<String>()
        funcTest(list1113)
        return "ccc"
    }

    @NaixFull
    fun func5() {
        Log.d("123", "31")
    }

    @Naix
    fun funcTest(pos: Int, xxx: Double, yyy: Int): Int {
        val np = NaixPrinter("123", "hhahtest")
        np.addParameter("pos", pos)
        np.addParameter("pos", pos)

        np.addCallMethod("xxx")
        funcTest(null)

        val yyy = 3
        val xx = yyy
        np.addReturn(xx)
        np.print()
        return xx
    }

    fun funcTest(list: List<String>?) {
        val xx = "cc"
        var yy = xx
    }
}
