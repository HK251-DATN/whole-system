package edu.hcmut.datn.back_office_service.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import edu.hcmut.datn.back_office_service.dao.SaleEvent;
import edu.hcmut.datn.back_office_service.exception.saleevent.SaleEventNotFoundException;
import edu.hcmut.datn.back_office_service.repository.SaleEventRepository;
import edu.hcmut.datn.back_office_service.service.SaleEventService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class SaleEventServiceImpl implements SaleEventService {

    private final SaleEventRepository saleEventRepository;

    @Override
    public SaleEvent create(SaleEvent saleEvent) {
        return saleEventRepository.save(saleEvent);
    }

    @Override
    public SaleEvent read(Long saleEventId) {
        return saleEventRepository.findById(saleEventId).orElseThrow(
                () -> new SaleEventNotFoundException("Sale event not found"));
    }

    @Override
    public List<SaleEvent> readAll(Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<SaleEvent> page = saleEventRepository.findAll(pageable);

        return page.toList();
    }

    @Override
    public SaleEvent update(Long saleEventId, SaleEvent saleEvent) {
        SaleEvent curSaleEvent = read(saleEventId);

        if (saleEvent.getName() != null) {
            curSaleEvent.setName(saleEvent.getName());
        }

        if (saleEvent.getDescription() != null) {
            curSaleEvent.setDescription(saleEvent.getDescription());
        }

        if (saleEvent.getImg() != null) {
            curSaleEvent.setImg(saleEvent.getImg());
        }

        if (saleEvent.getDisplayPriority() != null) {
            curSaleEvent.setDisplayPriority(saleEvent.getDisplayPriority());
        }

        if (saleEvent.getIsActive() != null) {
            curSaleEvent.setIsActive(saleEvent.getIsActive());
        }

        if (saleEvent.getBeginDate() != null) {
            curSaleEvent.setBeginDate(saleEvent.getBeginDate());
        }

        if (saleEvent.getEndDate() != null) {
            curSaleEvent.setEndDate(saleEvent.getEndDate());
        }

        if (saleEvent.getBeginTime() != null) {
            curSaleEvent.setBeginTime(saleEvent.getBeginTime());
        }

        if (saleEvent.getEndTime() != null) {
            curSaleEvent.setEndTime(saleEvent.getEndTime());
        }

        return saleEventRepository.save(curSaleEvent);
    }

    @Override
    public void delete(Long saleEventId) {
        SaleEvent event = read(saleEventId);

        saleEventRepository.delete(event);
    }

}
