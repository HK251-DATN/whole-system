package edu.hcmut.datn.back_office_service.service;

import java.util.List;

import edu.hcmut.datn.back_office_service.dao.Buyer;
import edu.hcmut.datn.back_office_service.dto.response.BuyerUserDTO;
import edu.hcmut.datn.back_office_service.repository.projection.BuyerUserProjection;

public interface BuyerService {
    Buyer create(Buyer buyer);

    Buyer read(Long buyerId);

    List<Buyer> readAll(Integer pageNum, Integer pageSize);

    Buyer update(Long buyerId, Buyer buyer);

    void delete (Long buyerId);
    
    BuyerUserProjection readBuyerInfo(Long userId);
    
    List<BuyerUserProjection> readBuyersInfo();
}
