package GestionPlat.example.demo.modules.stock.service;

import GestionPlat.example.demo.modules.stock.model.Category;

import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();
    List<Category> getRootCategories();
    Category createCategory(Category category);
}
