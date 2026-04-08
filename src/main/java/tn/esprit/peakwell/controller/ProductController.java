package tn.esprit.peakwell.controller;

import tn.esprit.peakwell.dto.ProductDTO;
import tn.esprit.peakwell.dto.ProductRequest;
import tn.esprit.peakwell.services.ProductService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

import tn.esprit.peakwell.services.DescriptionAPIService;

@RestController
@RequestMapping("/products")
@CrossOrigin("*")
public class ProductController {

    private final ProductService productService;
    private final DescriptionAPIService descriptionService;

    public ProductController(ProductService productService, DescriptionAPIService descriptionService) {
        this.productService = productService;
        this.descriptionService = descriptionService;
    }

    @PostMapping
    public ProductDTO addProduct(@Valid @RequestBody ProductRequest request) {
        return productService.addProduct(request);
    }

    @GetMapping
    public List<ProductDTO> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ProductDTO getProduct(@PathVariable Long id) {
        return productService.getProduct(id);
    }

    @PutMapping("/{id}")
    public ProductDTO updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {

        return productService.updateProduct(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }

    @PostMapping("/generate-description")
    public String generateDescription(@RequestParam String name) {

        return descriptionService.generateDescription(name, "");
    }

    @PostMapping("/{id}/consume")
    public void consumeStock(@PathVariable Long id, @RequestParam double quantity) {
        productService.consumeStock(id, quantity);
    }

    @GetMapping("/low-stock")
    public List<ProductDTO> getLowStockProducts() {
        return productService.getLowStockProducts();
    }

    @PostMapping("/{id}/restock")
    public void restock(@PathVariable Long id, @RequestParam double quantity) {
        productService.restock(id, quantity);
    }


}