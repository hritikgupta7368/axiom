package com.example.axiom.ui.utils

fun numberToWords(num: Double): String {
    // Round to handle cases like 99.99 for words, or convert to Long
    val amount = num.toLong()
    if (amount == 0L) return "Zero"

    val ones = arrayOf(
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
        "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
    )

    val tens = arrayOf(
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    )

    fun convertBelowThousand(n: Int): String {
        var str = ""
        var temp = n
        if (temp > 99) {
            str += ones[temp / 100] + " Hundred "
            temp %= 100
        }
        if (temp > 19) {
            str += tens[temp / 10] + " "
            temp %= 10
        }
        if (temp > 0) {
            str += ones[temp] + " "
        }
        return str.trim()
    }

    var remaining = amount
    var result = ""

    val crore = (remaining / 10_000_000).toInt()
    remaining %= 10_000_000

    val lakh = (remaining / 100_000).toInt()
    remaining %= 100_000

    val thousand = (remaining / 1_000).toInt()
    remaining %= 1_000

    val units = remaining.toInt()

    if (crore > 0) result += convertBelowThousand(crore) + " Crore "
    if (lakh > 0) result += convertBelowThousand(lakh) + " Lakh "
    if (thousand > 0) result += convertBelowThousand(thousand) + " Thousand "
    if (units > 0) result += convertBelowThousand(units)

    return result.trim()
}