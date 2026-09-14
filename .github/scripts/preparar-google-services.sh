#!/usr/bin/env bash
#
# Prepara o app/google-services.json antes do build no CI.
#
# Por que isto existe: o arquivo está no .gitignore (contém as chaves do Firebase e
# fica só na máquina de quem desenvolve), mas o plugin com.google.gms.google-services
# é aplicado sem condição no app/build.gradle.kts. Sem o arquivo, QUALQUER build no
# GitHub Actions morre com "File google-services.json is missing".
#
# Duas formas de funcionar:
#
#   1. Com o secret GOOGLE_SERVICES_JSON configurado no repositório — usa o arquivo de
#      verdade. É o que você quer se algum dia o CI precisar falar com o Firebase.
#
#   2. Sem o secret — escreve um arquivo de placeholder. O plugin fica satisfeito, o
#      app compila e os testes rodam. O Firebase simplesmente não funciona naquele
#      build, o que não faz diferença nenhuma para verificar compilação e interface.
#
# Como configurar o secret (opcional):
#   Settings → Secrets and variables → Actions → New repository secret
#   Nome: GOOGLE_SERVICES_JSON
#   Valor: o conteúdo inteiro do seu app/google-services.json

set -euo pipefail

destino="app/google-services.json"

if [ -n "${GOOGLE_SERVICES_JSON:-}" ]; then
    printf '%s' "$GOOGLE_SERVICES_JSON" > "$destino"
    echo "✅ $destino escrito a partir do secret do repositório."
    exit 0
fi

# O package_name precisa bater com o applicationId de app/build.gradle.kts,
# senão o plugin reclama que a configuração não corresponde ao app.
cat > "$destino" <<'JSON'
{
  "project_info": {
    "project_number": "000000000000",
    "project_id": "placeholder-ci",
    "storage_bucket": "placeholder-ci.appspot.com"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:000000000000:android:0000000000000000",
        "android_client_info": {
          "package_name": "com.joaop.matematicadivertida"
        }
      },
      "oauth_client": [],
      "api_key": [
        {
          "current_key": "placeholder-de-ci-nao-e-uma-chave-real"
        }
      ],
      "services": {
        "appinvite_service": {
          "other_platform_oauth_client": []
        }
      }
    }
  ],
  "configuration_version": "1"
}
JSON

echo "⚠️  Secret GOOGLE_SERVICES_JSON não configurado."
echo "    Escrevi um $destino de placeholder só para o build passar."
echo "    O Firebase não vai funcionar neste build — e não precisa."
