package GestionPlat.example.demo.modules.stock.service;

import GestionPlat.example.demo.modules.stock.dto.CategoryRequest;
import GestionPlat.example.demo.modules.stock.model.Category;

import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();
    List<Category> getRootCategories();
    Category createCategory(CategoryRequest request);
    Category createCategory(Category category);
}
