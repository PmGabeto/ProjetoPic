package com.example.df.backend.enums

enum class TipoProjetoLei(
    val sigla: String,
    val nome: String,
    val descricaoPedagogica: String
) {
    PL(
        "PL",
        "Projeto de Lei",
        "Destina-se a criar leis ordinárias que exigem a sanção do Governador."
    ),
    PLC(
        "PLC",
        "Projeto de Lei Complementar",
        "Projetos que regulamentam matérias específicas da Lei Orgânica do DF."
    ),
    PDL(
        "PDL",
        "Projeto de Decreto Legislativo",
        "Regula matérias de competência exclusiva da CLDF, como ratificação de convênios."
    ),
    PR(
        "PR",
        "Projeto de Resolução",
        "Regula assuntos de economia interna da Câmara Legislativa."
    ),
    PELO(
        "PELO",
        "Proposta de Emenda à Lei Orgânica",
        "Visa alterar a 'Constituição' do Distrito Federal."
    ),
    CPI(
        "CPI",
        "Comissão Parlamentar de Inquérito",
        "Investigação de fato determinado de relevante interesse público."
    ),
    REQ(
        "REQ",
        "Requerimento",
        "Pedido formal de informação ou de providência legislativa."
    ),
    IND(
        "IND",
        "Indicação",
        "Sugestão de atos administrativos ao Poder Executivo (GDF)."
    ),
    MOC(
        "MOC",
        "Moção",
        "Manifestação de protesto, repúdio, apoio ou congratulação."
    );

    companion object {
        /**
         * Método auxiliar para encontrar o Enum pela sigla (ex: "PL") que vem da API
         */
        fun fromSigla(sigla: String): TipoProjetoLei? {
            return entries.find { it.sigla == sigla }
        }
    }
}

enum class TipoVinculacao {
    AUTOR_PRINCIPAL,
    CO_AUTOR
}

enum class StatusPolitico {
    ATIVO,    // Em exercício
    INATIVO,  // Licenciado, Cassado ou Fim de Mandato
    SUPLENTE  // Assumiu temporariamente
}

enum class TipoAutor {
    PARLAMENTAR,
    UNIDADE_INTERNA, // Comissões e gabinetes
    ORGAO_EXTERNO    // GDF, TCDF, etc
}