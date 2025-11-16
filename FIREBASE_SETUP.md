# 🔥 Configuração Firebase - Matemática Divertida

## 📋 Passo a Passo

### 1️⃣ Criar Projeto no Firebase Console

1. Acesse: https://console.firebase.google.com/
2. Clique em **"Adicionar projeto"**
3. Nome do projeto: `Matemática Divertida` (ou o nome que preferir)
4. Clique em **"Continuar"**
5. Google Analytics:
   - ✅ **Recomendado**: Ativar (para métricas de uso)
   - ⚠️ Ou desativar se não quiser coletar dados
6. Clique em **"Criar projeto"**

---

### 2️⃣ Adicionar App Android ao Projeto

1. No console do Firebase, clique no ícone **Android**
2. Preencha os dados:
   - **Nome do pacote Android**: `com.joaop.matematicadivertida`
   - **Nome do app (opcional)**: `Matemática Divertida`
   - **Certificado de autenticação SHA-1 (opcional)**: Deixe em branco por enquanto
3. Clique em **"Registrar app"**

---

### 3️⃣ Baixar google-services.json

1. Na próxima tela, clique em **"Fazer download do google-services.json"**
2. **IMPORTANTE**: Salve este arquivo
3. Cole o arquivo na pasta: `app/` (ao lado do `build.gradle.kts`)

**Estrutura esperada:**
```
Jogo infantil/
├── app/
│   ├── build.gradle.kts
│   ├── google-services.json  ← AQUI!
│   └── src/
```

---

### 4️⃣ Obter SHA-1 (Para Google Sign-In futuro)

Se quiser adicionar login com Google no futuro, você precisará do SHA-1:

**No PowerShell, execute:**

```powershell
# Para Debug (desenvolvimento)
cd "c:\Users\joaop\OneDrive\Documentos\Creates\Jogo infantil"
keytool -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android

# Para Release (produção)
keytool -list -v -keystore "app\matematica-divertida.jks" -alias matematica-divertida-key -storepass matematica2024 -keypass matematica2024
```

Copie o SHA-1 que aparece e adicione nas configurações do app no Firebase Console:
- Vá em **Configurações do projeto** (ícone engrenagem)
- Role até **Seus apps**
- Clique em **Adicionar impressão digital**
- Cole o SHA-1

---

### 5️⃣ Compilar e Testar

Após colocar o `google-services.json` na pasta `app/`, compile:

```powershell
./gradlew assembleDebug
```

Se der erro de "google-services.json não encontrado", verifique o caminho!

---

## 🎯 O que Firebase oferece

### ✅ Já Configurado:
- **Firebase Analytics**: Métricas automáticas de uso do app
- **AdMob Integration**: Integração melhorada com anúncios

### 🔧 Opcional (descomente no build.gradle.kts):
- **Crashlytics**: Rastreamento automático de crashes
- **Cloud Messaging**: Notificações push
- **Remote Config**: Alterar configurações sem atualizar app
- **Cloud Firestore**: Banco de dados em nuvem
- **Authentication**: Login com Google/Facebook/Email

---

## 📊 Verificar Funcionamento

Após instalar o app com Firebase configurado:

1. Abra o Firebase Console
2. Vá em **Analytics** → **Eventos**
3. Aguarde alguns minutos
4. Você verá eventos como:
   - `first_open` (primeira abertura)
   - `session_start` (início de sessão)
   - `screen_view` (visualizações de tela)

---

## ⚠️ Arquivo google-services.json

**NUNCA compartilhe publicamente!**
- ❌ Não commite no GitHub público
- ✅ Adicione ao `.gitignore`:

```
# Firebase
google-services.json
```

Se precisar compartilhar o código, crie um arquivo de exemplo:
```
google-services.json.example
```

---

## 🚀 Próximos Passos

Depois de configurar, você pode:
1. ✅ Ver estatísticas de uso no Firebase Analytics
2. ✅ Integrar salvamento em nuvem (Firestore)
3. ✅ Adicionar login com Google
4. ✅ Enviar notificações de desafios diários
5. ✅ Rastrear crashes automaticamente

---

**Status Atual:**
- ✅ Plugins configurados
- ✅ Dependências adicionadas
- ⏳ **AGUARDANDO**: Arquivo `google-services.json`

**Próximo passo:** Baixe o `google-services.json` e coloque na pasta `app/`
