package ru.yandex.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ru.yandex.practicum.shareit.user.User;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ItemRepository itemRepository;

    private PageRequest page;
    private User user;
    private Item item1;
    private Item item2;
    private Item item3;

    @BeforeEach
    void setUp() {
        page = PageRequest.of(0, 20);
        user = initUser();
        item1 = initItem();
        item2 = initItem();
        item3 = initItem();

        item1.setOwner(user);
        item2.setOwner(user);
        item3.setOwner(user);

        entityManager.persist(user);
    }

    @Test
    void searchItemsByText_shouldReturnEmptyListOfItems() {
        String text = "аккумулятор";
        Page<Item> items = itemRepository.searchItemsByText(text, page);
        assertThat(items.getContent()).isEmpty();
    }

    @Test
    void searchItems_shouldReturnItems_ifTheSearchTextIsPresentInTheNameAndDescription() {
        String text = "дрель";

        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.persist(item3);

        Page<Item> items = itemRepository.searchItemsByText(text, page);

        assertThat(items.getContent()).hasSize(3).contains(item1, item2, item3);
    }

    @Test
    void searchItems_shouldReturnItems_ifTheSearchTextIsPresentOnlyInTheName() {
        String text = "дрель";

        item1.setName("Аккумулятор");
        item1.setDescription("Аккумулятор");
        item2.setDescription("Простая");

        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.persist(item3);

        Page<Item> items = itemRepository.searchItemsByText(text, page);

        assertThat(items.getContent()).hasSize(2).contains(item2, item3);
    }

    @Test
    void searchItems_shouldReturnItems_ifTheSearchTextIsPresentOnlyInTheDescription() {
        String text = "прост";

        item2.setName("Аккумулятор");
        item2.setDescription("Аккумулятор");

        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.persist(item3);

        Page<Item> items = itemRepository.searchItemsByText(text, page);

        assertThat(items.getContent()).hasSize(2).contains(item1, item3);
    }

    @Test
    void searchItems_shouldReturnEmptyListOfItems_ifItemsAreNotAvailable() {
        String text = "дрель";

        item1.setAvailable(false);
        item2.setAvailable(false);
        item3.setAvailable(false);

        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.persist(item3);

        Page<Item> items = itemRepository.searchItemsByText(text, page);

        assertThat(items.getContent()).isEmpty();
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
