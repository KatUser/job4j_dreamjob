package ru.job4j.dreamjob.controller;

import static java.time.LocalDateTime.now;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.City;
import ru.job4j.dreamjob.model.Vacancy;
import ru.job4j.dreamjob.service.CityService;
import ru.job4j.dreamjob.service.VacancyService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VacancyControllerTest {

    @Mock
    private VacancyService mockVacancyService;

    @Mock
    private CityService mockCityService;

    @Mock
    private MultipartFile mockTestFile;

    private VacancyController vacancyController;

    @BeforeEach
    public void initServices() {
        vacancyController = new VacancyController(mockVacancyService, mockCityService);
    }

    @DisplayName("Переход на страницу с вакансиями на запрос страницы вакансий")
    @Test
    public void whenRequestVacancyListPageThenGetPageWithVacancies() {
        var vacancy1 = new Vacancy(1, "test1", "desc1", now(), true, 1, 2);
        var vacancy2 = new Vacancy(2, "test2", "desc2", now(), false, 3, 4);
        var expectedVacancies = List.of(vacancy1, vacancy2);
        when(mockVacancyService.findAll()).thenReturn(expectedVacancies);

        var model = new ConcurrentModel();
        var view = vacancyController.getAll(model);
        var actualVacancies = model.getAttribute("vacancies");

        assertThat(view).isEqualTo("vacancies/list");
        assertThat(actualVacancies).isEqualTo(expectedVacancies);
    }

    @DisplayName("Получаем список город на странице создания вакансии")
    @Test
    public void whenRequestVacancyCreationPageThenGetPageWithCities() {
        var city1 = new City(1, "Москва");
        var city2 = new City(2, "Санкт-Петербург");
        var expectedCities = List.of(city1, city2);
        when(mockCityService.findAll()).thenReturn(expectedCities);

        var model = new ConcurrentModel();
        var view = vacancyController.getCreationPage(model);
        var actualCities = model.getAttribute("cities");

        assertThat(view).isEqualTo("vacancies/create");
        assertThat(actualCities).isEqualTo(expectedCities);
    }

    @DisplayName("Ошибка 404 при сохранении вакансии")
    @Test
    public void whenSomeExceptionThrownThenGetErrorPageWithMessage() {
        var expectedException = new RuntimeException("Failed to write file");
        when(mockVacancyService.save(any(Vacancy.class), any(FileDto.class))).thenThrow(expectedException);

        var model = new ConcurrentModel();
        var view = vacancyController.create(new Vacancy(), mockTestFile, model);
        var actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

    @DisplayName("Сохранение вакансии с нужным файлом и перенаправление на страницу с вакансиями")
    @Test
    public void whenPostVacancyWithFileThenSameDataAndRedirectToVacanciesPage() throws Exception {
        var vacancy = new Vacancy(1, "test1", "desc1", now(), true, 1, 2);
        var fileDto = new FileDto(mockTestFile.getOriginalFilename(), mockTestFile.getBytes());
        var vacancyArgumentCaptor = ArgumentCaptor.forClass(Vacancy.class);
        var fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(mockVacancyService.save(vacancyArgumentCaptor.capture(),
                fileDtoArgumentCaptor.capture())).thenReturn(vacancy);

        var model = new ConcurrentModel();
        var view = vacancyController.create(vacancy, mockTestFile, model);
        var actualVacancy = vacancyArgumentCaptor.getValue();
        var actualFileDto = fileDtoArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/vacancies");
        assertThat(actualVacancy).isEqualTo(vacancy);
        assertThat(fileDto).usingRecursiveComparison().isEqualTo(actualFileDto);
    }

    @DisplayName("Переход на страницу вакансии при запросе этой страницы по id")
    @Test
    public void whenGetVacancyByIdThenReceiveItsPage() {
        var vacancy = new Vacancy(1, "vacancy1", "desc1", now(), true, 1, 1);
        when(mockVacancyService.findById(1)).thenReturn(Optional.of(vacancy));

        var model = new ConcurrentModel();
        var view = vacancyController.getById(model, 1);
        var actualVacancy = model.getAttribute("vacancy");

        assertThat(view).isEqualTo("vacancies/one");
        assertThat(actualVacancy).isEqualTo(vacancy);
        verify(mockVacancyService, Mockito.times(1)).findById(Mockito.anyInt());
    }

    @DisplayName("Перенаправление на страницу с вакансиями при сохранении вакансии")
    @Test
    public void whenCreateVacancyThenRedirectedToAllVacanciesPage() {
        var vacancy = new Vacancy(1, "test1", "desc1", now(), true, 1, 2);
        when(mockVacancyService.save(any(Vacancy.class), any(FileDto.class))).thenReturn(vacancy);

        var model = new ConcurrentModel();
        var view = vacancyController.create(vacancy, mockTestFile, model);

        assertThat(view).isEqualTo("redirect:/vacancies");
    }

    @DisplayName("Перенаправление на страницу с вакансиями при удалении вакансии")
    @Test
    public void whenDeleteVacancyThenRedirectedToAllVacanciesPage() {
        var model = new ConcurrentModel();
        var view = vacancyController.delete(model, anyInt());

        assertThat(view).isEqualTo("redirect:/vacancies");
    }

    @DisplayName("Перенаправление на страницу с вакансиями при обновлении вакансии")
    @Test
    public void whenUpdateVacancyThenRedirectedToAllVacanciesPage() {
        var vacancy1 = new Vacancy(1, "test1", "desc1", now(), true, 1, 2);

        when(mockVacancyService.update(any(Vacancy.class), any(FileDto.class))).thenReturn(true);

        var model = new ConcurrentModel();
        var view = vacancyController.update(vacancy1, mockTestFile, model);

        assertThat(view).isEqualTo("redirect:/vacancies");
    }

    @DisplayName("Ошибка 404 при создании вакансии")
    @Test
    public void whenCreateVacancyThenGetError() {
        var vacancy1 = new Vacancy(1, "test1", "desc1", now(), true, 1, 2);
        when(mockVacancyService.save(Mockito.any(Vacancy.class), Mockito.any(FileDto.class)))
                .thenThrow(new RuntimeException());

        var model = new ConcurrentModel();
        var view = vacancyController.create(vacancy1, mockTestFile, model);

        assertThat(view).isEqualTo("errors/404");
    }

    @DisplayName("Ошибка 404 при получении вакансии с некорректным id")
    @Test
    public void whenGetVacancyWithNonExistingIdThenReceiveError() {
        when(mockVacancyService.findById(anyInt())).thenReturn(Optional.empty());
        var expectedException = new RuntimeException("Вакансия с указанным идентификатором не найдена");

        var model = new ConcurrentModel();
        var view = vacancyController.getById(model, 1);
        var actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

    @DisplayName("Ошибка 404 при обновлении вакансии с некорректным id")
    @Test
    public void whenUpdateVacancyWithNonExistingIdThenReceiveError() {
        var vacancy1 = new Vacancy(1, "test1", "desc1", now(), true, 1, 2);
        var expectedException = new RuntimeException("Вакансия с указанным идентификатором не найдена");

        when(mockVacancyService.update(Mockito.any(Vacancy.class), Mockito.any(FileDto.class)))
                .thenReturn(false);

        var model = new ConcurrentModel();
        var view = vacancyController.update(vacancy1, mockTestFile, model);
        var actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }
}