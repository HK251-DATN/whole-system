package microservice.base_source.presentation.rest;

import lombok.RequiredArgsConstructor;
import microservice.base_source.domain.entity.Address;
import microservice.base_source.domain.use_case.AddressUseCase;
import microservice.base_source.infrastructure.security.AuthenticatedUser;
import microservice.base_source.presentation.request.AddressRequest;
import microservice.base_source.presentation.response.global.ApiResponse;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpResponse;
import java.util.List;

@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {
    
    private final AddressUseCase addressUseCase;
    
    @PostMapping
    public ResponseEntity<ApiResponse<Address>> create(
            @RequestBody AddressRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal
            ) {
        
        try {
            String buyerId = principal.getId().toString();
            
            Address newAddr = request.toEntity();
            newAddr.setBuyerId(buyerId);
            Address address = addressUseCase.create(newAddr);
            
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Create address success", address));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }
    
    @GetMapping("/{addressId}")
    public ResponseEntity<ApiResponse<Address>> read(
            @PathVariable Long addressId,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        
        try {
            String buyerId = principal.getId().toString();
            
            Address address = addressUseCase.read(addressId);
            
            if (!address.getBuyerId().equals(buyerId)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.ERROR(HttpStatus.UNAUTHORIZED.toString(), "Unauthorized access!", null));
            }
            
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Read address successfully", address));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<Address>>> readBuyerAddresses(
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        
        try {
            String buyerId = principal.getId().toString();
        
            List<Address> userAddresses = addressUseCase.readBuyerAddresses(buyerId);
            
            if (userAddresses.isEmpty()) {
                return ResponseEntity.ok()
                        .body(ApiResponse.SKIP_AS_GOOD(HttpStatus.OK.toString(), "This buyer doesn't have any address yet", null));
            }
            
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Get user addresses successfully", userAddresses));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
        
    }
    
    @PutMapping("/{addressId}")
    public ResponseEntity<ApiResponse<Address>> update(
            @PathVariable Long addressId,
            @RequestBody AddressRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        
        try {
            String buyerId = principal.getId().toString();
            
            Address existedRequest = addressUseCase.read(addressId);
            
            if (!existedRequest.getBuyerId().equals(buyerId)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.ERROR(HttpStatus.UNAUTHORIZED.toString(), "Unauthorized access!", null));
            }
            
            Address updatedRequest = addressUseCase.update(addressId, request.toEntity());
            
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Update address successfully", updatedRequest));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }
    
    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long addressId,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        
        try {
            String buyerId = principal.getId().toString();
            
            Address existedRequest = addressUseCase.read(addressId);
            
            if (!existedRequest.getBuyerId().equals(buyerId)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.ERROR(HttpStatus.UNAUTHORIZED.toString(), "Unauthorized access!", null));
            }
            
            addressUseCase.delete(addressId);
            
            return ResponseEntity.ok()
                    .body(ApiResponse.SUCCESS(HttpStatus.OK.toString(), "Delete address successfully", null));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.ERROR(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), null));
        }
    }
}
