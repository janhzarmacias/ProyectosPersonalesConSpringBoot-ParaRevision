package ecommerce.ecommercevaldani.service;

import ecommerce.ecommercevaldani.model.Brand;
import ecommerce.ecommercevaldani.request.CreateBrandRequest;

import java.util.List;

public interface BrandService {

    public Brand createBrand(CreateBrandRequest brandRequest);
    public Brand updateBrand(Long brandId, CreateBrandRequest updatedBrand);
    public void deleteBrand(Long brandId);
    public Brand getBrandById(Long brandId);
    public List<Brand> getAllBrands();

}
