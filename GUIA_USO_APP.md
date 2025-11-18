# 📱 App de Matemática para Crianças - Guia de Uso

## ✅ Status de Instalação

**App instalado com sucesso no emulador Android!**

```
Dispositivo: Pixel 7 Pro (API 16)
App: com.joaop.matematicadivertida
Versão: 1.0 (Debug)
Status: ✅ Pronto para usar
```

---

## 🎮 Como Usar o App

### 1. **Iniciar o App**
- O app foi instalado no emulador Android Studio
- Procure por "Jogo Infantil" ou "Matemática Divertida" na lista de apps
- Clique para abrir

### 2. **Funcionalidades Principais**

#### 🎯 **Gameplay**
- Responda questões de matemática (Adição, Subtração, Multiplicação, Divisão)
- Clique em um dos 4 botões para responder
- Veja feedback imediato (correto/incorreto)
- Ganhe XP e moedas por acertos

#### 💡 **Sistema de Dicas**
- Clique no botão "💡 Dica" para ver uma dica
- Clique em "🔎 Como resolver" para ver a solução passo a passo
- Escolha entre "Ver Solução" ou "Fazer Junto" (modo interativo)

#### 📊 **Estatísticas**
- Acompanhe seu progresso em cada operação
- Veja taxa de acurácia (acertos/total)
- Tempo médio por questão
- Número de questões respondidas

#### 🏆 **Conquistas**
- Desbloqueie 14 tipos diferentes de conquistas
- Conquistas de quantidade: Primeiro Acerto, 10/50/100 corretos
- Conquistas de sequência: 5/10 acertos consecutivos
- Conquistas de nível: Atinja fases 10, 20, 30
- Conquistas de especialista: 100 acertos em cada operação

#### 💰 **Sistema de Gamificação**
- **XP**: Ganhe pontos por cada resposta correta
- **Moedas**: Ganhe moedas por acertos rápidos e perfeitos
- **Nível do Jogador**: Suba de nível com XP
- **Progresso de Fase**: Avance para fases cada vez mais desafiadoras

---

## 🔧 Requisitos Técnicos

- Android 7.0+ (API 24+)
- 100 MB de espaço livre
- Conexão de internet (para Firebase - release build apenas)

### Debug vs Release Build:
- **Debug**: Sem Firebase, Ads ou UMP (rápido para teste)
- **Release**: Com todas as funcionalidades (publicação)

---

## 📊 Testes Disponíveis

O projeto inclui **39 testes automatizados**:

### Testes de Gameplay (10 testes)
```bash
./gradlew :app:connectedDebugAndroidTest -Dtest=GameplayUITest
```

### Testes de Persistência (10 testes)
```bash
./gradlew :app:connectedDebugAndroidTest -Dtest=GameDataPersistenceTest
```

### Testes de Conquistas (12 testes)
```bash
./gradlew :app:connectedDebugAndroidTest -Dtest=AchievementsTest
```

### Testes de Startup (7 testes)
```bash
./gradlew :app:connectedDebugAndroidTest -Dtest=AppStartupTest
```

---

## 📝 Dados Salvos

O app salva automaticamente:
- Nível/Fase atual
- Estatísticas de cada operação (adição, subtração, multiplicação, divisão)
- Conquistas desbloqueadas
- XP e moedas totais
- Nível do jogador
- Desafio diário

**Local**: SharedPreferences (`JogoInfantil` storage)

---

## 🚀 Comandos Úteis

### Compilar Debug
```bash
./gradlew :app:compileDebugKotlin
```

### Compilar e Instalar
```bash
./gradlew :app:installDebug
```

### Executar Testes
```bash
./gradlew :app:connectedDebugAndroidTest
```

### Build Release (com dependências)
```bash
./gradlew :app:bundleRelease
```

### Limpar Build
```bash
./gradlew clean
```

---

## 📈 Estrutura do Projeto

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/joaop/matematicadivertida/
│   │   │   ├── MainActivity.kt (interface principal)
│   │   │   ├── OptionalDependencies.kt (reflection para Firebase/Ads)
│   │   │   ├── GameDataManager.kt (persistência)
│   │   │   └── data/
│   │   │       └── GameDataManager.kt (modelos de dados)
│   │   ├── debug/
│   │   │   ├── java/.../AdStubs.kt (stubs para debug)
│   │   │   └── DebugApplication.kt
│   │   └── res/ (recursos)
│   └── androidTest/
│       ├── GameplayUITest.kt
│       ├── GameDataPersistenceTest.kt
│       ├── AchievementsTest.kt
│       └── AppStartupTest.kt
└── build.gradle.kts
```

---

## 🎓 Educação para Crianças

O app foi desenhado especificamente para crianças com:

- ✅ **Interface colorida e amigável**
- ✅ **Feedback imediato e visual**
- ✅ **Sistema de gamificação (XP, moedas, conquistas)**
- ✅ **Dificuldade progressiva (infinitas fases)**
- ✅ **Sistema de dicas interativas**
- ✅ **Sem pressão - aprender é diversão!**

---

## 📞 Suporte

Se houver problemas:
1. Verifique que o emulador está rodando (API 16, Pixel 7 Pro)
2. Execute `./gradlew clean` para limpar
3. Reinstale: `./gradlew :app:installDebug`
4. Verifique os testes: `./gradlew :app:connectedDebugAndroidTest`

---

**Aproveite e bom aprendizado! 🎉**
