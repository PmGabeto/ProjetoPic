import requests
import json

BASE_URL = "https://ple.cl.df.gov.br/pleservico/api/public"


# ==============================
# FUNÇÕES DE API
# ==============================

def post_filter(body, page=0, size=1):
    url = f"{BASE_URL}/proposicao/filter?page={page}&size={size}"
    response = requests.post(url, json=body)
    response.raise_for_status()
    return response.json()

def post_endpoint(path, body= None):
    if body is None:
       body = {}

    url = f"{BASE_URL}{path}"
    response = requests.post(url, json=body)
    if response.status_code == 200:
        return response.json()
    elif response.status_code == 404:
        print(f"[INFO] Endpoint não encontrado {path}")
        return None
    else:
        print(f"[ERRO] {response.status_code} - {response.text}")
        return None

def get_endpoint(path):
    url = f"{BASE_URL}{path}"
    response = requests.get(url)

    if response.status_code == 200:
        return response.json()

    if response.status_code == 404:
        print(f"[INFO] Endpoint não encontrado: {path}")
        return None

    print(f"[ERRO] {response.status_code} - {response.text}")
    return None



# ==============================
# FUNÇÕES AUXILIARES
# ==============================

def gerar_link_publico(sigla):
    if not sigla:
        return None
    sigla = sigla.replace(" ", "_").replace("/", "_")
    return f"https://www.cl.df.gov.br/proposicao/-/documentos/{sigla}"




# ==============================
# EXECUÇÃO
# ==============================

# Buscar 1 Projeto de Lei de 2025
body = {
    "tipoProposicao": "Projeto de Lei",
    "ano": "2024",
    "ementa": True
}

resultado = post_filter(body)

proposicoes = resultado.get("content", [])

if not proposicoes:
    print("Nenhum PL de 2025 encontrado.")
    exit()

pl = proposicoes[0]
pid = pl.get("id")

print(f"\n===== PROPOSIÇÃO ID {pid} =====\n")

# Buscar dados complementares
detalhe = get_endpoint(f"/proposicao/{pid}/detalhe")
autores = get_endpoint(f"/proposicao/autores/{pid}")
tramitacoes = post_endpoint(f"/proposicao/{pid}/documento/ativas/order-by-pageable")
historico = post_endpoint(f"/historico-proposicao/{pid}?sort=base.dataHistorico,DESC")
documentos = get_endpoint(f"/proposicao/{pid}/documento/ativas/tipo-documento-options-vinculado")
status = get_endpoint(f"/tipo-documento/status")

# Consolidar dados
dados_completos = {
    "identificacao": {
        "siglaNumeroAno": pl.get("siglaNumeroAno"),
        "tipo": pl.get("tipoProposicao"),
        "descricao": pl.get("descricaoProposicao"),
        "id": pid
    },
    "ementa": pl.get("ementa"),
    "tema": pl.get("temaNome"),
    "regiaoAdministrativa": pl.get("regiaoAdministrativaNome"),
    "data": pl.get("dataLeitura"),
    "autoria_resumida": pl.get("autoria"),
    "autores_detalhados": autores,
    "tramitacoes": tramitacoes,
    "historico" : historico,
    "etapa_atual_resumo": pl.get("etapa"),
    "situacaoProposicao": pl.get("situacaoProposicao"),
    "documentos": documentos,
    "status" : status,
    "link_publico": gerar_link_publico(pl.get("siglaNumeroAno"))
}

# Exibir resultado formatado
print(json.dumps(dados_completos, indent=2, ensure_ascii=False))
