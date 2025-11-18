# Script para preparar screenshots para Google Play Store

Write-Host "🎮 MATEMÁTICA DIVERTIDA - Preparação de Screenshots" -ForegroundColor Cyan
Write-Host "=" * 60

# 1. Verificar se o emulador está rodando
Write-Host "`n📱 Verificando emulador..." -ForegroundColor Yellow
$emulatorRunning = adb devices 2>$null | Select-String "emulator"
if (-not $emulatorRunning) {
    Write-Host "❌ Emulador não encontrado!" -ForegroundColor Red
    Write-Host "Abra o emulador primeiro no Android Studio." -ForegroundColor Yellow
    Write-Host "Ou execute: emulator -avd Pixel_7_Pro" -ForegroundColor Yellow
    exit 1
}
Write-Host "✅ Emulador detectado!" -ForegroundColor Green

# 2. Criar pasta para screenshots
Write-Host "`n📁 Criando pasta screenshots..." -ForegroundColor Yellow
$screenshotDir = "screenshots"
New-Item -ItemType Directory -Force -Path $screenshotDir | Out-Null
Write-Host "✅ Pasta criada: $((Get-Item $screenshotDir).FullName)" -ForegroundColor Green

# 3. Instalar versão release
Write-Host "`n🔧 Instalando versão release do app..." -ForegroundColor Yellow
$buildResult = ./gradlew :app:installRelease 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ App instalado com sucesso!" -ForegroundColor Green
} else {
    Write-Host "⚠️ Erro ao instalar. Tente manualmente." -ForegroundColor Yellow
}

# 4. Instruções
Write-Host "`n" + ("=" * 60) -ForegroundColor Cyan
Write-Host "📸 AGORA SIGA ESTES PASSOS:" -ForegroundColor Cyan
Write-Host ("=" * 60) -ForegroundColor Cyan

Write-Host "`n1️⃣  Abra o app 'Matemática Divertida' no emulador" -ForegroundColor White
Write-Host "2️⃣  Para cada tela abaixo, pressione CTRL+S para capturar:" -ForegroundColor White
Write-Host ""
Write-Host "   📸 Screenshot 1: MENU INICIAL" -ForegroundColor Yellow
Write-Host "      → Tela de boas-vindas com botões" -ForegroundColor Gray
Write-Host ""
Write-Host "   📸 Screenshot 2: GAMEPLAY" -ForegroundColor Yellow
Write-Host "      → Questão de matemática aparecendo" -ForegroundColor Gray
Write-Host ""
Write-Host "   📸 Screenshot 3: MODO TREINO" -ForegroundColor Yellow
Write-Host "      → Clique em 'Treinar' e escolha uma operação" -ForegroundColor Gray
Write-Host "      → Capture com o card laranja no topo" -ForegroundColor Gray
Write-Host ""
Write-Host "   📸 Screenshot 4: DICAS PROGRESSIVAS" -ForegroundColor Yellow
Write-Host "      → Erre uma resposta de propósito" -ForegroundColor Gray
Write-Host "      → Clique no botão '💡 Dica'" -ForegroundColor Gray
Write-Host ""
Write-Host "   📸 Screenshot 5: ESTATÍSTICAS" -ForegroundColor Yellow
Write-Host "      → Clique em 'Stats' no menu" -ForegroundColor Gray
Write-Host ""
Write-Host "   📸 Screenshot 6: MICRO-LIÇÃO (OPCIONAL)" -ForegroundColor Yellow
Write-Host "      → No menu, mude de operação (ex: +, -, ×, ÷)" -ForegroundColor Gray
Write-Host "      → Capture quando o dialog de explicação aparecer" -ForegroundColor Gray
Write-Host ""

Write-Host "3️⃣  As screenshots ficam salvas em:" -ForegroundColor White
Write-Host "   C:\Users\$env:USERNAME\Pictures\Screenshots" -ForegroundColor Cyan
Write-Host ""
Write-Host "4️⃣  Copie as imagens para a pasta:" -ForegroundColor White
Write-Host "   $((Get-Item $screenshotDir).FullName)" -ForegroundColor Cyan
Write-Host ""

Write-Host ("=" * 60) -ForegroundColor Cyan
Write-Host "💡 DICAS:" -ForegroundColor Yellow
Write-Host ("=" * 60) -ForegroundColor Cyan
Write-Host "• Use CTRL+S para capturar rapidamente" -ForegroundColor Gray
Write-Host "• Ou clique no ícone 📷 na barra lateral do emulador" -ForegroundColor Gray
Write-Host "• Renomeie as imagens para: 01_menu.png, 02_game.png, etc" -ForegroundColor Gray
Write-Host "• Mínimo 2 screenshots, recomendado 4-6" -ForegroundColor Gray
Write-Host ""

# 5. Aguardar finalização
Write-Host ("=" * 60) -ForegroundColor Green
Write-Host "Pressione ENTER quando terminar de capturar..." -ForegroundColor Green
Read-Host

# 6. Verificar se há screenshots
Write-Host "`n🔍 Verificando screenshots capturadas..." -ForegroundColor Yellow
$screenshots = Get-ChildItem "$env:USERPROFILE\Pictures\Screenshots" -Filter "*.png" -ErrorAction SilentlyContinue | 
    Where-Object { $_.LastWriteTime -gt (Get-Date).AddMinutes(-30) }

if ($screenshots) {
    Write-Host "✅ Encontradas $($screenshots.Count) screenshots recentes!" -ForegroundColor Green
    Write-Host "`nDeseja copiar para a pasta do projeto? (S/N)" -ForegroundColor Yellow
    $response = Read-Host
    
    if ($response -eq "S" -or $response -eq "s") {
        $i = 1
        foreach ($screenshot in $screenshots) {
            $newName = "{0:D2}_screenshot.png" -f $i
            Copy-Item $screenshot.FullName -Destination "$screenshotDir\$newName"
            Write-Host "📋 Copiado: $newName" -ForegroundColor Cyan
            $i++
        }
        Write-Host "`n✅ Screenshots copiadas para: $screenshotDir" -ForegroundColor Green
    }
} else {
    Write-Host "⚠️ Nenhuma screenshot recente encontrada." -ForegroundColor Yellow
    Write-Host "Verifique manualmente em: $env:USERPROFILE\Pictures\Screenshots" -ForegroundColor Gray
}

# 7. Próximos passos
Write-Host "`n" + ("=" * 60) -ForegroundColor Cyan
Write-Host "📋 PRÓXIMOS PASSOS:" -ForegroundColor Cyan
Write-Host ("=" * 60) -ForegroundColor Cyan
Write-Host "1. Redimensionar para 1080x1920 (se necessário)" -ForegroundColor White
Write-Host "   → Use: https://www.iloveimg.com/resize-image" -ForegroundColor Gray
Write-Host ""
Write-Host "2. Criar ícone 512x512 e banner 1024x500" -ForegroundColor White
Write-Host "   → Use Canva: https://www.canva.com/" -ForegroundColor Gray
Write-Host "   → Consulte: GUIA_ASSETS_VISUAIS.md" -ForegroundColor Gray
Write-Host ""
Write-Host "3. Upload no Google Play Console" -ForegroundColor White
Write-Host "   → https://play.google.com/console" -ForegroundColor Gray
Write-Host ""

Write-Host "✨ Processo concluído!" -ForegroundColor Green
