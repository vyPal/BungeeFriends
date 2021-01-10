package com.vypalstudios.BungeeFriends.i18n;

import com.vypalstudios.BungeeFriends.BungeeFriends;
import com.vypalstudios.BungeeFriends.util.ConfigHelper;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

// ------BungeeFriends------
// author: vyPal
// -------------------------
public class I18NManager {

    private ResourceBundle bundle;

    public I18NManager(String resource) {
        bundle = ResourceBundle.getBundle(resource);
    }

    public String getMessage(String path) {
        if (I18NManager.class.getResource("/" + bundle.getBaseBundleName() + "_" + bundle.getLocale().getLanguage() + ".properties") != null) {
            return bundle.getString(path);
        }
        changeLocale(Locale.ENGLISH);
        return bundle.getString(path);
    }

    public void changeLocale(Locale locale) {
        if (locale != null) {
            bundle = ResourceBundle.getBundle(bundle.getBaseBundleName(), locale);
        } else {
            bundle = ResourceBundle.getBundle(bundle.getBaseBundleName(), Locale.ENGLISH);
        }
    }

    public void changeBundle(String resource) {
        bundle = ResourceBundle.getBundle(resource, bundle.getLocale());
    }

    public ResourceBundle getResourceBundle() {
        return bundle;
    }

    public Locale getLocale() {
        return bundle.getLocale();
    }

}
