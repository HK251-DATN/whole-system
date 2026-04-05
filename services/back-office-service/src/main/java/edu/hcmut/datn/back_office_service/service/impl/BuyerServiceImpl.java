package edu.hcmut.datn.back_office_service.service.impl;

import java.util.List;

import edu.hcmut.datn.back_office_service.dto.response.BuyerUserDTO;
import edu.hcmut.datn.back_office_service.repository.projection.BuyerUserProjection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import edu.hcmut.datn.back_office_service.dao.Buyer;
import edu.hcmut.datn.back_office_service.exception.buyer.BuyerAlreadyExistsException;
import edu.hcmut.datn.back_office_service.exception.buyer.BuyerNotFoundException;
import edu.hcmut.datn.back_office_service.repository.BuyerRepository;
import edu.hcmut.datn.back_office_service.service.BuyerService;

@Service
public class BuyerServiceImpl implements BuyerService {

    private final BuyerRepository buyerRepository;

    public BuyerServiceImpl(BuyerRepository buyerRepository) {
        this.buyerRepository = buyerRepository;
    }

    @Override
    public Buyer create(Buyer buyer) {
        if (buyerRepository.existsById(buyer.getUserId())) {
            throw new BuyerAlreadyExistsException("This user id has already linked with one buyer account");
        }
        return buyerRepository.save(buyer);
    }

    @Override
    public Buyer read(Long buyerId) {
        return buyerRepository.findById(buyerId).orElseThrow(() -> new BuyerNotFoundException("Buyer not found"));
    }

    @Override
    public List<Buyer> readAll(Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

        return buyerRepository.findAll(pageable).toList();
    }

    @Override
    public Buyer update(Long buyerId, Buyer buyer) {
        Buyer curBuyer = read(buyerId);

        if (buyer.getLoyaltyPoint() != null) {
            curBuyer.setLoyaltyPoint(buyer.getLoyaltyPoint());
        }

        if (buyer.getTotalOrders() != null) {
            curBuyer.setTotalOrders(buyer.getTotalOrders());
        }

        if (buyer.getTotalSpentAmount() != null) {
            curBuyer.setTotalSpentAmount(buyer.getTotalSpentAmount());
        }

        if (buyer.getMembershipLevel() != null) {
            curBuyer.setMembershipLevel(buyer.getMembershipLevel());
        }

        return buyerRepository.save(curBuyer);
    }

    @Override
    public void delete(Long buyerId) {
        Buyer buyer = read(buyerId);

        buyerRepository.delete(buyer);
    }
    
    @Override
    public BuyerUserProjection readBuyerInfo (Long userId) {
        return buyerRepository.getBuyerInfo(userId);
    }
    
    @Override
    public List<BuyerUserProjection> readBuyersInfo () {
        return buyerRepository.getAllBuyerInfo();
    }
}
