package ru.yandex.practicum.shareit.item;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.shareit.user.User;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Item {

    private Long id;

    private String name;

    private String description;

    private Boolean available;

    private User owner;
}
