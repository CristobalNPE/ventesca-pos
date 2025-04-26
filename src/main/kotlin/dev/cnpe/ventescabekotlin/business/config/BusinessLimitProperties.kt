package dev.cnpe.ventescabekotlin.business.config

import jakarta.validation.constraints.PositiveOrZero
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "app.business.limits")
@Validated
data class BusinessLimitProperties(

    @field:PositiveOrZero
    val maxUsersPerBusiness: Int = 10,

    @field:PositiveOrZero
    val maxBranchesPerBusiness: Int = 5
)