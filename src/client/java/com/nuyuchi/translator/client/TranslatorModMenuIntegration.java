package com.nuyuchi.translator.client;

import com.nuyuchi.translator.client.gui.TranslatorSettingsScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class TranslatorModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return TranslatorSettingsScreen::new;
    }
}
