package com.example.df.backend.enums

enum class TipoProjetoLei {
    PL,
    PLC,
    PDL,
    PR,
    PELO,
    CPI,
    REQ,
    IND,
    MOC
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

// Para facilitar a catalogação (Filtros futuros no app)
enum class AreaTematica {
    SAUDE,
    EDUCACAO,
    SEGURANCA,
    TRANSPORTE,
    MEIO_AMBIENTE,
    INFRAESTRUTURA,
    CULTURA,
    DIREITOS_HUMANOS,
    ORCAMENTO,
    ESPORTE,
    OUTROS
}