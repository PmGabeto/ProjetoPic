package com.example.df

import com.example.df.backend.config.AppConfig // <--- Importe o AppConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties

@SpringBootApplication
@EnableConfigurationProperties(AppConfig::class) // <--- ADICIONE ESTA LINHA OBRIGATORIAMENTE
class SwapiApplication

fun main(args: Array<String>) {
    runApplication<SwapiApplication>(*args)
}