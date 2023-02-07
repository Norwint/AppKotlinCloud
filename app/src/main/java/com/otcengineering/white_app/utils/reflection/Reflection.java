package com.otcengineering.white_app.utils.reflection;

import android.view.KeyEvent;
import android.widget.EditText;

import com.otcengineering.white_app.MyApp;
import com.otcengineering.white_app.utils.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Reflection {
    public Object value;

    public static Reflection instance(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Reflection ref = new Reflection();
        ref.value = Class.forName(className).newInstance();
        return ref;
    }

    public static Reflection of(Object obj) {
        Reflection ref = new Reflection();
        ref.value = obj;
        return ref;
    }

    public Reflection getSuperClass() {
        Reflection ref = new Reflection();
        Class cls = value.getClass().getSuperclass();
        ref.value = cls.cast(value);
        return ref;
    }

    public Reflection getField(String field) throws NoSuchFieldException, IllegalAccessException {
        Field fld = value.getClass().getDeclaredField(field);
        fld.setAccessible(true);
        Reflection ref = new Reflection();
        ref.value = fld.get(value);
        return ref;
    }

    public <T> T getValue() {
        return (T) value;
    }

    private void delay(float seconds) {
        long timeout = System.currentTimeMillis() + (long)(seconds * 1000);
        while (timeout - System.currentTimeMillis() > 0) {

        }
    }

    public Reflection write(String text) {
        if (value instanceof EditText) {
            EditText et = (EditText) value;
            Utils.runOnMainThread(et::requestFocus);
            for (int i = 0; i < text.length(); ++i) {
                char ch = text.charAt(i);
                boolean shift = ch >= 'A' && ch <= 'Z';

                KeyEvent keyEvent = new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, getKeyCode(ch), 0, shift ? KeyEvent.META_SHIFT_ON : 0);
                Utils.runOnMainThread(() -> MyApp.getCurrentActivity().dispatchKeyEvent(keyEvent));

                delay(0.1f);

                KeyEvent keyEvent2 = new KeyEvent(0, 0, KeyEvent.ACTION_UP, getKeyCode(ch), 0, shift ? KeyEvent.META_SHIFT_ON : 0);
                Utils.runOnMainThread(() -> MyApp.getCurrentActivity().dispatchKeyEvent(keyEvent2));
            }
        }

        return this;
    }

    public Reflection getFunction(String functionName, Class... types) throws NoSuchMethodException {
        Method method = value.getClass().getDeclaredMethod(functionName, types);
        method.setAccessible(true);
        Reflection refl = new Reflection();
        refl.value = method;
        return refl;
    }

    public void call(Object caller, Object... args) throws InvocationTargetException, IllegalAccessException {
        if (value instanceof Method) {
            Method method = (Method) value;
            method.invoke(caller, args);
        }
    }

    private int getKeyCode(char ch) {
        ch = Character.toLowerCase(ch);
        if (ch >= '0' && ch <= '9') {
            return 7 + (ch - '0');
        } else if (ch >= 'a' && ch <= 'z') {
            return 29 + (ch - 'a');
        } else if (ch == '.') {
            return KeyEvent.KEYCODE_PERIOD;
        } else if (ch == '@') {
            return KeyEvent.KEYCODE_AT;
        } else if (ch == ' ') {
            return KeyEvent.KEYCODE_SPACE;
        } else if (ch == 'ø') {
            return KeyEvent.KEYCODE_BACK;
        }
        return 0;
    }
}
