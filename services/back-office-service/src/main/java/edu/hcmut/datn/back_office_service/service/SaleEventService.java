package edu.hcmut.datn.back_office_service.service;

import java.util.List;

import edu.hcmut.datn.back_office_service.dao.SaleEvent;

public interface SaleEventService {
    SaleEvent create(SaleEvent saleEvent);

    SaleEvent read(Long saleEventId);

    List<SaleEvent> readAll(Integer pageNum, Integer pageSize);

    SaleEvent update(Long saleEventId, SaleEvent saleEvent);

    void delete(Long saleEventId);
}
