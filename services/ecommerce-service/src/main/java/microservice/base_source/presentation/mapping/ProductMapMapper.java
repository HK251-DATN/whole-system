// package microservice.base_source.presentation.mapping;

// import microservice.base_source.data_access.dto.DetailGeneralDTO;
// import microservice.base_source.presentation.response.searchProduct.ProductDetailResponse;
// import microservice.base_source.presentation.response.searchProduct.ProductGeneralResponse;
// import microservice.base_source.presentation.response.searchProduct.ProductSearchResponse;

// public class ProductMapMapper {
// 	public static ProductSearchResponse toFOSearchResult(DetailGeneralDTO dto) {
//         ProductDetailResponse detail = new ProductDetailResponse();
//         detail.setProductDetailId(dto.getProductDetailId());
//         detail.setDescription(dto.getDescription());
//         detail.setStatus(dto.getStatus());
//         detail.setQuantityAvailable(dto.getQuantityAvailable());
//         detail.setPrice(dto.getPrice());
//         detail.setRating(dto.getRating());
//         detail.setCreatedAt(dto.getCreatedAt());
//         // detail.setUpdatedAt(dto.getUpdatedAt());

//         ProductGeneralResponse general = new ProductGeneralResponse();
//         general.setProductGeneralId(dto.getProductGeneralId());
//         general.setCategoryId(dto.getCategoryId());
//         general.setProductName(dto.getProductName());
//         general.setDescription(dto.getGeneralDescription());
//         general.setStatus(dto.getGeneralStatus());
//         general.setPhotoUrls(dto.getPhotoUrls());

//         return new ProductSearchResponse(detail, general);
//     }
// }
