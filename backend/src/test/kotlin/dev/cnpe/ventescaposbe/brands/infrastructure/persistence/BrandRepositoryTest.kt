package dev.cnpe.ventescaposbe.brands.infrastructure.persistence

import dev.cnpe.ventescaposbe.brands.domain.model.Brand
import dev.cnpe.ventescaposbe.config.AbstractContainerTest
import dev.cnpe.ventescaposbe.shared.domain.vo.GeneratedCode
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.annotation.DirtiesContext

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BrandRepositoryTest : AbstractContainerTest() {

    @Autowired
    private lateinit var brandRepository: BrandRepository


    private lateinit var brandSony: Brand
    private lateinit var brandSamsung: Brand
    private lateinit var brandDefault: Brand


    @BeforeEach
    fun setupTestData() {

        brandSony = Brand(name = "Sony", code = GeneratedCode("SNY"))
        brandSamsung = Brand(name = "Samsung", code = GeneratedCode("SAM"))
        brandDefault = Brand(name = "DefaultBrand", code = GeneratedCode("DEF"), isDefault = true)

        brandRepository.saveAll(listOf(brandSony, brandSamsung, brandDefault))
        brandRepository.flush()
    }


    @Nested
    inner class ExistsByName {
        @Test
        fun `should return true for existing brand`() {
            val exists = brandRepository.existsByName("Sony")
            exists.shouldBeTrue()
        }

        @Test
        fun `should return false for non-existing brand`() {
            val exists = brandRepository.existsByName("NonExistent")
            exists.shouldBeFalse()
        }

    }

    @Nested
    inner class GetBrandCodeById {
        @Test
        fun `should return correct code for existing brand`() {
            val sonyId = brandRepository.findAll().first { it.name == "Sony" }.id!!
            val code = brandRepository.getBrandCodeById(sonyId)

            code.shouldNotBeNull()
            code shouldBe "SNY"
        }

        @Test
        fun `should return null for non-existing brand ID`() {
            val code = brandRepository.getBrandCodeById(9999L)
            code.shouldBeNull()
        }
    }

    @Nested
    inner class GetBrandByIsDefaultTrue {
        @Test
        fun `getBrandByIsDefaultTrue should return the default brand`() {
            val default = brandRepository.getBrandByIsDefaultTrue()

            default.shouldNotBeNull()
            default.id shouldBe brandDefault.id
            default.name shouldBe brandDefault.name
            default.code shouldBe brandDefault.code
            default.isDefault.shouldBeTrue()
        }
    }


}