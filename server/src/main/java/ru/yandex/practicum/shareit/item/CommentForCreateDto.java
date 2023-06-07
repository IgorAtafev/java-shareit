package ru.yandex.practicum.shareit.item;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class CommentForCreateDto {

    private String text;
}
