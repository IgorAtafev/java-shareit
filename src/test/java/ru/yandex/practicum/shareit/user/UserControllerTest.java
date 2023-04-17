package ru.yandex.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.yandex.practicum.shareit.validator.ErrorHandler;
import ru.yandex.practicum.shareit.validator.NotFoundException;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController controller;

    @BeforeEach
    void setMockMvc() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ErrorHandler())
                .build();
    }

    @Test
    void getUsers_shouldReturnEmptyListOfUsers() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(userService, times(1)).getUsers();
    }

    @Test
    void getUsers_shouldReturnListOfUsers() throws Exception {
        UserDto userDto1 = initUserDto();
        UserDto userDto2 = initUserDto();
        User user1 = initUser();
        User user2 = initUser();

        List<User> expectedUser = List.of(user1, user2);
        List<UserDto> expectedUserDto = List.of(userDto1, userDto2);
        String json = objectMapper.writeValueAsString(expectedUserDto);

        when(userService.getUsers()).thenReturn(expectedUser);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().json(json));

        verify(userService, times(1)).getUsers();
    }

    @Test
    void getUserById_shouldReturnUserById() throws Exception {
        Long userId = 1L;
        UserDto userDto = initUserDto();
        User user = initUser();
        String json = objectMapper.writeValueAsString(userDto);

        when(userService.getUserById(userId)).thenReturn(user);

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(content().json(json));

        verify(userService, times(1)).getUserById(userId);
    }

    @ParameterizedTest
    @ValueSource(longs = {-1L, 0L, 999L})
    void getUserById_shouldResponseWithNotFound_ifUserDoesNotExist(Long userId) throws Exception {
        when(userService.getUserById(userId)).thenThrow(NotFoundException.class);

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void createUser_shouldResponseWithOk() throws Exception {
        UserDto userDto = initUserDto();
        User user = initUser();
        String json = objectMapper.writeValueAsString(userDto);

        when(userService.createUser(user)).thenReturn(user);

        mockMvc.perform(post("/users").contentType("application/json").content(json))
                .andExpect(status().isCreated());

        verify(userService, times(1)).createUser(user);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidUsers")
    void createUser_shouldResponseWithBadRequest_ifUserIsInvalid(UserDto userDto) throws Exception {
        String json = objectMapper.writeValueAsString(userDto);

        mockMvc.perform(post("/users").contentType("application/json").content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUserById_shouldResponseWithOk() throws Exception {
        Long userId = 1L;
        UserDto userDto = initUserDto();
        User user = initUser();
        userDto.setId(userId);
        user.setId(userId);
        String json = objectMapper.writeValueAsString(userDto);

        when(userService.updateUser(user)).thenReturn(user);

        mockMvc.perform(patch("/users/{id}", userId).contentType("application/json").content(json))
                .andExpect(status().isOk());

        verify(userService, times(1)).updateUser(user);
    }

    @ParameterizedTest
    @ValueSource(longs = {-1L, 0L, 999L})
    void updateUserById_shouldResponseWithNotFound_ifUserDoesNotExist(Long userId) throws Exception {
        UserDto userDto = initUserDto();
        User user = initUser();
        userDto.setId(userId);
        user.setId(userId);
        String json = objectMapper.writeValueAsString(userDto);

        when(userService.updateUser(user)).thenThrow(NotFoundException.class);

        mockMvc.perform(patch("/users/{id}", userId).contentType("application/json").content(json))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).updateUser(user);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidUsers")
    void updateUserById_shouldResponseWithBadRequest_ifUserIsInvalid(UserDto userDto) throws Exception {
        Long userId = 1L;
        userDto.setId(userId);
        String json = objectMapper.writeValueAsString(userDto);

        mockMvc.perform(patch("/users/{id}", userId).contentType("application/json").content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void removeUserById_shouldResponseWithOk() throws Exception {
        Long userId = 1L;

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isOk());

        verify(userService, times(1)).removeUserById(userId);
    }

    @ParameterizedTest
    @ValueSource(longs = {-1L, 0L, 999L})
    void removeUserById_shouldResponseWithNotFound_ifUserDoesNotExist(Long userId) throws Exception {
        doThrow(NotFoundException.class).when(userService).removeUserById(userId);

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).removeUserById(userId);
    }

    private static Stream<Arguments> provideInvalidUsers() {
        return Stream.of(
                Arguments.of(initUserDto(userDto -> userDto.setEmail("mail.ru"))),
                Arguments.of(initUserDto(userDto -> userDto.setName(""))),
                Arguments.of(initUserDto(userDto -> userDto.setName("  "))),
                Arguments.of(initUserDto(userDto -> userDto.setName("us er"))),
                Arguments.of(initUserDto(userDto -> userDto.setName("u"))),
                Arguments.of(initUserDto(userDto -> userDto.setName("userr".repeat(10) + "r")))
        );
    }

    private static UserDto initUserDto(Consumer<UserDto> consumer) {
        UserDto userDto = initUserDto();

        consumer.accept(userDto);

        return userDto;
    }

    private static UserDto initUserDto() {
        UserDto userDto = new UserDto();

        userDto.setEmail("user@user.com");
        userDto.setName("user");

        return userDto;
    }

    private static User initUser() {
        User user = new User();

        user.setEmail("user@user.com");
        user.setName("user");

        return user;
    }
}