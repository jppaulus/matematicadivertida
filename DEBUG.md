# 🐛 Debug e Monitoramento - Jogo Infantil

## 🔌 Conectar Dispositivo

### Via USB
```pwsh
# Verificar dispositivos conectados
adb devices

# Se aparecer "unauthorized", aceite a permissão no dispositivo
# Se aparecer vazio, instale drivers USB do fabricante
```

### Via Wi-Fi (dispositivo e PC na mesma rede)
```pwsh
# 1. Conecte o dispositivo via USB primeiro
adb tcpip 5555

# 2. Descubra o IP do dispositivo (Configurações → Sobre → Status)
# Exemplo: 192.168.1.100

# 3. Desconecte o USB e conecte via Wi-Fi
adb connect 192.168.1.100:5555

# 4. Verificar conexão
adb devices
# Deve mostrar: 192.168.1.100:5555 device
```

## 📦 Instalar e Executar

```pwsh
cd "c:\Users\joaop\OneDrive\Documentos\Creates\Jogo infantil"

# Instalar APK
adb install -r app\build\outputs\apk\debug\app-debug.apk

# Executar o app
adb shell am start -n com.example.jogoinfantil/.MainActivity

# Forçar parada do app
adb shell am force-stop com.example.jogoinfantil

# Desinstalar
adb uninstall com.example.jogoinfantil
```

## 📊 Monitoramento de Logs

### Logs do Jogo (com emojis e eventos)
```pwsh
# Apenas logs do jogo
adb logcat -s JogoInfantil:D

# Limpar e monitorar
adb logcat -c; adb logcat -s JogoInfantil:D

# Salvar logs em arquivo
adb logcat -s JogoInfantil:D > logs-jogo.txt
```

### Logs de AdMob
```pwsh
# Eventos de anúncios (carregamento, exibição, cliques)
adb logcat -s Ads:D GoogleMobileAdsProvider:D

# Combinado: Jogo + AdMob
adb logcat -s JogoInfantil:D Ads:D GoogleMobileAdsProvider:D
```

### Logs de Consentimento UMP
```pwsh
# Eventos de consentimento GDPR
adb logcat -s ConsentForm:D UserMessagingPlatform:D JogoInfantil:D
```

### Todos os logs do app
```pwsh
# Filtrar por package
adb logcat | Select-String "jogoinfantil"

# Ou por PID do processo
$pid = (adb shell pidof -s com.example.jogoinfantil)
adb logcat --pid=$pid
```

### Logs filtrados por nível
```pwsh
# Apenas erros (E) e warnings (W)
adb logcat *:E *:W

# Debug (D) e acima
adb logcat JogoInfantil:D *:S
```

## 🔍 Eventos Monitorados

O app agora loga automaticamente:

### 🎮 Eventos do Jogo
- `🎮 Iniciando aplicativo...`
- `➡️ Avançando para fase X`
- `🎉 Todas as fases concluídas! Reiniciando...`

### 📢 Eventos de AdMob
- `📢 Inicializando Mobile Ads SDK...`
- `✅ Mobile Ads SDK inicializado`
- `📥 Carregando anúncio intersticial...`
- `✅ Intersticial carregado com sucesso`
- `🎬 Exibindo intersticial antes da fase X`
- `👁️ Intersticial exibido`
- `✖️ Intersticial fechado pelo usuário`
- `❌ Falha ao carregar intersticial: [erro]`
- `📥 Carregando banner...`
- `✅ Banner carregado com sucesso`
- `👆 Banner clicado`

### 🔐 Eventos de Consentimento
- `📋 Solicitando consentimento UMP...`
- `🔓 Solicitando atualização de consentimento UMP...`
- `✅ Informações de consentimento atualizadas`
- `📊 Status: canRequestAds=true/false`
- `⚠️ Consentimento não disponível`

## 🧪 Testes de Anúncios

### Verificar carregamento
1. Inicie o app e observe:
   ```
   JogoInfantil: 📢 Inicializando Mobile Ads SDK...
   JogoInfantil: ✅ Mobile Ads SDK inicializado
   JogoInfantil: 📥 Carregando banner...
   JogoInfantil: 📥 Carregando anúncio intersticial...
   ```

2. Banner deve aparecer no rodapé:
   ```
   JogoInfantil: ✅ Banner carregado com sucesso
   ```

3. Complete 5 acertos para ver intersticial:
   ```
   JogoInfantil: 🎬 Exibindo intersticial antes da fase 2
   JogoInfantil: 👁️ Intersticial exibido
   ```

### Simular erro de rede
```pwsh
# Desabilitar Wi-Fi/dados e verificar logs de falha
adb logcat -s JogoInfantil:D Ads:D
# Deve mostrar: ❌ Falha ao carregar...
```

## 🎯 Performance

### Monitor de FPS e GPU
```pwsh
# Ativar estatísticas de GPU no dispositivo
adb shell settings put global show_fps 1

# Ou via Configurações do desenvolvedor → Perfil de renderização GPU
```

### Uso de memória
```pwsh
# Memória do app
adb shell dumpsys meminfo com.example.jogoinfantil

# Resumido
adb shell dumpsys meminfo com.example.jogoinfantil | Select-String "TOTAL"
```

## 🔄 Rebuild e Reinstalar Rápido

```pwsh
# Pipeline completo
cd "c:\Users\joaop\OneDrive\Documentos\Creates\Jogo infantil"
./gradlew assembleDebug; adb install -r app\build\outputs\apk\debug\app-debug.apk; adb shell am start -n com.example.jogoinfantil/.MainActivity
```

## 📸 Screenshots e Gravação

```pwsh
# Capturar screenshot
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png

# Gravar vídeo (máx 180s)
adb shell screenrecord /sdcard/demo.mp4
# Pressione Ctrl+C para parar
adb pull /sdcard/demo.mp4
```

## 🛠️ Solução de Problemas

### "adb: device unauthorized"
- Aceite a permissão de depuração USB no dispositivo
- Se não aparecer: `adb kill-server; adb start-server`

### "adb: no devices/emulators found"
- Verifique cabo USB ou conexão Wi-Fi
- Ative "Depuração USB" em Configurações do desenvolvedor

### App não inicia
```pwsh
# Ver crash logs
adb logcat -s AndroidRuntime:E

# Limpar dados do app
adb shell pm clear com.example.jogoinfantil
```

### Anúncios não carregam
- Verifique internet do dispositivo
- Confirme IDs de teste nos logs
- Veja status do consentimento: `canRequestAds=true`
