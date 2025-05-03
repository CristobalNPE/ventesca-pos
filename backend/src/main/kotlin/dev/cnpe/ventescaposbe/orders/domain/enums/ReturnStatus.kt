package dev.cnpe.ventescaposbe.orders.domain.enums

import dev.cnpe.ventescaposbe.shared.domain.enums.DomainEnum
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Status of a return transaction.")
enum class ReturnStatus : DomainEnum {

    @Schema(description = "Return transaction successfully completed.")
    COMPLETED


    // will add PENDING, FAILED later if needed
}
