package com.travelnest.user.dto;

import com.travelnest.user.entity.Gender;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Size(max = 150)
    private String fullName;

    @Size(max = 20)
    private String phone;

    @Size(max = 500)
    private String avatarUrl;

    private LocalDate dateOfBirth;

    private Gender gender;

    @Size(max = 5)
    private String preferredLang;
}
