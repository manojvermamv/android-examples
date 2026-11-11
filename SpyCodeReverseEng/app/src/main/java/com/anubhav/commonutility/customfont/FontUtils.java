package com.anubhav.commonutility.customfont;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.anubhav.codereverse.R;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;


public class FontUtils {

    public FontUtils() {
    }

    public static String getFontPath(Context context) {
        return context.getString(R.string.app_font_regular);
    }

    public static String getFontPathLight(Context context) {
        return context.getString(R.string.app_font_light);
    }

    public static String getFontPathBold(Context context) {
        return context.getString(R.string.app_font_bold);
    }

    public static void setFont(Context context, ViewGroup root) {
        Typeface font = Typeface.createFromAsset(context.getAssets(), getFontPath(context));
        FontUtils.setFont(root, font);
    }

    public static void setFont(ViewGroup group, Typeface font) {
        Context context = group.getContext();
        Typeface fontBold = Typeface.createFromAsset(context.getAssets(), getFontPathBold(context));
        int count = group.getChildCount();
        View v;
        for (int i = 0; i < count; i++) {
            v = group.getChildAt(i);
            try {
                if (v instanceof TextView || v instanceof EditText || v instanceof Button) {
                    if (((TextView) v).getTypeface().getStyle() == Typeface.BOLD) {
                        ((TextView) v).setTypeface(fontBold);
                    } else {
                        ((TextView) v).setTypeface(font);
                    }
                } else if (v instanceof EditText || v instanceof Button) {
                    if (((EditText) v).getTypeface().getStyle() == Typeface.BOLD) {
                        ((EditText) v).setTypeface(fontBold);
                    } else {
                        ((EditText) v).setTypeface(font);
                    }
                } else if (v instanceof Button) {
                    if (((Button) v).getTypeface().getStyle() == Typeface.BOLD) {
                        ((Button) v).setTypeface(fontBold);
                    } else {
                        ((Button) v).setTypeface(font);
                    }
                } else if (v instanceof ViewGroup)
                    setFont((ViewGroup) v, font);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void applyFontToToolbar(Toolbar toolbar, Typeface typeface) {
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View view = toolbar.getChildAt(i);
            if (view instanceof TextView) {
                TextView tv = (TextView) view;
                tv.setTypeface(typeface);
                break;
            }
        }
    }

    public void applyFontToMenu(Menu menu, Typeface typeface) {
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            SubMenu subMenu = menuItem.getSubMenu();
            if (subMenu != null && subMenu.size() > 0) {
                for (int j = 0; j < subMenu.size(); j++) {
                    MenuItem subMenuItem = subMenu.getItem(j);
                    applyFontToMenuItem(subMenuItem, typeface);
                }
            }
            applyFontToMenuItem(menuItem, typeface);
        }
    }

    public void applyFontToSubMenu(SubMenu menu, Typeface typeface) {
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            SubMenu subMenu = menuItem.getSubMenu();
            if (subMenu != null && subMenu.size() > 0) {
                for (int j = 0; j < subMenu.size(); j++) {
                    MenuItem subMenuItem = subMenu.getItem(j);
                    applyFontToMenuItem(subMenuItem, typeface);
                }
            }
            applyFontToMenuItem(menuItem, typeface);
        }
    }

    private void applyFontToMenuItem(MenuItem mi, Typeface typeface) {
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new MenuItemTypefaceSpan("", typeface),
                0, mNewTitle.length(),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
    }

    public void applyFontToNavigationView(NavigationView navigationView, Typeface typeface) {
        applyFontToMenu(navigationView.getMenu(), typeface);
    }

    public void applyFontToView(View view, Typeface typeface) {
        if (view instanceof TextView) {
            TextView tv = (TextView) view;
            tv.setTypeface(typeface);
        }
    }

    public void applyFontToRadioGroup(RadioGroup radioGroup, Typeface typeface) {
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            View tabViewChild = radioGroup.getChildAt(i);
            if (tabViewChild instanceof TextView) {
                applyFontToView((TextView) tabViewChild, typeface);
            }
        }
    }

    public void applyFontToTabLayout(TabLayout tabLayout, Typeface typeface) {
        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();

        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);

            int tabChildsCount = vgTab.getChildCount();

            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    TextView tv = (TextView) tabViewChild;
                    tv.setTypeface(typeface);
                }
            }
        }
    }

}
