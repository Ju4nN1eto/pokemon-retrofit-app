package com.example.retrofitpokemonapp.model

import com.google.gson.annotations.SerializedName

data class Pokemon(
    @SerializedName("name")
    val name: String,
    @SerializedName("height")
    val height: Int,
    @SerializedName("weight")
    val weight: Int,
    @SerializedName("sprites")
    val sprites: Sprites
)

data class Sprites(
    @SerializedName("front_default")
    val frontDefault: String?
)

data class PokemonListResponse(
    @SerializedName("results")
    val results: List<PokemonBasic>
)

data class PokemonBasic(
    @SerializedName("name")
    val name: String,
    @SerializedName("url")
    val url: String
)