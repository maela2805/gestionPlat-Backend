package GestionPlat.example.demo.modules.stock.service.impl;

import GestionPlat.example.demo.modules.stock.dto.CategoryRequest;
import GestionPlat.example.demo.modules.stock.model.Category;
import GestionPlat.example.demo.modules.stock.repository.CategoryRepository;
import GestionPlat.example.demo.modules.stock.repository.ProductRepository;
import GestionPlat.example.demo.modules.stock.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    public List<Category> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        for (Category cat : categories) {
            cat.setProductCount(computeRecursiveProductCount(cat));
        }
        return categories;
    }

    @Override
    public List<Category> getRootCategories() {
        List<Category> roots = categoryRepository.findByParentIsNull();
        for (Category cat : roots) {
            cat.setProductCount(computeRecursiveProductCount(cat));
        }
        return roots;
    }

    private int computeRecursiveProductCount(Category cat) {
        long directCount = productRepository.countByCategoryId(cat.getId());
        int total = (int) directCount;

        List<Category> children = categoryRepository.findByParentId(cat.getId());
        for (Category child : children) {
            total += computeRecursiveProductCount(child);
        }

        return total;
    }

    @Override
    public Category createCategory(CategoryRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new RuntimeException("Le nom de la catégorie est obligatoire.");
        }

        String name = request.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new RuntimeException("La catégorie '" + name + "' existe déjà !");
        }

        Category.CategoryBuilder builder = Category.builder()
                .name(name)
                .description(request.getDescription());

        Long parentId = request.getParentId();
        if (parentId == null && request.getParentCategory() != null) {
            parentId = request.getParentCategory().getId();
        }

        if (parentId != null) {
            final Long finalParentId = parentId;
            Category parent = categoryRepository.findById(finalParentId)
                    .orElseThrow(() -> new RuntimeException("Catégorie parente introuvable (ID: " + finalParentId + ")"));
            builder.parent(parent);
        }

        return categoryRepository.save(builder.build());
    }

    @Override
    public Category createCategory(Category category) {
        if (category.getName() == null || category.getName().isBlank()) {
            throw new RuntimeException("Le nom de la catégorie est obligatoire.");
        }

        String name = category.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new RuntimeException("La catégorie '" + name + "' existe déjà !");
        }

        category.setName(name);
        return categoryRepository.save(category);
    }
}
