package io.nexure.discount

import io.nexure.discount.models.ApplyDiscountRequest
import io.nexure.discount.models.Product
import io.nexure.discount.models.ProductResponse
import io.nexure.discount.repository.ProductRepository
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConcurrencyTest {

    @Test
    fun `test concurrent discount application - idempotency`() = testApplication {
        application {
            module()
        }

        // Setup: Create a test product
        val productRepo = ProductRepository()
        val testProduct = Product(
            id = "test-product-1",
            name = "Test Laptop",
            basePrice = 1000.0,
            country = "india"
        )

        runBlocking {
            productRepo.createProduct(testProduct)
        }

        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }

        // Apply the same discount concurrently 10 times
        val discountRequest = ApplyDiscountRequest(
            discountId = "test-discount",
            percent = 10.0
        )

        val results = runBlocking {
            (1..10).map {
                async(Dispatchers.IO) {
                    client.put("/products/test-product-1/discount") {
                        contentType(ContentType.Application.Json)
                        setBody(discountRequest)
                    }
                }
            }.awaitAll()
        }

        // All requests should succeed (200 OK)
        results.forEach { response ->
            assertEquals(HttpStatusCode.OK, response.status)
        }

        // Verify the discount was only applied once
        val finalProduct = client.get("/products?country=india").body<List<ProductResponse>>()
        val product = finalProduct.first { it.id == "test-product-1" }

        assertEquals(1, product.discounts.size, "Discount should only be applied once despite concurrent requests")
        assertEquals("test-discount", product.discounts[0].discountId)
        assertEquals(10.0, product.discounts[0].percent)

        // Verify final price calculation: 1000 * (1 - 0.10) * (1 + 0.25) = 1125.0
        assertEquals(1125.0, product.finalPrice, 0.01)
    }

    @Test
    fun `test multiple different discounts applied concurrently`() = testApplication {
        application {
            module()
        }

        // Setup: Create a test product
        val productRepo = ProductRepository()
        val testProduct = Product(
            id = "test-product-2",
            name = "Test Phone",
            basePrice = 800.0,
            country = "GERMANY"
        )

        runBlocking {
            productRepo.createProduct(testProduct)
        }

        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }

        // Apply 3 different discounts concurrently
        val discounts = listOf(
            ApplyDiscountRequest("DISCOUNT1", 5.0),
            ApplyDiscountRequest("DISCOUNT2", 10.0),
            ApplyDiscountRequest("DISCOUNT3", 15.0)
        )

        val results = runBlocking {
            discounts.flatMap { discount ->
                (1..5).map {
                    async(Dispatchers.IO) {
                        client.put("/products/test-product-2/discount") {
                            contentType(ContentType.Application.Json)
                            setBody(discount)
                        }
                    }
                }
            }.awaitAll()
        }

        // All requests should succeed
        results.forEach { response ->
            assertEquals(HttpStatusCode.OK, response.status)
        }

        // Verify exactly 3 discounts were applied
        val finalProduct = client.get("/products?country=germany").body<List<ProductResponse>>()
        val product = finalProduct.first { it.id == "test-product-2" }

        assertEquals(3, product.discounts.size, "Exactly 3 different discounts should be applied")

        val discountIds = product.discounts.map { it.discountId }.toSet()
        assertTrue(discountIds.containsAll(setOf("DISCOUNT1", "DISCOUNT2", "DISCOUNT3")))

        // Verify final price: 800 * (1 - 0.30) * (1 + 0.19) = 666.40
        val expectedPrice = 800.0 * (1 - 0.30) * (1 + 0.19)
        assertEquals(expectedPrice, product.finalPrice, 0.01)
    }
}