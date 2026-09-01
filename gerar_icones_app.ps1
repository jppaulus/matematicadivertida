# 🎨 SCRIPT AUTOMÁTICO PARA GERAR ÍCONES DO APP (COM SUPORTE A ÍCONES ADAPTATIVOS)
# =====================================================
# Este script converte uma imagem 512x512 em todos os tamanhos
# necessários para os ícones de launcher do Android, incluindo
# suporte para Android 8.0+ (Adaptive Icons)

Write-Host "🎨 GERADOR DE ÍCONES - Matemática Divertida" -ForegroundColor Cyan
Write-Host "=" -repeat 50 -ForegroundColor Cyan
Write-Host ""

# ===== CONFIGURAÇÕES =====
$sourceImage = "icone_calculadora_512x512.png"
$baseDir = "app\src\main\res"

# Tamanhos Legacy (Android antigo)
$legacySizes = @{
    "mipmap-mdpi"    = 48
    "mipmap-hdpi"    = 72
    "mipmap-xhdpi"   = 96
    "mipmap-xxhdpi"  = 144
    "mipmap-xxxhdpi" = 192
}

# Tamanhos Adaptive (Android 8.0+) - Camada de Background
# 108dp é o tamanho padrão da camada completa
$adaptiveSizes = @{
    "mipmap-mdpi"    = 108
    "mipmap-hdpi"    = 162
    "mipmap-xhdpi"   = 216
    "mipmap-xxhdpi"  = 324
    "mipmap-xxxhdpi" = 432
}

# ===== VERIFICAR IMAGEM FONTE =====
if (-not (Test-Path $sourceImage)) {
    Write-Host "❌ ERRO: Arquivo '$sourceImage' não encontrado!" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Imagem fonte encontrada: $sourceImage" -ForegroundColor Green

# ===== VERIFICAR IMAGEMAGICK =====
$magickPaths = @(
    "magick",
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
            break
        }
    } catch { continue }
}

if (-not $magickPath) {
    Write-Host "❌ ImageMagick não encontrado!" -ForegroundColor Red
    exit 1
}

# ===== BACKUP DOS ÍCONES ANTIGOS =====
Write-Host "�� Fazendo backup..." -ForegroundColor Yellow
$backupDir = "backup_icones_" + (Get-Date -Format "yyyyMMdd_HHmmss")
New-Item -ItemType Directory -Path $backupDir -Force | Out-Null

foreach ($density in $legacySizes.Keys) {
    $srcDir = Join-Path $baseDir $density
    if (Test-Path $srcDir) {
        $destDir = Join-Path $backupDir $density
        New-Item -ItemType Directory -Path $destDir -Force | Out-Null
        Copy-Item "$srcDir\*" -Destination $destDir -Recurse -Force
    }
}

# ===== GERAR ÍCONES LEGACY =====
Write-Host "🎨 Gerando ícones Legacy..." -ForegroundColor Cyan
foreach ($density in $legacySizes.Keys) {
    $size = $legacySizes[$density]
    $outputDir = Join-Path $baseDir $density
    $outputFile = Join-Path $outputDir "ic_launcher.png"
    if (-not (Test-Path $outputDir)) { New-Item -ItemType Directory -Path $outputDir -Force | Out-Null }
    & $magickPath convert $sourceImage -resize "${size}x${size}!" $outputFile 2>$null
}

# ===== GERAR ÍCONES ADAPTIVE (BACKGROUND) =====
Write-Host "🎨 Gerando ícones Adaptive..." -ForegroundColor Cyan
foreach ($density in $adaptiveSizes.Keys) {
    $size = $adaptiveSizes[$density]
    $outputDir = Join-Path $baseDir $density
    $outputFile = Join-Path $outputDir "ic_launcher_background.png"
    if (-not (Test-Path $outputDir)) { New-Item -ItemType Directory -Path $outputDir -Force | Out-Null }
    & $magickPath convert $sourceImage -resize "${size}x${size}!" $outputFile 2>$null
}

# ===== CRIAR XML ADAPTIVE =====
Write-Host "📝 Criando XMLs..." -ForegroundColor Cyan
$anydpiDir = Join-Path $baseDir "mipmap-anydpi-v26"
if (-not (Test-Path $anydpiDir)) { New-Item -ItemType Directory -Path $anydpiDir -Force | Out-Null }

$xmlContent = @"
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@mipmap/ic_launcher_background"/>
    <foreground android:drawable="@android:color/transparent"/>
</adaptive-icon>
"@

Set-Content -Path (Join-Path $anydpiDir "ic_launcher.xml") -Value $xmlContent
Set-Content -Path (Join-Path $anydpiDir "ic_launcher_round.xml") -Value $xmlContent

# ===== GERAR ÍCONES ROUND LEGACY =====
Write-Host "🔄 Gerando ícones redondos..." -ForegroundColor Cyan
foreach ($density in $legacySizes.Keys) {
    $size = $legacySizes[$density]
    $outputDir = Join-Path $baseDir $density
    $outputFile = Join-Path $outputDir "ic_launcher_round.png"
    & $magickPath convert $sourceImage -resize "${size}x${size}!" `
        "(" -size "${size}x${size}" xc:none -fill white -draw "circle $($size/2),$($size/2) $($size/2),0" ")" `
        -compose CopyOpacity -composite $outputFile 2>$null
}

Write-Host "🎉 SUCESSO! Ícones atualizados." -ForegroundColor Green
