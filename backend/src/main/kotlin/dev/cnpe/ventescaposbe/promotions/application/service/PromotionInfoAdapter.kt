package dev.cnpe.ventescaposbe.promotions.application.service

import dev.cnpe.ventescaposbe.currency.service.MoneyFactory
import dev.cnpe.ventescaposbe.currency.vo.Money
import dev.cnpe.ventescaposbe.promotions.application.api.PromotionInfoPort
import dev.cnpe.ventescaposbe.promotions.application.api.dto.DiscountApplicationResult
import dev.cnpe.ventescaposbe.promotions.application.api.dto.ItemContextData
import dev.cnpe.ventescaposbe.promotions.application.api.dto.OrderContextData
import dev.cnpe.ventescaposbe.promotions.domain.enums.DiscountApplicability.*
import dev.cnpe.ventescaposbe.promotions.domain.model.DiscountRule
import dev.cnpe.ventescaposbe.promotions.infrastructure.persistence.DiscountRuleRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

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
     * Central method to check if a rule applies based on the order context.
     * Verifies applicability targets and then checks conditions like min quantity/spend.
     */
    private fun isRuleApplicable(rule: DiscountRule, orderContext: OrderContextData): Boolean {
        val applicableItems = filterApplicableItems(rule, orderContext.currentItems)

        if (rule.applicability != ORDER_TOTAL && applicableItems.isEmpty()) {
            log.debug { "Rule ${rule.id} (${rule.applicability}) requires specific items/cats/brands, but none found in order." }
            return false
        }

        val conditionsMet = checkMinimumQuantity(rule, applicableItems) &&
                checkMinimumSpend(rule, orderContext, applicableItems)

        if (!conditionsMet) {
            log.debug { "Rule ${rule.id} applicability met, but condition checks failed." }
        }

        return conditionsMet
    }

    /**
     * Filters a list of order items based on the DiscountRule's applicability criteria.
     * Returns the subset of items that the rule *could* potentially apply to or be based on.
     */
    private fun filterApplicableItems(rule: DiscountRule, allItems: List<ItemContextData>): List<ItemContextData> {
        log.trace { "Filtering items for Rule ${rule.id} with applicability ${rule.applicability}" }
        return when (rule.applicability) {

            ORDER_TOTAL -> allItems
            SPECIFIC_PRODUCTS -> {
                if (rule.targetProductIds.isEmpty()) {
                    log.warn { "Rule ${rule.id} applicability is SPECIFIC_PRODUCTS but targetProductIds is empty." }
                    emptyList()
                } else {
                    allItems.filter { rule.targetProductIds.contains(it.productId) }
                }
            }

            CATEGORIES -> {
                if (rule.targetCategoryIds.isEmpty()) {
                    log.warn { "Rule ${rule.id} applicability is CATEGORIES but targetCategoryIds is empty." }
                    emptyList()
                } else {
                    allItems.filter { item ->
                        item.categoryId != null && rule.targetCategoryIds.contains(item.categoryId)
                    }
                }
            }

            BRANDS -> {
                if (rule.targetBrandIds.isEmpty()) {
                    log.warn { "Rule ${rule.id} applicability is BRANDS but targetBrandIds is empty." }
                    emptyList()
                } else {
                    allItems.filter { item ->
                        item.brandId != null && rule.targetBrandIds.contains(item.brandId)
                    }
                }
            }
        }.also {
            log.trace { "Rule ${rule.id}: Applicable items found: ${it.map { i -> i.productId }}" }
        }
    }

    /** Checks if the minimum quantity condition (if defined) is met by the applicable items. */
    private fun checkMinimumQuantity(rule: DiscountRule, applicableItems: List<ItemContextData>): Boolean {
        val minQty = rule.minimumQuantity ?: return true

        val totalApplicableQuantity = applicableItems.sumOf { it.quantity }
        val passes = totalApplicableQuantity >= minQty

        if (!passes) {
            log.debug {
                "Rule ${rule.id} Condition Failed: Minimum quantity ($minQty) " +
                        "not met by applicable items quantity ($totalApplicableQuantity)."
            }
        }
        return passes
    }

    /** Checks if the minimum spend condition (if defined) is met. */
    private fun checkMinimumSpend(
        rule: DiscountRule,
        orderContext: OrderContextData,
        applicableItems: List<ItemContextData>
    ): Boolean {
        val minSpend: Money = rule.minimumSpend ?: return true

        val amountToCheck: Money? = when (rule.applicability) {
            ORDER_TOTAL -> orderContext.currentTotalAmountPreDiscount
            else -> {
                if (applicableItems.isEmpty()) {
                    log.debug {
                        "Rule ${rule.id} Condition Check: " +
                                "Cannot meet minimum spend on applicable items because no applicable items were found."
                    }
                    null
                } else {
                    applicableItems.fold(moneyFactory.zero(minSpend.currencyCode)) { sum, item ->
                        if (item.currentItemTotalPrice.currencyCode == sum.currencyCode) {
                            sum + item.currentItemTotalPrice
                        } else {
                            log.error {
                                "CRITICAL Currency mismatch during min spend check for rule ${rule.id}. " +
                                        "Required: ${minSpend.currencyCode}, " +
                                        "Item ${item.productId} " +
                                        "Currency: ${item.currentItemTotalPrice.currencyCode}. " +
                                        "This should not happen if order items share currency."
                            }
                            sum.copy(amount = BigDecimal.valueOf(-1))
                        }
                    }.takeIf { !it.isNegative() }
                }
            }
        }

        if (amountToCheck == null) {
            return false
        }

        if (amountToCheck.currencyCode != minSpend.currencyCode) {
            log.warn {
                "Rule ${rule.id} Condition Check Failed: Currency mismatch cannot satisfy minimum spend. " +
                        "Required: ${minSpend.currencyCode}, Check Amount Currency: ${amountToCheck.currencyCode}."
            }
            return false
        }

        val passes = amountToCheck >= minSpend
        if (!passes) {
            log.debug { "Rule ${rule.id} Condition Failed: Minimum spend ($minSpend) not met by relevant total ($amountToCheck)." }
        }
        return passes
    }
}