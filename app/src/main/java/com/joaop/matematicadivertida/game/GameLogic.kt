package com.joaop.matematicadivertida

import android.content.Context
import android.util.Log
import kotlin.random.Random

/**
 * Regras do jogo que não dependem da tela: montagem da fase, geração de questões,
 * dicas progressivas e reforço positivo.
 *
 * Estava tudo dentro de MainActivity.kt, que passava de 2.900 linhas. Nada aqui usa
 * Compose — é matemática pura e texto —, então vive melhor separado: dá para ler,
 * ajustar a dificuldade e testar sem abrir a interface.
 */

fun generateAdaptiveLevel(
    level: Int, 
    totalCorrect: Int, 
    totalWrong: Int,
    consecutiveCorrect: Int
): LevelConfig {
    // Taxa de acerto do jogador
    val totalAnswers = totalCorrect + totalWrong
    val accuracy = if (totalAnswers > 0) {
        totalCorrect.toFloat() / totalAnswers
    } else {
        0.5f
    }

    // Configuração base da fase (por nível)
    var cfg = levelConfig(level, totalCorrect)

    // Ajuste fino pela performance RECENTE
    // Se a criança está indo muito bem (>=80% e 5 acertos seguidos), aumenta um pouco o intervalo
    if (accuracy >= 0.8f && consecutiveCorrect >= 5) {
        cfg = cfg.copy(
            min = maxOf(0, cfg.min - 1),
            max = cfg.max + 3,
            description = cfg.description + " ⚡ (ficou um pouquinho mais difícil)"
        )
    }

    // Se está com dificuldade (<50% e já respondeu bastante), reduz o intervalo
    if (accuracy < 0.5f && totalAnswers >= 10) {
        cfg = cfg.copy(
            min = 0,
            max = maxOf(cfg.min + 5, (cfg.max * 0.7f).toInt()),
            targetCorrect = maxOf(3, cfg.targetCorrect - 1),
            description = "🌟 Fase de ajuda: vamos praticar devagar" 
        )
    }

    return cfg
}

fun parseQuestionFromText(text: String): Question? {
    // Tenta reconstruir uma questão do texto salvo (ex: "5 + 3 = ?")
    try {
        val parts = text.replace("=", "").replace("?", "").trim().split(Regex("[+\\-×÷]"))
        if (parts.size != 2) return null
        
        val a = parts[0].trim().toIntOrNull() ?: return null
        val b = parts[1].trim().toIntOrNull() ?: return null
        
        val op = when {
            text.contains("+") -> Op.ADD
            text.contains("-") -> Op.SUB
            text.contains("×") -> Op.MUL
            text.contains("÷") -> Op.DIV
            else -> return null
        }
        
        val correct = when (op) {
            Op.ADD -> a + b
            Op.SUB -> a - b
            Op.MUL -> a * b
            Op.DIV -> if (b != 0 && a % b == 0) a / b else return null
        }
        
        // Gerar opções incorretas
        val options = buildList {
            add(correct)
            var tries = 0
            while (size < 3 && tries < 20) {
                tries++
                val delta = Random.nextInt(1, maxOf(3, correct / 2 + 1))
                val sign = if (Random.nextBoolean()) 1 else -1
                val cand = (correct + sign * delta).coerceAtLeast(0)
                if (cand != correct && cand !in this) add(cand)
            }
        }.shuffled()
        
        return Question(text, correct, options, op)
    } catch (e: Exception) {
        return null
    }
}

fun levelConfig(level: Int, totalCorrect: Int): LevelConfig = when {
    // INÍCIO ABSOLUTO: sempre adição até 10, independente da fase
    totalCorrect < 10 -> LevelConfig(
        ops = listOf(Op.ADD),
        min = 0,
        max = 10,
        targetCorrect = 5,
        description = "Adição bem simples até 10"
    )

    // Depois de 10 acertos: adição até 20
    totalCorrect < 20 -> LevelConfig(
        ops = listOf(Op.ADD),
        min = 0,
        max = 20,
        targetCorrect = 6,
        description = "Adição até 20"
    )

    // 20–39 acertos: adição e subtração até 20
    totalCorrect < 40 -> LevelConfig(
        ops = listOf(Op.ADD, Op.SUB),
        min = 0,
        max = 20,
        targetCorrect = 6,
        description = "Somar e subtrair até 20"
    )

    // 40–59 acertos: adição e subtração até 50
    totalCorrect < 60 -> LevelConfig(
        ops = listOf(Op.ADD, Op.SUB),
        min = 0,
        max = 50,
        targetCorrect = 7,
        description = "Somar e subtrair até 50"
    )

    // 60–89 acertos: introduz multiplicação simples
    totalCorrect < 90 -> LevelConfig(
        ops = listOf(Op.ADD, Op.SUB, Op.MUL),
        min = 0,
        max = 10,
        targetCorrect = 7,
        description = "Adição, subtração e início da multiplicação"
    )

    // 90–119 acertos: tabuada e divisão exata simples
    totalCorrect < 120 -> LevelConfig(
        ops = listOf(Op.ADD, Op.SUB, Op.MUL, Op.DIV),
        min = 0,
        max = 10,
        targetCorrect = 8,
        description = "Quatro operações com números pequenos"
    )

    // 120+ acertos: modo avançado, sobe lentamente com o nível
    else -> {
        val phase = (level - 1).coerceAtLeast(0) / 5
        val minRange = 5 + phase * 5
        val maxRange = 20 + phase * 10
        val target = minOf(10 + phase, 15)
        LevelConfig(
            ops = listOf(Op.ADD, Op.SUB, Op.MUL, Op.DIV),
            min = minRange,
            max = maxRange,
            targetCorrect = target,
            description = "⭐ Desafio progressivo (fase ${level})"
        )
    }
}

fun generateQuestion(cfg: LevelConfig): Question {
    return try {
        // Proteção contra lista de operações vazia
        val ops = if (cfg.ops.isEmpty()) listOf(Op.ADD) else cfg.ops
        val minVal = maxOf(0, minOf(cfg.min, cfg.max))
        val maxVal = maxOf(minVal + 1, maxOf(cfg.min, cfg.max))
        
        val op = ops.random()
        val a = Random.nextInt(minVal, maxVal + 1)
        val b = Random.nextInt(minVal, maxVal + 1)

        val (text, correct) = when (op) {
            Op.ADD -> "$a + $b = ?" to (a + b)
            Op.SUB -> {
                val x = maxOf(a, b); val y = minOf(a, b)
                "$x - $y = ?" to (x - y)
            }
            Op.MUL -> "$a × $b = ?" to (a * b)
            Op.DIV -> {
                // Garantir divisão exata
                val divisor = Random.nextInt(2, 11)
                val result = Random.nextInt(minVal, maxVal + 1)
                val dividend = divisor * result
                "$dividend ÷ $divisor = ?" to result
            }
        }

        val options = buildList {
            add(correct)
            var tries = 0
            while (size < 3 && tries < 20) {
                tries++
                val delta = when (op) {
                    Op.MUL -> Random.nextInt(1, maxOf(7, correct / 2 + 1))
                    Op.DIV -> Random.nextInt(1, 5)
                    else -> Random.nextInt(1, maxOf(5, correct / 3 + 1))
                }
                val sign = if (Random.nextBoolean()) 1 else -1
                val cand = (correct + sign * delta).coerceAtLeast(0)
                if (cand != correct && cand !in this) add(cand)
            }
        }.shuffled()

        Question(text, correct, options, op)
    } catch (e: Exception) {
        Log.e("JogoInfantil", "Erro na geração de questão: ${e.message}")
        Question("2 + 2 = ?", 4, listOf(4, 3, 5), Op.ADD)
    }
}

fun getPositiveReinforcement(
    questionText: String, 
    correctAnswer: Int, 
    operation: Op,
    consecutive: Int,
    responseTime: Long
): String {
    // Extrair os números da questão
    val numbers = questionText.replace("=", "").replace("?", "").trim()
    val parts = numbers.split(Regex("[+\\-×÷]")).map { it.trim() }
    val a = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val b = parts.getOrNull(1)?.toIntOrNull() ?: 0
    
    // Mensagens base por operação
    val baseMessages = when (operation) {
        Op.ADD -> listOf(
            "Perfeito! $a + $b = $correctAnswer mesmo! 🎉",
            "Isso aí! Você somou direitinho!",
            "Muito bem! $correctAnswer está certo!",
            "Parabéns! Você é bom em somar!"
        )
        Op.SUB -> listOf(
            "Excelente! $a - $b = $correctAnswer! 👏",
            "Muito bem! Você subtraiu certinho!",
            "Perfeito! $correctAnswer é a resposta!",
            "Ótimo! Você manda bem em subtração!"
        )
        Op.MUL -> listOf(
            "Sensacional! $a × $b = $correctAnswer! ⭐",
            "Isso! Você multiplicou perfeitamente!",
            "Show! $correctAnswer está certinho!",
            "Parabéns! Você domina a multiplicação!"
        )
        Op.DIV -> listOf(
            "Incrível! $a ÷ $b = $correctAnswer! 🌟",
            "Muito bem! Você dividiu como um mestre!",
            "Perfeito! $correctAnswer é isso mesmo!",
            "Excelente! Você arrasa na divisão!"
        )
    }
    
    // Adicionar mensagem de streak ou velocidade
    val prefix = when {
        consecutive >= 10 -> "IMPARÁVEL! "
        consecutive >= 5 -> "EM CHAMA! 🔥 "
        responseTime < 3000 -> "QUE RÁPIDO! ⚡ "
        else -> ""
    }
    
    return prefix + baseMessages.random()
}

fun getProgressiveHint(question: Question, level: Int): String {
    // Extrai os números da pergunta
    val numbers = question.text.replace("=", "").replace("?", "").trim()
    val parts = numbers.split(Regex("[+\\-×÷]")).map { it.trim() }
    val a = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val b = parts.getOrNull(1)?.toIntOrNull() ?: 0
    
    return when (question.op) {
        Op.ADD -> {
            when (level) {
                1 -> // Dica conceitual
                    when {
                        a <= 5 && b <= 5 -> "Use seus dedos para contar!"
                        b <= 5 -> "Comece no $a e conte mais $b"
                        else -> "Que tal separar em partes menores?"
                    }
                2 -> // Estratégia específica
                    when {
                        a <= 5 && b <= 5 -> "Conte nos dedos: $a em uma mão e $b na outra."
                        a <= 10 -> "Comece em $a e conte: ${(a+1)}, ${(a+2)}..."
                        b == 10 -> "Somar 10 é fácil: coloque 1 na frente!"
                        else -> "Some primeiro $a + ${b/2}, depois some mais ${b - b/2}"
                    }
                3 -> // Passo a passo completo
                    when {
                        a <= 5 && b <= 5 -> "Passo 1: Levante $a dedos\nPasso 2: Levante mais $b dedos\nPasso 3: Conte todos: ${question.correct}!"
                        else -> "$a + $b = ?\nPasso 1: Comece em $a\nPasso 2: Some +1 cada vez, $b vezes\nResultado: ${question.correct}"
                    }
                else -> "Tente de novo!"
            }
        }
        Op.SUB -> {
            when (level) {
                1 -> // Dica conceitual
                    when {
                        b <= 5 -> "Conte para trás!"
                        else -> "Quanto falta para $b chegar em $a?"
                    }
                2 -> // Estratégia específica
                    when {
                        b <= 5 -> "Comece em $a e volte $b números."
                        a <= 20 -> "Pense: quanto falta para $b chegar em $a?"
                        b == 10 -> "Tirar 10: diminua 1 da esquerda!"
                        else -> "Tire um pouco de cada vez: primeiro ${b/2}, depois mais ${b - b/2}"
                    }
                3 -> // Passo a passo completo
                    "$a - $b = ?\nPasso 1: Tenho $a\nPasso 2: Tiro $b\nPasso 3: Sobram ${question.correct}!"
                else -> "Tente de novo!"
            }
        }
        Op.MUL -> {
            val smaller = minOf(a, b)
            val bigger = maxOf(a, b)
            when (level) {
                1 -> // Dica conceitual
                    when {
                        smaller == 2 -> "Multiplicar por 2 é dobrar!"
                        smaller <= 5 -> "Some o mesmo número várias vezes"
                        else -> "Use a tabuada!"
                    }
                2 -> // Estratégia específica
                    when {
                        smaller == 2 -> "$bigger × 2 = $bigger + $bigger"
                        smaller == 5 -> "Multiplique por 10 e divida por 2"
                        smaller == 10 -> "Coloque um zero no final!"
                        else -> "Some $bigger, $smaller vezes"
                    }
                3 -> // Passo a passo completo
                    when {
                        smaller <= 3 -> "$bigger × $smaller = $bigger + " + List(smaller - 1) { "$bigger" }.joinToString(" + ") + " = ${question.correct}"
                        else -> "Tabuada do $smaller:\n$bigger × $smaller = ${question.correct}"
                    }
                else -> "Tente de novo!"
            }
        }
        Op.DIV -> {
            when (level) {
                1 -> // Dica conceitual
                    when {
                        b == 2 -> "Dividir por 2 é achar a metade!"
                        b <= 5 -> "Quantos grupos de $b cabem em $a?"
                        else -> "Use a tabuada ao contrário!"
                    }
                2 -> // Estratégia específica
                    when {
                        b == 2 -> "Metade de $a é quanto?"
                        b == 10 -> "Tire o último zero de $a"
                        else -> "Pense: $b vezes o quê dá $a?"
                    }
                3 -> // Passo a passo completo
                    "$a ÷ $b = ?\nPasso 1: Quantos grupos de $b em $a?\nPasso 2: $b × ${question.correct} = $a\nResposta: ${question.correct}!"
                else -> "Tente de novo!"
            }
        }
    }
}

fun getSmartHint(question: Question): String {
    // Função legada - agora usa getProgressiveHint nível 3
    return getProgressiveHint(question, level = 3)
}

fun getHint(question: Question, config: LevelConfig): String {
    val op = config.ops.firstOrNull() ?: Op.ADD
    return when (question.op) {
        Op.ADD -> "Dica: Conte nos dedos ou some os números!"
        Op.SUB -> "Dica: Comece do número maior e conte para trás!"
        Op.MUL -> "Dica: Lembre da tabuada ou some várias vezes!"
        Op.DIV -> "Dica: Quantas vezes cabe? Pense na multiplicação!"
    }
}

fun shareText(context: Context, title: String, text: String) {
    try {
        val sendIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            putExtra(android.content.Intent.EXTRA_TEXT, "$text\n\nBaixe grátis o Matemática Divertida no Google Play!")
            type = "text/plain"
        }
        val shareIntent = android.content.Intent.createChooser(sendIntent, title)
        context.startActivity(shareIntent)
    } catch (e: Exception) {
        Log.e("JogoInfantil", "Erro ao compartilhar: ${e.message}")
    }
}
