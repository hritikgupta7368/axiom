package com.example.axiom.ui.utils

import com.example.axiom.ui.screens.finances.Invoice.components.InvoiceWithItems
import com.example.axiom.ui.screens.finances.customer.components.ContactType
import com.example.axiom.ui.screens.finances.customer.components.PartyWithContacts
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun convertLongToTime(time: Long): String {
    val date = Date(time)
    val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return format.format(date)
}

object InvoiceHtmlGenerator {

    private const val MAX_ROWS = 16

    fun PartyWithContacts.getPhone(): String? {
        return contacts.firstOrNull { it.contactType == ContactType.PHONE }?.value
    }


    fun generateInvoiceHtml(
        invoice: InvoiceWithItems,
        logoUri: String
    ): String {


        val hasShipping = invoice.invoice.deliveryCharge > 0
        val itemLimit = if (hasShipping) MAX_ROWS - 1 else MAX_ROWS

        val itemRows = buildString {

            // 1. Render item rows
            for (i in 0 until itemLimit) {
                val item = invoice.items.getOrNull(i)

                if (item != null) {
                    append(
                        """
                        <tr class="item-row">
                            <td>${i + 1}</td>
                            <td>${item.productNameSnapshot}</td>
                            <td>${item.hsnSnapshot}</td>
                            <td>${item.quantity.toInt()}</td>
                            <td>${item.unitSnapshot}</td>
                            <td class="right">${"%.2f".format(item.sellingPriceAtTime)}</td>
                            <td class="right">${"%.2f".format(item.taxableAmount)}</td>
                        </tr>
                        """.trimIndent()
                    )
                } else {
                    append(
                        """
                <tr class="item-row">
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                </tr>
                """.trimIndent()
                    )
                }
            }

            // 2. Append shipping row if present
            if (hasShipping) {
                append(
                    """
            <tr class="item-row">
               <td>&nbsp;</td>
                <td>Shipping Charges</td>
                <td>&nbsp;</td>
                <td>&nbsp;</td>
                <td>&nbsp;</td>
                <td>&nbsp;</td>
                <td class="right">${"%.2f".format(invoice.invoice.deliveryCharge)}</td>
            </tr>
            """.trimIndent()
                )
            }
        }

        val vehicleNo = invoice.invoice.vehicleNumber.orEmpty()
        val eWayNo = invoice.invoice.eWayBillNumber.orEmpty()
        val shippedTo = invoice.invoice.shippedToAddress.orEmpty()
        val customerPhone = invoice.customer?.getPhone().orEmpty()
        val totalTax = invoice.invoice.cgstAmount +
                invoice.invoice.sgstAmount +
                invoice.invoice.igstAmount

        val reverseCharge = "N.A."
        val gstRate = invoice.invoice.globalGstRate

        val sellerAddress = invoice.seller?.party?.address.orEmpty()


        val sellerPhone = invoice.seller
            ?.contacts
            ?.firstOrNull { it.contactType == ContactType.PHONE }
            ?.value.orEmpty()

        val sellerEmail = invoice.seller
            ?.contacts
            ?.firstOrNull { it.contactType == ContactType.EMAIL }
            ?.value.orEmpty()

        val sellerWebsite = invoice.seller
            ?.contacts
            ?.firstOrNull { it.contactType == ContactType.WEBSITE }
            ?.value.orEmpty()





        return """
<!doctype html>
<html>
<head>
<meta charset="UTF-8" />
<style>
@font-face {
    font-family: 'Impact';
    src: url('file:///android_asset/impact.ttf') format('truetype');
    font-weight: normal;
    font-style: normal;
}
@page {
                  size: A4;
                  margin: 12mm 4mm;
              }

              body {
                  margin: 0;
                  background: #fff;
              }
              p,
              section {
                  margin: 0;
              }

               .page {
                    box-sizing: border-box;
                    font-family: Arial, sans-serif;
                    color: #000;
                    border: 0.4mm solid #000;
                }

              /* ===== TEXT AlIGN ===== */

              .text-center {
                  text-align: center;
              }

              /* ===== BORDER ===== */
              .border-t {
                  border-top: 0.3mm solid #000;
              }

              .border-r {
                  border-right: 0.3mm solid #000;
              }

              .border-b {
                  border-bottom: 0.3mm solid #000;
              }

              .border-l {
                  border-left: 0.3mm solid #000;
              }

              /* OVERRIDE colgroup borders inside bank rows */
              .bank-row td {
                  border-right: none !important;
              }

              /*padding*/

              /* ===== SECTIONS(Final) ===== */
              .section-1 {
                  display: grid;
                  grid-template-columns: 1fr auto 1fr;
                  align-items: start; /* FIX */
                  padding: 4px 4px 6px 4px; /* top tight, bottom relaxed */
                  position: relative;
              }
              .invoice-logo {
                  position: absolute;
                  top: 8mm;
                  right: 2mm;

                  height: 18mm; /* control size via height only */
                  width: auto;

                  object-fit: contain;
              }
              /* LEFT */
              .s1-left {
                  font-size: 14px; /* increased */
                  font-weight: 700; /* stronger */
                  text-align: left;
              }

              /* CENTER — ONLY place where Impact is used */
              .s1-center {
                  font-family: 'Impact', sans-serif;
                  font-size: 38px;
                  font-weight: normal; /* Impact is already heavy */
                  letter-spacing: 0.5px;
                  text-align: center;
                  line-height: 1;
              }

              /* RIGHT */
              .s1-right {
                  font-size: 12px;
                  font-weight: 600;
                  text-align: right;
              }

              .section-2 {
                  background-color: rgb(128, 99, 97);
                  color: white;
                  padding: 3px 2px;
                  font-weight: 900;
                  font-size: 15px;
              }

              .section-3 {
                  display: flex;
                  justify-content: space-between;
                  color: gray;
                  font-weight: 600;
                  padding: 4px 2px;
              }

              .s3-left {
                  width: 40%;
              }

              .s3-right {
                  width: 60%;
              }

              .section-3 p {
                  margin: 0;
                  padding: 4px 2px;
                  font-size: 13px;
              }

              .section-4 {
                  padding: 3px 0px;
                  font-size: 22.6px;
                  font-weight: 600;
                  font-family: "Times New Roman", Times, serif;
                  color: rgb(46, 41, 93);
              }

              .section-5 {
                  display: flex;
                  font-size: 12px;
              }

              .col {
                  box-sizing: border-box;
              }

              .col-1 {
                  width: 40%;
              }
              .col-2 {
                  width: 25%;
              }
              .col-3 {
                  width: 35%;
              }

              .col-2 .value {
                  margin-left: auto;
                  text-align: right;
              }

              .col-header {
                  font-weight: 600;
                  text-align: center;
                  border-bottom: 0.3mm solid #000;
                  padding: 4px 0;
              }

              .row {
                  display: flex;
                  align-items: flex-start;
                  padding: 2px 0;
                  justify-content: space-between;
              }

              .label {
                  width: 90px; /* fixed label width */
                  font-weight: 600;
                  flex-shrink: 0;
                  padding-left: 2px;
              }

              .value {
                  flex: 1;
                  word-break: break-word;
                  padding-right: 2px;
              }
              .shipped-value {
                  padding: 2px;
                  font-size: 12px;
                  line-height: 1.4;
                  word-break: break-word;
              }

              /* ===== TABLE BASE ===== */
              table {
                  width: 100%;
                  border-collapse: collapse;
              }

              td,
              th {
                  font-size: 12.5px;
                  vertical-align: top;
              }

              /* ===== GENERIC ROLES ===== */
              table td.label {
                  text-align: right;
                  font-weight: 600;
                  padding-right: 4px;
              }

              table td.value {
                  text-align: right;
                  font-weight: 700;
                  padding-right: 4px;
              }

              table td.center {
                  text-align: center;
              }

              table td.left {
                  text-align: left;
              }
              table td.right {
                  text-align: right;
              }

              /* ===== SECTION HEADERS ===== */
              table td.section-title {
                  font-weight: 700;
                  text-align: center;
              }

              /* ===== TOTALS ===== */
              .total-row .label {
                  text-align: left;
              }

              .total-row .value {
                  text-align: right;
                  font-weight: 700;
              }

              /* ===== BANK / TERMS ===== */
              .bank-row td {
                  text-align: left;
              }

              /* ===== SIGNATURE ===== */
              .authorised {
                  text-align: center;
                  vertical-align: bottom;

                  font-weight: 600;
              }

              /* REMOVE right border ONLY inside bank rows */
              .bank-row td.border-r {
                  border-right: none;
              }
              /* ===== ITEM ROW COLUMN ALIGNMENT ===== */
              .item-row td:nth-child(1) {
                  /* Sr No */
                  text-align: center;
                  padding-right: 2px;
              }

              .item-row td:nth-child(2) {
                  /* Particulars */
                  text-align: left;
              }

              .item-row td:nth-child(3) {
                  /* HSN */
                  text-align: center;
              }

              .item-row td:nth-child(4) {
                  /* Qty */
                  text-align: right;
              }

              .item-row td:nth-child(5) {
                  /* Unit */
                  text-align: center;
              }

              .item-row td:nth-child(6) {
                  /* Rate */
                  text-align: center;
              }

              .item-row td:nth-child(7) {
                  /* Amount */
                  text-align: right;
                  padding-right: 2px;
              }
              /* Amount column – totals */
              .totalAmount,
              .total-row .value {
                  text-align: right;
                  font-weight: 600;
              }
          </style>
      </head>

<body>
    <div class="page">
        <!-- Header -->
        <section>
            <!-- section-1 -->
            <div class="section-1 border-b">
                <img src="file:///android_asset/logo.png" class="invoice-logo" />
                <div class="s1-left">GST : ${invoice.seller?.party?.gstNumber ?: ""}</div>
                <div class="s1-center">${invoice.seller?.party?.businessName ?: ""}</div>
                <div class="s1-right">Original for Recipient</div>
            </div>
            
            <!--section-2-->
            <p class="section-2">Manufacturer and Supply of Industrial Goods</p>
            <!--section-3-->
                  <div class="section-3 border-b">
                      <div class="s3-left">
                           ${
            (invoice.seller?.party?.billingAddress ?: "")
                .replace("\r\n", "\n")
                .split("\n")
                .filter { it.isNotBlank() }
                .joinToString("") { "<p>${it.trim()}</p>" }
        }
                      </div>

                      <div class="s3-right">
                        <p>Mob : ${sellerPhone}</p>
                        <p>Web : ${sellerWebsite}</p>
                        <p>Email : ${sellerEmail}</p>
                      </div>
                  </div>

                  <!--section-4-->
                  <p class="text-center border-b section-4">TAX INVOICE</p>

                  <!--section-5-->
                  <div class="section-5 border-b">
                      <!-- COLUMN 1 -->
                      <div class="col col-1 border-r">
                          <div class="col-header">Customer Detail</div>

                          <div class="row">
                              <span class="label">M/S</span>
                              <span class="value">${invoice.customer?.party?.businessName ?: ""}</span>
                          </div>

                          <div class="row">
                              <span class="label">Address</span>
                              <span class="value">${invoice.customer?.party?.billingAddress ?: ""}</span>
                          </div>

                          <div class="row">
                              <span class="label">Phone</span>
                              <span class="value">${customerPhone}</span>
                          </div>

                          <div class="row">
                              <span class="label">GSTIN</span>
                              <span class="value">${invoice.customer?.party?.gstNumber ?: ""}</span>
                          </div>
                      </div>

                      <!-- COLUMN 2 -->
                      <div class="col col-2 border-r">
                          <div class="col-header">&nbsp;</div>
                          <div class="row">
                              <span class="label">Invoice no.</span>
                              <span class="value">${invoice.invoice.invoiceNumber}</span>
                          </div>

                          <div class="row">
                              <span class="label">Invoice Date</span>
                              <span class="value">${convertLongToTime(invoice.invoice.invoiceDate)}</span>
                          </div>

                          <div class="row">
                              <span class="label">E-Way Bill Date</span>
                              <span class="value">${invoice.invoice.eWayBillDate?.let { convertLongToTime(it) } ?: ""}</span>
                          </div>

                          <div class="row">
                              <span class="label">Vehicle No.</span>
                              <span class="value">$vehicleNo</span>
                          </div>

                          <div class="row">
                              <span class="label">E-Way No.</span>
                              <span class="value">$eWayNo</span>
                          </div>
                      </div>

                      <!-- COLUMN 3 -->
                      <div class="col col-3">
                          <div class="col-header">Shipped To :</div>
                          <div class="shipped-value">$shippedTo</div>
                      </div>
                  </div>

                  <div class="border-b">&nbsp;</div>
        </section>
        
        
        <!--Table-->
              <table>
                  <colgroup>
                      <col style="width: 5%" class="border-r" />
                      <!-- Sr. No. (Fixed pixel width) -->
                      <col style="width: 35%" class="border-r" />
                      <!-- Particulars (Percentage) -->
                      <col style="width: 15%" class="border-r" />
                      <!-- HSN / SAC -->
                      <col style="width: 10%" class="border-r" />
                      <!-- Qty -->
                      <col style="width: 10%" class="border-r" />
                      <!-- Unit -->
                      <col style="width: 12%" class="border-r" />
                      <!-- Rate -->
                      <col style="width: 13%" />
                      <!-- Amount -->
                  </colgroup>
                  <thead>
                      <tr class="border-b" style="background-color: rgb(238, 238, 238)">
                          <th>Sr.No.</th>
                          <th>Particulars</th>
                          <th>HSN / SAC</th>
                          <th>Qty</th>
                          <th>Unit</th>
                          <th>Rate(per unit)</th>
                          <th>Amount</th>
                      </tr>
                  </thead>
                  <tbody>
                  ${itemRows}
                  <tr class="border-b border-t">
                      <td colspan="3" style="font-weight: 600" class="right">Total</td>
                      <td>&nbsp;</td>
                      <td>&nbsp;</td>
                      <td>&nbsp;</td>
                      <td class="totalAmount">${"%.2f".format(invoice.invoice.totalTaxableAmount)}</td>
                  </tr>
                  <tr class="border-b">
                      <td colspan="7">&nbsp;</td>
                  </tr>
                  <tr class="border-b total-row">
                      <td colspan="3" class="section-title border-r">Total in Words</td>
                      <td colspan="3" class="label left">Taxable Amount</td>
                      <td class="value">${"%.2f".format(invoice.invoice.totalTaxableAmount)}</td>
                  </tr>
                  
                  <tr class="border-b total-row">
                      <td rowspan="2" colspan="3" class="center border-r">Rupees ${invoice.invoice.amountInWords} Only</td>
                      <td colspan="3" class="label left">${if (invoice.invoice.igstAmount > 0) "Add: IGST @ ${gstRate}%" else "&nbsp;"}</td>
                      <td class="value">${if (invoice.invoice.igstAmount > 0) "%.2f".format(invoice.invoice.igstAmount) else "&nbsp;"}</td>
                  </tr>
                   <!--CGST-->
                  <tr class="border-b total-row">
                      <td colspan="3" class="label left">${if (invoice.invoice.cgstAmount > 0) "Add: CGST @ ${"%.0f".format(gstRate / 2)}%" else "&nbsp;"}</td>
                      <td class="value">${if (invoice.invoice.cgstAmount > 0) "%.2f".format(invoice.invoice.cgstAmount) else "&nbsp;"}</td>

                  </tr>
                  
                  <!--Bank header + SGST-->
                  <tr class="border-b">
                      <td colspan="3" class="section-title border-r">Bank Details</td>
                      <td colspan="3" class="label left">${if (invoice.invoice.sgstAmount > 0) "Add: SGST @ ${"%.0f".format(gstRate / 2)}%" else "&nbsp;"}</td>
                      <td class="value">${if (invoice.invoice.sgstAmount > 0) "%.2f".format(invoice.invoice.sgstAmount) else "&nbsp;"}</td>
                  </tr>
                  
                   <!-- ROUND OFF (NEW) -->
                    <tr class="border-b">
                      <td colspan="3" class="border-r">&nbsp;</td>
                      <td colspan="3" class="label left">Total Tax</td>
                      <td class="value">${"%.2f".format(totalTax)}</td>
                  </tr>
                  
                  <!--Bank name + total tax-->
                  <tr class="bank-row">
                      <td colspan="2" class="left">Bank Name</td>
                      <td class="left border-r">${invoice.seller?.party?.bankName ?: ""}</td>
                      <td colspan="3" class="label border-b left" style="font-style: italic;" >Adjustment (Round Off)</td>
                     <td class="border-b value" style="font-style: italic;" >${if (invoice.invoice.roundOff != 0.0) "%.2f".format(invoice.invoice.roundOff) else "&nbsp;"}</td>
                  </tr>
                  <!--Branch + total after tax-->
                  <tr class="bank-row">
                      <td colspan="2" class="left">Branch Name</td>
                      <td class="left border-r">${invoice.seller?.party?.branchName ?: ""}</td>
                      <td colspan="3" class="label border-b left">Grand Total</td>
                      <td class="value border-b">${"%.2f".format(invoice.invoice.grandTotal)}</td>
                  </tr>
                  <!--Account + E&O.E-->
                  <tr class="bank-row">
                      <td colspan="2" class="left">Bank Account Number</td>
                      <td class="left border-r">${invoice.seller?.party?.accountNumber ?: ""}</td>
                      <td colspan="4" class="value border-b">(E &amp; O.E)</td>
                  </tr>
                  <!--IFSC + Reverse Charge-->
                  <tr class="border-b bank-row">
                      <td colspan="2" class="left">Bank Branch IFSC</td>
                      <td class="left border-r">${invoice.seller?.party?.ifscCode ?: ""}</td>
                      <td colspan="3" class="label border-r left">GST Payable on Reverse Charge</td>
                      <td class="value">${reverseCharge}</td>
                  </tr>
                  <!--Terms + Company-->
                  <tr class="border-b">
                      <td colspan="3" class="section-title border-r">Terms and Conditions</td>
                      <td colspan="4" class="section-title">For Megha Enterprises</td>
                  </tr>
                  <!--Terms list-->
                  <tr>
                      <td colspan="3" class="left border-r">1. Subject to Ghaziabad Jurisdiction.</td>
                      <td colspan="4"></td>
                  </tr>
                  <tr>
                      <td colspan="3" class="left border-r">2. Our responsibility ceases as soon as the goods leave our premises.</td>
                      <td colspan="4"></td>
                  </tr>
                  <tr>
                      <td colspan="3" class="left border-r">3. Goods once sold will not be taken back.</td>
                      <td colspan="4"></td>
                  </tr>
                  <tr>
                      <td colspan="3" class="left border-r">4. Delivery ex-premises</td>
                      <td colspan="4" class="authorised border-t">Authorised Signatory</td>
                  </tr>
                   </tbody>
              </table>
              
 </div>

</body>
</html>
""".trimIndent()
    }
}
