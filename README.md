# Matemática Divertida (Android)

Jogo educativo de matemática para crianças, feito com Jetpack Compose e publicado na Google Play (pacote `com.joaop.matematicadivertida`).

## Funcionalidades
- Fases infinitas com dificuldade adaptativa: adição, subtração, multiplicação e divisão
- Dicas progressivas em 3 níveis, micro-lições e repetição espaçada das questões erradas
- Chefões a cada 5 fases, Trilha de Mundos, Desafio Relâmpago (60 s) e Modo Treino
- XP e nível do jogador, moedas, 7 avatares, 14 conquistas e power-ups (Escudo e Bomba 50/50)
- Atividades diárias: sequência 🔥, prêmio de 7 dias, Roleta da Sorte e desafio diário
- Retenção: onboarding no primeiro uso, lembrete diário local e pedido de avaliação in-app

## Requisitos
- Android Studio recente (AGP 8.13, Gradle 8.13)
- JDK 21
- Android SDK 36 (minSdk 24)

## Configuração local
1. Coloque o `google-services.json` do Firebase em `app/`. Ele não é versionado.
2. Para gerar o release assinado, crie `keystore.properties` na raiz a partir de `keystore.properties.example`. Sem ele, o release é gerado sem assinatura.
3. `gradle.properties` aponta `org.gradle.java.home` para o JDK da máquina de desenvolvimento. Em outra máquina, sobrescreva essa propriedade em `%USERPROFILE%\.gradle\gradle.properties`.

> **Pasta com acento:** o Gradle não carrega as classes dos testes unitários quando o caminho do projeto tem caracteres como "á" (`ClassNotFoundException`). Rode os testes a partir de uma pasta sem acento ou de uma unidade criada com `subst`.

## Anúncios e Política para Famílias
- Único formato de anúncio: banner (`BannerAdView`). Intersticial, recompensado e tela cheia não podem ser usados neste app.
- `RequestConfiguration` marca TFCD e classificação máxima **G**; a permissão `AD_ID` é removida do manifesto.
- O SDK de anúncios só é inicializado depois que a UMP libera (`canRequestAds()`).
- Builds debug usam os IDs de teste do AdMob (`app/src/debug/res/values/admob_ids.xml`) e desligam o Firebase.

## Estrutura (`app/src/main/java/com/joaop/matematicadivertida/`)
- `MainActivity.kt`: `GameApp()` com estado e navegação, tela de jogo e configurações
- `ui/MainMenu.kt`, `ui/Onboarding.kt`: menu principal, primeiro uso e convite do lembrete
- `ui/GameDialogs.kt`, `ui/TimeAttackScreen.kt`, `ui/StatsAndAchievements.kt`, `ui/TrainingMode.kt`, `ui/VisualAids.kt`: telas e diálogos
- `game/QuestionLogic.kt`: geração de questões, níveis e dicas
- `ads/BannerAd.kt`: banner do AdMob
- `data/GameDataManager.kt`: persistência (SharedPreferences) e regras de progresso
- `retention/DailyReminder.kt`, `retention/ReviewPrompter.kt`: lembrete diário e avaliação na loja
- `MyFirebaseMessagingService.kt`: notificações push (FCM)

## Testes
```bash
./gradlew testDebugUnitTest
```

```bash
./gradlew connectedDebugAndroidTest
```

O primeiro comando roda a lógica do jogo e da retenção na JVM; o segundo, os testes de UI e de persistência num emulador ou aparelho.

O CI (`.github/workflows/ci-java21.yml`) compila o debug e roda testes unitários e lint; em outro job, roda os testes instrumentados num emulador. Sem o segredo `GOOGLE_SERVICES_JSON`, ele usa o `google-services.json` de placeholder em `.github/ci/`.
