package com.example.df.backend.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "app")
class AppConfig(
    var google: GoogleConfig = GoogleConfig(),
    var storage: StorageConfig = StorageConfig(),
    var token: TokenConfig = TokenConfig(),
    var jwt: JwtConfig = JwtConfig()
)

data class GoogleConfig(
    var maps: MapsConfig = MapsConfig()
)

data class MapsConfig(
    var apiKey: String = ""
)

data class StorageConfig(
    var type: String = "LOCAL",
    var localPath: String = "./uploads"
)

data class TokenConfig(
    var duplicata: DuplicataConfig = DuplicataConfig(),
    var fotos: FotosConfig = FotosConfig()
)

data class DuplicataConfig(
    var raioMetros: Int = 50 // precisa sair de raio para outro método de conta, primeiro que 50metro é muito. talvez até seguir com <1m
)

data class FotosConfig(
    var maxQuantidade: Int = 5,
    var maxTamanhoMb: Long = 5
)

data class JwtConfig(
    var secret: String = "",
    var expirationMs: Long = 86400000
)
