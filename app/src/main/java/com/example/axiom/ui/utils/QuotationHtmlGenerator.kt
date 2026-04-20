package com.example.axiom.ui.utils

import com.example.axiom.ui.screens.finances.quotation.components.FullQuotation


object QuotationHtmlGenerator {
    fun generateQuotationHtml(quotation: FullQuotation): String {

        // Helper to generate the items rows dynamically
        val itemsHtml = quotation.items.joinToString("\n") { item ->
            """
            <tr>
            <td class="col-desc">
                ${item.productNameSnapshot}
                ${if (item.hsnSnapshot.isNotBlank()) "<span class=\"item-subtext\">${item.hsnSnapshot.replace("\n", "<br>")}</span>" else ""}
            </td>
            <td class="col-qty">${item.quantity}</td>
            <td class="col-rate">${item.quotationPriceAtTime}</td>
            <td class="col-disc">0 %</td>
            <td class="col-amount">${item.taxableAmount}</td>
            </tr>
            """.trimIndent()
        }

        // Helper to generate terms dynamically
//        val termsHtml = quotation.terms.joinToString("\n") { term ->
//            "<li>$term</li>"
//        }

        val termsHtml =
            "<li>Please pay within 15 days from the date of invoice, overdue<br>interest @ 14% will be charged on delayed payments.</li>\n" +
                    "<li>Please quote invoice number when remitting funds.</li>"



        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Quotation</title>
            <style>
            @import url('https://fonts.googleapis.com/css2?family=Inter:ital,wght@0,300;0,400;0,600;0,700;0,900;1,900&display=swap');

            :root {
                --primary-blue: #0ba1d6;
                --light-blue: #eaf8fc;
                --text-dark: #222222;
                --text-gray: #666666;
                --border-color: #555555;
            }

            * { margin: 0; padding: 0; box-sizing: border-box; font-family: 'Inter', sans-serif; }
            body { background-color: #f0f0f0; color: var(--text-dark); font-size: 11px; line-height: 1.4; }

            .page { width: 210mm; min-height: 297mm; padding: 15mm 15mm 10mm 15mm; margin: 10mm auto; background: white; box-shadow: 0 0 10px rgba(0,0,0,0.1); position: relative; -webkit-print-color-adjust: exact; print-color-adjust: exact; }
            header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 25px; }
            .logo-container { display: flex; align-items: center; gap: 10px; }
            .logo-box { background-color: var(--primary-blue); width: 50px; height: 40px; position: relative; clip-path: polygon(0 0, 100% 0, 85% 100%, 0% 100%); }
            .logo-box::before { content: 'F'; color: white; font-size: 24px; font-weight: 700; position: absolute; left: 10px; top: 5px; }
            .logo-box::after { content: ''; position: absolute; background: white; height: 3px; width: 15px; top: 15px; left: 22px; box-shadow: 0 8px 0 white; }
            .company-name-logo { font-size: 22px; font-weight: 900; font-style: italic; line-height: 1; text-transform: uppercase; }
            .doc-title { font-size: 32px; font-weight: 300; color: var(--text-dark); }
            .info-section { display: flex; justify-content: space-between; margin-bottom: 30px; }
            .info-col { width: 30%; }
            .info-col h3 { font-size: 13px; font-weight: 700; margin-bottom: 8px; }
            .info-col p { margin-bottom: 5px; color: var(--text-gray); }
            .info-col.company-info p, .info-col.client-info p { color: var(--text-dark); }
            .meta-table { width: 100%; border-collapse: collapse; }
            .meta-table td { padding: 3px 0; font-size: 11px; }
            .meta-table td:nth-child(1) { font-weight: 700; width: 50%; }
            .meta-table td:nth-child(2) { text-align: right; color: var(--text-dark); }
            .supply-info { margin-top: 15px; }
            .supply-info table { width: 100%; }
            .supply-info td { padding: 2px 0; }
            .supply-info td:nth-child(1) { font-weight: 700;}
            .supply-info td:nth-child(2) { text-align: right; }
            .items-table { width: 100%; border-collapse: collapse; margin-bottom: 25px; border: 1px solid var(--border-color); }
            .items-table th, .items-table td { border: 1px solid var(--border-color); padding: 10px; }
            .items-table th { background-color: var(--light-blue); font-size: 11px; font-weight: 700; text-align: center; }
            .items-table th.col-desc { text-align: left; width: 45%; }
            .items-table th.col-amount { font-size: 10px; line-height: 1.1; }
            .items-table td.col-desc { font-weight: 500; }
            .items-table td.col-qty, .items-table td.col-rate, .items-table td.col-disc, .items-table td.col-amount { text-align: center; }
            .item-subtext { display: block; font-size: 9.5px; color: var(--text-gray); font-weight: 400; margin-top: 5px; }
            .lower-section { display: flex; justify-content: space-between; }
            .left-content { width: 55%; }
            .right-content { width: 38%; }
            .content-block { margin-bottom: 20px; }
            .content-block h4 { font-size: 12px; font-weight: 700; margin-bottom: 8px; }
            .terms-list { padding-left: 15px; color: var(--text-gray); }
            .terms-list li { margin-bottom: 4px; padding-left: 5px; }
            .notes-text { color: var(--text-gray); }
            .attachments-list { list-style: none; padding: 0; }
            .attachments-list li { margin-bottom: 5px; font-weight: 600; color: var(--text-dark); }
            .attachments-list li span { text-decoration: underline; }
            .summary-table-wrap { border: 1px solid var(--border-color); margin-bottom: 40px; }
            .summary-table { width: 100%; border-collapse: collapse; }
            .summary-table td { padding: 8px 10px; border-bottom: 1px solid var(--border-color); }
            .summary-table tr:last-child td { border-bottom: none; }
            .summary-table td:nth-child(1) { font-weight: 600; }
            .summary-table td:nth-child(2) { text-align: right; font-weight: 600; border-left: 1px solid var(--border-color); }
            .row-total td { background-color: var(--primary-blue); color: white; font-weight: 700; }
            .words-total { padding: 8px 10px; font-size: 10px; }
            .words-total strong { display: block; margin-bottom: 3px; }
            .signature-box { text-align: center; margin-top: 30px; }
            .signature-image { width: 150px; height: 60px; margin: 0 auto 5px auto; font-family: 'Brush Script MT', cursive; font-size: 36px; color: #2b1f59; display: flex; align-items: center; justify-content: center; transform: rotate(-5deg); }
            .signature-text { font-size: 11px; color: var(--text-dark); }
            .footer-bar { position: absolute; bottom: 15mm; left: 15mm; right: 15mm; border: 1px solid var(--text-dark); padding: 10px; text-align: center; font-size: 11px; }
            
            @media print {
                body { background-color: white; }
                .page { margin: 0; padding: 15mm 15mm 10mm 15mm; box-shadow: none; width: 100%; }
                @page { size: A4; margin: 0; }
            }
            </style>
            </head>
            <body>

            <div class="page">

            <header>
            <div class="logo-container">
            <div class="logo-box"></div>
            <div class="company-name-logo">
            ${quotation.seller?.party?.businessName?.replace("\n", "<br>")}
            </div>
            </div>
            <div class="doc-title">
            Quotation
            </div>
            </header>

            <div class="info-section">
            <div class="info-col company-info">
            <h3>Quotation by</h3>
            <p>${quotation.seller?.party?.businessName}</p>
            <p>${quotation.seller?.party?.billingAddress?.replace("\n", "<br>")}</p>
            </div>

            <div class="info-col client-info">
            <h3>Quotation to</h3>
            <p>${quotation.customer?.party?.businessName}</p>
            <p>${quotation.customer?.party?.billingAddress?.replace("\n", "<br>")}</p>
            </div>

            <div class="info-col meta-info">
            <table class="meta-table">
            <tr>
            <td>Quotation No:</td>
            <td>${quotation.quotation.quotationNumber}</td>
            </tr>
            <tr>
            <td>Date:</td>
            <td>${quotation.quotation.quotationDate}</td>
            </tr>
            <tr>
            <td>Valid Until:</td>
            <td>${quotation.quotation.validUntilDate}</td>
            </tr>
            </table>
            <div class="supply-info">
            <table class="meta-table">
            <tr>
            <td>Country of supply:</td>
            <td>India</td>
            </tr>
            <tr>
            <td>Place of supply:</td>
            <td></td>
            </tr>
            </table>
            </div>
            </div>
            </div>

            <table class="items-table">
            <thead>
            <tr>
            <th class="col-desc">Item</th>
            <th class="col-qty">Quantity</th>
            <th class="col-rate">Rate</th>
            <th class="col-disc">Discount</th>
            <th class="col-amount">Amount<br><span style="font-weight:400; font-size:9px;">(After Discount)</span></th>
            </tr>
            </thead>
            <tbody>
            $itemsHtml
            </tbody>
            </table>

            <div class="lower-section">

            <div class="left-content">
            <div class="content-block">
            <h4>Terms and Conditions</h4>
            <ol class="terms-list">
            $termsHtml
            </ol>
            </div>

            <div class="content-block">
            <h4>Additional Notes</h4>
            <p class="notes-text">

It is a long established fact that a reader will be distracted by the<br>
readable content of a page when looking at its layout. The point of<br>
using Lorem Ipsum is that it has a more-or-less normal distribution of<br>
letters, as opposed to using 'Content here, content here.
            </p>
            </div>

          

            <div class="right-content">
            <div class="summary-table-wrap">
            <table class="summary-table">
            <tr>
            <td>Sub Total</td>
            <td>${quotation.quotation.itemSubTotal}</td>
            </tr>
            <tr>
            <td>Discount</td>
            <td>${quotation.quotation.globalDiscountAmount}</td>
            </tr>
            <tr class="row-total">
            <td>Total Amount</td>
            <td>${quotation.quotation.grandTotal}</td>
            </tr>
            </table>
            <div class="words-total">
            <strong>Quotation Total In Words:</strong>
           Nineteen Thousand Rupees Only
            </div>
            </div>

            <div class="signature-box">
            <div class="signature-image">
            ${quotation.seller?.party?.bankName}
            </div>
            <div class="signature-text">Authorized Signature</div>
            </div>
            </div>

            </div>

            <div class="footer-bar">
            For any enquiries, email us on <strong>${"Email of seller "}</strong> or call us on <strong>${"9212047198"}</strong>
            </div>

            </div>

            </body>
            </html>
        """.trimIndent()
    }
}