package io.nexure.discount.models

import kotlinx.serialization.Serializable

@Serializable
data class Discount(
    val discountId: String,
    val percent: Double
)

@Serializable
data class Product(
    val id: String,
    val name: String,
    val basePrice: Double,
    val country: String,
    val discounts: List<Discount> = emptyList()
)

@Serializable
data class ProductResponse(
    val id: String,
    val name: String,
    val basePrice: Double,
    val country: String,
    val vat: Double,
    val discounts: List<Discount>,
    val finalPrice: Double
)

@Serializable
data class ApplyDiscountRequest(
    val discountId: String,
    val percent: Double
)

@Serializable
data class ErrorResponse(
    val error: String
)

@Serializable
data class CountryVat(
    val country: String,
    val vat: Double
)