# ============================================
# CAPTURA AUTOMÁTICA DE SCREENSHOTS
# Para Google Play Store - Matemática Divertida
# ============================================

$screenshotsDir = "screenshots/play-store"
$packageName = "com.joaop.matematicadivertida"

# Criar diretórios
New-Item -ItemType Directory -Force -Path $screenshotsDir | Out-Null

Write-Host "`n╔══════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  📸 CAPTURA AUTOMÁTICA DE SCREENSHOTS       ║" -ForegroundColor Cyan
Write-Host "╚══════════════════════════════════════════════╝`n" -ForegroundColor Cyan

# Encontrar ADB
$adbPath = $null
$possiblePaths = @(
    "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe",
    "$env:USERPROFILE\AppData\Local\Android\Sdk\platform-tools\adb.exe",
    "C:\Android\Sdk\platform-tools\adb.exe",
    "$env:ANDROID_HOME\platform-tools\adb.exe"
)

foreach ($path in $possiblePaths) {
    if (Test-Path $path) {
        $adbPath = $path
        break
    }
}

if (-not $adbPath) {
    Write-Host "❌ ERRO: ADB não encontrado!" -ForegroundColor Red
    Write-Host "   Instale o Android SDK ou abra o Android Studio primeiro." -ForegroundColor Yellow
    Write-Host "`n   Caminhos verificados:" -ForegroundColor Gray
    foreach ($path in $possiblePaths) {
        Write-Host "   - $path" -ForegroundColor Gray
    }
    exit 1
}

Write-Host "✓ ADB encontrado: $adbPath" -ForegroundColor Green

# Verificar se emulador está conectado
$devices = & $adbPath devices
if ($devices -notmatch "emulator") {
    Write-Host "❌ ERRO: Nenhum emulador detectado!" -ForegroundColor Red
    Write-Host "   Inicie o emulador no Android Studio primeiro." -ForegroundColor Yellow
    exit 1
}

Write-Host "✓ Emulador detectado" -ForegroundColor Green

# Verificar se app está instalado
$appInstalled = & $adbPath shell pm list packages | Select-String $packageName
if (-not $appInstalled) {
    Write-Host "⚠️ App não instalado. Instalando..." -ForegroundColor Yellow
    ./gradlew :app:installDebug
}

Write-Host "✓ App instalado`n" -ForegroundColor Green

Write-Host "INSTRUÇÕES:" -ForegroundColor Yellow
Write-Host "• Posicione cada tela no emulador" -ForegroundColor White
Write-Host "• Pressione ENTER para capturar" -ForegroundColor White
Write-Host "• Use conteúdo interessante (jogue algumas rodadas antes)`n" -ForegroundColor White

# Iniciar app
Write-Host "Abrindo app..." -ForegroundColor Cyan
& $adbPath shell am start -n "$packageName/.MainActivity" | Out-Null
Start-Sleep -Seconds 2

# Screenshot 1 - Menu Principal
Write-Host "`n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor DarkGray
Write-Host "📱 1/6 - MENU PRINCIPAL" -ForegroundColor Cyan
Write-Host "   → Mostra logo, stats do jogador e botões" -ForegroundColor White
Read-Host "   Pressione ENTER"
& $adbPath exec-out screencap -p > "$screenshotsDir/01_menu.png"
Write-Host "   ✓ Salvo`n" -ForegroundColor Green

# Screenshot 2 - Gameplay
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor DarkGray
Write-Host "📱 2/6 - GAMEPLAY" -ForegroundColor Cyan
Write-Host "   → Clique em JOGAR e mostre uma questão" -ForegroundColor White
Read-Host "   Pressione ENTER"
& $adbPath exec-out screencap -p > "$screenshotsDir/02_gameplay.png"
Write-Host "   ✓ Salvo`n" -ForegroundColor Green

# Screenshot 3 - Dicas
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor DarkGray
Write-Host "📱 3/6 - SISTEMA DE DICAS" -ForegroundColor Cyan
Write-Host "   → Clique em 'Ver Dica' para mostrar balão" -ForegroundColor White
Read-Host "   Pressione ENTER"
& $adbPath exec-out screencap -p > "$screenshotsDir/03_dicas.png"
Write-Host "   ✓ Salvo`n" -ForegroundColor Green

# Screenshot 4 - Micro-lição
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor DarkGray
Write-Host "📱 4/6 - MICRO-LIÇÃO" -ForegroundColor Cyan
Write-Host "   → Erre uma questão para ver explicação" -ForegroundColor White
Read-Host "   Pressione ENTER"
& $adbPath exec-out screencap -p > "$screenshotsDir/04_microlição.png"
Write-Host "   ✓ Salvo`n" -ForegroundColor Green

# Screenshot 5 - Stats
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor DarkGray
Write-Host "📱 5/6 - ESTATÍSTICAS" -ForegroundColor Cyan
Write-Host "   → Volte ao menu (🏠) e clique em STATS" -ForegroundColor White
Read-Host "   Pressione ENTER"
& $adbPath exec-out screencap -p > "$screenshotsDir/05_stats.png"
Write-Host "   ✓ Salvo`n" -ForegroundColor Green

# Screenshot 6 - Modo Treino
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor DarkGray
Write-Host "📱 6/6 - MODO TREINO" -ForegroundColor Cyan
Write-Host "   → Feche stats e clique em MODO TREINO" -ForegroundColor White
Read-Host "   Pressione ENTER"
& $adbPath exec-out screencap -p > "$screenshotsDir/06_treino.png"
Write-Host "   ✓ Salvo`n" -ForegroundColor Green

Write-Host "`n╔══════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║  ✓ 6 SCREENSHOTS CAPTURADAS!                ║" -ForegroundColor Green
Write-Host "╚══════════════════════════════════════════════╝`n" -ForegroundColor Green

Write-Host "📁 Localização: $screenshotsDir/`n" -ForegroundColor Cyan

# Abrir pasta
$fullPath = Resolve-Path $screenshotsDir
explorer $fullPath

Write-Host "PRÓXIMO PASSO:" -ForegroundColor Yellow
Write-Host "→ Verifique as imagens e execute o script de assets visuais`n" -ForegroundColor White
