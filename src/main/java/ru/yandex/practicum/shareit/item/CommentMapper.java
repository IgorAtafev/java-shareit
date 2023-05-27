package ru.yandex.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CommentMapper {

    public CommentForResponseDto toDto(Comment comment) {
        CommentForResponseDto commentDto = new CommentForResponseDto();

        commentDto.setId(comment.getId());
        commentDto.setText(comment.getText());
        commentDto.setAuthorName(comment.getAuthor().getName());
        commentDto.setCreated(comment.getCreated());

        return commentDto;
    }

    public List<CommentForResponseDto> toDtos(Collection<Comment> comments) {
        if (comments == null) {
            return null;
        }

        return  comments.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Comment toComment(CommentForCreateDto commentDto) {
        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        return comment;
    }
}
