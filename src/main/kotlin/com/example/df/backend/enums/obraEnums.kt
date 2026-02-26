package com.example.df.backend.enums

enum class StatusObra {
    PLANEJAMENTO,   // Ainda no papel
    LICITACAO,      // Escolhendo empresa
    EM_ANDAMENTO,   // Trator na rua
    PARALISADA,     // O pesadelo do cidadão
    CONCLUIDA       // Obra entregue
}

enum class OrgaoExecutor {
    NOVACAP, // Urbanização
    DER,     // Estradas
    SODF,    // Secretaria de Obras
    CAESB,   // Água/Esgoto
    CEB,     // Energia (ou Neoenergia)
    DETRAN,  // Sinalização
    OUTROS
}