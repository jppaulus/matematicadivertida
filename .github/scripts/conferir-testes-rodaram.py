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
    print(f"  {raiz.get('name')}: {raiz.get('tests')} teste(s)")

print(
    f"\nTotal: {total} teste(s) em {len(arquivos)} arquivo(s) de resultado "
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
