package com.example.axiom.ui.screens.finances.Invoice.components

data class FinancialSummary(
    val itemSubTotal: Double,
    val totalTaxableAmount: Double,
    val cgstAmount: Double,
    val sgstAmount: Double,
    val igstAmount: Double,
    val totalTax: Double,
    val grandTotalBeforeRound: Double,
    val roundOff: Double,
    val grandTotal: Double
)

fun extractStateCodeFromGst(gst: String?): String? {
    if (gst.isNullOrBlank() || gst.length < 2) return null
    return gst.substring(0, 2)
}

fun resolveSupplyType(
    sellerStateCode: String?,
    customerStateCode: String?
): SupplyType = when {
    sellerStateCode.isNullOrBlank() || customerStateCode.isNullOrBlank() -> SupplyType.INTER_STATE
    sellerStateCode == customerStateCode -> SupplyType.INTRA_STATE
    else -> SupplyType.INTER_STATE
}


object BillingCalculator {
    /**
     * Calculates the exact financial breakdown for both Invoices and Purchases.
     * Applies a single Global GST rate to the final taxable total.
     */
    fun calculate(
        itemSubTotal: Double,
        discountAmount: Double,
        shippingCharges: Double,
        extraCharges: Double,
        globalGstRate: Double,
        supplyType: SupplyType,
        isRoundOffEnabled: Boolean
    ): FinancialSummary {
        val taxableAfterDiscount = itemSubTotal - discountAmount
        val totalTaxableAmount = taxableAfterDiscount + shippingCharges + extraCharges

        val isIntraState = supplyType == SupplyType.INTRA_STATE

        // Calculate GST globally
        val cgstAmount = if (isIntraState) (totalTaxableAmount * (globalGstRate / 2)) / 100 else 0.0
        val sgstAmount = if (isIntraState) (totalTaxableAmount * (globalGstRate / 2)) / 100 else 0.0
        val igstAmount = if (!isIntraState) (totalTaxableAmount * globalGstRate) / 100 else 0.0

        val totalTax = cgstAmount + sgstAmount + igstAmount
        val grandTotalBeforeRound = totalTaxableAmount + totalTax

        val roundOff = if (isRoundOffEnabled) {
            kotlin.math.round(grandTotalBeforeRound) - grandTotalBeforeRound
        } else {
            0.0
        }

        val grandTotal = grandTotalBeforeRound + roundOff

        return FinancialSummary(
            itemSubTotal = itemSubTotal,
            totalTaxableAmount = totalTaxableAmount,
            cgstAmount = cgstAmount,
            sgstAmount = sgstAmount,
            igstAmount = igstAmount,
            totalTax = totalTax,
            grandTotalBeforeRound = grandTotalBeforeRound,
            roundOff = roundOff,
            grandTotal = grandTotal
        )
    }
}