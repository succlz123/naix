package org.succlz123.naix.app

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import org.succlz123.naix.lib.NaixFull

class SecondActivity : Activity() {

    @NaixFull
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        val str = intent.getStringExtra("params")
        (findViewById<View>(R.id.content) as TextView).text = "Go from First Activity $str"
    }
}