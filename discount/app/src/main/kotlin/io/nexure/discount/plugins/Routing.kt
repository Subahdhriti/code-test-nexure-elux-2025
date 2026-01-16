package io.nexure.discount.plugins

import io.nexure.discount.models.ApplyDiscountRequest
import io.nexure.discount.service.ProductService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val productService = ProductService()

    routing {
        route("/products") {
            get {
                val country = call.request.queryParameters["country"]
                if (country.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Country parameter is required"))
                    return@get
                }

                try {
                    val products = productService.getProductsByCountry(country)
                    call.respond(HttpStatusCode.OK, products)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }

            put("/{id}/discount") {
                val productId = call.parameters["id"]
                if (productId.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Product ID is required"))
                    return@put
                }

                try {
                    val request = call.receive<ApplyDiscountRequest>()
                    val product = productService.applyDiscountToProduct(
                        productId = productId,
                        discountId = request.discountId,
                        percent = request.percent
                    )
                    call.respond(HttpStatusCode.OK, product)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal server error"))
                }
            }
        }
    }
}