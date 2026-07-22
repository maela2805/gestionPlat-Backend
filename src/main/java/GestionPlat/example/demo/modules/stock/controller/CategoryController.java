package GestionPlat.example.demo.modules.stock.controller;

import GestionPlat.example.demo.modules.stock.model.Category;
import GestionPlat.example.demo.modules.stock.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock/categories")
@RequiredArgsConstructor
@Tag(name = "3. Gestion de Stock - Catégories", description = "Gestion des catégories arborescentes de plats/ingrédients")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Liste de toutes les catégories")
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @Operation(summary = "Liste des catégories racines (sans parent)")
    @GetMapping("/roots")
    public ResponseEntity<List<Category>> getRootCategories() {
        return ResponseEntity.ok(categoryService.getRootCategories());
    }

    @Operation(summary = "Créer une nouvelle catégorie")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_MANAGER', 'WRITE_STOCK')")
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        return ResponseEntity.ok(categoryService.createCategory(category));
    }
}
