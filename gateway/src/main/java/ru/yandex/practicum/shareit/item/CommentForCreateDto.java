package ru.yandex.practicum.shareit.item;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.shareit.validator.ValidationOnCreate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class CommentForCreateDto {

    @NotBlank(groups = ValidationOnCreate.class, message = "Text cannot be empty")
    @Size(
            groups = ValidationOnCreate.class, min = 2, max = 1000,
            message = "Text must contain at least 2 and no more than 1000 characters"
    )
    private String text;
}
