package org.succlz123.naix.app;

import android.util.Log;

import org.succlz123.naix.lib.NaixPrinter;

import java.util.ArrayList;
import java.util.List;

import kotlin.jvm.internal.Intrinsics;

public class TestJavaFile {

    public void func1(int xx) {
        java.util.ArrayList<Object> list = new java.util.ArrayList();
        list.add(xx);
        int position = -3;
        int number = 3;
    }

    public final int func2(String pos, double xxx, long yyy) {
        NaixPrinter pp = new NaixPrinter("org/succlz123/naix/app/MainActivity", "func2");
        pp.addParameter("pos", (Object) pos);
        pp.addParameter("xxx", xxx);
        pp.addParameter("yyy", yyy);

        if (Intrinsics.areEqual(pos, "3")) {
            String xx = "cc";
        }

        boolean var8 = false;
        ArrayList list = new ArrayList();
        list.add("cc");
        list.add("cc");
        list.add("cc");
        boolean var9 = false;

        ArrayList list1113 = new ArrayList();
        this.func3(list1113);

        pp.addReturn(3);
        return 3;
    }

    void func3(List list) {
        long time = System.currentTimeMillis();
        Log.d("TestJavaFile", String.valueOf(time));
    }
}
