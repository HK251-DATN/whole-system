package edu.hcmut.datn.back_office_service.service;

import java.util.List;

import edu.hcmut.datn.back_office_service.dao.DemandResponse;

public interface DemandResponseService {

    DemandResponse create(DemandResponse demandResponse);

    DemandResponse read(Long demandResponseId);

    List<DemandResponse> readAll(Integer pageNum, Integer pageSize);

    DemandResponse update(Long demandResponseId, DemandResponse demandResponse);

    void delete(Long demandResponseId);
}
