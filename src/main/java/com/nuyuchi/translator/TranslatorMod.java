package com.nuyuchi.translator;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TranslatorMod implements ModInitializer {
    public static final String MOD_ID = "fabric-translator-mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Translator Mod is loading!");
    }
}