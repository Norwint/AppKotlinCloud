package com.otcengineering.white_app.fragments;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.otcengineering.white_app.MyApp;
import com.otcengineering.white_app.R;
import com.otcengineering.white_app.components.CustomDialog;
import com.otcengineering.white_app.utils.LanguageUtils;

import java.util.Locale;

/**
 * Created by cenci7
 */

public class BaseFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LanguageUtils.setLocale(MyApp.getUserLocale());
    }

    protected void showCustomDialog(int messageRes, DialogInterface.OnDismissListener listener) {
        final Context ctx = getContext();
        if(ctx == null) {
            return;
        }
        CustomDialog customDialog = new CustomDialog(ctx, messageRes, false);
        customDialog.setOnDismissListener(listener);
        customDialog.show();
    }

    protected void showCustomDialog(int messageRes) {
        final Context ctx = getContext();
        if(ctx == null) {
            return;
        }
        CustomDialog customDialog = new CustomDialog(ctx, messageRes, false);
        customDialog.show();
    }

    protected void showCustomDialog(String message) {
        final Context ctx = getContext();
        if(ctx == null) {
            return;
        }
        CustomDialog customDialog = new CustomDialog(ctx, message, false);
        customDialog.show();
    }

    protected void showCustomDialogError() {
        final Context ctx = getContext();
        if(ctx == null) {
            return;
        }
        CustomDialog customDialog = new CustomDialog(ctx, R.string.error_default, true);
        customDialog.show();
    }

    protected void showCustomDialogError(String message) {
        final Context ctx = getContext();
        if(ctx == null) {
            return;
        }
        CustomDialog customDialog = new CustomDialog(ctx, message, true);
        customDialog.show();
    }

    void changeFragment(Fragment fragment, final int layoutID) {
        try {
            FragmentManager fragmentManager = getChildFragmentManager();
            fragmentManager
                    .beginTransaction()
                    .replace(layoutID, fragment)
                    .commit();
        } catch (Exception e) {
            //Log.e("CommunicationsFragment", "RuntimeException", e);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(wrap(context, MyApp.getUserLocale().getLanguage()));
    }

    private static ContextWrapper wrap(Context context, String language) {
        Resources res = context.getResources();
        android.content.res.Configuration configuration = res.getConfiguration();
        Locale newLocale = Locale.forLanguageTag(language);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(newLocale);
            LocaleList localeList = new LocaleList(newLocale);
            LocaleList.setDefault(localeList);
            configuration.setLocales(localeList);
            context = context.createConfigurationContext(configuration);

            Resources applicationRes = context.getApplicationContext().getResources();
            Configuration applicationConf = applicationRes.getConfiguration();
            applicationConf.setLocale(newLocale);
            applicationRes.updateConfiguration(applicationConf,
                    applicationRes.getDisplayMetrics());
        } else {
            configuration.setLocale(newLocale);
            context = context.createConfigurationContext(configuration);
        }

        return new ContextWrapper(context);
    }
}
