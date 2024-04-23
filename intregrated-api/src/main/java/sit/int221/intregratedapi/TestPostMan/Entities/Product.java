package sit.int221.intregratedapi.TestPostMan.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
//gfchvghstrdyiuo
@Entity
@Table(name="products")
@Data
public class Product {
    @Id
    private String productCode;
    private String productName;
    private String productLine;
    private String productScale;
    private String productVendor;
    private String productDescription;
    private Integer quantityInStock;
    @JsonIgnore
    private Double buyPrice;
    @Column(name = "msrp")
    private Double price;
}
