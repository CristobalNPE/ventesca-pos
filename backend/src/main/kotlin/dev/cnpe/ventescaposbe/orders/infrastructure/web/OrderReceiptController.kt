package dev.cnpe.ventescaposbe.orders.infrastructure.web

import dev.cnpe.ventescaposbe.orders.application.dto.response.ReceiptResponse
import dev.cnpe.ventescaposbe.orders.application.service.PdfReceiptGeneratorService
import dev.cnpe.ventescaposbe.orders.application.service.ReceiptService
import dev.cnpe.ventescaposbe.security.annotation.RequirePosOperatorRoles
import dev.cnpe.ventescaposbe.shared.application.dto.ApiResult
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/orders/{orderId}/receipt")
@Tag(name = "Order Receipts", description = "Generate receipts for completed orders.")
@RequirePosOperatorRoles
class OrderReceiptController(
    private val receiptService: ReceiptService,
    private val pdfReceiptGeneratorService: PdfReceiptGeneratorService
) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Get structured receipt data (JSON)",
        description = "Retrieves detailed, structured receipt data for a completed order, suitable for frontend rendering."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Receipt data retrieved successfully.",
                content = [Content(schema = Schema(implementation = ReceiptResponse::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "Order not found.",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "409", description = "Invalid order state (Order not COMPLETED or REFUNDED).",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(responseCode = "401", description = "Unauthorized."),
            ApiResponse(responseCode = "403", description = "Forbidden.")
        ]
    )
    fun getReceiptJson(
        @Parameter(description = "ID of the order") @PathVariable orderId: Long
    ): ReceiptResponse {
        return receiptService.generateReceiptData(orderId)
    }

    @GetMapping(produces = [MediaType.APPLICATION_PDF_VALUE])
    @Operation(
        summary = "Generate receipt as PDF",
        description = "Generates and returns a PDF representation of the receipt for a completed order."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "PDF receipt generated successfully.",
                content = [Content(mediaType = MediaType.APPLICATION_PDF_VALUE)]
            ),
            ApiResponse(
                responseCode = "404", description = "Order not found.",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(
                responseCode = "409", description = "Invalid order state (Order not COMPLETED or REFUNDED).",
                content = [Content(schema = Schema(implementation = ApiResult::class))]
            ),
            ApiResponse(responseCode = "500", description = "Error during PDF generation."),
            ApiResponse(responseCode = "401", description = "Unauthorized."),
            ApiResponse(responseCode = "403", description = "Forbidden.")
        ]
    )
    fun getReceiptPdf(
        @Parameter(description = "ID of the order") @PathVariable orderId: Long
    ): ResponseEntity<ByteArray> {
        val receiptData = receiptService.generateReceiptData(orderId)

        val pdfBytes = pdfReceiptGeneratorService.generatePdfReceipt(receiptData)

        val headers = HttpHeaders()

        headers.contentType = MediaType.APPLICATION_PDF

        headers.setContentDispositionFormData(
            "receipt-${receiptData.header.orderNumber}.pdf",
            "receipt-${receiptData.header.orderNumber}.pdf"
        )

        headers.add(
            HttpHeaders.CONTENT_DISPOSITION,
            "inline; filename=receipt-${receiptData.header.orderNumber}.pdf"
        )

        return ResponseEntity(pdfBytes, headers, HttpStatus.OK)
    }
}