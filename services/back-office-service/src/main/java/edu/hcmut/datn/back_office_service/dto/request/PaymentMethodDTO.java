package edu.hcmut.datn.back_office_service.dto.request;

import edu.hcmut.datn.back_office_service.common.enums.PaymentProvider;
import edu.hcmut.datn.back_office_service.common.enums.PaymentType;
import edu.hcmut.datn.back_office_service.dao.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodDTO {

    private PaymentType paymentType;
    private PaymentProvider paymentProvider;
    private String accountNum;
    private Boolean isActive;
    private Boolean isDefault;
    private Long buyerId;

    public PaymentMethod toEntity() {
        PaymentMethod paymentMethod = new PaymentMethod();

        if (paymentType != null) {
            paymentMethod.setPaymentType(paymentType);
        }

        if (paymentProvider != null) {
            paymentMethod.setPaymentProvider(paymentProvider);
        }

        if (accountNum != null) {
            paymentMethod.setAccountNum(accountNum);
        }

        if (isDefault != null) {
            paymentMethod.setIsDefault(isDefault);
        }

        if (isActive != null) {
            paymentMethod.setIsActive(isActive);
        }

        if (buyerId != null) {
            paymentMethod.setBuyerId(buyerId);
        }

        return paymentMethod;
    }
}
