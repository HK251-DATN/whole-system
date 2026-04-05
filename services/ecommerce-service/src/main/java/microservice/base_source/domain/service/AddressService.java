package microservice.base_source.domain.service;

import lombok.RequiredArgsConstructor;
import microservice.base_source.domain.entity.Address;
import microservice.base_source.domain.exception.type.NotFoundException;
import microservice.base_source.domain.use_case.AddressUseCase;
import microservice.base_source.persistence.repository.AddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService implements AddressUseCase {
    
    private final AddressRepository addressRepository;
    
    @Override
    @Transactional
    public Address create (Address address) {
        if (address.getIsDefault()) {
            // Set all other user's address isDefault to false
            List<Address> userAddrs = readBuyerAddresses(address.getBuyerId());
            
            userAddrs.forEach(addr -> addr.setIsDefault(false));
            
            addressRepository.saveAll(userAddrs);
        }
        
        return addressRepository.save(address);
    }
    
    @Override
    public Address read (Long addressId) {
        return addressRepository.findById(addressId).orElseThrow(
                () -> new RuntimeException("Not found address with id: " + addressId)
        );
    }
    
    @Override
    public List<Address> readBuyerAddresses (String buyerId) {
        return addressRepository.findByBuyerId(buyerId);
    }
    
    @Override
    @Transactional
    public Address update (Long addressId, Address address) {
        Address curAddr = read(addressId);
        
        if (address.getCommune() != null && !curAddr.getCommune().equals(address.getCommune())) {
            curAddr.setCommune(address.getCommune());
        }
        
        if (address.getDistrict() != null && !curAddr.getDistrict().equals(address.getDistrict())) {
            curAddr.setDistrict(address.getDistrict());
        }
        
        if (address.getProvince() != null && !curAddr.getProvince().equals(address.getProvince())) {
            curAddr.setProvince(address.getProvince());
        }
        
        if (address.getReceiverName() != null && !curAddr.getReceiverName().equals(address.getReceiverName())) {
            curAddr.setReceiverName(address.getReceiverName());
        }
        
        if (address.getReceiverPNum() != null && !curAddr.getReceiverPNum().equals(address.getReceiverPNum())) {
            curAddr.setReceiverPNum(address.getReceiverPNum());
        }
        
        if (address.getDetail() != null && !curAddr.getDetail().equals(address.getDetail())) {
            curAddr.setDetail(address.getDetail());
        }

        // Update is_default only if:
        // is_default is not null
        // AND the new is_default is true
        // AND the old is_default is false
        if (address.getIsDefault() != null && address.getIsDefault() && !curAddr.getIsDefault()) {
            // Set all other user's address isDefault to false
            List<Address> userAddrs = readBuyerAddresses(curAddr.getBuyerId());
            
            for (Address addr : userAddrs) {
                if (addr.getAddressId().equals(addressId))
                    continue;
                
                addr.setIsDefault(false);
            }
            
            addressRepository.saveAllAndFlush(userAddrs);
            
            // Set curAddr isDefault to true
            curAddr.setIsDefault(true);
        }
        
        return addressRepository.save(curAddr);
    }
    
    @Override
    public void delete (Long addressId) {
        Address addr = read(addressId);
        
        addressRepository.delete(addr);
    }
}
