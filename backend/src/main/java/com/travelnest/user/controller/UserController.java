package com.travelnest.user.controller;

import com.travelnest.common.api.ApiResponse;
import com.travelnest.security.AuthenticatedUser;
import com.travelnest.user.dto.AdminCreateStaffRequest;
import com.travelnest.user.dto.UpdateProfileRequest;
import com.travelnest.user.dto.UpdateUserStatusRequest;
import com.travelnest.user.dto.UserAddressRequest;
import com.travelnest.user.dto.UserAddressResponse;
import com.travelnest.user.dto.UserProfileResponse;
import com.travelnest.user.entity.UserStatus;
import com.travelnest.user.service.UserAddressService;
import com.travelnest.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Users")
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserAddressService userAddressService;

    public UserController(UserService userService, UserAddressService userAddressService) {
        this.userService = userService;
        this.userAddressService = userAddressService;
    }

    @GetMapping("/staff")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> getStaffAccounts(
            @RequestParam(required = false) UserStatus status
    ) {
        return ResponseEntity.ok(ApiResponse.success(userService.getStaffAccounts(status)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(userService.getCurrentUserProfile(authenticatedUser)));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Profile updated successfully",
                userService.updateCurrentUserProfile(authenticatedUser, request)
        ));
    }

    @GetMapping("/me/addresses")
    public ResponseEntity<ApiResponse<List<UserAddressResponse>>> getAddresses(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(userAddressService.getCurrentUserAddresses(authenticatedUser)));
    }

    @PostMapping("/me/addresses")
    public ResponseEntity<ApiResponse<UserAddressResponse>> createAddress(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody UserAddressRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Address created successfully",
                userAddressService.createCurrentUserAddress(authenticatedUser, request)
        ));
    }

    @PutMapping("/me/addresses/{addressId}")
    public ResponseEntity<ApiResponse<UserAddressResponse>> updateAddress(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long addressId,
            @Valid @RequestBody UserAddressRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Address updated successfully",
                userAddressService.updateCurrentUserAddress(authenticatedUser, addressId, request)
        ));
    }

    @PatchMapping("/me/addresses/{addressId}/default")
    public ResponseEntity<ApiResponse<UserAddressResponse>> setDefaultAddress(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long addressId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Default address updated successfully",
                userAddressService.setDefaultAddress(authenticatedUser, addressId)
        ));
    }

    @DeleteMapping("/me/addresses/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long addressId
    ) {
        userAddressService.deleteCurrentUserAddress(authenticatedUser, addressId);
        return ResponseEntity.ok(ApiResponse.success("Address deleted successfully", null));
    }

    @PostMapping("/staff")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> createStaff(
            @Valid @RequestBody AdminCreateStaffRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Staff account created successfully",
                userService.createStaff(request)
        ));
    }

    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "User status updated successfully",
                userService.updateUserStatus(userId, request)
        ));
    }
}
