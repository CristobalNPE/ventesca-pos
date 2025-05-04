package dev.cnpe.ventescaposbe.orders.application.service

import com.lowagie.text.*
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import dev.cnpe.ventescaposbe.currency.service.MoneyFactory
import dev.cnpe.ventescaposbe.currency.vo.Money
import dev.cnpe.ventescaposbe.orders.application.dto.response.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.time.format.DateTimeFormatter

private val log = KotlinLogging.logger {}

@Service
class PdfReceiptGeneratorService(
    private val moneyFactory: MoneyFactory
) {

    companion object {
        private val FONT_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 8f, Font.NORMAL)
        private val FONT_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9f, Font.BOLD)
        private val FONT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12f, Font.BOLD)
        private val FONT_SMALL = FontFactory.getFont(FontFactory.HELVETICA, 7f, Font.NORMAL)
        private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }

    fun generatePdfReceipt(receiptData: ReceiptResponse): ByteArray {
        log.debug { "Generating PDF receipt for Order: ${receiptData.header.orderNumber}" }
        val outputStream = ByteArrayOutputStream()
        val document = Document(PageSize.A7.rotate(), 15f, 15f, 15f, 15f)

        try {
            PdfWriter.getInstance(document, outputStream)
            document.open()

            addHeader(document, receiptData)

            addItemTable(document, receiptData.items)

            addTotals(document, receiptData.totals)

            addPayments(document, receiptData.payments)

            addFooter(document, receiptData.footer)

            document.close()
            log.info { "PDF generated successfully for Order: ${receiptData.header.orderNumber}" }
            return outputStream.toByteArray()

        } catch (e: DocumentException) {
            log.error(e) { "Error generating PDF document for Order: ${receiptData.header.orderNumber}" }
            throw RuntimeException("Failed to generate PDF receipt", e)
        } catch (e: Exception) {
            log.error(e) { "Unexpected error during PDF generation for Order: ${receiptData.header.orderNumber}" }
            throw e
        } finally {
            outputStream.close()
            if (document.isOpen) {
                try {
                    document.close()
                } catch (ignored: Exception) {
                }
            }
        }
    }

    // *******************************
    // 🔰 Private Helpers
    // *******************************

    private fun addHeader(document: Document, receiptData: ReceiptResponse) {
        val header = receiptData.header
        document.add(Paragraph(header.businessName, FONT_TITLE).apply { alignment = Element.ALIGN_CENTER })
        document.add(Paragraph(header.branchName, FONT_NORMAL).apply { alignment = Element.ALIGN_CENTER })
        header.branchAddress?.let { document.add(Paragraph(it, FONT_SMALL).apply { alignment = Element.ALIGN_CENTER }) }
        header.branchPhone?.let {
            document.add(Paragraph("Tel: $it", FONT_SMALL).apply {
                alignment = Element.ALIGN_CENTER
            })
        }
        document.add(Chunk.NEWLINE)
        document.add(Paragraph("Order: ${header.orderNumber}", FONT_NORMAL))
        document.add(Paragraph("Date: ${header.orderTimestamp.format(DATE_TIME_FORMATTER)}", FONT_NORMAL))
        document.add(Paragraph("Cashier: ${header.cashierName ?: header.cashierId}", FONT_NORMAL))
        header.sessionNumber?.let { document.add(Paragraph("Session: $it", FONT_SMALL)) }
        receiptData.customerInfo?.let {
            document.add(Paragraph("Customer: ${it.fullName}" + (it.taxId?.let { tid -> " (ID: $tid)" } ?: ""),
                FONT_NORMAL))
        }
        document.add(Chunk.NEWLINE)
    }

    private fun addItemTable(document: Document, items: List<ReceiptLineItem>) {
        val table = PdfPTable(floatArrayOf(4f, 1f, 2f, 2f)).apply { widthPercentage = 100f }

        table.addCell(createHeaderCell("Product"))
        table.addCell(createHeaderCell("Qty"))
        table.addCell(createHeaderCell("Unit Price", Element.ALIGN_RIGHT))
        table.addCell(createHeaderCell("Total", Element.ALIGN_RIGHT))

        items.forEach { item ->
            table.addCell(createCell("${item.productName}${item.sku?.let { " [$it]" } ?: ""}"))
            table.addCell(createCell(formatQuantity(item.quantity), Element.ALIGN_CENTER))
            table.addCell(createCell(formatMoney(item.unitPrice), Element.ALIGN_RIGHT))
            table.addCell(createCell(formatMoney(item.lineTotal), Element.ALIGN_RIGHT))

            item.discountApplied?.let { discount ->
                val discountCell = createCell(
                    "  Discount: -${formatMoney(discount)}",
                    Element.ALIGN_RIGHT,
                    FONT_SMALL
                ).apply { this.colspan = 4 }
                table.addCell(discountCell)
            }
        }
        document.add(table)
        document.add(Chunk.NEWLINE)
    }

    private fun addTotals(document: Document, totals: ReceiptTotals) {
        val totalsTable = PdfPTable(2).apply { widthPercentage = 60f; horizontalAlignment = Element.ALIGN_RIGHT }
        totalsTable.addCell(createTotalsCell("Subtotal (Net):"))
        totalsTable.addCell(createTotalsCell(formatMoney(totals.subTotal), Element.ALIGN_RIGHT))
        totalsTable.addCell(createTotalsCell("Tax:"))
        totalsTable.addCell(createTotalsCell(formatMoney(totals.taxAmount), Element.ALIGN_RIGHT))
        totalsTable.addCell(createTotalsCell("Total (Gross):"))
        totalsTable.addCell(createTotalsCell(formatMoney(totals.grossTotal), Element.ALIGN_RIGHT))

        if (totals.discountAmount.amount > java.math.BigDecimal.ZERO) {
            totalsTable.addCell(createTotalsCell("Discount:"))
            totalsTable.addCell(createTotalsCell("-${formatMoney(totals.discountAmount)}", Element.ALIGN_RIGHT))
        }

        totalsTable.addCell(createTotalsCell("Final Amount:", Element.ALIGN_LEFT, FONT_BOLD))//todo: check alignment
        totalsTable.addCell(createTotalsCell(formatMoney(totals.finalAmount), Element.ALIGN_RIGHT, FONT_BOLD))
        totalsTable.addCell(createTotalsCell("Total Paid:"))
        totalsTable.addCell(createTotalsCell(formatMoney(totals.totalPaid), Element.ALIGN_RIGHT))

        totals.changeDue?.let {
            totalsTable.addCell(createTotalsCell("Change Due:"))
            totalsTable.addCell(createTotalsCell(formatMoney(it), Element.ALIGN_RIGHT))
        }
        document.add(totalsTable)
        document.add(Chunk.NEWLINE)
    }

    private fun addPayments(document: Document, payments: List<ReceiptPayment>) {
        document.add(Paragraph("Payments:", FONT_BOLD))
        val paymentTable = PdfPTable(2).apply { widthPercentage = 60f; horizontalAlignment = Element.ALIGN_LEFT }
        payments.forEach { payment ->
            paymentTable.addCell(createCell(payment.method))
            paymentTable.addCell(createCell(formatMoney(payment.amount), Element.ALIGN_RIGHT))
            payment.transactionReference?.let { ref ->
                paymentTable.addCell(createCell(" Ref: $ref", Element.ALIGN_LEFT, FONT_SMALL).apply { colspan = 2 })
            }
        }
        document.add(paymentTable)
        document.add(Chunk.NEWLINE)
    }

    private fun addFooter(document: Document, footer: ReceiptFooter?) {
        footer?.message?.let { document.add(Paragraph(it, FONT_NORMAL).apply { alignment = Element.ALIGN_CENTER }) }
        footer?.returnPolicy?.let { document.add(Paragraph(it, FONT_SMALL).apply { alignment = Element.ALIGN_CENTER }) }
    }

    private fun createCell(text: String?, alignment: Int = Element.ALIGN_LEFT, font: Font = FONT_NORMAL): PdfPCell {
        return PdfPCell(Phrase(text ?: "", font)).apply {
            this.horizontalAlignment = alignment
            this.border = Rectangle.NO_BORDER
            this.paddingTop = 2f //todo: Check padding
        }
    }

    private fun createHeaderCell(text: String, alignment: Int = Element.ALIGN_LEFT): PdfPCell {
        return PdfPCell(Phrase(text, FONT_BOLD)).apply {
            this.horizontalAlignment = alignment
            this.border = Rectangle.BOTTOM
            this.paddingBottom = 4f
            this.paddingTop = 2f

        }
    }

    private fun createTotalsCell(
        text: String,
        alignment: Int = Element.ALIGN_LEFT,
        font: Font = FONT_NORMAL
    ): PdfPCell {
        return createCell(text, alignment, font).apply { paddingLeft = 1f; paddingTop = 3f } //todo chekc padding
    }

    private fun formatMoney(money: Money): String {
        return moneyFactory.format(money, LocaleContextHolder.getLocale())
    }

    private fun formatQuantity(quantity: Double): String {
        return if (quantity == quantity.toInt().toDouble()) {
            quantity.toInt().toString()
        } else {
            String.format("%.2f", quantity)
        }
    }
}