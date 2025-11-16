# 🎓 Matemática Divertida

**Jogo educativo de matemática para crianças de 7 a 10 anos**

## 🎮 Características

### 📚 Sistema de Fases (30 níveis)
- **Fases 1-5**: Adição simples até 10
- **Fases 6-10**: Soma e subtração até 20
- **Fases 11-15**: Tabuada (2 ao 10)
- **Fases 16-18**: Desafio misto (soma, subtração, multiplicação)
- **Fases 19-22**: Números até 50
- **Fases 23-25**: Divisão exata
- **Fases 26-28**: Multiplicação avançada
- **Fases 29-30**: 🏆 Desafio Final (todas as operações)

### 🎯 Mecânicas de Jogo
- ❤️ **Sistema de Vidas**: 3 vidas por fase (perde 1 vida a cada erro)
- 💡 **Dicas Inteligentes**: 3 dicas disponíveis por fase
- ⭐ **Progressão**: Acertos necessários aumentam com a dificuldade
- 📊 **Barra de Progresso**: Visual claro do avanço na fase
- 🎉 **Recompensas**: Feedback positivo a cada conquista

### 🎨 Interface
- Cores vibrantes e atraentes para crianças
- Fontes grandes e legíveis (28-36sp)
- Botões grandes para facilitar o toque
- Emojis e feedback visual constante
- Cards com bordas arredondadas
- Animações suaves

### 💰 Monetização
- Banner fixo no rodapé
- Intersticial a cada fase concluída
- IDs de teste configurados (trocar antes de publicar)

## 🚀 Melhorias Implementadas

### v2.0 - "Matemática Divertida"
✅ Nome intuitivo e atraente  
✅ Ícone personalizado com números e símbolos matemáticos  
✅ 30 fases (era 20)  
✅ Sistema de vidas (3 por fase)  
✅ Sistema de dicas (3 por fase)  
✅ Operação de divisão adicionada  
✅ Progressão de dificuldade refinada  
✅ Descrição de cada fase  
✅ Dialog de Game Over  
✅ Dicas contextuais por tipo de operação  
✅ Interface completamente reformulada  
✅ Logs detalhados para debug  

## 📱 Como Usar

### Desenvolvimento
```pwsh
# Build
./gradlew assembleDebug

# Instalar
adb install -r app\build\outputs\apk\debug\app-debug.apk

# Monitorar logs
adb logcat -s JogoInfantil:D Ads:D
```

### Publicação
1. Crie conta no Google Play Console
2. Cadastre app no AdMob e pegue IDs reais
3. Substitua os IDs de teste:
   - `AndroidManifest.xml`: App ID
   - `MainActivity.kt`: Banner e Intersticial
4. Gere keystore e assine o APK
5. Configure política de privacidade
6. Teste com crianças de 7-10 anos

## 🎓 Objetivos Educacionais

- Reforçar as 4 operações básicas
- Desenvolver raciocínio lógico
- Aumentar velocidade de cálculo mental
- Gamificar o aprendizado de matemática
- Criar experiência divertida e desafiadora

## 🏆 Sistema de Dificuldade

### Progressão
- Números começam pequenos (1-10)
- Aumentam gradualmente até 50
- Operações simples → complexas
- Acertos necessários aumentam (5 → 10)
- Tempo de raciocínio diminui naturalmente

### Balanceamento
- Vidas resetam a cada fase
- Dicas resetam a cada fase
- Game Over retorna à fase 1
- Completar todas as fases = reinício automático

## 🎨 Paleta de Cores

- **Primária**: Azul #2196F3 (confiança, aprendizado)
- **Secundária**: Amarelo #FFEB3B (alegria, energia)
- **Sucesso**: Verde #4CAF50 (acerto, progresso)
- **Erro**: Vermelho #F44336 (atenção)
- **Dica**: Laranja #FF9800 (ajuda, destaque)
- **Background**: Amarelo claro #FFF8E1 (suave, não cansa)

## 📊 Métricas Sugeridas

- Taxa de conclusão por fase
- Uso de dicas por fase
- Tempo médio por questão
- Taxa de erro por operação
- Retenção D1, D7, D30
- Receita por usuário (ARPU)

## 🔮 Futuras Melhorias

- [ ] Sistema de conquistas/badges
- [ ] Ranking local
- [ ] Modo multiplayer
- [ ] Temas personalizáveis
- [ ] Sons e música
- [ ] Animações nas transições
- [ ] Tutorial interativo
- [ ] Relatório de progresso para pais
- [ ] Modo treino (sem vidas)
- [ ] Desafios diários
