package io.nexure.discount.repository

import io.github.oshai.kotlinlogging.KotlinLogging
import io.nexure.discount.database.Countries
import io.nexure.discount.database.DatabaseFactory.dbQuery
import io.nexure.discount.database.ProductDiscounts
import io.nexure.discount.database.Products
import io.nexure.discount.models.CountryVat
import io.nexure.discount.models.Discount
import io.nexure.discount.models.Product
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.postgresql.util.PSQLException

private val log = KotlinLogging.logger {}

class ProductRepository {

    suspend fun getCountryByName(country: String): CountryVat?  = dbQuery {
        Countries.select { Countries.country eq country }
            .map { row ->
                CountryVat(
                    country = row[Countries.country],
                    vat = row[Countries.vat]
                )
            }
            .singleOrNull()
    }

    suspend fun getAllProductsByCountry(country: String): List<Product> = dbQuery {
        val products = Products.select { Products.country eq country }.map { row ->
            val productId = row[Products.id]
            val discounts = ProductDiscounts
                .select { ProductDiscounts.productId eq productId }
                .map { discountRow ->
                    Discount(
                        discountId = discountRow[ProductDiscounts.discountId],
                        percent = discountRow[ProductDiscounts.percent]
                    )
                }

            Product(
                id = productId,
                name = row[Products.name],
                basePrice = row[Products.basePrice],
                country = row[Products.country],
                discounts = discounts
            )
        }
        products
    }

    suspend fun getProductById(id: String): Product? = dbQuery {
        Products.select { Products.id eq id }
            .map { row ->
                val productId = row[Products.id]
                val discounts = ProductDiscounts
                    .select { ProductDiscounts.productId eq productId }
                    .map { discountRow ->
                        Discount(
                            discountId = discountRow[ProductDiscounts.discountId],
                            percent = discountRow[ProductDiscounts.percent]
                        )
                    }

                Product(
                    id = productId,
                    name = row[Products.name],
                    basePrice = row[Products.basePrice],
                    country = row[Products.country],
                    discounts = discounts
                )
            }
            .singleOrNull()
    }

    suspend fun applyDiscount(productId: String, discount: Discount): Boolean = dbQuery {
        try {
            ProductDiscounts.insert {
                it[ProductDiscounts.productId] = productId
                it[discountId] = discount.discountId
                it[percent] = discount.percent
            }
            true
        } catch (e: ExposedSQLException) {
            // Check if it's a duplicate key violation (PostgreSQL error code 23505)
            val cause = e.cause
            if (cause is PSQLException && cause.sqlState == "23505") {
                // Discount already applied - idempotent behavior
                log.info { "Discount ${discount.discountId} already applied for product $productId" }
                false
            } else {
                throw e
            }
        }
    }

    suspend fun createProduct(product: Product): Product = dbQuery {
        Products.insert {
            it[id] = product.id
            it[name] = product.name
            it[basePrice] = product.basePrice
            it[country] = product.country
        }
        product
    }
}