# 🧪 Como Testar se seus IDs Reais do AdMob Estão Ativos

## 📋 Pré-requisitos
- Conta AdMob criada há pelo menos 24 horas
- App compilado com seus IDs reais

---

## 🔄 Passo 1: Ativar IDs Reais

1. Abra o arquivo: `app/src/main/res/values/admob_ids.xml`

2. **Substitua os IDs de teste por seus IDs reais:**

```xml
<string name="admob_app_id">ca-app-pub-9116858830076274~4107883547</string>
<string name="admob_banner_id">ca-app-pub-9116858830076274/9412181118</string>
<string name="admob_interstitial_id">ca-app-pub-9116858830076274/9412181118</string>
<string name="admob_rewarded_id">ca-app-pub-9116858830076274/9412181118</string>
```

3. **Remova os comentários do UMP** no `MainActivity.kt` (linha ~62):

Troque:
```kotlin
// Log.d(TAG, "📋 Solicitando consentimento UMP...")
// requestConsent()
```

Por:
```kotlin
Log.d(TAG, "📋 Solicitando consentimento UMP...")
requestConsent()
```

4. **Descomente as verificações canRequestAds()** nas funções:
   - `loadInterstitial()` (linha ~198)
   - `loadRewardedAd()` (linha ~238)
   - `BannerAdView()` (linha ~1034)

5. Compile: `./gradlew assembleDebug`

---

## 🎮 Passo 2: Testar no Dispositivo

### ✅ **Se IDs estão ATIVOS:**
- Banner aparece na parte inferior imediatamente
- Intersticial aparece após 3 fases
- Anúncio recompensado carrega ao clicar no botão
- **Logs no Logcat:**
  ```
  ✅ Mobile Ads SDK inicializado com sucesso
  ✅ Banner carregado com sucesso
  ✅ Intersticial carregado com sucesso
  ✅ Anúncio recompensado carregado
  ```

### ❌ **Se IDs ainda NÃO estão ativos:**
- Anúncios não aparecem
- **Logs mostram erros:**
  ```
  ❌ Falha ao carregar banner
  Código: 3 (ERROR_CODE_NO_FILL)
  ```
  ou
  ```
  ❌ Falha ao carregar intersticial
  Código: 1 (ERROR_CODE_INTERNAL_ERROR)
  ```

---

## 📊 Passo 3: Ver Logs Detalhados

Após instalar o APK, use o Logcat:

```powershell
adb logcat -s JogoInfantil:D
```

**Procure por:**
- ✅ `"✅ Banner carregado com sucesso"` = Funcionando!
- ❌ `"ERROR_CODE_NO_FILL"` = IDs ainda não ativos
- ❌ `"ERROR_CODE_INTERNAL_ERROR"` = IDs inválidos ou conta suspensa

---

## ⏰ Quanto tempo demora?

| Situação | Tempo Esperado |
|----------|----------------|
| Conta nova criada hoje | 24-48 horas |
| Conta com app já publicado | Imediato |
| Primeira solicitação de anúncio | Até 1 hora após primeira request |

---

## 🚨 Códigos de Erro Comuns

| Código | Significado | Solução |
|--------|-------------|---------|
| **0** | ERROR_CODE_INTERNAL_ERROR | Aguarde 24-48h |
| **1** | ERROR_CODE_INVALID_REQUEST | Verifique se IDs estão corretos |
| **2** | ERROR_CODE_NETWORK_ERROR | Problemas de internet |
| **3** | ERROR_CODE_NO_FILL | IDs não ativos OU sem anúncios disponíveis |

---

## 🎯 Forma Mais Rápida de Verificar

**No Console do AdMob:**
1. Vá em https://apps.admob.com/
2. Clique no seu app
3. Vá em "Unidades de anúncio"
4. Se aparecer **"Pronta para exibir anúncios"** = Ativo! ✅
5. Se aparecer **"Aguardando primeira solicitação"** = Ainda não ativo ⏳

---

## 💡 Dica

**Enquanto aguarda ativação:**
- Use os IDs de teste (já configurados)
- Continue desenvolvendo e testando
- Quando confirmar ativação no Console, troque para IDs reais

**IDs de teste sempre funcionam imediatamente!**
