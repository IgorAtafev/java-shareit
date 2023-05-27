package ru.yandex.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CommentMapperTest {

    private LocalDateTime currentDateTime = LocalDateTime.of(2023, 5, 8, 12, 5);

    private final CommentMapper commentMapper = new CommentMapper();

    @Test
    void toDto_shouldReturnCommentForResponseDto() {
        Comment comment = initComment();

        CommentForResponseDto commentDto = commentMapper.toDto(comment);

        assertThat(commentDto.getId()).isEqualTo(1L);
        assertThat(commentDto.getText()).isEqualTo("Комментарий пользователя");
        assertThat(commentDto.getAuthorName()).isEqualTo("Автор");
        assertThat(commentDto.getCreated()).isEqualTo(currentDateTime);
    }

    @Test
    void toDtos_shouldReturnEmptyListOfCommentForResponseDtos() {
        assertThat(commentMapper.toDtos(Collections.emptyList())).isEmpty();
    }

    @Test
    void toDtos_shouldReturnListOfCommentForResponseDtos() {
        Comment comment1 = initComment();
        Comment comment2 = initComment();
        CommentForResponseDto commentDto1 = initCommentForResponseDto();
        CommentForResponseDto commentDto2 = initCommentForResponseDto();

        List<CommentForResponseDto> expected = List.of(commentDto1, commentDto2);

        assertThat(commentMapper.toDtos(List.of(comment1, comment2))).isEqualTo(expected);
    }

    @Test
    void toComment_shouldReturnComment() {
        CommentForCreateDto commentDto = initCommentForCreateDto();
        Comment comment = commentMapper.toComment(commentDto);
        assertThat(comment.getText()).isEqualTo("Комментарий пользователя");
    }

    private CommentForResponseDto initCommentForResponseDto() {
        CommentForResponseDto commentDto = new CommentForResponseDto();

        commentDto.setId(1L);
        commentDto.setText("Комментарий пользователя");
        commentDto.setAuthorName("Автор");
        commentDto.setCreated(currentDateTime);

        return commentDto;
    }

    private CommentForCreateDto initCommentForCreateDto() {
        CommentForCreateDto commentDto = new CommentForCreateDto();
        commentDto.setText("Комментарий пользователя");
        return commentDto;
    }

    private Comment initComment() {
        Comment comment = new Comment();

        comment.setId(1L);
        comment.setText("Комментарий пользователя");
        comment.setItem(new Item());
        comment.setAuthor(new User());
        comment.getAuthor().setName("Автор");
        comment.setCreated(currentDateTime);

        return comment;
    }
}
