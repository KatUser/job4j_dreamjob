package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ConcurrentModel;
import ru.job4j.dreamjob.model.User;
import ru.job4j.dreamjob.service.UserService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService mockUserService;

    private UserController userController;

    @BeforeEach

    public void initServices() {
        userController = new UserController(mockUserService);
    }

    @DisplayName("Получаем страницу регистрации")
    @Test
    void whenGetRegistrationPageThenReceiveIt() {
        var view = userController.getRegistrationPage();
        assertThat(view).isEqualTo("users/register");
    }

    @DisplayName("Перенаправление на страницу с вакансиями при сохранении пользователя")
    @Test
    void whenSaveUserThenGetRedirectedToAllVacanciesPage() {
        var user = new User(1, "email", "name", "password");
        when(mockUserService.save(user)).thenReturn(Optional.of(user));

        var model = new ConcurrentModel();
        var view = userController.register(user, model);

        assertThat(view).isEqualTo("redirect:/vacancies");
    }

    @DisplayName("Ошибка при сохранении дубликата email")
    @Test
    void whenSaveUserWithAlreadyRegisteredEmailThenReceiveError() {
        var user = new User(1, "email", "name", "password");
        when(mockUserService.save(user)).thenReturn(Optional.empty());

        var model = new ConcurrentModel();
        var view = userController.register(user, model);

        assertThat(view).isEqualTo("errors/404");
        assertThat(model.getAttribute("message"))
                .isEqualTo("Пользователь с таким email уже существует");
    }

    @DisplayName("Страница входа в систему")
    @Test
    void whenGetLoginPage() {
        var view = userController.getLoginPage();

        assertThat(view).isEqualTo("users/login");
    }

    @DisplayName("Успешный вход с в систему")
    @Test
    void whenLoginTheGoToAllVacanciesPage() {
        var request = new MockHttpServletRequest();
        var user = new User(1, "email", "name", "password");
        when(mockUserService.findByEmailAndPassword(anyString(), anyString()))
                .thenReturn(Optional.of(user));

        var model = new ConcurrentModel();
        var view = userController.loginUser(user, model, request);
        var httpSession = request.getSession();

        assertThat(view).isEqualTo("redirect:/vacancies");
        assertThat(httpSession).isNotNull();
        assertThat(httpSession.getAttribute("user")).isEqualTo(user);
    }

    @DisplayName("Ошибка при входе в систему")
    @Test
    void whenFailToLoginThenReceiveError() {
        var request = new MockHttpServletRequest();
        var user = new User(1, "email", "name", "password");
        when(mockUserService.findByEmailAndPassword(anyString(), anyString()))
                .thenReturn(Optional.empty());

        var model = new ConcurrentModel();
        var view = userController.loginUser(user, model, request);
        var httpSession = request.getSession();

        assertThat(view).isEqualTo("users/login");
        assertThat(model.getAttribute("error"))
                .isEqualTo("Почта или пароль введены неверно");
        assertThat(httpSession).isNotNull();
        assertThat(httpSession.getAttribute("user")).isEqualTo(null);
    }
}