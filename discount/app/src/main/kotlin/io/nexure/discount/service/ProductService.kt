package io.nexure.discount.service

import io.github.oshai.kotlinlogging.KotlinLogging
import io.nexure.discount.models.*
import io.nexure.discount.repository.ProductRepository

private val log = KotlinLogging.logger {}


class ProductService(private val repository: ProductRepository = ProductRepository()) {

    suspend fun getProductsByCountry(country: String): List<ProductResponse> {
        val countryInfo = repository.getCountryByName(country)
            ?: run {
                log.warn { "Invalid country requested: $country" }
                throw IllegalArgumentException("Invalid country: $country.")
            }

        val products = repository.getAllProductsByCountry(countryInfo.country)
        return products.map { product ->
            ProductResponse(
                id = product.id,
                name = product.name,
                basePrice = product.basePrice,
                country = product.country,
                vat = countryInfo.vat,
                discounts = product.discounts,
                finalPrice = calculateFinalPrice(product.basePrice, product.discounts, countryInfo)
            )
        }
    }

    suspend fun applyDiscountToProduct(productId: String, discountId: String, percent: Double): ProductResponse {
        if (percent <= 0 || percent > 100) {
            log.warn { "Invalid discount percent: $percent" }
            throw IllegalArgumentException("Discount percent must be between 0 (exclusive) and 100")
        }

        val product = repository.getProductById(productId)
            ?: run {
                log.warn { "Product not found: $productId" }
                throw IllegalArgumentException("Product not found: $productId")
            }

        val countryInfo = repository.getCountryByName(product.country)
            ?: run {
                log.warn { "Invalid country for product ${product.id}: ${product.country}"}
                throw IllegalArgumentException("Invalid country: ${product.country}.")
            }

        val discount = Discount(discountId = discountId, percent = percent)

        // This is idempotent - returns false if discount already exists
        repository.applyDiscount(productId, discount)

        // Fetch updated product
        val updatedProduct = repository.getProductById(productId)!!

        return ProductResponse(
            id = updatedProduct.id,
            name = updatedProduct.name,
            basePrice = updatedProduct.basePrice,
            country = updatedProduct.country,
            vat = countryInfo.vat,
            discounts = updatedProduct.discounts,
            finalPrice = calculateFinalPrice(updatedProduct.basePrice, updatedProduct.discounts, countryInfo)
        )
    }

    private fun calculateFinalPrice(basePrice: Double, discounts: List<Discount>, country: CountryVat): Double {
        val totalDiscountPercent = discounts.sumOf { it.percent }
        val priceAfterDiscount = basePrice * (1 - totalDiscountPercent / 100)
        val finalPrice = priceAfterDiscount * (1 + country.vat/100)
        return finalPrice
    }
}