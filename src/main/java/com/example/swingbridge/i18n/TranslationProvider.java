package com.example.swingbridge.i18n;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.springframework.stereotype.Component;

import com.vaadin.flow.i18n.I18NProvider;

@Component
public class TranslationProvider implements I18NProvider {

    private static final String BUNDLE_NAME = "vaadin-i18n.translations";

    @Override
    public List<Locale> getProvidedLocales() {
        return List.of(Locale.GERMAN, Locale.ENGLISH);
    }

    @Override
    public String getTranslation(String key, Locale locale, Object... params) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
            String value = bundle.getString(key);
            if (params.length > 0) {
                value = MessageFormat.format(value, params);
            }
            return value;
        } catch (MissingResourceException e) {
            return "!" + key;
        }
    }
}
