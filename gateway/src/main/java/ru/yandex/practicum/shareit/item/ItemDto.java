package ru.yandex.practicum.shareit.item;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.shareit.booking.BookingForItemsDto;
import ru.yandex.practicum.shareit.validator.ValidationOnCreate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class ItemDto {

    @Null(groups = ValidationOnCreate.class, message = "Id must be null")
    private Long id;

    @NotBlank(groups = ValidationOnCreate.class, message = "Name cannot be empty")
    @Size(min = 2, max = 100, message = "Name must contain at least 2 and no more than 100 characters")
    private String name;

    @NotBlank(groups = ValidationOnCreate.class, message = "Description cannot be empty")
    @Size(min = 2, max = 200, message = "Description must contain at least 2 and no more than 200 characters")
    private String description;

    @NotNull(groups = ValidationOnCreate.class, message = "Available cannot be empty")
    private Boolean available;

    @Null(groups = ValidationOnCreate.class, message = "Last booking must be null")
    private BookingForItemsDto lastBooking;

    @Null(groups = ValidationOnCreate.class, message = "Next booking must be null")
    private BookingForItemsDto nextBooking;

    @Null(groups = ValidationOnCreate.class, message = "Comments must be null")
    private List<CommentForResponseDto> comments;

    private Long requestId;
}
