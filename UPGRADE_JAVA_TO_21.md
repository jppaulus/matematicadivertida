# Upgrade para Java 21 (LTS)

## Resumo
- O projeto está configurado para usar Java 21 no Gradle (runtime) e no toolchain/compilação (Kotlin + Java).

## Estado atual (referência)
- Gradle Wrapper: 8.13
- Android Gradle Plugin (AGP): 8.13.1
- Kotlin: 1.9.25
- Java: 21 (ex.: 21.0.8)

## O que foi feito
- `app/build.gradle.kts`
  - `kotlin { jvmToolchain(21) }`
  - `compileOptions` com `sourceCompatibility` e `targetCompatibility` em `JavaVersion.VERSION_21`
  - `kotlinOptions.jvmTarget = "21"`
- `gradle.properties`
  - `org.gradle.java.home` apontando para um JDK 21 local (neste ambiente: `C:/jdk/jdk-21/jdk-21.0.8`)

## Como verificar
- Rodar `./gradlew.bat -version` e confirmar que o JVM/Daemon está em Java 21.
- Rodar `./gradlew.bat :app:assembleDebug`.

## CI
- Há um workflow em `.github/workflows/ci-java21.yml` que configura JDK 21 e executa o build.

## Observações
- A ferramenta automática `generate_upgrade_plan` (Java upgrade tools) não conseguiu concluir o fluxo para este projeto Android/Gradle; a migração foi validada via build real com Gradle usando Java 21.

