package edu.hcmut.datn.back_office_service.repository;

import edu.hcmut.datn.back_office_service.dto.response.BuyerUserDTO;
import edu.hcmut.datn.back_office_service.repository.projection.BuyerUserProjection;
import org.springframework.data.jpa.repository.JpaRepository;

import edu.hcmut.datn.back_office_service.dao.Buyer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BuyerRepository extends JpaRepository<Buyer, Long>{

    Boolean existsByUserId(Long userId);
    
    @Query(value = """
            SELECT
                B.BUYER_ID              AS buyerId,
                B.USER_ID               AS userId,
                B.UPDATED_AT            AS createdAt,
                B.CREATED_AT            AS updatedAt,
                B.LOYALTY_POINT         AS loyaltyPoint,
                B.TOTAL_ORDERS          AS totalOrders,
                B.TOTAL_SPENT_AMOUNT    AS totalSpentAmount,
                B.MEMBERSHIP_LEVEL      AS membershipLevel,
                U.EMAIL                 AS email,
                U.F_NAME                AS fName,
                U.L_NAME                AS lName,
                U.AVT_URL               AS avtUrl,
                U.DOB                   AS dob,
                U.P_NUM                 AS pNum,
                U.GENDER                AS gender,
                U.ACC_STATUS            AS accStatus
            FROM
                BUYERS B
                INNER JOIN USERS U ON B.USER_ID = U.USER_ID
            """, nativeQuery = true)
    List<BuyerUserProjection> getAllBuyerInfo();
    
    @Query(value = """
            SELECT
                B.BUYER_ID              AS buyerId,
                B.USER_ID               AS userId,
                B.UPDATED_AT            AS createdAt,
                B.CREATED_AT            AS updatedAt,
                B.LOYALTY_POINT         AS loyaltyPoint,
                B.TOTAL_ORDERS          AS totalOrders,
                B.TOTAL_SPENT_AMOUNT    AS totalSpentAmount,
                B.MEMBERSHIP_LEVEL      AS membershipLevel,
                U.EMAIL                 AS email,
                U.F_NAME                AS fName,
                U.L_NAME                AS lName,
                U.AVT_URL               AS avtUrl,
                U.DOB                   AS dob,
                U.P_NUM                 AS pNum,
                U.GENDER                AS gender,
                U.ACC_STATUS            AS accStatus
            FROM
                BUYERS B
                INNER JOIN USERS U ON B.USER_ID = U.USER_ID
            WHERE
                B.USER_ID = :userId
            """, nativeQuery = true)
    BuyerUserProjection getBuyerInfo(@Param("userId") Long userId);
}
