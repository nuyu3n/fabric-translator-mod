package com.nuyuchi.translator.client.gui;

import com.nuyuchi.translator.TranslatorModClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class TranslatorSettingsScreen extends Screen {
    private final Screen previousScreen;
    private EditBox apiUrlBox;
    private EditBox languageBox;
    private Button saveButton;
    private Button resetButton;
    private Button backButton;
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
        this.apiUrlBox.setValue(TranslatorModClient.getApiUrl());
        this.addRenderableWidget(this.apiUrlBox);

        // 言語入力
        this.languageBox = new EditBox(this.font, fieldX, this.panelTop + 88, fieldWidth, 20,
            Component.translatable("screen.translator.settings.target_language"));
        this.languageBox.setMaxLength(32);
        this.languageBox.setBordered(true);
        this.languageBox.setTextColor(0xFFFFFFFF);
        this.languageBox.setTextColorUneditable(0xFFFFFFFF);
        this.languageBox.setValue(TranslatorModClient.getTargetLanguage());
        this.addRenderableWidget(this.languageBox);

        // 保存ボタン
        this.saveButton = this.addRenderableWidget(Button.builder(
            Component.translatable("screen.translator.settings.save"),
                button -> saveSettings())
            .pos(this.panelLeft + 12, this.panelBottom - 30)
            .size(90, 20)
            .build());

        // リセットボタン
        this.resetButton = this.addRenderableWidget(Button.builder(
            Component.translatable("screen.translator.settings.reset"),
                button -> resetSettings())
            .pos(this.panelLeft + 110, this.panelBottom - 30)
            .size(90, 20)
            .build());

        // 戻るボタン
        this.backButton = this.addRenderableWidget(Button.builder(
            Component.translatable("screen.translator.settings.cancel"),
                button -> this.onClose())
            .pos(this.panelRight - 102, this.panelBottom - 30)
            .size(90, 20)
            .build());
    }

    private void saveSettings() {
        String apiUrl = this.apiUrlBox.getValue();
        String language = this.languageBox.getValue();

        if (!apiUrl.isEmpty()) {
            TranslatorModClient.setApiUrl(apiUrl);
        }
        if (!language.isEmpty()) {
            TranslatorModClient.setTargetLanguage(language);
        }

        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.displayClientMessage(
                Component.translatable("screen.translator.settings.saved"),
                false);
        }

        this.onClose();
    }

    private void resetSettings() {
        this.apiUrlBox.setValue("https://script.google.com/macros/.../exec");
        this.languageBox.setValue("ja");

        TranslatorModClient.setApiUrl("https://script.google.com/macros/.../exec");
        TranslatorModClient.setTargetLanguage("ja");

        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.displayClientMessage(
                Component.translatable("screen.translator.settings.reset_done"),
                false);
        }

        this.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xB0000000);
        graphics.fill(this.panelLeft - 1, this.panelTop - 1, this.panelRight + 1, this.panelBottom + 1, 0xFF4A4F5A);
        graphics.fill(this.panelLeft, this.panelTop, this.panelRight, this.panelBottom, 0xF014171D);
        graphics.fill(this.panelLeft, this.panelTop, this.panelRight, this.panelTop + 22, 0xFF9A4DCC);

        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(this.font, Component.translatable("screen.translator.settings.title"), this.width / 2, this.panelTop + 7, 0xFFFFFFFF);
        graphics.drawString(this.font, Component.translatable("screen.translator.settings.api_url"), this.panelLeft + 12, this.panelTop + 28, 0xFFFFFFFF);
        graphics.drawString(this.font, Component.translatable("screen.translator.settings.target_language_hint"), this.panelLeft + 12, this.panelTop + 78, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.previousScreen);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}
