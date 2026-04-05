package microservice.base_source.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import microservice.base_source.domain.entity.FeedBack;
import microservice.base_source.persistence.dto.FeedBackDTO;

@Repository
public interface FeedBackRepository extends JpaRepository<FeedBack, Long> {

	@Query(
		value = """
			WITH LIST_DETAIL AS (
				SELECT PRODUCT_DETAIL_ID, rating 
				FROM PRODUCT_DETAIL
				WHERE batch_id = :batchId
			),
			LIST_FEEDBACK AS (
				SELECT 	FB.BUYER_ID,
						FB.PRODUCT_DETAIL_ID,
						FB.CONTENT,
						FB.IMG,
						FB.DETAIL,
						FB.created_at
				FROM LIST_DETAIL LPD
				INNER JOIN FEED_BACK FB
					ON LPD.PRODUCT_DETAIL_ID = FB.PRODUCT_DETAIL_ID
			)
			SELECT 	B.NAME 					AS name,
					B.AVATAR 				AS avatar,
					B.ALIAS_NM 				AS aliasNm,
					LFB.BUYER_ID 			AS buyerId,
					LFB.PRODUCT_DETAIL_ID 	AS productDetailId,
					LFB.CONTENT 			AS content,
					LFB.IMG 				AS img,
					LFB.DETAIL 				AS detail,
					LFB.created_at 			AS createdAt
			FROM LIST_FEEDBACK LFB
			INNER JOIN BUYER B
				ON B.BUYER_ID = LFB.BUYER_ID
			LIMIT :size 
			OFFSET ((:page - 1) * :size);
			""",
		nativeQuery = true
	)
	List<FeedBackDTO> getByBatchId(Long batchId, Integer page, Integer size);
}