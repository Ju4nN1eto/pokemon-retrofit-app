package com.example.retrofitpokemonapp.api

import com.tuempresa.retrofitpokemonapp.data.api.PokeApiService
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import strikt.api.expectThat
import strikt.assertions.*

class PokeApiServiceTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: PokeApiService

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PokeApiService::class.java)
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `getPokemonDetails debe retornar un pokemon exitosamente`() = runBlocking {
        // Given - Preparar respuesta simulada
        val mockResponse = """
            {
                "name": "pikachu",
                "height": 4,
                "weight": 60,
                "sprites": {
                    "front_default": "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png"
                }
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setBody(mockResponse)
                .setResponseCode(200)
        )

        // When - Ejecutar la petición
        val response = apiService.getPokemonDetails("pikachu")

        // Then - Verificar con Strikt
        expectThat(response) {
            get { isSuccessful }.isTrue()
            get { body() }.isNotNull().and {
                get { name }.isEqualTo("pikachu")
                get { height }.isEqualTo(4)
                get { weight }.isEqualTo(60)
                get { sprites.frontDefault }.isNotNull()
            }
        }
    }

    @Test
    fun `getPokemonDetails debe manejar error 404`() = runBlocking {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody("{\"error\": \"Not Found\"}")
        )

        // When
        val response = apiService.getPokemonDetails("pokemoninexistente")

        // Then
        expectThat(response) {
            get { isSuccessful }.isFalse()
            get { code() }.isEqualTo(404)
        }
    }

    @Test
    fun `getPokemonList debe retornar lista de pokemon`() = runBlocking {
        // Given
        val mockResponse = """
            {
                "results": [
                    {"name": "bulbasaur", "url": "https://pokeapi.co/api/v2/pokemon/1/"},
                    {"name": "ivysaur", "url": "https://pokeapi.co/api/v2/pokemon/2/"}
                ]
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setBody(mockResponse)
                .setResponseCode(200)
        )

        // When
        val response = apiService.getPokemonList(limit = 2)

        // Then
        expectThat(response) {
            get { isSuccessful }.isTrue()
            get { body() }.isNotNull().and {
                get { results }.hasSize(2)
                get { results[0].name }.isEqualTo("bulbasaur")
                get { results[1].name }.isEqualTo("ivysaur")
            }
        }
    }

    @Test
    fun `verificar que la URL de la peticion es correcta`() = runBlocking {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setBody("{\"name\": \"test\", \"height\": 1, \"weight\": 1, \"sprites\": {}}")
                .setResponseCode(200)
        )

        // When
        apiService.getPokemonDetails("pikachu")
        val request = mockWebServer.takeRequest()

        // Then
        expectThat(request) {
            get { path }.isEqualTo("/pokemon/pikachu")
            get { method }.isEqualTo("GET")
        }
    }
}