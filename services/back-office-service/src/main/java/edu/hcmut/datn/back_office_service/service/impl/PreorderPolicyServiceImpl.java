package edu.hcmut.datn.back_office_service.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import edu.hcmut.datn.back_office_service.dao.PreorderPolicy;
import edu.hcmut.datn.back_office_service.exception.preorderpolicy.PreorderPolicyNotFoundException;
import edu.hcmut.datn.back_office_service.repository.PreorderPolicyRepository;
import edu.hcmut.datn.back_office_service.service.PreorderPolicyService;

@Service
public class PreorderPolicyServiceImpl implements PreorderPolicyService {

    private final PreorderPolicyRepository preorderPolicyRepository;

    public PreorderPolicyServiceImpl(PreorderPolicyRepository preorderPolicyRepository) {
        this.preorderPolicyRepository = preorderPolicyRepository;
    }

    @Override
    public PreorderPolicy create(PreorderPolicy policy) {
        return preorderPolicyRepository.save(policy);
    }

    @Override
    public PreorderPolicy read(Long policyId) {
        return preorderPolicyRepository.findById(policyId).orElseThrow(() -> new PreorderPolicyNotFoundException("Preorder Policy Not Found"));
    }

    @Override
    public List<PreorderPolicy> readAll(Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

        Page<PreorderPolicy> page = preorderPolicyRepository.findAll(pageable);

        return page.toList();
    }

    @Override
    public PreorderPolicy update(Long policyId, PreorderPolicy policy) {
        PreorderPolicy cur = read(policyId);

        if (policy.getIsActive() != null) {
            cur.setIsActive(policy.getIsActive());
        }

        if (policy.getRequirePayment() != null) {
            cur.setRequirePayment(policy.getRequirePayment());
        }

        if (policy.getDepositPercentage() != null) {
            cur.setDepositPercentage(policy.getDepositPercentage());
        }

        if (policy.getMinPreorderDay() != null) {
            cur.setMinPreorderDay(policy.getMinPreorderDay());
        }

        if (policy.getAllowCancel() != null) {
            cur.setAllowCancel(policy.getAllowCancel());
        }

        if (policy.getNotes() != null) {
            cur.setNotes(policy.getNotes());
        }

        if (policy.getCancelDeadline() != null) {
            cur.setCancelDeadline(policy.getCancelDeadline());
        }

        return preorderPolicyRepository.save(cur);
    }

    @Override
    public void delete(Long policyId) {
        preorderPolicyRepository.delete(read(policyId));
    }
}
