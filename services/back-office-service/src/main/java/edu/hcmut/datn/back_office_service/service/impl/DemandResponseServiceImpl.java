package edu.hcmut.datn.back_office_service.service.impl;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import edu.hcmut.datn.back_office_service.dao.DemandResponse;
import edu.hcmut.datn.back_office_service.exception.demandresponse.DemandResponseNotFoundException;
import edu.hcmut.datn.back_office_service.repository.DemandResponseRepository;
import edu.hcmut.datn.back_office_service.service.DemandResponseService;

@Service
public class DemandResponseServiceImpl implements DemandResponseService {

    private final DemandResponseRepository demandResponseRepository;

    public DemandResponseServiceImpl(DemandResponseRepository demandResponseRepository) {
        this.demandResponseRepository = demandResponseRepository;
    }

    @Override
    public DemandResponse create(DemandResponse demandResponse) {
        return demandResponseRepository.save(demandResponse);
    }

    @Override
    public DemandResponse read(Long demandResponseId) {
        return demandResponseRepository.findById(demandResponseId).orElseThrow(() -> new DemandResponseNotFoundException("Demand Response not found"));
    }

    @Override
    public List<DemandResponse> readAll(Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

        return demandResponseRepository.findAll(pageable).toList();
    }

    @Override
    public DemandResponse update(Long demandResponseId, DemandResponse demandResponse) {
        DemandResponse curDemandResponse = read(demandResponseId);

        if (demandResponse.getStatus() != null) {
            curDemandResponse.setStatus(demandResponse.getStatus());
        }

        return demandResponseRepository.save(curDemandResponse);
    }

    @Override
    public void delete(Long demandResponseId) {
        DemandResponse demandResponse = read(demandResponseId);

        demandResponseRepository.delete(demandResponse);
    }

}
