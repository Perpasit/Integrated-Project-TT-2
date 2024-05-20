package sit.int221.integratedapi.TestPostMan.Models;

import lombok.Getter;
import lombok.Setter;
import sit.int221.integratedapi.TestPostMan.Entities.Product;

import java.util.List;

@Getter
@Setter
public class ProductPage {
    private List<Product> productList;
    private int pageNumber;
    private int pageSize;
    private int totalElements;
    private int totalPages;
}
