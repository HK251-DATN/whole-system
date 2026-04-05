package edu.hcmut.datn.back_office_service.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import edu.hcmut.datn.back_office_service.dao.PaymentMethod;
import edu.hcmut.datn.back_office_service.exception.paymentmethod.PaymentMethodNotFoundException;
import edu.hcmut.datn.back_office_service.repository.PaymentMethodRepository;
import edu.hcmut.datn.back_office_service.service.PaymentMethodService;

@Service
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;

    public PaymentMethodServiceImpl(PaymentMethodRepository paymentMethodRepository) {
        this.paymentMethodRepository = paymentMethodRepository;
    }

    @Override
    public PaymentMethod create(PaymentMethod newPaymentMethod) {
        return paymentMethodRepository.save(newPaymentMethod);
    }

    @Override
    public PaymentMethod read(Long paymentMethodId) {
        return paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new PaymentMethodNotFoundException("Payment Method Not Found"));
    }

    @Override
    public List<PaymentMethod> readAll(Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

        Page<PaymentMethod> paymentMethods = paymentMethodRepository.findAll(pageable);

        return paymentMethods.toList();
    }

    @Override
    public PaymentMethod update(Long paymentMethodId, PaymentMethod updateMethod) {
        PaymentMethod curPaymentMethod = read(paymentMethodId);

        if (updateMethod.getPaymentProvider() != null) {
            curPaymentMethod.setPaymentProvider(updateMethod.getPaymentProvider());
        }

        if (updateMethod.getAccountNum() != null) {
            curPaymentMethod.setAccountNum(updateMethod.getAccountNum());
        }

        if (updateMethod.getIsDefault() != null && updateMethod.getIsDefault()) {
            curPaymentMethod.setIsDefault(true);

            List<PaymentMethod> buyerPaymentMethods = paymentMethodRepository
                    .findByBuyerId(curPaymentMethod.getBuyerId());

            for (PaymentMethod paymentMethod : buyerPaymentMethods) {
                if (!paymentMethod.getPaymentMethodId().equals(paymentMethodId)) {
                    paymentMethod.setIsDefault(false);
                }
            }

            paymentMethodRepository.saveAll(buyerPaymentMethods);
        }

        if (updateMethod.getIsActive() != null) {
            curPaymentMethod.setIsActive(updateMethod.getIsActive());
        }

        return paymentMethodRepository.save(curPaymentMethod);
    }

    @Override
    public void delete(Long paymentMethodId) {
        paymentMethodRepository.delete(read(paymentMethodId));
    }
}
