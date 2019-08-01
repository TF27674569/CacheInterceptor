package com.ubtech.test;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.ubtech.test", appContext.getPackageName());
    }
    public static String get(){
        try {
            return " 我是try里面的返回值";
        }finally {
            return " 我是finally里面的返回值";
        }
    }


    public static void main(String[] args) {
        System.out.println("---------------------------------");
        System.out.println(get());
    }

}
