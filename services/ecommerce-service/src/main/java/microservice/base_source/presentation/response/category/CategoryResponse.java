package microservice.base_source.presentation.response.category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse {
	private Long parentId;
    private String parentName;
    private String parentDescription;
    private String parentIconUrl;
    private Integer parentDisplayOrder;

    private Long subId;
    private String subName;
    private String subParentDescription;
    private String subIconUrl;
    private Integer subDisplayOrder;
}
