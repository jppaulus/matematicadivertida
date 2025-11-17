# Jogo Infantil (Android)

Jogo educativo simples (7–10 anos) com 20 fases usando Jetpack Compose. Monetização com AdMob (banner fixo e intersticial entre fases, usando IDs de teste por padrão).

## Requisitos
- Android Studio (Giraffe ou superior)
- Android SDK 24+ (minSdk 24)
- JDK 17

## Como abrir e executar
1. Abra o Android Studio e escolha "Open": selecione a pasta deste projeto.
2. Aguarde o Gradle sincronizar.
3. Conecte um dispositivo ou inicie um emulador Android (API 24+).
4. Run ▶ para instalar e rodar o app.

## AdMob
- O projeto usa IDs de teste do Google por padrão:
  - App ID (manifest): `ca-app-pub-3940256099942544~3347511713`
  - Para builds de debug, o projeto usa IDs de teste do AdMob automaticamente com uma sobreposição de recurso (`app/src/debug/res/values/admob_ids.xml`). Isso previne erros de 'configuração incorreta do publisher' e de incompatibilidade de formato de anúncio durante os testes locais.
  - Banner: `ca-app-pub-3940256099942544/6300978111`
  - Intersticial: `ca-app-pub-3940256099942544/1033173712`
- Antes de publicar, crie um app no AdMob e substitua pelos seus IDs reais:
  - `AndroidManifest.xml` → meta-data `com.google.android.gms.ads.APPLICATION_ID`
  - `MainActivity.kt` → `adUnitId` do Banner e o ID do Intersticial na função `InterstitialAd.load()`
- Em desenvolvimento mantenha IDs de teste para evitar violações de política.

## Consentimento (UE) – UMP
- O app integra o User Messaging Platform (UMP). O consentimento é solicitado no início.
- O carregamento de anúncios é condicionado por `canRequestAds()`; enquanto não houver consentimento, anúncios não são carregados.
- Para testar, use as opções de debug da UMP se necessário (consulte a doc do SDK).

## Ícones e Splash
- Ícones adaptativos criados em `res/mipmap-anydpi-v26/ic_launcher*.xml` com foreground em `res/drawable/ic_launcher_foreground.xml`.
- Splash Screen (Android 12+): tema `Theme.JogoInfantil.Splash` configurado no Manifest, usando `androidx.core:core-splashscreen`.

## Estrutura
- `app/src/main/java/com/example/jogoinfantil/MainActivity.kt` — UI, lógica das fases, anúncios.
- `app/src/main/AndroidManifest.xml` — permissões e App ID do AdMob.
- `app/build.gradle.kts` — dependências (Compose, Mobile Ads SDK).

## Jogo
- 20 fases totais.
- Cada fase exige 5 acertos.
- Dificuldade progride: soma → soma/subtração → multiplicação → misto.
- Intersticial exibido ao concluir fases (se carregado). Banner fixo no rodapé.

## Dicas
- Ajuste `targetCorrect` por fase em `levelConfig` se quiser alongar/encurtar.
- Para revisar métricas, habilite Test Ads até finalizar QA.

## Publicação
- Ative assinatura, versões e políticas de privacidade conforme Play Store.
- Avalie usar Consent SDK (UE) e mensagens de privacidade.
