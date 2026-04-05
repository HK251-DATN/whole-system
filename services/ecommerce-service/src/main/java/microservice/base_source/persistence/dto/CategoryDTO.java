package microservice.base_source.persistence.dto;

public interface CategoryDTO {
	Long 	getParentId();
	String 	getParentName();
	String 	getParentDescription();
	String 	getParenticonUrl();
	Integer getParentDisplayOrder();

	// subCagetTegory
	Long 	getSubId();
	String 	getSubName();
	String 	getSubparentDescription();
	String 	getSubiconUrl();
	Integer getSubDisplayOrder();
}