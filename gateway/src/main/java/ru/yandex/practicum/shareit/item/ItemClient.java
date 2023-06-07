package ru.yandex.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.yandex.practicum.shareit.client.BaseClient;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {

    @Autowired
    public ItemClient(@Value("${shareit_server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + "/items"))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> getItemsByUserId(Long userId, Map<String, Object> parameters) {
        return get("?from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> getItemById(Long userId, Long id) {
        return get("/" + id, userId);
    }

    public ResponseEntity<Object> createItem(Long userId, ItemDto itemDto) {
        return post("", userId, itemDto);
    }

    public ResponseEntity<Object> updateItemById(Long userId, Long id, ItemDto itemDto) {
        return patch("/" + id, userId, itemDto);
    }

    public ResponseEntity<Object> searchItems(Map<String, Object> parameters) {
        return get("/search?text={text}&from={from}&size={size}", parameters);
    }

    public ResponseEntity<Object> createComment(Long userId, Long id, CommentForCreateDto commentDto) {
        return post("/" + id + "/comment", userId, commentDto);
    }
}
