package microservice.base_source.domain.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import microservice.base_source.domain.entity.SaleProduct;
import microservice.base_source.domain.exception.type.WarnException;
import microservice.base_source.domain.use_case.SaleProductUseCase;
import microservice.base_source.persistence.repository.SaleProductRepository;

@Service
public class SaleProductService implements SaleProductUseCase {
	@Autowired
	private SaleProductRepository saleProductRepository;

	@Override
	public List<SaleProduct> getAll(Integer page, Integer size) {
		Pageable pageable = PageRequest.of(page - 1, size);
		Page<SaleProduct> saleProductList = saleProductRepository.findAll(pageable);
		return saleProductList.getContent();
	}

	@Override
	public SaleProduct get(Long saleEventid, String batchId) {
		Optional<SaleProduct> existingSaleProduct = saleProductRepository.findOneByEventAndBatch(saleEventid, batchId);
		if (existingSaleProduct.isEmpty()) {
			throw new WarnException("SaleProduct not found");
		}
		return existingSaleProduct.get();
	}

	@Override
	public SaleProduct create(SaleProduct saleProduct) {
		if (saleProductRepository.existsByEventAndBatch(saleProduct.getSaleEventId(), saleProduct.getBatchId())) {
			throw new WarnException("SaleProduct already exists");
		}	
		return saleProductRepository.save(saleProduct);
	}

	@Override
	public SaleProduct update(Long saleEventid, String batchId, SaleProduct saleProduct) {
		Optional<SaleProduct> existingSaleProduct = saleProductRepository.findOneByEventAndBatch(saleProduct.getSaleEventId(), saleProduct.getBatchId());
		if (existingSaleProduct.isEmpty()) {
			throw new WarnException("SaleProduct not found");
		}
		// Update maxQty, maxBuy, disVal
		SaleProduct toUpdate = existingSaleProduct.get();
		toUpdate.setMaxQty(saleProduct.getMaxQty());
		toUpdate.setMaxBuy(saleProduct.getMaxBuy());
		toUpdate.setDisVal(saleProduct.getDisVal());
		return saleProductRepository.save(toUpdate);
	}

	@Override
	public void delete(Long saleEventid, String batchId) {
		Optional<SaleProduct> existingSaleProduct = saleProductRepository.findOneByEventAndBatch(saleEventid, batchId);
		if (existingSaleProduct.isEmpty()) {
			System.out.println("SaleProduct not found, skip delete");
		}
		saleProductRepository.delete(existingSaleProduct.get());
	}

	@Override
	public SaleProduct get(String batchId) {
		Optional<SaleProduct> existingSaleProduct = saleProductRepository.findOneByBatch(batchId);
		if (existingSaleProduct.isEmpty()) {
			throw new WarnException("SaleProduct not found");
		}
		return existingSaleProduct.get();
	}

}
