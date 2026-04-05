package edu.hcmut.datn.back_office_service.service;

import java.util.List;

import edu.hcmut.datn.back_office_service.dao.PreorderPolicy;

public interface PreorderPolicyService {
    PreorderPolicy create(PreorderPolicy policy);

    PreorderPolicy read(Long policyId);

    List<PreorderPolicy> readAll(Integer pageNum, Integer pageSize);

    PreorderPolicy update(Long policyId, PreorderPolicy policy);

    void delete(Long policyId);
}
