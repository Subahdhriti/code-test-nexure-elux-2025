package io.nexure.discount.database

import org.jetbrains.exposed.sql.Table

object Products : Table("products") {
    val id = varchar("id", 255)
    val name = varchar("name", 255)
    val basePrice = double("base_price")
    val country = varchar("country", 50)

    override val primaryKey = PrimaryKey(id)
}

object ProductDiscounts : Table("product_discounts") {
    val productId = varchar("product_id", 255).references(Products.id)
    val discountId = varchar("discount_id", 255)
    val percent = double("percent")

    override val primaryKey = PrimaryKey(productId, discountId)

    init {
        index(true, productId, discountId)
    }
}

object Countries : Table("country_vat") {
    val country = varchar("country", 50)
    val vat = double("vat")

    override val primaryKey = PrimaryKey(country)
}