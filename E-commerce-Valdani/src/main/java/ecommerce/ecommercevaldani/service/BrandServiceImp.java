package ecommerce.ecommercevaldani.service;

import ecommerce.ecommercevaldani.model.Branch;
import ecommerce.ecommercevaldani.model.Brand;
import ecommerce.ecommercevaldani.repository.BrandRepository;
import ecommerce.ecommercevaldani.request.CreateBrandRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BrandServiceImp implements BrandService {

    @Autowired
    private BrandRepository brandRepository;

    @Override
    public Brand createBrand(CreateBrandRequest brandRequest) {

        Brand brand = new Brand();
        brand.setName(brandRequest.getName());
        brand.setCreatedAt(LocalDateTime.now());

        return brandRepository.save(brand);
    }

    @Override
    public Brand updateBrand(Long brandId, CreateBrandRequest updatedBrand) {

        Brand brand = getBrandById(brandId);
        if(updatedBrand.getName() != null){
            brand.setName(updatedBrand.getName());
        }
        return brandRepository.save(brand);
    }

    @Override
    public void deleteBrand(Long brandId) {
        Brand brand = getBrandById(brandId);
        brandRepository.delete(brand);
    }

    @Override
    public Brand getBrandById(Long brandId) {
        Optional<Brand> brand = brandRepository.findById(brandId);
        if (brand.isEmpty()) {
            throw new RuntimeException("Brand not found with ID: " + brandId);
        }
        return brand.get();
    }

    @Override
    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }
}
