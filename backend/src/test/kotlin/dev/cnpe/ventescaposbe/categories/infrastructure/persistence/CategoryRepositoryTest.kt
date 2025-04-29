package dev.cnpe.ventescaposbe.categories.infrastructure.persistence

import dev.cnpe.ventescaposbe.categories.domain.model.Category
import dev.cnpe.ventescaposbe.config.AbstractContainerTest
import dev.cnpe.ventescaposbe.shared.domain.vo.GeneratedCode
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
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
class CategoryRepositoryTest : AbstractContainerTest() {

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    private lateinit var electronics: Category
    private lateinit var clothing: Category
    private lateinit var phones: Category
    private lateinit var defaultCategory: Category


    @BeforeEach
    fun setUpTestData() {
        electronics = Category(
            name = "Electronics",
            color = "#FF0000",
            isDefault = false,
            code = GeneratedCode("ELEC"),
        )
        clothing = Category(
            name = "Clothing",
            color = "#00FF00",
            code = GeneratedCode("CLTH"),
            isDefault = false
        )

        defaultCategory = Category(
            name = "General",
            color = "#888888",
            code = GeneratedCode("GENE"),
            isDefault = true
        )

        categoryRepository.saveAll(listOf(electronics, clothing, defaultCategory))
        categoryRepository.flush()

        phones = Category(
            name = "Smartphones",
            color = "#FF9900",
            code = GeneratedCode("ELEC-PHONE"),
            isDefault = false,
            parent = electronics
        )

        categoryRepository.save(phones)
        categoryRepository.flush()
    }

    @Nested
    inner class FindAllParentCategories {

        @Test
        fun `should return only root categories`() {
            val parents = categoryRepository.findAllParentCategories()

            parents shouldHaveSize 3
            val parentNames = parents.map { it.name }
            parentNames shouldContainExactlyInAnyOrder listOf("General", "Electronics", "Clothing")
        }
    }

    @Nested
    inner class GetDefaultCategory {

        @Test
        fun `should return the category marked as default`() {
            val foundDefault = categoryRepository.getDefaultCategory()

            foundDefault.shouldNotBeNull()
            foundDefault.id shouldBe defaultCategory.id
            foundDefault.name shouldBe "General"
            foundDefault.isDefault shouldBe true
        }
    }

    @Nested
    inner class GetCategoryCodeById {

        @Test
        fun `should return correct code for existing category`() {
            val code = categoryRepository.getCategoryCodeById(electronics.id!!)

            code.shouldNotBeNull()
            code shouldBe "ELEC"
        }

        @Test
        fun `should return correct code for existing subcategory`() {
            val code = categoryRepository.getCategoryCodeById(phones.id!!)

            code.shouldNotBeNull()
            code shouldBe "ELEC-PHONE"
        }

        @Test
        fun `should return null for non-existing category ID`() {
            val code = categoryRepository.getCategoryCodeById(Long.MAX_VALUE)
            code.shouldBeNull()
        }
    }

    @Nested
    inner class ExistsByCode {

        @Test
        fun `should return true for existing name`() {
            val exists = categoryRepository.existsByCode(GeneratedCode("CLTH"))
            exists shouldBe true
        }

        @Test
        fun `should return false for non-existing code`() {
            val exists = categoryRepository.existsByCode(GeneratedCode("NONEX"))
            exists shouldBe false
        }
    }

    @Nested
    inner class ExistsByName {

        @Test
        fun `should return true for existing name`() {
            val exists = categoryRepository.existsByName("Smartphones")
            exists shouldBe true
        }

        @Test
        fun `should return false for non-existsing name`() {
            val exists = categoryRepository.existsByName("NonExistent")
            exists shouldBe false
        }

        @Test
        fun `should return false for existing name with different case`() {
            val exists = categoryRepository.existsByName("SMARTPHONES")
            exists shouldBe false
        }
    }

}