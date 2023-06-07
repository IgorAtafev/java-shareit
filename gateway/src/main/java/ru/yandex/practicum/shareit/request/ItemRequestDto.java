package ru.yandex.practicum.shareit.request;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.shareit.item.ItemDto;
import ru.yandex.practicum.shareit.validator.ValidationOnCreate;
import ru.yandex.practicum.shareit.validator.ValidationOnUpdate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class ItemRequestDto {

    @Null(groups = ValidationOnCreate.class, message = "Id must be null")
    private Long id;

    @NotBlank(groups = {ValidationOnCreate.class, ValidationOnUpdate.class}, message = "Description cannot be empty")
    @Size(
            groups = {ValidationOnCreate.class, ValidationOnUpdate.class}, min = 2, max = 200,
            message = "Description must contain at least 2 and no more than 200 characters"
    )
    private String description;

    @Null(groups = ValidationOnCreate.class, message = "Date of creation must be null")
    private LocalDateTime created;

    private List<ItemDto> items = new ArrayList<>();
}
