$sr = 22050

function Create-Wav($path, $samples) {
    $m = New-Object IO.MemoryStream
    $w = New-Object IO.BinaryWriter($m)
    $w.Write([char[]]"RIFF")
    $w.Write([int]($samples.Length + 36))
    $w.Write([char[]]"WAVE")
    $w.Write([char[]]"fmt ")
    $w.Write([int]16)
    $w.Write([int16]1)
    $w.Write([int16]1)
    $w.Write([int]$sr)
    $w.Write([int]($sr * 2))
    $w.Write([int16]2)
    $w.Write([int16]16)
    $w.Write([char[]]"data")
    $w.Write([int]$samples.Length)
    $w.Write($samples)
    [IO.File]::WriteAllBytes($path, $m.ToArray())
    $w.Close()
    $m.Close()
}

# ACERTO - arpejo alegre (C6 E6 G6 C7)
$ms = 450
$n = [int](($ms / 1000.0) * $sr)
$d = New-Object byte[] ($n * 2)
$f = @(1047, 1319, 1568, 2093)
$nd = $n / 4

for ($i = 0; $i -lt $n; $i++) {
    $ni = [Math]::Min([int]($i / $nd), 3)
    $fr = $f[$ni]
    $t = $i / $sr
    $np = $i - ($ni * $nd)
    $att = [Math]::Min($np / ($nd * 0.08), 1)
    $dec = [Math]::Min(($nd - $np) / ($nd * 0.4), 1)
    $env = $att * $dec * 0.65
    $s = [Math]::Sin(2 * [Math]::PI * $fr * $t) * $env
    $sv = [int]($s * 24000)
    $sv = [Math]::Max(-32768, [Math]::Min(32767, $sv))
    [Array]::Copy([BitConverter]::GetBytes([int16]$sv), 0, $d, $i * 2, 2)
}

Create-Wav "app\src\main\res\raw\sound_correct.wav" $d
Write-Host "Criado: sound_correct.wav"

# ERRO - som mais audível e distinto (duas notas descendo)
$ms = 400
$n = [int](($ms / 1000.0) * $sr)
$d = New-Object byte[] ($n * 2)

for ($i = 0; $i -lt $n; $i++) {
    $t = $i / $sr
    $p = $i / $n
    
    # Duas notas: primeira metade em 350Hz, segunda em 250Hz
    if ($p -lt 0.5) {
        $fr = 350
        $localP = $p * 2
        $env = (1 - $localP * 0.3) * 0.7
    } else {
        $fr = 250
        $localP = ($p - 0.5) * 2
        $env = (1 - $localP) * 0.6
    }
    
    $s = [Math]::Sin(2 * [Math]::PI * $fr * $t) * $env
    $sv = [int]($s * 26000)
    $sv = [Math]::Max(-32768, [Math]::Min(32767, $sv))
    [Array]::Copy([BitConverter]::GetBytes([int16]$sv), 0, $d, $i * 2, 2)
}

Create-Wav "app\src\main\res\raw\sound_wrong.wav" $d
Write-Host "Criado: sound_wrong.wav"

Get-ChildItem "app\src\main\res\raw"
