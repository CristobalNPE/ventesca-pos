package dev.cnpe.ventescaposbe.promotions.application.api

import dev.cnpe.ventescaposbe.promotions.application.api.dto.DiscountApplicationResult
import dev.cnpe.ventescaposbe.promotions.application.api.dto.ItemContextData
import dev.cnpe.ventescaposbe.promotions.application.api.dto.OrderContextData

/**
 * Port defining interactions for retrieving applicable promotions and discounts.
 * Implementations will contain the logic to evaluate DiscountRules based on provided context.
 */
interface PromotionInfoPort {

    /**
     * Finds potentially applicable discounts for a single item, typically used for automatic item-level promotions.
     * This checks active, valid DiscountRules that match the item's context (product, category, brand).
     * Note: This might return multiple results if multiple promotions apply and are combinable. The caller (OrderService)
     * would need to decide how to handle multiple applicable discounts (e.g., apply best one, apply all combinable).
     *
     * @param itemContext Contextual data about the specific order item.
     * @return A list of DiscountApplicationResult objects representing applicable discounts, or an empty list if none apply.
     */
    fun getApplicableItemDiscounts(itemContext: ItemContextData): List<DiscountApplicationResult>


    /**
     * Finds potentially applicable discounts for the entire order, typically used for automatic order-level promotions.
     * This checks active, valid DiscountRules based on the overall order context (total amount, items, customer).
     * Similar to item discounts, this might return multiple results if rules allow combinations.
     *
     * @param orderContext Contextual data about the entire order.
     * @return A list of DiscountApplicationResult objects representing applicable discounts, or an empty list if none apply.
     */
    fun getApplicableOrderDiscounts(orderContext: OrderContextData): List<DiscountApplicationResult>


    /**
     * Validates and retrieves discount details for a manually applied promotion/discount rule ID or name.
     * Checks if the rule exists, is active, valid, and applicable to the given order context.
     * This is used when the cashier explicitly selects a discount to apply.
     *
     * @param discountRuleId The ID of the DiscountRule selected by the user.
     * @param orderContext Contextual data about the order the discount is being applied to.
     * @return A DiscountApplicationResult if the rule is valid and applicable, null otherwise.
     * @throws // Consider specific exceptions for "RuleNotFound", "RuleNotActive", "RuleNotApplicable" if needed.
     */
    fun getManualDiscountApplication(discountRuleId: Long, orderContext: OrderContextData): DiscountApplicationResult?
}