# 🚀 GUIA COMPLETO: PUBLICAR NO GOOGLE PLAY STORE

## 📋 VISÃO GERAL

Este guia cobre TODO o processo de publicação do app **Matemática Divertida** na Google Play Store, desde a criação da conta até a submissão final.

---

## ETAPA 1: CRIAR CONTA DE DESENVOLVEDOR 💳

### 1.1 Requisitos
- Conta Google válida
- Cartão de crédito/débito
- Taxa única: **USD $25** (~R$ 125)

### 1.2 Passos
1. Acesse: https://play.google.com/console/signup
2. Faça login com sua conta Google
3. Aceite os termos de serviço
4. Pague a taxa de registro ($25)
5. Complete o perfil de desenvolvedor

⏱️ **Tempo**: ~15 minutos + aprovação em 24-48h

---

## ETAPA 2: HABILITAR GITHUB PAGES 🌐

### 2.1 Configurar GitHub Pages
1. Acesse: https://github.com/jppaulus/matematicadivertida/settings/pages
2. Em **"Source"**, selecione: **"main"** e **"/ (root)"**
3. Clique em **"Save"**
4. Aguarde 1-2 minutos

### 2.2 Verificar URL
Acesse e teste:
```
https://jppaulus.github.io/matematicadivertida/POLITICA_PRIVACIDADE.html
```

✅ **Esta é a URL que você usará no Play Console!**

---

## ETAPA 3: CAPTURAR SCREENSHOTS 📸

### 3.1 Preparar Emulador
```powershell
# Abrir emulador
cd "C:\Users\joaop\AppData\Local\Android\Sdk\emulator"
.\emulator -avd Pixel_7_Pro

# OU abrir pelo Android Studio
```

### 3.2 Instalar App
```powershell
cd "c:\Users\joaop\OneDrive\Documentos\Creates\Jogo infantil"
.\gradlew :app:installDebug
```

### 3.3 Capturar Screenshots
```powershell
.\capturar_screenshots_automatico.ps1
```

**Siga as instruções na tela para capturar:**
1. Menu principal
2. Gameplay
3. Sistema de dicas
4. Micro-lição
5. Estatísticas
6. Modo treino

📁 **Screenshots salvos em**: `screenshots/play-store/`

---

## ETAPA 4: CRIAR ASSETS VISUAIS 🎨

### 4.1 Ícone 512x512

**Opção A: Canva (Recomendado)**
1. Acesse: https://www.canva.com/
2. Criar design → Tamanho personalizado: **512 x 512 px**
3. Use template sugerido em `GUIA_COMPLETO_ASSETS.md`
4. Elementos:
   - Emoji 🎮 grande centralizado
   - Símbolos + - × ÷ coloridos
   - Fundo gradiente azul/verde
5. Baixar como PNG → `icone_512x512.png`

**Cores do App:**
- Azul: `#2196F3`
- Verde: `#4CAF50`
- Amarelo: `#FFC107`
- Laranja: `#FF9800`

### 4.2 Feature Graphic 1024x500

1. Canva → Tamanho personalizado: **1024 x 500 px**
2. Layout sugerido:
   ```
   [🎮]  MATEMÁTICA DIVERTIDA
         Aprenda brincando! 🌟
   ```
3. Use mesmas cores do ícone
4. Baixar como PNG → `feature_graphic_1024x500.png`

📖 **Consulte**: `GUIA_COMPLETO_ASSETS.md` para templates detalhados

---

## ETAPA 5: BUILD FINAL 🔨

### 5.1 Compilar AAB Release
```powershell
cd "c:\Users\joaop\OneDrive\Documentos\Creates\Jogo infantil"
.\gradlew clean :app:bundleRelease
```

### 5.2 Verificar Arquivo
```powershell
Get-ChildItem "app\build\outputs\bundle\release"
```

✅ **Arquivo gerado**: `app-release.aab` (~11.4 MB)

📁 **Localização**: `app\build\outputs\bundle\release\app-release.aab`

---

## ETAPA 6: CRIAR APP NO PLAY CONSOLE 📱

### 6.1 Acessar Console
1. Acesse: https://play.google.com/console
2. Clique em **"Criar app"**

### 6.2 Informações Básicas
- **Nome do app**: `Matemática Divertida`
- **Idioma padrão**: `Português (Brasil)`
- **Tipo**: `App`
- **Gratuito/pago**: `Gratuito`

### 6.3 Declarações
- [ ] ✅ App segue políticas do Google Play
- [ ] ✅ App segue leis de exportação dos EUA
- [ ] ✅ Criado por desenvolvedor/empresa registrada

Clique em **"Criar app"**

---

## ETAPA 7: CONFIGURAR PRESENÇA NA LOJA 🏪

### 7.1 Descrição do App

**Navegue**: Console → Seu App → **"Presença na loja principal"** → **"Listagem da loja principal"**

Preencha:

**Título:**
```
Matemática Divertida
```

**Descrição curta:**
```
Aprenda matemática jogando! Adição, subtração, multiplicação e divisão para crianças.
```

**Descrição completa:**
*(Copie de `INFORMACOES_PLAY_CONSOLE.md` - seção "Descrição Completa")*

### 7.2 Gráficos da Loja

**Navegue**: **"Gráficos da loja"**

**Upload obrigatório:**
- ✅ **Ícone**: `icone_512x512.png`
- ✅ **Feature Graphic**: `feature_graphic_1024x500.png`
- ✅ **Screenshots**: Mínimo 2, recomendado 4-8
  - `01_menu.png`
  - `02_gameplay.png`
  - `03_dicas.png`
  - `04_microlição.png`
  - `05_stats.png`
  - `06_treino.png`

### 7.3 Categorização
- **Categoria**: `Educação`
- **Tags**: `matemática, educação, crianças, jogos educativos`

### 7.4 Detalhes de Contato
- **E-mail**: `joaopgomes9110@gmail.com`
- **Site (opcional)**: `https://github.com/jppaulus/matematicadivertida`

**Salvar**

---

## ETAPA 8: CLASSIFICAÇÃO DE CONTEÚDO 🛡️

### 8.1 Iniciar Questionário

**Navegue**: **"Classificação de conteúdo"** → **"Iniciar questionário"**

**Endereço de e-mail:** `joaopgomes9110@gmail.com`

**Categoria do app:** `Educação`

### 8.2 Responder Questionário

**Violência:**
- Há violência realista? `Não`
- Há violência fantasiosa? `Não`

**Sexualidade:**
- Há conteúdo sexual? `Não`

**Drogas:**
- Há referência a drogas? `Não`

**Linguagem:**
- Há palavrões? `Não`

**Medo:**
- Há conteúdo assustador? `Não`

**Jogos de Azar:**
- Há jogos de azar? `Não`

**Outros:**
- Há anúncios? `Sim`
  - Anúncios apropriados para todas as idades? `Sim`

**Enviar questionário**

---

## ETAPA 9: PÚBLICO-ALVO E CONTEÚDO 👶

### 9.1 Público-Alvo

**Navegue**: **"Público-alvo e conteúdo"** → **"Público-alvo"**

**Faixa etária:**
- [ ] ✅ 5 anos ou menos
- [ ] ✅ 6-8 anos
- [ ] ✅ 9-12 anos

**Seu app é direcionado a crianças?**
- ✅ `Sim, meu app é direcionado principalmente a crianças`

### 9.2 Anúncios
**Seu app exibe anúncios?**
- ✅ `Sim`

**Os anúncios são apropriados para crianças?**
- ✅ `Sim, todos os anúncios seguem as políticas de Anúncios e Famílias do Google Play`

### 9.3 COPPA (Lei de Privacidade Infantil - EUA)
**Seu app coleta informações pessoais de crianças?**
- ❌ `Não` *(dados armazenados apenas localmente)*

**Salvar**

---

## ETAPA 10: POLÍTICA DE PRIVACIDADE 🔒

### 10.1 Adicionar URL

**Navegue**: **"Política de Privacidade"**

**URL da política de privacidade:**
```
https://jppaulus.github.io/matematicadivertida/POLITICA_PRIVACIDADE.html
```

**Teste a URL** antes de salvar!

**Salvar**

---

## ETAPA 11: CONFIGURAR VERSÃO DE PRODUÇÃO 🚀

### 11.1 Criar Primeira Versão

**Navegue**: **"Versões"** → **"Produção"** → **"Criar nova versão"**

### 11.2 Upload do AAB

**Clique em "Upload"** e selecione:
```
app\build\outputs\bundle\release\app-release.aab
```

⏱️ Aguarde o upload (pode levar 2-5 minutos)

### 11.3 Notas de Versão

**Nome da versão:** `1.0.0`

**Notas da versão (pt-BR):**
```
🎉 Versão Inicial

Funcionalidades:
• 4 operações matemáticas (adição, subtração, multiplicação, divisão)
• 30 níveis de dificuldade progressiva
• Sistema de dicas contextuais
• Micro-lições pedagógicas
• Modo treino para praticar operações específicas
• Estatísticas detalhadas de desempenho
• Sistema de conquistas
• Gamificação com XP, moedas e estrelas

Aprenda matemática brincando! 🎮
```

### 11.4 Revisão Final

Revise:
- [ ] AAB carregado
- [ ] Notas de versão preenchidas
- [ ] Versão: 1 (1.0.0)

**Clique em "Salvar"**

---

## ETAPA 12: CONFIGURAÇÕES DE DISTRIBUIÇÃO 🌍

### 12.1 Países e Regiões

**Navegue**: **"Países/regiões"**

**Opção A - Mundial (Recomendado):**
- [ ] ✅ Selecionar todos os países

**Opção B - Apenas Brasil:**
- [ ] ✅ Brasil

**Salvar**

---

## ETAPA 13: ENVIAR PARA REVISÃO ✅

### 13.1 Verificar Checklist

**Navegue**: **"Painel"** → Verifique todas as seções:

- [ ] ✅ Listagem da loja completa
- [ ] ✅ Gráficos carregados (ícone, feature, screenshots)
- [ ] ✅ Classificação de conteúdo finalizada
- [ ] ✅ Público-alvo configurado
- [ ] ✅ Política de privacidade adicionada
- [ ] ✅ AAB enviado
- [ ] ✅ Países/regiões selecionados

### 13.2 Enviar

**Navegue**: **"Versões"** → **"Produção"** → **Versão pendente**

**Clique em "Enviar para revisão"**

🎉 **Pronto! Seu app foi enviado para análise!**

---

## ETAPA 14: AGUARDAR APROVAÇÃO ⏳

### 14.1 Tempo de Revisão
- **Primeira submissão**: 24-72 horas
- **Geralmente**: 1-2 dias úteis

### 14.2 Status da Revisão

**Acompanhar em**: Console → Seu App → **"Painel"**

**Status possíveis:**
- 🟡 **Em análise**: Google está revisando
- ✅ **Aprovado**: App publicado na loja!
- ❌ **Rejeitado**: Verifique e-mail com motivos

### 14.3 Se Rejeitado

1. Leia o e-mail do Google com feedback
2. Corrija os problemas apontados
3. Crie nova versão e reenvie

**Problemas comuns:**
- Ícone/screenshots não carregam
- Política de privacidade inacessível
- Descrição muito curta/genérica
- Classificação etária incorreta

---

## ETAPA 15: APP PUBLICADO! 🎊

### 15.1 Após Aprovação

Você receberá e-mail: **"Seu app foi publicado"**

**Link da loja:**
```
https://play.google.com/store/apps/details?id=com.joaop.matematicadivertida
```

### 15.2 Compartilhar

Compartilhe o link nas redes sociais:
- Facebook
- Instagram
- WhatsApp
- LinkedIn

### 15.3 Monitorar

**Console → Estatísticas:**
- Downloads
- Avaliações
- Comentários
- Receita (AdMob)

---

## 📚 DOCUMENTAÇÃO DE REFERÊNCIA

### Arquivos Criados
- ✅ `GUIA_COMPLETO_ASSETS.md` - Como criar ícone e banner
- ✅ `HOSPEDAR_POLITICA.md` - Hospedar política no GitHub Pages
- ✅ `INFORMACOES_PLAY_CONSOLE.md` - Dados para preencher no console
- ✅ `capturar_screenshots_automatico.ps1` - Script para screenshots

### URLs Importantes
- **Play Console**: https://play.google.com/console
- **Política de Privacidade**: https://jppaulus.github.io/matematicadivertida/POLITICA_PRIVACIDADE.html
- **Repositório GitHub**: https://github.com/jppaulus/matematicadivertida

---

## ⚠️ AVISOS IMPORTANTES

### 🔐 BACKUP DA KEYSTORE
```
⚠️ CRÍTICO: Guarde matematica-divertida.jks e as senhas em local seguro!
Sem o arquivo .jks, você NÃO poderá atualizar o app no futuro!
```

**Faça backup em:**
- Pen drive
- Google Drive (criptografado)
- Dropbox

### 📧 E-mail de Contato
```
Sempre responda e-mails do Google Play Console em até 7 dias.
E-mails não respondidos podem resultar em suspensão do app.
```

### 🔄 Atualizações Futuras
Para atualizar o app:
1. Incremente `versionCode` em `build.gradle.kts`
2. Compile novo AAB
3. Console → Produção → Criar nova versão
4. Upload do AAB → Enviar

---

## 🆘 PRECISA DE AJUDA?

### Suporte Google Play
- **Central de Ajuda**: https://support.google.com/googleplay/android-developer
- **Comunidade**: https://groups.google.com/g/android-developers
- **Chat**: Disponível no Play Console (ícone 💬)

### Problemas Comuns

**"AAB não carrega"**
- Verifique tamanho (<150 MB)
- Recompile: `.\gradlew clean :app:bundleRelease`

**"Política de privacidade inacessível"**
- Teste URL no navegador
- Aguarde 5 minutos após habilitar GitHub Pages

**"Ícone rejeitado"**
- Verifique tamanho: 512x512
- Use PNG 32-bit
- Sem bordas arredondadas

---

## ✅ CHECKLIST FINAL

Antes de enviar, confirme:

- [ ] Conta de desenvolvedor criada e paga ($25)
- [ ] GitHub Pages habilitado
- [ ] Política de privacidade acessível
- [ ] 6 screenshots capturados
- [ ] Ícone 512x512 criado
- [ ] Feature graphic 1024x500 criado
- [ ] AAB compilado (11.4 MB)
- [ ] Todas as descrições preenchidas
- [ ] Classificação de conteúdo completa
- [ ] Público-alvo configurado (crianças)
- [ ] Política de privacidade URL adicionada
- [ ] AAB enviado com notas de versão
- [ ] Países/regiões selecionados
- [ ] Backup da keystore feito
- [ ] Tudo revisado e enviado

---

**🎉 PARABÉNS! SEU APP ESTÁ NO CAMINHO PARA A GOOGLE PLAY STORE! 🚀**

---

**Criado em**: 18/11/2025
**Autor**: João Paulo
**App**: Matemática Divertida v1.0.0
