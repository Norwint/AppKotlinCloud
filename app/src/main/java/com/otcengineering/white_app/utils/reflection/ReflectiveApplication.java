package com.otcengineering.white_app.utils.reflection;

import android.app.Activity;

public class ReflectiveApplication {
    IReflectiveApplication m_app;

    public ReflectiveApplication(IReflectiveApplication ira) {
        m_app = ira;
    }

    public Activity getCurrentActivity() {
        return m_app.getActivity();
    }

    public Reflection getCurrentActivityReflection() {
        return Reflection.of(getCurrentActivity());
    }
}
