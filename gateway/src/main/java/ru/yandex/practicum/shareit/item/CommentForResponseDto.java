package ru.yandex.practicum.shareit.item;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class CommentForResponseDto {

    private Long id;

    private String text;

    private String authorName;

    private LocalDateTime created;
}
