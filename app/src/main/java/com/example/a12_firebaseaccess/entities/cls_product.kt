package com.example.a12_firebaseaccess.entities


class cls_product {
    var productID: String = ""
    var productName: String = ""
    var unitPrice: Double = 0.0
    var unitsInStock: String = ""
    var unitsOnOrder: String = ""
    var discontinued: String = ""

    constructor() {}

    constructor(
        productID: String,
        productName: String,
        unitPrice: Double,
        unitsInStock: String,
        unitsOnOrder: String,
        discontinued: String
    ) {
        this.productID = productID
        this.productName = productName
        this.unitPrice = unitPrice
        this.unitsInStock = unitsInStock
        this.unitsOnOrder = unitsOnOrder
        this.discontinued = discontinued
    }
}
