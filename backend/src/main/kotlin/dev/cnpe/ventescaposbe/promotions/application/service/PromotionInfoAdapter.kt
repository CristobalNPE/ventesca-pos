package dev.cnpe.ventescaposbe.promotions.application.service

import dev.cnpe.ventescaposbe.currency.service.MoneyFactory
import dev.cnpe.ventescaposbe.promotions.application.api.PromotionInfoPort
import dev.cnpe.ventescaposbe.promotions.application.api.dto.DiscountApplicationResult
import dev.cnpe.ventescaposbe.promotions.application.api.dto.ItemContextData
import dev.cnpe.ventescaposbe.promotions.application.api.dto.OrderContextData
import dev.cnpe.ventescaposbe.promotions.domain.enums.DiscountApplicability
import dev.cnpe.ventescaposbe.promotions.domain.model.DiscountRule
import dev.cnpe.ventescaposbe.promotions.infrastructure.persistence.DiscountRuleRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger {}

@Service
@Transactional(readOnly = true)
class PromotionInfoAdapter(
    private val discountRuleRepository: DiscountRuleRepository,
    private val moneyFactory: MoneyFactory
) : PromotionInfoPort {

    // TODO
    override fun getApplicableItemDiscounts(itemContext: ItemContextData): List<DiscountApplicationResult> {
        log.warn { "getApplicableItemDiscounts is not yet implemented. Returning empty list." }
        return emptyList()
    }

    // TODO
    override fun getApplicableOrderDiscounts(orderContext: OrderContextData): List<DiscountApplicationResult> {
        log.warn { "getApplicableOrderDiscounts is not yet implemented. Returning empty list." }
        return emptyList()
    }

    override fun getManualDiscountApplication(
        discountRuleId: Long,
        orderContext: OrderContextData
    ): DiscountApplicationResult? {
        log.debug { "Validating manual application of DiscountRule ID: $discountRuleId for Order Context." }

        val rule = discountRuleRepository.findByIdOrNull(discountRuleId)
            ?: run {
                log.debug { "Validating manual application of DiscountRule ID: $discountRuleId for Order Context." }
                return null
            }

        if (!rule.isValidNow()) {
            log.warn { "Manual discount application failed: DiscountRule ID $discountRuleId is inactive or outside its valid date range." }
            return null
        }

        if (!isRuleApplicable(rule, orderContext)) {
            log.warn { "Manual discount application failed: DiscountRule ID $discountRuleId conditions not met for the provided order context." }
            return null
        }

        log.info { "Manual discount validation successful for Rule ID: $discountRuleId ('${rule.name}')." }
        return DiscountApplicationResult(
            discountRuleId = rule.id!!,
            description = rule.name,
            type = rule.type,
            value = rule.value
        )
    }

    // *******************************
    // 🔰 Private Helpers
    // *******************************

    /**
     * Determines if a discount rule is applicable for a given order context.
     *
     * @param rule The discount rule to evaluate.
     * @param orderContext The context of the order, including items, total amounts, and currencies.
     * @return `true` if the discount rule is applicable, otherwise `false`.
     */
    private fun isRuleApplicable(rule: DiscountRule, orderContext: OrderContextData): Boolean {

        val hasMinimumSpend = rule.minimumSpend != null

        if (rule.applicability == DiscountApplicability.ORDER_TOTAL && hasMinimumSpend) {
            if (rule.minimumSpend!!.currencyCode != orderContext.currentTotalAmountPreDiscount.currencyCode) {
                log.warn {
                    "Currency mismatch between rule minimum spend (${rule.minimumSpend!!.currencyCode}) " +
                            "and order total (${orderContext.currentTotalAmountPreDiscount.currencyCode}) " +
                            "for rule ${rule.id}. Applicability check failed."
                }
                return false // different currencies
            }
            if (orderContext.currentTotalAmountPreDiscount < rule.minimumSpend!!) {
                log.debug {
                    "Rule ${rule.id} minimum spend (${rule.minimumSpend}) " +
                            "not met by order total (${orderContext.currentTotalAmountPreDiscount})."
                }
                return false
            }
        }

        val applicableItems = when (rule.applicability) {
            DiscountApplicability.ORDER_TOTAL -> orderContext.currentItems
            DiscountApplicability.SPECIFIC_PRODUCTS -> orderContext.currentItems.filter {
                rule.targetProductIds.contains(
                    it.productId
                )
            }

            DiscountApplicability.CATEGORIES -> orderContext.currentItems.filter { rule.targetCategoryIds.contains(it.categoryId) }
            DiscountApplicability.BRANDS -> orderContext.currentItems.filter { rule.targetBrandIds.contains(it.brandId) }
        }

        if (rule.applicability != DiscountApplicability.ORDER_TOTAL && applicableItems.isEmpty()) {
            log.debug { "Rule ${rule.id} applies to specific items/cats/brands, but no matching items found in the order." }
            return false
        }

        if (rule.minimumQuantity != null) {
            val totalApplicableQuantity = applicableItems.sumOf { it.quantity }
            if (totalApplicableQuantity < rule.minimumQuantity!!) {
                log.debug {
                    "Rule ${rule.id} minimum quantity (${rule.minimumQuantity}) " +
                            "not met by applicable items quantity ($totalApplicableQuantity)."
                }
                return false
            }
        }

        if (rule.applicability != DiscountApplicability.ORDER_TOTAL && hasMinimumSpend) {
            if (applicableItems.isEmpty()) return false

            val applicableItemsSpend =
                applicableItems.fold(moneyFactory.zero(rule.minimumSpend!!.currencyCode)) { sum, item ->
                    if (item.currentItemTotalPrice.currencyCode == sum.currencyCode) {
                        sum + item.currentItemTotalPrice
                    } else {
                        log.warn {
                            "Currency mismatch between rule minimum spend (${rule.minimumSpend!!.currencyCode}) " +
                                    "and item total (${item.currentItemTotalPrice.currencyCode}) for item product " +
                                    "ID ${item.productId}, rule ${rule.id}. Skipping item for spend calculation."
                        }
                        sum // skip if currency mismatches
                    }
                }

            if (applicableItemsSpend < rule.minimumSpend!!) {
                log.debug {
                    "" +
                            "Rule ${rule.id} minimum spend (${rule.minimumSpend}) not met by applicable items total spend ($applicableItemsSpend)."
                }
                return false
            }
        }

        return true
    }

}