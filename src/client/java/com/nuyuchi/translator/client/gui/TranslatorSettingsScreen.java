package com.nuyuchi.translator.client.gui;

import com.nuyuchi.translator.TranslatorModClient;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TranslatorSettingsScreen extends Screen {
    private final Screen previousScreen;
    private EditBox apiUrlBox;
    private EditBox languageBox;
    private int panelLeft;
    private int panelTop;
    private int panelRight;
    private int panelBottom;

    public TranslatorSettingsScreen(Screen previousScreen) {
        super(Component.translatable("screen.translator.settings.title"));
        this.previousScreen = previousScreen;
    }

    @Override
    protected void init() {
        int panelWidth = 420;
        int panelHeight = 180;
        this.panelLeft = this.width / 2 - panelWidth / 2;
        this.panelTop = this.height / 2 - panelHeight / 2;
        this.panelRight = this.panelLeft + panelWidth;
        this.panelBottom = this.panelTop + panelHeight;

        int fieldWidth = panelWidth - 24;
        int fieldX = this.panelLeft + 12;

        // API URL入力
        this.apiUrlBox = new EditBox(this.font, fieldX, this.panelTop + 38, fieldWidth, 20,
            Component.translatable("screen.translator.settings.api_url"));
        this.apiUrlBox.setMaxLength(4096);
        this.apiUrlBox.setBordered(true);
        this.apiUrlBox.setTextColor(0xFFFFFFFF);
        this.apiUrlBox.setTextColorUneditable(0xFFFFFFFF);
        
        // Null安全性の確保（単純な条件分岐を使用）
        String currentApiUrl = TranslatorModClient.getApiUrl();
        this.apiUrlBox.setValue(currentApiUrl != null ? currentApiUrl : "");
        this.addRenderableWidget(this.apiUrlBox);

        // 言語入力
        this.languageBox = new EditBox(this.font, fieldX, this.panelTop + 88, fieldWidth, 20,
            Component.translatable("screen.translator.settings.target_language"));
        this.languageBox.setMaxLength(32);
        this.languageBox.setBordered(true);
        this.languageBox.setTextColor(0xFFFFFFFF);
        this.languageBox.setTextColorUneditable(0xFFFFFFFF);
        
        // Null安全性の確保（単純な条件分岐を使用）
        String currentLang = TranslatorModClient.getTargetLanguage();
        this.languageBox.setValue(currentLang != null ? currentLang : "ja");
        this.addRenderableWidget(this.languageBox);

        // 保存ボタン
        this.addRenderableWidget(Button.builder(
            Component.translatable("screen.translator.settings.save"),
                button -> saveSettings())
            .pos(this.panelLeft + 12, this.panelBottom - 30)
            .size(90, 20)
            .build());

        // リセットボタン
        this.addRenderableWidget(Button.builder(
            Component.translatable("screen.translator.settings.reset"),
                button -> resetSettings())
            .pos(this.panelLeft + 110, this.panelBottom - 30)
            .size(90, 20)
            .build());

        // 戻るボタン
        this.addRenderableWidget(Button.builder(
            Component.translatable("screen.translator.settings.cancel"),
                button -> this.onClose())
            .pos(this.panelRight - 102, this.panelBottom - 30)
            .size(90, 20)
            .build());
    }

    @Override
    public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {
        // 1. 下地の描画
        super.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTick);

        // 2. 画面タイトル（centeredText を使用、.getString() で String に変換）
        guiGraphicsExtractor.centeredText(this.font, this.title.getString(), this.width / 2, this.panelTop + 12, 0xFFFFFFFF);

        // 3. 各入力ボックスの真上のラベル（text を使用、.getString() で String に変換）
        guiGraphicsExtractor.text(this.font, Component.translatable("screen.translator.settings.api_url").getString(), this.panelLeft + 12, this.panelTop + 26, 0xFFA0A0A0);
        guiGraphicsExtractor.text(this.font, Component.translatable("screen.translator.settings.target_language").getString(), this.panelLeft + 12, this.panelTop + 76, 0xFFA0A0A0);
    }

    private void saveSettings() {
        String apiUrl = this.apiUrlBox.getValue();
        String language = this.languageBox.getValue();

        if (apiUrl != null && !apiUrl.isEmpty()) {
            TranslatorModClient.setApiUrl(apiUrl);
        }
        if (language != null && !language.isEmpty()) {
            TranslatorModClient.setTargetLanguage(language);
        }

        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.sendSystemMessage(
                Component.translatable("screen.translator.settings.saved"));
        }

        this.onClose();
    }

    private void resetSettings() {
        String defaultUrl = "https://script.google.com/macros/.../exec";
        String defaultLang = "ja";
        
        this.apiUrlBox.setValue(defaultUrl);
        this.languageBox.setValue(defaultLang);

        TranslatorModClient.setApiUrl(defaultUrl);
        TranslatorModClient.setTargetLanguage(defaultLang);

        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.sendSystemMessage(
                Component.translatable("screen.translator.settings.reset_done"));
        }

        this.onClose();
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}