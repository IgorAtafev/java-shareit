package ru.yandex.practicum.shareit.user;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.shareit.validator.ValidationOnCreate;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class UserDto {

    @Null(message = "Id must be null")
    private Long id;

    @NotNull(groups = ValidationOnCreate.class, message = "Email cannot be empty")
    @Email(message = "Email must be valid")
    private String email;

    @NotNull(groups = ValidationOnCreate.class, message = "Name cannot be empty")
    @Pattern(regexp = "^\\S{2,50}$", message = "Name must contain at least 2 and no more than 500 characters")
    private String name;
}