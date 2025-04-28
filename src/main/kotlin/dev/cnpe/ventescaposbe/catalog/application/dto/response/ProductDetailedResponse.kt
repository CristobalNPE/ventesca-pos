package dev.cnpe.ventescaposbe.catalog.application.dto.response

import dev.cnpe.ventescaposbe.catalog.application.dto.common.ProductDetails
import dev.cnpe.ventescaposbe.catalog.application.dto.common.ProductPricingData
import dev.cnpe.ventescaposbe.catalog.application.dto.common.ProductRelationsData
import dev.cnpe.ventescaposbe.catalog.application.dto.common.ProductStockData
import dev.cnpe.ventescaposbe.shared.application.dto.ResourceAuditData
import dev.cnpe.ventescaposbe.shared.domain.vo.Image
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Complete details for a single product.")
data class ProductDetailedResponse(

    @Schema(description = "Product ID.", requiredMode = Schema.RequiredMode.REQUIRED)
    val id: Long,

    @Schema(description = "Core product details.", requiredMode = Schema.RequiredMode.REQUIRED)
    val details: ProductDetails,

    @Schema(description = "Pricing information.", requiredMode = Schema.RequiredMode.REQUIRED)
    val pricing: ProductPricingData,

    @Schema(description = "Stock information.", requiredMode = Schema.RequiredMode.REQUIRED)
    val stockInfo: ProductStockData,

    @Schema(description = "List of product images.")
    val photos: List<Image>?,

    @Schema(description = "Relationship IDs.", requiredMode = Schema.RequiredMode.REQUIRED)
    val relations: ProductRelationsData,

    @Schema(description = "Audit information.", requiredMode = Schema.RequiredMode.REQUIRED)
    val auditData: ResourceAuditData


)
