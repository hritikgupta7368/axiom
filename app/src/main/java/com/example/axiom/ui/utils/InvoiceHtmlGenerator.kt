package com.example.axiom.ui.utils

import com.example.axiom.data.finances.Invoice
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun convertLongToTime(time: Long): String {
    val date = Date(time)
    // Use dd/MM/yyyy (Note: MM must be uppercase for Month)
    val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return format.format(date)
}

object InvoiceHtmlGenerator {

    private const val MAX_ROWS = 16


    fun generateInvoiceHtml(
        invoice: Invoice,
        logoUri: String
    ): String {
        val hasShipping = invoice.shippingCharge != null && invoice.shippingCharge > 0
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
                    <td>${item.name}</td>
                    <td>${item.hsn}</td>
                    <td class="right">${item.quantity.toInt()}</td>
                    <td>${item.unit}</td>
                    <td class="right">${"%.2f".format(item.price)}</td>
                    <td class="right">${"%.2f".format(item.total)}</td>
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
                <td class="right">${"%.2f".format(invoice.shippingCharge)}</td>
            </tr>
            """.trimIndent()
                )
            }
        }

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
                <div class="s1-left">Gst : 09AFVPG6478A2Z1</div>
                <div class="s1-center">Megha Enterprises</div>
                <div class="s1-right">Original for Recipient</div>
            </div>
            
            <!--section-2-->
            <p class="section-2">Manufacturer and Supply of Industrial Goods</p>
            <!--section-3-->
                  <div class="section-3 border-b">
                      <div class="s3-left">
                          <p>33/12, Site 2 Loni Road</p>
                          <p>UPSIDC INDL AREA, Mohan Nagar</p>
                          <p>Ghaziabad (U.P) - 201007</p>
                      </div>

                      <div class="s3-right">
                          <p>Mob : 9212047198</p>
                          <p>Web :</p>
                          <p>Email : </p>
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
                              <span class="value">${invoice.customerDetails?.name}</span>
                          </div>

                          <div class="row">
                              <span class="label">Address</span>
                              <span class="value">${invoice.customerDetails?.address}</span>
                          </div>

                          <div class="row">
                              <span class="label">Phone</span>
                              <span class="value"></span>
                          </div>

                          <div class="row">
                              <span class="label">GSTIN</span>
                              <span class="value">${invoice.customerDetails?.gstin}</span>
                          </div>
                      </div>

                      <!-- COLUMN 2 -->
                      <div class="col col-2 border-r">
                          <div class="col-header">&nbsp;</div>
                          <div class="row">
                              <span class="label">Invoice no.</span>
                              <span class="value">${invoice.invoiceNo}</span>
                          </div>

                          <div class="row">
                              <span class="label">Invoice Date</span>
                              <span class="value">${convertLongToTime(invoice.date.toLong())}</span>
                          </div>

                          <div class="row">
                              <span class="label">Purchase No.</span>
                              <span class="value"></span>
                          </div>

                          <div class="row">
                              <span class="label">Vehicle No.</span>
                              <span class="value">${invoice.vehicleNumber}</span>
                          </div>

                          <div class="row">
                              <span class="label">E-Way No.</span>
                              <span class="value"></span>
                          </div>
                      </div>

                      <!-- COLUMN 3 -->
                      <div class="col col-3">
                          <div class="col-header">Shipped To :</div>
                          <div class="shipped-value">${invoice.shippedTo}</div>
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
                      <td class="totalAmount">${invoice.totalBeforeTax}</td>
                  </tr>
                  <tr class="border-b">
                      <td colspan="7">&nbsp;</td>
                  </tr>
                  <tr class="border-b total-row">
                      <td colspan="3" class="section-title border-r">Total in Words</td>
                      <td colspan="3" class="label left">Taxable Amount</td>
                      <td class="value">${invoice.totalBeforeTax}</td>
                  </tr>
                  <tr class="border-b total-row">
                      <td rowspan="2" colspan="3" class="center border-r">Rupees ${invoice.amountInWords} Only</td>
                      <td colspan="3" class="label left">Add: IGST</td>
                      <td class="value">${invoice.gst.igstAmount}</td>
                  </tr>
                   <!--CGST-->
                  <tr class="border-b total-row">
                      <td colspan="3" class="label left">Add: CGST @ 9%</td>
                      <td class="value">${invoice.gst.cgstAmount}</td>
                  </tr>
                  <!--Bank header + SGST-->
                  <tr class="border-b">
                      <td colspan="3" class="section-title border-r">Bank Details</td>
                      <td colspan="3" class="label left">Add: SGST @ 9%</td>
                      <td class="value">${invoice.gst.cgstAmount}</td>
                  </tr>
                  <!--Bank name + total tax-->
                  <tr class="bank-row">
                      <td colspan="2" class="left">Bank Name</td>
                      <td class="left border-r">Bank of India</td>
                      <td colspan="3" class="label border-b left">Total Tax</td>
                      <td class="border-b value">${invoice.gst.totalTax}</td>
                  </tr>
                  <!--Branch + total after tax-->
                  <tr class="bank-row">
                      <td colspan="2" class="left">Branch Name</td>
                      <td class="left border-r">Sahibabad</td>
                      <td colspan="3" class="label border-b left">Total Amount After Tax</td>
                      <td class="value border-b">${invoice.totalAmount}</td>
                  </tr>
                  <!--Account + E&O.E-->
                  <tr class="bank-row">
                      <td colspan="2" class="left">Bank Account Number</td>
                      <td class="left border-r">714620110000095</td>
                      <td colspan="4" class="value border-b">(E &amp; O.E)</td>
                  </tr>
                  <!--IFSC + Reverse Charge-->
                  <tr class="border-b bank-row">
                      <td colspan="2" class="left">Bank Branch IFSC</td>
                      <td class="left border-r">BKID0007146</td>
                      <td colspan="3" class="label border-r left">GST Payable on Reverse Charge</td>
                      <td class="value">N.A.</td>
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
