package ecommerce.ecommercevaldani.model;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ProductBranchKey implements Serializable {

    private Long productId;
    private Long branchId;

    public ProductBranchKey() {
    }

    public ProductBranchKey(Long productId, Long branchId) {
        this.productId = productId;
        this.branchId = branchId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductBranchKey)) return false;
        ProductBranchKey that = (ProductBranchKey) o;
        return Objects.equals(productId, that.productId) &&
                Objects.equals(branchId, that.branchId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, branchId);
    }
}


