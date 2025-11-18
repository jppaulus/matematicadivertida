# 📸 Como Tirar Screenshots do App

## 🎯 Método 1: Usando o Emulador (RECOMENDADO)

### Passo a Passo:

1. **Abra o app no emulador**
   ```powershell
   Set-Location "C:\Users\joaop\OneDrive\Documentos\Creates\Jogo infantil"
   ./gradlew :app:installRelease
   ```

2. **Navegue pelas telas que quer capturar**:
   - Tela inicial (menu)
   - Gameplay com questão
   - Modo Treino
   - Dicas progressivas (erre de propósito para aparecer)
   - Micro-lição (mude de operação para aparecer)
   - Tela de estatísticas

3. **Capturar screenshot**:
   - **Método A**: Clique no ícone de câmera 📷 na barra lateral do emulador
   - **Método B**: Pressione `Ctrl + S` no teclado
   - **Método C**: Use a ferramenta de captura do Windows (`Win + Shift + S`)

4. **As imagens são salvas automaticamente em**:
   ```
   C:\Users\joaop\Pictures\Screenshots
   ```
   Ou dentro do Android Studio em: `View > Tool Windows > Emulator > Screenshot`

---

## 🖼️ Método 2: Usando ADB Screenshot Command

Se o emulador estiver rodando:

```powershell
# Screenshot da tela inicial
adb shell screencap -p /sdcard/screenshot_menu.png
adb pull /sdcard/screenshot_menu.png "screenshots/01_menu.png"

# Screenshot do gameplay
# (navegue no app antes de executar)
adb shell screencap -p /sdcard/screenshot_game.png
adb pull /sdcard/screenshot_game.png "screenshots/02_gameplay.png"
```

---

## 📋 Checklist de Screenshots Recomendadas

Capture estas 6 telas (na ordem):

- [ ] **01_menu.png** - Tela inicial com logo e botões
- [ ] **02_gameplay.png** - Questão de matemática aparecendo
- [ ] **03_dicas.png** - Sistema de dicas (erre para aparecer)
- [ ] **04_treino.png** - Modo treino por operação (card laranja)
- [ ] **05_stats.png** - Tela de estatísticas e conquistas
- [ ] **06_microlecao.png** - Micro-lição (mude operação para aparecer)

---

## ✂️ Depois de Capturar

### Redimensionar para Play Store (1080x1920):

**Opção A: Usar site online (FÁCIL)**
1. Acesse: https://www.iloveimg.com/resize-image
2. Upload das screenshots
3. Redimensionar para: 1080 x 1920 pixels
4. Download

**Opção B: Usar PowerShell com ImageMagick**
```powershell
# Instalar ImageMagick
winget install ImageMagick.ImageMagick

# Redimensionar todas
Get-ChildItem screenshots/*.png | ForEach-Object {
    magick $_.FullName -resize 1080x1920 -quality 100 "screenshots/resized_$($_.Name)"
}
```

---

## 🎨 Adicionar Texto nas Screenshots (OPCIONAL)

Use Canva para adicionar descrições:

1. Acesse: https://www.canva.com/
2. Crie design personalizado: 1080 x 1920
3. Upload sua screenshot como fundo
4. Adicione texto em cima:
   - "Aprenda brincando! 🎮"
   - "Dicas inteligentes 💡"
   - "Acompanhe seu progresso 📊"

---

## 💡 Dicas Importantes

1. **Capture em modo Release** - O app já está instalado!
2. **Use o emulador maior** - Pixel 7 Pro tem boa resolução
3. **Modo retrato** - Certifique-se que está vertical
4. **Limpe a tela** - Feche notificações antes de capturar
5. **Boa iluminação** - Use tema claro do Android

---

## 🚀 Atalho Rápido

Execute este script para abrir o app e preparar:

```powershell
Set-Location "C:\Users\joaop\OneDrive\Documentos\Creates\Jogo infantil"
./gradlew :app:installRelease
Write-Host "✅ App instalado! Abra no emulador e tire as screenshots."
Write-Host "📸 Use Ctrl+S ou o ícone de câmera na barra do emulador."
Write-Host "💾 Screenshots salvos em: C:\Users\joaop\Pictures\Screenshots"
```

---

**Tempo estimado**: 10-15 minutos para 6 screenshots! 📱✨
