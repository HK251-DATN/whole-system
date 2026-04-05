package microservice.base_source.persistence.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface DetailGeneralDTO {
	// general info
	Long   		  getProductGeneralId();
	Long   		  getCategoryId();
	String 		  getProviderId();
	String 		  getName();
	String 		  getDescription();
	String 		  getImg();

	// batch detail info
	String 		  getBatchId();
	Integer 	  getQuantity();  // status AVAILABLE, OUT_OF_STOCK when quantity = 0
	BigDecimal 	  getOriginPrice();
	BigDecimal 	  getDisVal();    // discount percent, null if not in sale
	BigDecimal 	  getAvgRate();   // default: 0 
	int 		  getNumRate();	  // default: 0
	Long		  getSaleEventId();
	LocalDateTime getCreatedAt(); // sort ASC/DESC
}
