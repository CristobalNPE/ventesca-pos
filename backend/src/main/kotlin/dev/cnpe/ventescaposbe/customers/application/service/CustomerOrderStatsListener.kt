package dev.cnpe.ventescaposbe.customers.application.service

import dev.cnpe.ventescaposbe.customers.infrastructure.persistence.CustomerRepository
import dev.cnpe.ventescaposbe.orders.application.api.OrderInfoPort
import dev.cnpe.ventescaposbe.orders.event.OrderCompletedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger {}

@Service
class CustomerOrderStatsListener(
    private val customerRepository: CustomerRepository,
    private val orderInfoPort: OrderInfoPort
) {

    @ApplicationModuleListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun onOrderCompleted(event: OrderCompletedEvent) {
        log.debug { "Received OrderCompletedEvent (Order ID: ${event.orderId}) - Checking for customer stats update." }

        val orderInfo = orderInfoPort.getOrderStatsInfo(event.orderId)
        if (orderInfo == null) {
            log.warn { "Could not find order info via Port for Order ID ${event.orderId} to update customer stats." }
            return
        }

        val customerId = orderInfo.customerId
        if (customerId == null) {
            log.debug { "Order ${event.orderId} has no associated customer. Skipping stats update." }
            return
        }

        try {
            val customerOpt = customerRepository.findById(customerId)
            if (customerOpt.isEmpty) {
                log.error { "Customer ID $customerId associated with completed Order ${event.orderId} not found! Cannot update stats." }
                return
            }
            val customer = customerOpt.get()

            if (customer.totalSpent.currencyCode == orderInfo.finalAmount.currencyCode) {
                customer.totalSpent += orderInfo.finalAmount
            } else {
                log.error {
                    "Currency mismatch between customer ${customer.id} (${customer.totalSpent.currencyCode}) " +
                            "and order ${orderInfo.orderId} (${orderInfo.finalAmount.currencyCode}). Cannot add to totalSpent."
                }
            }

            customer.totalOrders += 1
            customer.lastOrderDate = orderInfo.orderTimestamp

            customerRepository.save(customer)
            log.info { "Updated stats for Customer ID $customerId based on Order ${event.orderId}. New total orders: ${customer.totalOrders}" }

        } catch (e: Exception) {
            log.error(e) { "Failed to update stats for Customer ID $customerId from OrderCompletedEvent ${event.orderId}" }
            // todo retry/dead-letter queue.
        }
    }
}
