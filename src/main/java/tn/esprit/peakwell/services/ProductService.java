package tn.esprit.peakwell.services;

import tn.esprit.peakwell.dto.ProductDTO;
import tn.esprit.peakwell.dto.ProductRequest;
import tn.esprit.peakwell.entities.Product;
import tn.esprit.peakwell.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    private Product mapToEntity(ProductRequest dto) {

        Product p = new Product();

        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setCalories(dto.getCalories());
        p.setProtein(dto.getProtein());
        p.setCarbs(dto.getCarbs());
        p.setFats(dto.getFats());
        p.setCategory(dto.getCategory());
        p.setAllergens(dto.getAllergens());
        p.setStock(dto.getStock());
        p.setUnit(dto.getUnit());
        p.setImage(dto.getImage());

        return p;
    }

    private ProductDTO mapToDTO(Product product) {

        ProductDTO dto = new ProductDTO();

        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setCalories(product.getCalories());
        dto.setProtein(product.getProtein());
        dto.setCarbs(product.getCarbs());
        dto.setFats(product.getFats());
        dto.setCategory(product.getCategory());
        dto.setAllergens(product.getAllergens());
        dto.setStock(product.getStock());
        dto.setUnit(product.getUnit());
        dto.setImage(product.getImage());

        return dto;
    }

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductDTO addProduct(ProductRequest request) {

        Product product = mapToEntity(request);

        Product saved = productRepository.save(product);

        return mapToDTO(saved);
    }

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public ProductDTO getProduct(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        return mapToDTO(product);
    }

    public ProductDTO updateProduct(Long id, ProductRequest request) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCalories(request.getCalories());
        product.setProtein(request.getProtein());
        product.setCarbs(request.getCarbs());
        product.setFats(request.getFats());
        product.setCategory(request.getCategory());
        product.setAllergens(request.getAllergens());
        product.setStock(request.getStock());
        product.setUnit(request.getUnit());
        product.setImage(request.getImage());

        Product updated = productRepository.save(product);

        return mapToDTO(updated);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }


}