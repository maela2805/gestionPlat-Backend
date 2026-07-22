package GestionPlat.example.demo.modules.stock.service.impl;

import GestionPlat.example.demo.modules.stock.model.Category;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductServiceImplTest {

    @Test
    void shouldResolveToParentCategoryWhenAChildCategoryIsSelected() {
        ProductServiceImpl service = new ProductServiceImpl(null, null, null);

        Category parentCategory = Category.builder().id(1L).name("Boisson").build();
        Category childCategory = Category.builder().id(2L).name("Boisson gazeuse").parent(parentCategory).build();

        Category resolvedCategory = service.resolveEffectiveCategory(childCategory);

        assertEquals(parentCategory, resolvedCategory);
    }
}
