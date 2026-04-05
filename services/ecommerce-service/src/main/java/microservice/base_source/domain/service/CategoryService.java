package microservice.base_source.domain.service;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import microservice.base_source.domain.entity.Category;
import microservice.base_source.domain.exception.type.CategoryNotFoundException;
import microservice.base_source.domain.use_case.CategoryUseCase;
import microservice.base_source.persistence.dto.CategoryDTO;
import microservice.base_source.persistence.repository.CategoryRepository;
import microservice.base_source.presentation.response.category.CategoryResponse;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService implements CategoryUseCase {
	@Autowired
	private CategoryRepository categoryRepository;

	@Override
	public List<CategoryResponse> getAll(Integer page, Integer size) {
		List<CategoryDTO> rows =
            categoryRepository.getAll();

		if (rows.isEmpty()) {
			return Collections.emptyList();
		}

		return rows.stream()
				.map(r -> new CategoryResponse(
					r.getParentId(),
					r.getParentName(),
					r.getParentDescription(),
					r.getParenticonUrl(),
					r.getParentDisplayOrder(),
					r.getSubId(),
					r.getSubName(),
					r.getSubparentDescription(),
					r.getSubiconUrl(),
					r.getSubDisplayOrder()
				))
				.toList();
	}

	@Override
	public Category get(Long id) {
		return categoryRepository.findById(id).orElseThrow(() -> new CategoryNotFoundException("Category not found"));
	}

//	@Override
//	public Category create(Category category) {
//		Category newCategory = categoryRepository.save(category);
//
//		CategoryCreatedEvent event = new CategoryCreatedEvent(
//				newCategory.getCategoryId(),
//				newCategory.getCategoryName(),
//				newCategory.getDescription()
//		);
//
//		categoryProducer.publishCategoryCreated(event);
//
//		return newCategory;
//	}

	@Override
	public Category update(Long id, Category category) {
		Category existingCategory = categoryRepository.findById(id)
				.orElseThrow(() -> new CategoryNotFoundException("Category not found"));
		// Update all fields 
		BeanUtils.copyProperties(category, existingCategory, "id");
		return categoryRepository.save(existingCategory);
	}

	@Override
	public void delete(Long id) {
		categoryRepository.findById(id)
			.ifPresentOrElse(
				categoryRepository::delete,
				() -> {}	
			);
	}

	@Override
	public List<Category> search(String searchName, Integer page, Integer size) {
		return categoryRepository.search(searchName, page, size);
	}
    
    @Override
    @Transactional
    public Category createFromEvent(Category category) {
        // This method is called by Kafka consumer
        // The category already has an ID assigned from back-office
        return categoryRepository.save(category);
    }
 
}
