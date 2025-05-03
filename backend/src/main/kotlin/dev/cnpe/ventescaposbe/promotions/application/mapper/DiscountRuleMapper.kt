package dev.cnpe.ventescaposbe.promotions.application.mapper

import dev.cnpe.ventescaposbe.currency.service.MoneyFactory
import dev.cnpe.ventescaposbe.promotions.application.dto.request.CreateDiscountRuleRequest
import dev.cnpe.ventescaposbe.promotions.application.dto.response.DiscountRuleResponse
import dev.cnpe.ventescaposbe.promotions.domain.enums.DiscountApplicability.*
import dev.cnpe.ventescaposbe.promotions.domain.model.DiscountRule
import dev.cnpe.ventescaposbe.shared.application.dto.ResourceAuditData
import org.springframework.stereotype.Component

@Component
class DiscountRuleMapper(
    private val moneyFactory: MoneyFactory
) {

    /**
     * Converts a `CreateDiscountRuleRequest` object into a `DiscountRule` entity by mapping
     * all properties and applying transformations, validations, and default values where necessary.
     *
     * @param request The `CreateDiscountRuleRequest` containing all data required to define a discount rule.
     *                This includes details like rule*/
    fun toEntity(request: CreateDiscountRuleRequest): DiscountRule {
        val minimumSpendMoney = if (request.minimumSpendAmount != null && request.minimumSpendCurrency != null) {
            moneyFactory.createMoney(request.minimumSpendAmount, request.minimumSpendCurrency)
        } else {
            null
        }

        validateApplicability(request)

        return DiscountRule(
            name = request.name,
            description = request.description,
            type = request.type,
            value = request.value,
            startDate = request.startDate,
            endDate = request.endDate,
            isActive = request.isActive,
            applicability = request.applicability,
            targetProductIds = request.targetProductIds?.toMutableSet() ?: mutableSetOf(),
            targetCategoryIds = request.targetCategoryIds?.toMutableSet() ?: mutableSetOf(),
            targetBrandIds = request.targetBrandIds?.toMutableSet() ?: mutableSetOf(),
            minimumQuantity = request.minimumQuantity,
            minimumSpend = minimumSpendMoney,
            isCombinable = request.isCombinable
        )
    }


    /**
     * Converts a `DiscountRule` entity into a `DiscountRuleResponse` object by mapping all relevant fields.
     *
     * @param entity The `DiscountRule` entity to be converted, containing details such as the rule's name,
     *               description, type, value, and applicable targets.
     * @return A `DiscountRuleResponse` object populated with the corresponding fields from the provided `DiscountRule` entity.
     */
    fun toResponse(entity: DiscountRule): DiscountRuleResponse {
        return DiscountRuleResponse(
            id = entity.id!!,
            name = entity.name,
            description = entity.description,
            type = entity.type,
            value = entity.value,
            startDate = entity.startDate,
            endDate = entity.endDate,
            isActive = entity.isActive,
            applicability = entity.applicability,
            targetProductIds = entity.targetProductIds.takeIf { it.isNotEmpty() },
            targetCategoryIds = entity.targetCategoryIds.takeIf { it.isNotEmpty() },
            targetBrandIds = entity.targetBrandIds.takeIf { it.isNotEmpty() },
            minimumQuantity = entity.minimumQuantity,
            minimumSpend = entity.minimumSpend,
            isCombinable = entity.isCombinable,
            auditData = ResourceAuditData.fromBaseEntity(entity)
        )
    }

    private fun validateApplicability(request: CreateDiscountRuleRequest) {
        when (request.applicability) {

            SPECIFIC_PRODUCTS ->
                require(!request.targetProductIds.isNullOrEmpty()) {
                    "Target Product IDs must be provided when applicability is SPECIFIC_PRODUCTS."
                }

            CATEGORIES ->
                require(!request.targetCategoryIds.isNullOrEmpty()) {
                    "Target Category IDs must be provided when applicability is CATEGORIES."
                }

            BRANDS ->
                require(!request.targetBrandIds.isNullOrEmpty()) {
                    "Target Brand IDs must be provided when applicability is BRANDS."
                }

            ORDER_TOTAL -> {
                require(request.targetProductIds.isNullOrEmpty()) {
                    "Target Product IDs must be empty for ORDER_TOTAL applicability."
                }
                require(request.targetCategoryIds.isNullOrEmpty()) {
                    "Target Category IDs must be empty for ORDER_TOTAL applicability."
                }
                require(request.targetBrandIds.isNullOrEmpty()) {
                    "Target Brand IDs must be empty for ORDER_TOTAL applicability."
                }
            }
        }
        // TODO: Validate that target IDs actually exist using info ports? maybe in the service layer.
    }

}