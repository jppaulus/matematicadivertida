# ============================================
# CAPTURA DE SCREENSHOTS MULTI-DEVICE
# Para Google Play Store - Matemática Divertida
# ============================================

$packageName = "com.joaop.matematicadivertida"

Write-Host "`n╔══════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  📸 CAPTURA DE SCREENSHOTS (CELULAR/TABLET) ║" -ForegroundColor Cyan
Write-Host "╚══════════════════════════════════════════════╝`n" -ForegroundColor Cyan

# 1. Selecionar Tipo de Dispositivo
Write-Host "Para qual dispositivo você vai capturar agora?" -ForegroundColor Yellow
Write-Host "[1] Celular (Phone)" -ForegroundColor White
Write-Host "[2] Tablet (7 ou 10 polegadas)" -ForegroundColor White
$deviceType = Read-Host "Escolha (1 ou 2)"

if ($deviceType -eq "1") {
    $screenshotsDir = "screenshots/phone"
    $deviceLabel = "CELULAR"
} elseif ($deviceType -eq "2") {
    $screenshotsDir = "screenshots/tablet"
    $deviceLabel = "TABLET"
} else {
    Write-Host "❌ Opção inválida." -ForegroundColor Red
    exit 1
}

# Criar diretório específico
New-Item -ItemType Directory -Force -Path $screenshotsDir | Out-Null
Write-Host "`n📂 Salvando em: $screenshotsDir" -ForegroundColor Green

# 2. Encontrar ADB
$adbPath = $null
$possiblePaths = @(
    "adb",
    "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe",
    "$env:USERPROFILE\AppData\Local\Android\Sdk\platform-tools\adb.exe",
    "C:\Android\Sdk\platform-tools\adb.exe",
    "$env:ANDROID_HOME\platform-tools\adb.exe"
)

foreach ($path in $possiblePaths) {
    if ($path -eq "adb") {
        try { $null = Get-Command adb -ErrorAction Stop; $adbPath = "adb"; break } catch {}
    } elseif (Test-Path $path) {
        $adbPath = $path
        break
    }
}

if (-not $adbPath) {
    Write-Host "❌ ERRO: ADB não encontrado!" -ForegroundColor Red
    exit 1
}

# 3. Verificar conexão e Selecionar Dispositivo
Write-Host "`n🔍 Verificando dispositivos..." -ForegroundColor Yellow
$deviceOutput = & $adbPath devices
$lines = $deviceOutput -split "`r`n" | Where-Object { $_ -match "device|emulator" -and $_ -notmatch "List of devices" }

if ($lines.Count -eq 0) {
    Write-Host "❌ ERRO: Nenhum dispositivo conectado!" -ForegroundColor Red
    Write-Host "   Certifique-se de que o emulador de $deviceLabel está rodando." -ForegroundColor Yellow
    exit 1
}

$selectedSerial = $null

if ($lines.Count -eq 1) {
    $selectedSerial = $lines[0].Split("`t")[0].Trim()
    Write-Host "✓ Dispositivo único detectado: $selectedSerial" -ForegroundColor Green
} else {
    Write-Host "⚠️ Múltiplos dispositivos detectados:" -ForegroundColor Yellow
    for ($i = 0; $i -lt $lines.Count; $i++) {
        Write-Host "[$($i+1)] $($lines[$i])"
    }
    $selection = Read-Host "Selecione o número do dispositivo para capturar (1-$($lines.Count))"
    try {
        $index = [int]$selection - 1
        if ($index -ge 0 -and $index -lt $lines.Count) {
            $selectedSerial = $lines[$index].Split("`t")[0].Trim()
        }
    } catch {}
}

if (-not $selectedSerial) {
    Write-Host "❌ Seleção inválida ou dispositivo não encontrado." -ForegroundColor Red
    exit 1
}

Write-Host "📱 Usando dispositivo: $selectedSerial" -ForegroundColor Cyan

# 4. Verificar Instalação
Write-Host "`n📦 Verificando app..." -ForegroundColor Yellow
$appInstalled = & $adbPath -s $selectedSerial shell pm list packages | Select-String $packageName
if (-not $appInstalled) {
    Write-Host "⚠️ App não instalado. Instalando..." -ForegroundColor Yellow
    ./gradlew :app:installDebug
}
Write-Host "✓ App pronto!" -ForegroundColor Green

# 5. Iniciar Captura
Write-Host "`n" + ("="*50) -ForegroundColor Cyan
Write-Host "🚀 INICIANDO CAPTURA PARA $deviceLabel" -ForegroundColor Cyan
Write-Host ("="*50) -ForegroundColor Cyan
Write-Host "Siga as instruções e posicione a tela no emulador." -ForegroundColor Gray

# Iniciar app
& $adbPath -s $selectedSerial shell am start -n "$packageName/.MainActivity" | Out-Null
Start-Sleep -Seconds 2

# --- LOOP DE CAPTURA ---
$screens = @(
    @{ Name="01_menu"; Title="MENU PRINCIPAL"; Desc="Tela inicial com logo e botões" },
    @{ Name="02_gameplay"; Title="GAMEPLAY (PERGUNTA)"; Desc="Tela com uma conta para resolver" },
    @{ Name="03_acerto"; Title="FEEDBACK ACERTO"; Desc="Responda CORRETAMENTE (tela verde/confete)" },
    @{ Name="04_erro"; Title="FEEDBACK ERRO/DICA"; Desc="Responda ERRADO ou clique na DICA" },
    @{ Name="05_stats"; Title="ESTATÍSTICAS"; Desc="Tela de estatísticas (botão Stats no menu)" },
    @{ Name="06_treino"; Title="MODO TREINO"; Desc="Tela de seleção de treino" }
)

foreach ($screen in $screens) {
    Write-Host "`n📸 $($screen.Title)" -ForegroundColor Yellow
    Write-Host "   👉 $($screen.Desc)" -ForegroundColor White
    Read-Host "   [Pressione ENTER para capturar]"
    
    $file = "$screenshotsDir/$($screen.Name).png"
    & $adbPath -s $selectedSerial exec-out screencap -p > $file
    
    if ((Get-Item $file).Length -gt 0) {
        Write-Host "   ✅ Salvo: $($screen.Name).png" -ForegroundColor Green
    } else {
        Write-Host "   ❌ Falha ao salvar (arquivo vazio)!" -ForegroundColor Red
    }
}

Write-Host "`n" + ("="*50) -ForegroundColor Green
Write-Host "✅ CAPTURA DE $deviceLabel CONCLUÍDA!" -ForegroundColor Green
Write-Host "📂 Arquivos em: $screenshotsDir" -ForegroundColor Green
Write-Host ("="*50) -ForegroundColor Green

# Abrir pasta
Invoke-Item $screenshotsDir
