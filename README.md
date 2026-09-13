# Matemática Divertida (Android)

Jogo educativo de matemática para crianças de 6 a 10 anos, feito com Jetpack Compose.
Em produção na Google Play.

- Pacote: `com.joaop.matematicadivertida`
- Versão atual: **1.2.8** (versionCode 32)

## Requisitos

- Android Studio (Giraffe ou superior)
- Android SDK 24+ (`minSdk 24`, `compileSdk`/`targetSdk 36`)
- JDK 21 (LTS)

## Como abrir e executar

1. No Android Studio, "Open" e selecione a pasta do projeto.
2. Espere o Gradle sincronizar (o primeiro build baixa as dependências).
3. Conecte um aparelho ou inicie um emulador (API 24+).
4. Run ▶.

Pela linha de comando:

```
./gradlew assembleDebug          # APK de debug
./gradlew test                   # testes unitários
./gradlew connectedDebugAndroidTest   # testes instrumentados (precisa de emulador)
```

## ⚠️ Política para Famílias do Google Play — leia antes de mexer em anúncios

O app é dirigido a crianças, então vale a Política para Famílias. Duas versões já
foram rejeitadas por causa disso. As regras abaixo não são preferência de estilo:

1. **Só banner ancorado.** Nada de intersticial, tela cheia ou recompensado. A versão
   22 (1.2.4) foi rejeitada com "anúncios que não podem ser fechados".
2. **Nunca use IDs de teste do Google em build de release.** Unidades de teste servem
   criativos de demonstração que ignoram a classificação máxima de conteúdo — foi o
   motivo da segunda rejeição. Os IDs de teste ficam só em
   `app/src/debug/res/values/admob_ids.xml`, que substitui os de produção no debug.
3. **TFCD e classificação G são definidas em código**, em `MainActivity.onCreate`, via
   `RequestConfiguration`, antes de `MobileAds.initialize()`. O SDK **não** lê essas
   flags do manifest — as meta-data que existiam lá eram ignoradas.
4. **A permissão de ID de publicidade (AAID) é removida** no `AndroidManifest.xml`,
   inclusive das bibliotecas que a declaram por conta própria.

### Consentimento (UMP)

O consentimento roda em `requestConsent()` antes de qualquer `loadAd()`. Enquanto a
UMP não confirmar, `MainActivity.canShowAds` fica `false` e o `BannerAdView` não
carrega nada. Falha no consentimento nunca vira "mostra anúncio mesmo assim".

## Como o jogo funciona

- **Fases infinitas.** A dificuldade sobe pelo total de acertos, não pelo número da
  fase — veja `levelConfig()` em `game/GameLogic.kt`. Começa em adição até 10.
- **Chefões** a cada 5 fases (`getBossForLevel`).
- **Mecânicas diárias:** sequência 🔥, prêmio diário de 7 dias, Roleta da Sorte e
  desafio do dia.
- **Apoio ao aprendizado:** dicas progressivas (3 níveis), micro-lições ao introduzir
  uma operação nova, reta numérica, blocos e repetição espaçada das questões erradas.
- **Outros modos:** Desafio Relâmpago (60s), Modo Treino por operação e Trilha de Mundos.

## Retenção

A versão 1.2.8 atacou a retenção de 2 dias, que estava em ≈ 0:

- **Lembrete local diário** às 18h, agendado com WorkManager
  (`notifications/ReminderScheduler.kt`). A mensagem sai do estado real do jogo e
  nenhuma notificação é enviada se a criança já jogou e não deixou nada pendente.
- **Pedido de avaliação in-app** (`review/ReviewPrompt.kt`), só em momento de vitória
  e com guardas de frequência.
- **Menu com hierarquia e onboarding** de 3 passos na primeira execução.

Métrica para acompanhar no Play Console: Estatísticas → Retenção → retenção de
dispositivos por 2 dias.

## Estrutura

```
app/src/main/java/com/joaop/matematicadivertida/
├── MainActivity.kt                  # telas: menu, jogo, configurações, diálogos, banner
├── game/GameLogic.kt                # regras puras: fase, questões, dicas, reforço
├── data/GameDataManager.kt          # persistência: streak, conquistas, power-ups, bosses
├── models/DataModels.kt             # Op, Question, Achievement, Avatar, BossInfo, PowerUp
├── notifications/                   # lembrete local diário (WorkManager)
├── review/ReviewPrompt.kt           # pedido de avaliação na Play
├── ui/                              # onboarding, menu, estatísticas, treino
├── SoundFeedbackPlayer.kt
└── MyFirebaseMessagingService.kt    # push remoto (FCM)
```

Todos os arquivos usam o mesmo pacote `com.joaop.matematicadivertida`, mesmo estando em
subpastas — é intencional, evita imports entre eles.

## Assinatura

As credenciais ficam em `keystore.properties`, na raiz, **fora do git**. Copie
`keystore.properties.example` e preencha com os seus valores. Este repositório é
público: nunca comite senha de keystore.

## Publicação

- Cada envio precisa de um `versionCode` novo. O Play reserva todo código já enviado:
  23, 30 e 31 estão ocupados.
- Gere o bundle com `./gradlew bundleRelease` (sai em
  `app/build/outputs/bundle/release/`).
- O texto de "novidades" da loja está em `RELEASE_NOTES.txt`.
