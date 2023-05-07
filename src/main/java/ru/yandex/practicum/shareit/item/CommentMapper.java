package ru.yandex.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.shareit.user.UserService;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CommentMapper {

    private final ItemService itemService;
    private final UserService userService;

    public CommentForResponseDto toCommentDto(Comment comment) {
        CommentForResponseDto commentDto = new CommentForResponseDto();

        commentDto.setId(comment.getId());
        commentDto.setText(comment.getText());
        commentDto.setAuthorName(comment.getAuthor().getName());
        commentDto.setCreated(comment.getCreated());

        return commentDto;
    }

    public List<CommentForResponseDto> toCommentDto(Collection<Comment> comments) {
        return  comments.stream()
                .map(this::toCommentDto)
                .collect(Collectors.toList());
    }

    public Comment toComment(CommentForCreateDto commentDto, Long itemId, Long authorId) {
        Comment comment = new Comment();

        comment.setText(commentDto.getText());
        comment.setItem(itemService.getItemById(itemId));
        comment.setAuthor(userService.getUserById(authorId));

        return comment;
    }
}
