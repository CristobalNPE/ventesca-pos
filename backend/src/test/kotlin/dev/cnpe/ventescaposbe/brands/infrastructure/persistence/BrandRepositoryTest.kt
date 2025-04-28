package dev.cnpe.ventescaposbe.brands.infrastructure.persistence

import dev.cnpe.ventescaposbe.brands.domain.model.Brand
import dev.cnpe.ventescaposbe.config.BaseIntegrationTest
import dev.cnpe.ventescaposbe.config.PersistenceAuditConfig
import dev.cnpe.ventescaposbe.shared.domain.vo.GeneratedCode
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.repository.findByIdOrNull

@DataJpaTest
@Import(PersistenceAuditConfig::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BrandRepositoryTest : BaseIntegrationTest() {

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

    @Test
    fun `existByName should return true for existing brand`() {
        val exists = brandRepository.existsByName("Sony")
        exists.shouldBeTrue()
    }

    @Test
    fun `existByName should return false for non-existing brand`() {
        val exists = brandRepository.existsByName("NonExistent")
        exists.shouldBeFalse()
    }

    @Test
    fun `getBrandCodeById should return correct code for existing brand`() {
        val sonyId = brandRepository.findAll().first { it.name == "Sony" }.id!!
        val code = brandRepository.getBrandCodeById(sonyId)

        code.shouldNotBeNull()
        code shouldBe "SNY"
    }

    @Test
    fun `getBrandCodeById should return null for non-existing brand ID`() {
        val code = brandRepository.getBrandCodeById(9999L)
        code.shouldBeNull()
    }

    @Test
    fun `getBrandByIsDefaultTrue should return the default brand`() {
        val default = brandRepository.getBrandByIsDefaultTrue()

        default.shouldNotBeNull()
        default.id shouldBe brandDefault.id
        default.name shouldBe brandDefault.name
        default.code shouldBe brandDefault.code
        default.isDefault.shouldBeTrue()
    }

    @Test
    fun `findByIdOrNull should return brand for existing ID`() {
        val samsungId = brandRepository.findAll().first { it.name == "Samsung" }.id!!
        val found = brandRepository.findByIdOrNull(samsungId)

        found.shouldNotBeNull()
        found.name shouldBe brandSamsung.name
    }

    @Test
    fun `findByIdOrNull should return null for non-existing ID`() {
        val found = brandRepository.findByIdOrNull(9999L)
        found.shouldBeNull()
    }

    @Test
    fun `findAll should return all saved brands`() {
        val allBrands = brandRepository.findAll()
        allBrands shouldHaveSize 3
    }
}