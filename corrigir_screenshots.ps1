# Script para corrigir screenshots para o Google Play Console
# Remove o canal Alpha (transparência) que costuma causar rejeição

Write-Host "🔧 CORRIGINDO SCREENSHOTS..." -ForegroundColor Cyan

$dirs = @("screenshots\phone", "screenshots\tablet")
$magick = "magick"

foreach ($dir in $dirs) {
    if (Test-Path $dir) {
        Write-Host "`n📂 Processando pasta: $dir" -ForegroundColor Yellow
        $images = Get-ChildItem $dir -Filter "*.png"
        
        foreach ($img in $images) {
            Write-Host "   → Corrigindo: $($img.Name)..." -NoNewline
            
            $tempName = $img.FullName + ".tmp.png"
            
            # Remove canal alpha e garante sRGB
            & $magick convert $img.FullName -alpha off -colorspace sRGB $tempName
            
            if (Test-Path $tempName) {
                Move-Item $tempName $img.FullName -Force
                Write-Host " ✅ OK" -ForegroundColor Green
            } else {
                Write-Host " ❌ Erro" -ForegroundColor Red
            }
        }
    }
}

Write-Host "`n✨ Concluído! Tente fazer o upload novamente." -ForegroundColor Green
