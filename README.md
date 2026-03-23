# Translator Mod
Minecraftのチャットをワンクリックで指定の言語に翻訳するMODです。GASのAPI経由で翻訳します。Fabric環境、Minecraft Version 1.12.11で動作します。

## 1. 前提MOD
このMODを完全に動作させるには、以下の導入が必要です。
* Fabric Loader
* Fabric API
* Mod Menu

## 2. GoogleAppScriptのプロジェクト作成方法と翻訳APIの取得方法
この手順を踏まないと、翻訳することができません。
1. あなたのGoogleアカウントで https://script.google.com/ にアクセスします。
2. 新しいプロジェクトを作成してください。プロジェクト名は、「Minecraft翻訳用API」など適当につけてもらって大丈夫です。
3. 最初に表示されているコードをすべて選択し、削除してから以下のコードを貼り付けてください。
```
function doGet(e) {
  var {text, target = 'ja', source = ''} = e.parameter;
  var translatedText = LanguageApp.translate(text, source, target);
  return ContentService.createTextOutput(translatedText).setMimeType(ContentService.MimeType.TEXT);
}
```
4. デプロイボタンをクリックし、新しくウェブアプリとしてデプロイをしてください。概要は適当に入力し、実行者は自分、アクセス権は全員に設定した後、デプロイボタンを押してください。生成されたWeb Appの https://script.google.com/macros/s/.../exec という型のURLをコピーし、この手順は完了です。このURLは誰にも知らせないでください。

## 3. 使い方と初期設定
1. ダウンロードした `.jar` ファイルを `mods` フォルダに配置します。
2. 一度Minecraftを起動して閉じると、`config` フォルダ内に設定ファイルが生成されます。
3. `translator-url.txt` を開き、自身で用意した翻訳API（GASなど）のURLを貼り付けて保存してください。
4. `translator-lang.txt` で、翻訳先の言語コード（日本語なら `ja`）を変更できます。

## 4. その他
ライセンス: Apache-2.0
なにかバグや問題や要望がある場合はDiscordのDMまでお願いします。<ユーザー名: @nuyuchi>

Copyright (c) 2026 Nuyuchi
