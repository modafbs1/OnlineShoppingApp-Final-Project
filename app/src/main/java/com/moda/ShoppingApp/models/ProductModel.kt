package com.moda.ShoppingApp.models

data class ProductModel(
    var id: String = "",
    var name: String? = "",
    var description: String? = "",
    var price: String? = "",
    var imageUrl: String? = "",
    var rating: Float = 0f,
    var category: String? = "",
    var location: String? = "",
    var soldCount: Int? = 0

)

