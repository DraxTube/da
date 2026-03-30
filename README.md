# StreamApp 🎬

App Android stile Netflix per guardare Batman: The Brave and the Bold.  
**65 episodi · 3 stagioni · Blocco pubblicità e popup integrato**

---

## ✅ Funzionalità

- 🎨 UI stile Netflix (sfondo dark, badge rosso, schede episodi)
- 📺 Compatibile **TV Android** + **Smartphone**
- 🚫 **Blocco pubblicità e popup** a livello di rete e JavaScript
- 📂 Episodi caricati da JSON locale (nessun server necessario)
- 🔄 Navigazione per stagioni con tab
- ⏩ Player WebView ottimizzato per supervideo.cc

---

## 🚀 Build con GitHub Actions (raccomandato)

1. Crea un nuovo repository GitHub **privato**
2. Carica tutti i file di questo ZIP nel repository
3. Vai su **Actions** → seleziona **"Build APK"** → clicca **"Run workflow"**
4. Aspetta ~5 minuti
5. Scarica l'APK dagli **Artifacts** del workflow completato

> Il workflow scarica automaticamente il `gradle-wrapper.jar` da Gradle ufficiale.

---

## 🛠 Build locale (opzionale)

### Prerequisiti
- Java 17+
- Android SDK con Build Tools 34

### Passaggi

```bash
# 1. Scarica il gradle wrapper jar
mkdir -p gradle/wrapper
curl -L "https://github.com/gradle/gradle/raw/v8.4.0/gradle/wrapper/gradle-wrapper.jar" \
     -o gradle/wrapper/gradle-wrapper.jar

# 2. Build debug APK
chmod +x gradlew
./gradlew assembleDebug

# 3. APK si trova in:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 📁 Struttura progetto

```
StreamApp/
├── .github/workflows/build.yml   ← GitHub Actions CI
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── assets/episodes.json  ← Tutti gli episodi
│       ├── java/com/streamapp/player/
│       │   ├── Episode.java
│       │   ├── EpisodeAdapter.java
│       │   ├── EpisodeLoader.java
│       │   ├── MainActivity.java  ← Schermata principale
│       │   └── PlayerActivity.java ← Player con ad-blocker
│       └── res/
│           ├── layout/           ← UI XML
│           ├── drawable/         ← Grafica
│           └── values/           ← Colori, temi, stringhe
├── gradle/wrapper/
├── build.gradle
├── settings.gradle
└── gradlew
```

---

## 🔒 Ad Blocker

Il blocco pubblicità funziona su **due livelli**:

1. **Livello rete** — Blocca richieste HTTP verso 30+ domini pubblicitari noti  
2. **Livello JavaScript** — Inietta JS che:
   - Blocca `window.open()` (niente popup)
   - Nasconde elementi DOM pubblicitari ogni 500ms
   - Blocca link con `target="_blank"` (redirect ad)
   - Blocca navigazioni esterne al dominio supervideo

---

## 📺 Supporto TV

- `android.software.leanback` dichiarato (non obbligatorio)
- Focus con D-pad su RecyclerView e CardView
- Animazioni scale su focus
- Supporto tasto BACK del telecomando
- `android.hardware.touchscreen` dichiarato come non obbligatorio

---

*Generato automaticamente da Claude*
