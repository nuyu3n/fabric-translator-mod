package com.nuyuchi.translator;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.sounds.SoundEvents;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.Duration;

public class TranslatorModClient implements ClientModInitializer {

    private static String apiUrl = "";
    private static String targetLanguage = "ja";
    private static final String TRANSLATE_MARKER_PREFIX = "##TRANSLATE:";
    private static final String TRANSLATE_MARKER_SUFFIX = "##";
    private static final AtomicInteger messageIdCounter = new AtomicInteger(0);
    private static final Map<Integer, String> pendingTranslations = new ConcurrentHashMap<>();
    
    private static final Map<String, String> translationCache = new ConcurrentHashMap<>();

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public void onInitializeClient() {
        TranslatorMod.LOGGER.info("Translator Client Mod is loading!");
        loadConfig();
    }

    private void loadConfig() {
        try {
            Path configDir = FabricLoader.getInstance().getConfigDir();
            Path urlFile = configDir.resolve("translator-url.txt");
            Path langFile = configDir.resolve("translator-lang.txt");

            if (!Files.exists(urlFile)) {
                Files.writeString(urlFile, "https://script.google.com/macros/.../exec");
                TranslatorMod.LOGGER.warn("API URL file created: " + urlFile);
            } else {
                apiUrl = Files.readString(urlFile).trim();
            }

            if (!Files.exists(langFile)) {
                Files.writeString(langFile, "ja");
            } else {
                targetLanguage = Files.readString(langFile).trim();
            }

        } catch (Exception e) {
            TranslatorMod.LOGGER.error("Configuration loading failed", e);
        }
    }

    public static Component withTranslatePrefix(Component original, MessageSignature signature, GuiMessageTag tag) {
        if (original == null) {
            return Component.empty();
        }

        String plainText = original.getString();
        if (!shouldAttachTranslateButton(plainText, signature, tag)) {
            return original;
        }

        int messageId = messageIdCounter.incrementAndGet();
        pendingTranslations.put(messageId, plainText);
        if (messageId > 2000) {
            pendingTranslations.remove(messageId - 2000);
        }

        String marker = "/" + TRANSLATE_MARKER_PREFIX + messageId + TRANSLATE_MARKER_SUFFIX;
        String previewText = buildPreviewText(plainText);
        MutableComponent hoverText = Component.empty()
            .append(Component.translatable("translator.chat.translate_button.hover"))
            .append(Component.literal("\u3000:" + previewText));

        MutableComponent prefix = Component.translatable("translator.chat.translate_button.label")
                .withStyle(Style.EMPTY
            .withColor(0x777777)
                .withBold(true)
                        .withClickEvent(new ClickEvent.RunCommand(marker))
            .withHoverEvent(new HoverEvent.ShowText(hoverText)));

        MutableComponent prefixSpace = Component.literal(" ")
            .withStyle(Style.EMPTY.withColor(0x777777).withUnderlined(false));

        MutableComponent container = Component.empty();
        container.append(prefix);
        container.append(prefixSpace);
        container.append(original);
        return container;
    }

    private static boolean shouldAttachTranslateButton(String plainText, MessageSignature signature, GuiMessageTag tag) {
        if (plainText == null || plainText.isBlank()) {
            return false;
        }

        String normalized = plainText.replaceAll("§[0-9a-fA-Fk-oK-OrR]", "").trim();

        if (normalized.isBlank()) {
            return false;
        }

        if (normalized.startsWith(Component.translatable("translator.chat.translate_button.label").getString())
            || normalized.startsWith("翻 ")
            || normalized.startsWith("【")) {
            return false;
        }

        return true;
    }

    public static boolean handleTranslateMarker(String markerCommand) {
        if (markerCommand == null) {
            return false;
        }

        String normalizedCommand = markerCommand.startsWith("/") ? markerCommand.substring(1) : markerCommand;
        if (!normalizedCommand.startsWith(TRANSLATE_MARKER_PREFIX) || !normalizedCommand.endsWith(TRANSLATE_MARKER_SUFFIX)) {
            return false;
        }

        String idText = normalizedCommand.substring(
                TRANSLATE_MARKER_PREFIX.length(),
                normalizedCommand.length() - TRANSLATE_MARKER_SUFFIX.length()
        );

        try {
            int id = Integer.parseInt(idText);
            String text = pendingTranslations.get(id);
            if (text != null && !text.isBlank()) {
                translateText(text);
                return true;
            }
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    public static void translateText(String originalText) {
        final String currentApiUrl = apiUrl;
        final String currentTargetLanguage = targetLanguage;

        if (originalText == null || originalText.trim().isEmpty() ||
            currentApiUrl.isEmpty() || currentApiUrl.equals("https://script.google.com/macros/.../exec")) {
            showErrorMessage(Component.translatable("translator.error.api_not_configured"));
            return;
        }

        playTranslateStartSound();

        String cacheKey = buildCacheKey(originalText, currentApiUrl, currentTargetLanguage);

        if (translationCache.containsKey(cacheKey)) {
            showTranslationResult(translationCache.get(cacheKey), currentTargetLanguage);
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                String encodedText = URLEncoder.encode(originalText, StandardCharsets.UTF_8.toString());
                String urlString = currentApiUrl + "?text=" + encodedText + "&target=" + currentTargetLanguage;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(urlString))
                        .GET()
                        .timeout(Duration.ofSeconds(10))
                        .build();

                HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                String translatedText = response.body().trim();

                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    showErrorMessage(Component.translatable("translator.error.http_status", response.statusCode()));
                    return;
                }

                if (!translatedText.isEmpty() && !looksLikeHtmlResponse(translatedText)) {
                    
                    if (translationCache.size() > 2000) {
                        translationCache.clear();
                    }
                    translationCache.put(cacheKey, translatedText);

                    showTranslationResult(translatedText, currentTargetLanguage);
                } else {
                    showErrorMessage(Component.translatable("translator.error.html_response"));
                }
            } catch (Exception e) {
                TranslatorMod.LOGGER.error("Translation request failed", e);
                showErrorMessage(Component.translatable("translator.error.request", e.getMessage()));
            }
        });
    }

    private static void playTranslateStartSound() {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            var player = mc.player;
            if (player != null) {
                player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.6f, 0.9f);
            }
        });
    }

    private static void showTranslationResult(String translatedText, String language) {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            var player = mc.player;
            
            if (player != null && player.connection != null) {
                String safeLang = language != null ? language : "ja";
                String safeText = translatedText != null ? translatedText : "";

                MutableComponent translationPrefix = Component.translatable("translator.chat.result_prefix", safeLang)
                    .withStyle(Style.EMPTY.withBold(true).withColor(0xFF55FF));
                MutableComponent translationBody = Component.literal(safeText)
                        .withStyle(Style.EMPTY.withColor(0xAAAAAA).withBold(false).withUnderlined(false));

                MutableComponent translationMsg = Component.empty();
                translationMsg.append(translationPrefix);
                translationMsg.append(translationBody);

                player.displayClientMessage(translationMsg, false);
                player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.8f, 1.15f);
            }
        });
    }

    private static String buildCacheKey(String originalText, String currentApiUrl, String currentTargetLanguage) {
        return currentApiUrl + "||" + currentTargetLanguage + "||" + originalText;
    }

    private static String buildPreviewText(String text) {
        if (text == null || text.isBlank()) {
            return "...";
        }

        String normalized = text.trim();
        int codePointCount = normalized.codePointCount(0, normalized.length());
        if (codePointCount <= 20) {
            return normalized;
        }

        int endIndex = normalized.offsetByCodePoints(0, 20);
        return normalized.substring(0, endIndex) + "...";
    }

    private static boolean looksLikeHtmlResponse(String responseText) {
        String normalized = responseText.stripLeading().toLowerCase();
        return normalized.startsWith("<!doctype html")
                || normalized.startsWith("<html")
                || normalized.startsWith("<head")
                || normalized.startsWith("<body")
                || normalized.contains("<html");
    }

    private static void showErrorMessage(Component message) {
        if (message == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            if (mc.player != null) {
                mc.player.displayClientMessage(message, false);
            }
        });
    }

    public static String getApiUrl() {
        return apiUrl;
    }

    public static void setApiUrl(String url) {
        try {
            apiUrl = url;
            translationCache.clear();
            Path configDir = FabricLoader.getInstance().getConfigDir();
            Files.writeString(configDir.resolve("translator-url.txt"), url);
        } catch (Exception e) {
            TranslatorMod.LOGGER.error("Failed to set API URL", e);
        }
    }

    public static String getTargetLanguage() {
        return targetLanguage;
    }

    public static void setTargetLanguage(String lang) {
        try {
            targetLanguage = lang;
            translationCache.clear();
            Path configDir = FabricLoader.getInstance().getConfigDir();
            Files.writeString(configDir.resolve("translator-lang.txt"), lang);
        } catch (Exception e) {
            TranslatorMod.LOGGER.error("Failed to set target language", e);
        }
    }
}