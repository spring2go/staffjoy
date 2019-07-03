package xyz.staffjoy.company.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.staffjoy.common.validation.PhoneNumber;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewDirectoryEntry {
    @NotBlank
    private String companyId;
    @Builder.Default
    private String name = "";
    @Email
    private String email;
    @PhoneNumber
    private String phoneNumber;
    @Builder.Default
    private String internalId = "";
}
