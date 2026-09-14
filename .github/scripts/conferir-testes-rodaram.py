#!/usr/bin/env python3
"""
Falha se nenhum teste instrumentado tiver sido executado.

Por que isto existe: o Gradle considera `connectedAndroidTest` um sucesso mesmo quando
zero testes rodam. Foi o que aconteceu na execução 34831179999 — "Starting 0 tests",
"BUILD SUCCESSFUL", job verde, nada verificado. A causa foi a falta do
testInstrumentationRunner, mas o problema de fundo é outro: um resultado vazio não pode
se parecer com um resultado bom.

Este script transforma esse silêncio numa falha explícita.
"""

import glob
import sys
import xml.etree.ElementTree as ET

PADRAO = "app/build/outputs/androidTest-results/**/*.xml"

arquivos = glob.glob(PADRAO, recursive=True)
total = falhas = erros = pulados = 0

# Conta por classe, e nao por arquivo. Numa execucao com um emulador so, o AGP
# escreve um unico XML com TODAS as classes dentro e um atributo "name" que traz
# apenas uma delas -- reportar por arquivo diria "AchievementsTest: 35 testes",
# escondendo que as outras tres classes tambem rodaram.
por_classe: dict[str, int] = {}

for caminho in arquivos:
    try:
        raiz = ET.parse(caminho).getroot()
    except ET.ParseError:
        continue
    if raiz.tag != "testsuite":
        continue
    total += int(raiz.get("tests", 0))
    falhas += int(raiz.get("failures", 0))
    erros += int(raiz.get("errors", 0))
    pulados += int(raiz.get("skipped", 0))
    for caso in raiz.findall("testcase"):
        nome = caso.get("classname", "(sem classe)").rsplit(".", 1)[-1]
        por_classe[nome] = por_classe.get(nome, 0) + 1

for nome in sorted(por_classe):
    print(f"  {nome}: {por_classe[nome]} teste(s)")

print(
    f"\nTotal: {total} teste(s) em {len(por_classe)} classe(s) "
    f"— {falhas} falha(s), {erros} erro(s), {pulados} pulado(s)"
)

if total == 0:
    sys.exit(
        "\nERRO: nenhum teste instrumentado foi executado.\n"
        "Um job verde que não rodou teste nenhum não verifica nada. Confira o\n"
        "testInstrumentationRunner em app/build.gradle.kts e se o APK de teste foi\n"
        "instalado no emulador."
    )

print("\nOK: os testes rodaram de verdade.")
