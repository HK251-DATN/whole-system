package edu.hcmut.datn.back_office_service.service;

import java.util.List;

import edu.hcmut.datn.back_office_service.dao.PaymentMethod;

public interface PaymentMethodService {

    PaymentMethod create(PaymentMethod newPaymentMethod);

    PaymentMethod read(Long paymentMethodId);

    List<PaymentMethod> readAll(Integer pageNum, Integer pageSize);

    PaymentMethod update(Long paymentMethodId, PaymentMethod updateMethod);

    void delete(Long paymentMethodId);
}
