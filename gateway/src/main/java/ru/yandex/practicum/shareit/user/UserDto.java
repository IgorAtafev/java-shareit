package ru.yandex.practicum.shareit.user;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.shareit.validator.ValidationOnCreate;
import ru.yandex.practicum.shareit.validator.ValidationOnUpdate;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class UserDto {

    @Null(groups = ValidationOnCreate.class, message = "Id must be null")
    private Long id;

    @NotEmpty(groups = ValidationOnCreate.class, message = "Email cannot be empty")
    @Email(groups = {ValidationOnCreate.class, ValidationOnUpdate.class}, message = "Email must be valid")
    private String email;

    @NotNull(groups = ValidationOnCreate.class, message = "Name cannot be empty")
    @Pattern(
            groups = {ValidationOnCreate.class, ValidationOnUpdate.class},
            regexp = "^\\S{2,50}$",
            message = "Name must contain at least 2 and no more than 50 characters"
    )
    private String name;
}
