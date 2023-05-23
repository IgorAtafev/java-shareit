package ru.yandex.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.shareit.user.User;
import ru.yandex.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentMapperTest {

    private LocalDateTime currentDateTime;

    @Mock
    private ItemService itemService;

    @Mock
    private UserService userService;

    @InjectMocks
    private CommentMapper commentMapper;

    @BeforeEach
    void setUp() {
        currentDateTime = LocalDateTime.of(2023, 5, 8, 12, 5);
    }

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
        Long itemId = 1L;
        Long authorId = 2L;
        CommentForCreateDto commentDto = initCommentForCreateDto();

        Item item = initItem();
        item.setId(itemId);
        User author = initUser();
        author.setId(authorId);

        when(itemService.getItemById(itemId)).thenReturn(item);
        when(userService.getUserById(authorId)).thenReturn(author);

        Comment comment = commentMapper.toComment(commentDto, itemId, authorId);

        assertThat(comment.getText()).isEqualTo("Комментарий пользователя");
        assertThat(comment.getItem()).isEqualTo(item);
        assertThat(comment.getAuthor()).isEqualTo(author);

        verify(itemService, times(1)).getItemById(itemId);
        verify(userService, times(1)).getUserById(authorId);
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

    private Item initItem() {
        Item item = new Item();

        item.setName("Дрель");
        item.setDescription("Простая дрель");
        item.setAvailable(true);

        return item;
    }

    private User initUser() {
        User user = new User();

        user.setEmail("user@user.com");
        user.setName("user");

        return user;
    }
}
