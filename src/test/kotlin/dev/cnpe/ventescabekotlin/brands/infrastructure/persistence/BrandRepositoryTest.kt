package dev.cnpe.ventescabekotlin.brands.infrastructure.persistence

import dev.cnpe.ventescabekotlin.brands.domain.model.Brand
import dev.cnpe.ventescabekotlin.brands.infrastructure.persistence.BrandRepository
import dev.cnpe.ventescabekotlin.shared.domain.vo.GeneratedCode
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class BrandRepositoryTest {

    companion object {

        @Container
        @ServiceConnection
        val postgresContainer = PostgreSQLContainer("postgres:16.8-alpine")
        // .withDatabaseName("testdb")
        // .withUsername("testuser")
        // .withPassword("testpass")
    }

    @Autowired
    private lateinit var brandRepository: BrandRepository

    private lateinit var brandSony: Brand
    private lateinit var brandSamsung: Brand
    private lateinit var brandDefault: Brand


    @BeforeEach
    fun setUp() {
        brandSony = Brand(name = "Sony", code = GeneratedCode("SNY"))
        brandSamsung = Brand(name = "Samsung", code = GeneratedCode("SAM"))
        brandDefault = Brand(name = "Default", code = GeneratedCode("DEF"), isDefault = true)

        brandRepository.saveAll(listOf(brandSony, brandSamsung, brandDefault))
        brandRepository.flush()
    }

    @AfterEach
    fun tearDown() {
        brandRepository.deleteAll()
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
        val sonyId = brandSony.id!!
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
        val samsungId = brandSamsung.id!!
        val found = brandRepository.findByIdOrNull(samsungId)

        found.shouldNotBeNull()
        found.name shouldBe brandSamsung.name // found.name shouldBe "Samsung" //TODO: reasons to use "Samsung" instead of brandSamsung.name??
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