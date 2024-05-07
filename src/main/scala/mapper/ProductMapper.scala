package mapper

import model.domain.Product
import model.dto.ProductDto

object ProductMapper {
  extension (product: Product) {
    def toProductDto: ProductDto = ProductDto(product.id, product.name, product.calories)
  }
}
