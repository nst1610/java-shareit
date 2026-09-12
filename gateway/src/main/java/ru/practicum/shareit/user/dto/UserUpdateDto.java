package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto {
    @Pattern(regexp = ".*\\S.*", message = "User name must not be blank")
    private String name;

    @Pattern(regexp = ".*\\S.*", message = "User email must not be blank")
    @Email(message = "User email must be valid")
    private String email;
}
