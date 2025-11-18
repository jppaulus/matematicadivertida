# 🌐 HOSPEDAR POLÍTICA DE PRIVACIDADE NO GITHUB PAGES

## ✅ PASSO 1: Habilitar GitHub Pages

1. Acesse: https://github.com/jppaulus/matematicadivertida
2. Clique em **"Settings"** (Configurações)
3. No menu lateral, clique em **"Pages"**
4. Em **"Source"**, selecione: **"Deploy from a branch"**
5. Em **"Branch"**, selecione: **"main"** e **"/ (root)"**
6. Clique em **"Save"**

⏱️ **Aguarde 1-2 minutos** para o site ser publicado.

---

## ✅ PASSO 2: Verificar URL Gerada

Após habilitar, a URL será:

```
https://jppaulus.github.io/matematicadivertida/
```

Teste acessando:
- **Página principal**: https://jppaulus.github.io/matematicadivertida/
- **Política de Privacidade**: https://jppaulus.github.io/matematicadivertida/POLITICA_PRIVACIDADE.html

---

## ✅ PASSO 3: Criar Página HTML da Política

O GitHub Pages precisa de um arquivo `.html` para exibir corretamente.

### Execute este script para criar o HTML:

```powershell
# Criar versão HTML da política de privacidade
$mdContent = Get-Content "POLITICA_PRIVACIDADE.md" -Raw

$htmlTemplate = @"
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Política de Privacidade - Matemática Divertida</title>
    <style>
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
            line-height: 1.6;
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
            background: #f5f5f5;
        }
        .container {
            background: white;
            padding: 40px;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        h1 {
            color: #2196F3;
            border-bottom: 3px solid #2196F3;
            padding-bottom: 10px;
        }
        h2 {
            color: #4CAF50;
            margin-top: 30px;
        }
        strong {
            color: #333;
        }
        a {
            color: #2196F3;
            text-decoration: none;
        }
        a:hover {
            text-decoration: underline;
        }
        .footer {
            margin-top: 40px;
            padding-top: 20px;
            border-top: 1px solid #ddd;
            text-align: center;
            color: #666;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>📱 Política de Privacidade</h1>
        <p><strong>Matemática Divertida</strong></p>
        <p><em>Última atualização: $(Get-Date -Format 'dd/MM/yyyy')</em></p>
        
        <h2>1. Introdução</h2>
        <p>O aplicativo <strong>Matemática Divertida</strong> ("nós", "nosso" ou "aplicativo") respeita a privacidade de seus usuários ("você" ou "usuário"). Esta Política de Privacidade explica como coletamos, usamos, divulgamos e protegemos suas informações quando você usa nosso aplicativo móvel.</p>
        
        <h2>2. Informações que Coletamos</h2>
        <h3>2.1 Informações Coletadas Automaticamente</h3>
        <ul>
            <li><strong>Dados de Uso:</strong> Informações sobre como você interage com o aplicativo, incluindo páginas visitadas, tempo gasto e funcionalidades utilizadas.</li>
            <li><strong>Dados do Dispositivo:</strong> Informações sobre o dispositivo que você usa para acessar o aplicativo, como modelo do dispositivo, sistema operacional, identificadores únicos do dispositivo e endereço IP.</li>
            <li><strong>Dados de Publicidade:</strong> Coletamos informações relacionadas aos anúncios exibidos, incluindo cliques, impressões e interações com anúncios através do Google AdMob.</li>
        </ul>
        
        <h3>2.2 Informações que Você Fornece</h3>
        <ul>
            <li><strong>Progresso do Jogo:</strong> Dados sobre seu desempenho, pontuações, níveis completados e conquistas desbloqueadas são armazenados localmente no dispositivo.</li>
        </ul>
        
        <h2>3. Como Usamos Suas Informações</h2>
        <ul>
            <li><strong>Fornecer e Melhorar o Serviço:</strong> Para operar, manter e melhorar o aplicativo e suas funcionalidades.</li>
            <li><strong>Personalizar a Experiência:</strong> Para adaptar o conteúdo e as funcionalidades do aplicativo às suas necessidades e preferências.</li>
            <li><strong>Anúncios:</strong> Para exibir anúncios personalizados através do Google AdMob. Estes anúncios podem ser baseados em suas interações com o aplicativo e outros sites ou aplicativos.</li>
            <li><strong>Análise e Estatísticas:</strong> Para entender como os usuários interagem com o aplicativo, visando melhorias contínuas.</li>
        </ul>
        
        <h2>4. Compartilhamento de Informações</h2>
        <p>Não vendemos, alugamos ou compartilhamos suas informações pessoais com terceiros, exceto nas seguintes circunstâncias:</p>
        <ul>
            <li><strong>Provedores de Serviços:</strong> Compartilhamos informações com terceiros que prestam serviços em nosso nome, como o Google AdMob para exibição de anúncios.</li>
            <li><strong>Conformidade Legal:</strong> Podemos divulgar suas informações se exigido por lei ou em resposta a solicitações legais.</li>
        </ul>
        
        <h2>5. Serviços de Terceiros</h2>
        <h3>5.1 Google AdMob</h3>
        <p>Utilizamos o Google AdMob para exibir anúncios no aplicativo. O AdMob pode coletar e processar dados sobre seu dispositivo e suas interações com anúncios. Para mais informações, consulte a <a href="https://policies.google.com/privacy" target="_blank">Política de Privacidade do Google</a>.</p>
        
        <h3>5.2 Opt-Out de Anúncios Personalizados</h3>
        <p>Você pode optar por não receber anúncios personalizados através das configurações do seu dispositivo:</p>
        <ul>
            <li><strong>Android:</strong> Configurações > Google > Anúncios > Desativar personalização de anúncios</li>
        </ul>
        
        <h2>6. Privacidade de Crianças</h2>
        <p>Nosso aplicativo é destinado ao público infantil. Estamos em conformidade com a <strong>Children's Online Privacy Protection Act (COPPA)</strong> e não coletamos intencionalmente informações pessoais de crianças menores de 13 anos sem o consentimento dos pais.</p>
        <ul>
            <li><strong>Armazenamento Local:</strong> Todos os dados de progresso são armazenados localmente no dispositivo e não são transmitidos para nossos servidores.</li>
            <li><strong>Anúncios Apropriados:</strong> Os anúncios exibidos são configurados para serem adequados para todas as idades através do Google AdMob.</li>
        </ul>
        
        <h2>7. Segurança das Informações</h2>
        <p>Implementamos medidas de segurança para proteger suas informações contra acesso não autorizado, alteração, divulgação ou destruição. No entanto, nenhum método de transmissão pela Internet ou armazenamento eletrônico é 100% seguro.</p>
        
        <h2>8. Seus Direitos</h2>
        <p>Você tem os seguintes direitos em relação às suas informações:</p>
        <ul>
            <li><strong>Acesso:</strong> Solicitar acesso às informações que coletamos sobre você.</li>
            <li><strong>Correção:</strong> Solicitar a correção de informações imprecisas.</li>
            <li><strong>Exclusão:</strong> Solicitar a exclusão de suas informações, sujeito a obrigações legais.</li>
            <li><strong>Opt-Out:</strong> Optar por não receber anúncios personalizados através das configurações do dispositivo.</li>
        </ul>
        
        <h2>9. Alterações a Esta Política</h2>
        <p>Podemos atualizar esta Política de Privacidade periodicamente. Notificaremos você sobre quaisquer alterações publicando a nova política no aplicativo e atualizando a data de "Última atualização".</p>
        
        <h2>10. Contato</h2>
        <p>Se você tiver dúvidas ou preocupações sobre esta Política de Privacidade, entre em contato conosco:</p>
        <ul>
            <li><strong>E-mail:</strong> <a href="mailto:joaopgomes9110@gmail.com">joaopgomes9110@gmail.com</a></li>
        </ul>
        
        <div class="footer">
            <p>© $(Get-Date -Format 'yyyy') Matemática Divertida. Todos os direitos reservados.</p>
        </div>
    </div>
</body>
</html>
"@

# Salvar arquivo HTML
$htmlTemplate | Out-File -FilePath "POLITICA_PRIVACIDADE.html" -Encoding UTF8

Write-Host "✓ Arquivo HTML criado: POLITICA_PRIVACIDADE.html" -ForegroundColor Green
```

---

## ✅ PASSO 4: Commit e Push

```powershell
git add POLITICA_PRIVACIDADE.html
git commit -m "Adicionar política de privacidade em HTML para GitHub Pages"
git push origin main
```

---

## ✅ PASSO 5: Testar URL

Após 1-2 minutos, acesse:

**URL FINAL DA POLÍTICA:**
```
https://jppaulus.github.io/matematicadivertida/POLITICA_PRIVACIDADE.html
```

✅ **Use esta URL no Google Play Console!**

---

## 🔧 ALTERNATIVA: Google Sites (Mais Simples)

Se preferir não usar GitHub Pages:

1. Acesse: https://sites.google.com/
2. Clique em **"Em branco"** para criar site
3. Dê um nome: "Matemática Divertida - Privacidade"
4. Copie o conteúdo de `POLITICA_PRIVACIDADE.md`
5. Cole no site e formate
6. Clique em **"Publicar"**
7. Use a URL gerada no Play Console

---

## 📌 URL para Play Console

Após publicar, você terá uma URL pública:

- **GitHub Pages**: `https://jppaulus.github.io/matematicadivertida/POLITICA_PRIVACIDADE.html`
- **Google Sites**: `https://sites.google.com/view/seu-site/privacidade`

✅ **Copie e cole no campo "URL da política de privacidade" no Play Console**

---

## ✅ CHECKLIST

- [ ] GitHub Pages habilitado no repositório
- [ ] Arquivo HTML criado e commitado
- [ ] URL testada e funcionando
- [ ] URL copiada para usar no Play Console

---

**🎯 PRÓXIMO PASSO:** Com a política hospedada, você pode prosseguir para o build final do APK!
