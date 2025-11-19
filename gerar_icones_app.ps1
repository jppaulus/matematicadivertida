# 🎨 SCRIPT AUTOMÁTICO PARA GERAR ÍCONES DO APP
# =====================================================
# Este script converte uma imagem 512x512 em todos os tamanhos
# necessários para os ícones de launcher do Android

Write-Host "🎨 GERADOR DE ÍCONES - Matemática Divertida" -ForegroundColor Cyan
Write-Host "=" -repeat 50 -ForegroundColor Cyan
Write-Host ""

# ===== CONFIGURAÇÕES =====
$sourceImage = "icone_calculadora_512x512.png"
$baseDir = "app\src\main\res"

# Tamanhos necessários para Android (densidade: tamanho)
$sizes = @{
    "mipmap-mdpi"    = 48
    "mipmap-hdpi"    = 72
    "mipmap-xhdpi"   = 96
    "mipmap-xxhdpi"  = 144
    "mipmap-xxxhdpi" = 192
}

# ===== VERIFICAR IMAGEM FONTE =====
if (-not (Test-Path $sourceImage)) {
    Write-Host "❌ ERRO: Arquivo '$sourceImage' não encontrado!" -ForegroundColor Red
    Write-Host ""
    Write-Host "📋 INSTRUÇÕES:" -ForegroundColor Yellow
    Write-Host "1. Salve a imagem da calculadora como: $sourceImage" -ForegroundColor White
    Write-Host "2. A imagem deve ter exatamente 512x512 pixels" -ForegroundColor White
    Write-Host "3. Formato: PNG com fundo transparente ou colorido" -ForegroundColor White
    Write-Host "4. Coloque na mesma pasta deste script" -ForegroundColor White
    Write-Host ""
    Write-Host "💡 DICA: Você pode usar o Canva para exportar em 512x512" -ForegroundColor Cyan
    Write-Host ""
    pause
    exit 1
}

Write-Host "✅ Imagem fonte encontrada: $sourceImage" -ForegroundColor Green
Write-Host ""

# ===== VERIFICAR IMAGEMAGICK =====
Write-Host "🔍 Verificando ImageMagick..." -ForegroundColor Yellow

$magickPaths = @(
    "magick",  # Se estiver no PATH
    "C:\Program Files\ImageMagick-7.1.1-Q16-HDRI\magick.exe",
    "C:\Program Files\ImageMagick\magick.exe",
    "$env:ProgramFiles\ImageMagick-7.1.1-Q16-HDRI\magick.exe",
    "$env:ProgramFiles\ImageMagick\magick.exe"
)

$magickPath = $null
foreach ($path in $magickPaths) {
    try {
        $result = & $path --version 2>&1
        if ($LASTEXITCODE -eq 0) {
            $magickPath = $path
            Write-Host "✅ ImageMagick encontrado: $magickPath" -ForegroundColor Green
            break
        }
    } catch {
        continue
    }
}

if (-not $magickPath) {
    Write-Host "❌ ImageMagick não encontrado!" -ForegroundColor Red
    Write-Host ""
    Write-Host "📥 INSTALAR IMAGEMAGICK:" -ForegroundColor Yellow
    Write-Host "1. Baixe em: https://imagemagick.org/script/download.php#windows" -ForegroundColor White
    Write-Host "2. Escolha: ImageMagick-7.x.x-Q16-HDRI-x64-dll.exe" -ForegroundColor White
    Write-Host "3. Durante instalação, marque: 'Add application directory to PATH'" -ForegroundColor White
    Write-Host "4. Reinicie o PowerShell após a instalação" -ForegroundColor White
    Write-Host ""
    Write-Host "🌐 Abrindo página de download..." -ForegroundColor Cyan
    Start-Process "https://imagemagick.org/script/download.php#windows"
    Write-Host ""
    pause
    exit 1
}

Write-Host ""

# ===== BACKUP DOS ÍCONES ANTIGOS =====
Write-Host "💾 Fazendo backup dos ícones antigos..." -ForegroundColor Yellow

$backupDir = "backup_icones_" + (Get-Date -Format "yyyyMMdd_HHmmss")
New-Item -ItemType Directory -Path $backupDir -Force | Out-Null

$backedUp = 0
foreach ($density in $sizes.Keys) {
    $oldIcon = Join-Path $baseDir $density "ic_launcher.png"
    if (Test-Path $oldIcon) {
        $destDir = Join-Path $backupDir $density
        New-Item -ItemType Directory -Path $destDir -Force | Out-Null
        Copy-Item $oldIcon -Destination $destDir
        $backedUp++
    }
}

Write-Host "✅ Backup criado em: $backupDir ($backedUp arquivos)" -ForegroundColor Green
Write-Host ""

# ===== GERAR ÍCONES =====
Write-Host "🎨 Gerando ícones em múltiplas resoluções..." -ForegroundColor Cyan
Write-Host ""

$generated = 0
$failed = 0

foreach ($density in $sizes.Keys) {
    $size = $sizes[$density]
    $outputDir = Join-Path $baseDir $density
    $outputFile = Join-Path $outputDir "ic_launcher.png"
    
    Write-Host "  📐 $density (${size}x${size}px)..." -NoNewline
    
    # Criar diretório se não existir
    if (-not (Test-Path $outputDir)) {
        New-Item -ItemType Directory -Path $outputDir -Force | Out-Null
    }
    
    # Converter imagem
    try {
        & $magickPath convert $sourceImage -resize "${size}x${size}!" $outputFile 2>$null
        
        if (Test-Path $outputFile) {
            Write-Host " ✅" -ForegroundColor Green
            $generated++
        } else {
            Write-Host " ❌ Falhou" -ForegroundColor Red
            $failed++
        }
    } catch {
        Write-Host " ❌ Erro: $_" -ForegroundColor Red
        $failed++
    }
}

Write-Host ""

# ===== GERAR ÍCONES ROUND (OPCIONAL) =====
Write-Host "🔄 Deseja gerar ícones redondos (ic_launcher_round.png)? (S/N)" -ForegroundColor Yellow
$response = Read-Host

if ($response -match '^[Ss]') {
    Write-Host ""
    Write-Host "🎨 Gerando ícones redondos..." -ForegroundColor Cyan
    Write-Host ""
    
    foreach ($density in $sizes.Keys) {
        $size = $sizes[$density]
        $outputDir = Join-Path $baseDir $density
        $outputFile = Join-Path $outputDir "ic_launcher_round.png"
        
        Write-Host "  📐 $density (${size}x${size}px)..." -NoNewline
        
        try {
            # Criar máscara circular
            & $magickPath convert $sourceImage -resize "${size}x${size}!" `
                "(" -size "${size}x${size}" xc:none -fill white -draw "circle $($size/2),$($size/2) $($size/2),0" ")" `
                -compose CopyOpacity -composite $outputFile 2>$null
            
            if (Test-Path $outputFile) {
                Write-Host " ✅" -ForegroundColor Green
            } else {
                Write-Host " ❌ Falhou" -ForegroundColor Red
            }
        } catch {
            Write-Host " ❌ Erro: $_" -ForegroundColor Red
        }
    }
    Write-Host ""
}

# ===== RESUMO =====
Write-Host "=" -repeat 50 -ForegroundColor Cyan
Write-Host "📊 RESUMO DA GERAÇÃO" -ForegroundColor Cyan
Write-Host "=" -repeat 50 -ForegroundColor Cyan
Write-Host ""
Write-Host "✅ Ícones gerados: $generated" -ForegroundColor Green
Write-Host "❌ Falhas: $failed" -ForegroundColor Red
Write-Host "💾 Backup salvo em: $backupDir" -ForegroundColor Yellow
Write-Host ""

if ($generated -eq $sizes.Count) {
    Write-Host "🎉 SUCESSO! Todos os ícones foram gerados!" -ForegroundColor Green
    Write-Host ""
    Write-Host "📋 PRÓXIMOS PASSOS:" -ForegroundColor Cyan
    Write-Host "1. Recompilar o app: .\gradlew clean :app:bundleRelease" -ForegroundColor White
    Write-Host "2. Desinstalar versão antiga: .\gradlew :app:uninstallAll" -ForegroundColor White
    Write-Host "3. Instalar nova versão: .\gradlew :app:installDebug" -ForegroundColor White
    Write-Host "4. Verificar o novo ícone na tela inicial do dispositivo" -ForegroundColor White
    Write-Host ""
    Write-Host "💡 DICA: Verifique também o ícone no app/src/main/res/mipmap-anydpi-v26/" -ForegroundColor Cyan
    Write-Host "         (ícones adaptativos para Android 8.0+)" -ForegroundColor Cyan
} else {
    Write-Host "⚠️ ATENÇÃO: Alguns ícones falharam ao gerar!" -ForegroundColor Yellow
    Write-Host "Verifique os erros acima e tente novamente." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=" -repeat 50 -ForegroundColor Cyan
Write-Host ""
pause
