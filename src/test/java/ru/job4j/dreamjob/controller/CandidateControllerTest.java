package ru.job4j.dreamjob.controller;

import static java.time.LocalDateTime.now;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.Candidate;
import ru.job4j.dreamjob.model.City;
import ru.job4j.dreamjob.service.CandidateService;
import ru.job4j.dreamjob.service.CityService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandidateControllerTest {
    @Mock
    private CandidateService mockCandidateService;

    @Mock
    private CityService mockCityService;

    @Mock
    private MultipartFile mockTestFile;

    @Mock
    private Candidate mockCandidate;

    private CandidateController candidateController;

    @BeforeEach
    public void initServices() {
        candidateController
                = new CandidateController(mockCandidateService, mockCityService);
    }

    @DisplayName("Получаем страницу со списком кандидатов")
    @Test
    void whenRequestCandidatesPageThenReceiveAllCandidates() {
        var candidate1 = new Candidate(1, "test1", "description1", now(), 1, 1);
        var candidate2 = new Candidate(2, "test2", "description2", now(), 2, 2);
        var expectedCandidates = List.of(candidate1, candidate2);
        when(mockCandidateService.findAll()).thenReturn(expectedCandidates);

        var model = new ConcurrentModel();
        var view = candidateController.getAll(model);
        var actualCandidates = model.getAttribute("candidates");

        assertThat(view).isEqualTo("candidates/list");
        assertThat(actualCandidates).isEqualTo(expectedCandidates);
    }

    @DisplayName("Получаем список городов при переходе на страницу создания резюме")
    @Test
    void whenRequestCandidateCreationPageThenGetPageWithCities() {
        var city1 = new City(1, "Москва");
        var city2 = new City(2, "Санкт-Петербург");
        var expectedCities = List.of(city1, city2);
        when(mockCityService.findAll()).thenReturn(expectedCities);

        var model = new ConcurrentModel();
        var view = candidateController.getCreationPage(model);
        var actualCities = model.getAttribute("cities");

        assertThat(view).isEqualTo("candidates/create");
        assertThat(actualCities).isEqualTo(expectedCities);
    }

    @DisplayName("При сохранении кандидата переходим на страницу с кандидатами")
    @Test
    void whenPostCandidateWithFileThenSameDataAndRedirectToCandidatesPage() {
        var candidate = new Candidate(1, "test1", "description1", now(), 1, 1);

        when(mockCandidateService.save(any(Candidate.class), any(FileDto.class))).thenReturn(candidate);

        var model = new ConcurrentModel();
        var view = candidateController.create(candidate, mockTestFile, model);

        assertThat(view).isEqualTo("redirect:/candidates");
    }

    @DisplayName("Переход на страницу резюме по клику на его id")
    @Test
    void whenGetCandidateByIdThenReceiveTheCandidatePage() {
        var candidate = new Candidate(1, "test1", "description1", now(), 1, 1);
        when(mockCandidateService.findById(anyInt())).thenReturn(Optional.of(candidate));

        var model = new ConcurrentModel();
        var view = candidateController.getById(model, 1);
        var actualCandidate = model.getAttribute("candidate");

        assertThat(view).isEqualTo("candidates/one");
        assertThat(actualCandidate).isEqualTo(candidate);
    }

    @DisplayName("Обновление резюме, перенаправление на страницу с резюме")
    @Test
    void whenUpdateCandidateThenRedirectedToAllCandidatesPage() {
        var candidate = new Candidate(1, "test1", "description1", now(), 1, 1);
        when(mockCandidateService.update(any(Candidate.class), any(FileDto.class)))
                .thenReturn(true);

        var model = new ConcurrentModel();
        var view = candidateController.update(candidate, mockTestFile, model);

        assertThat(view).isEqualTo("redirect:/candidates");
    }

    @DisplayName("Удаление резюме, перенаправление на страницу с резюме")
    @Test
    void whenDeleteACandidateThenRedirectedToAllCandidatesPage() {
        var model = new ConcurrentModel();
        var view = candidateController.delete(model, anyInt());

        assertThat(view).isEqualTo("redirect:/candidates");
    }

    @DisplayName("Ошибка 404 при сохранении резюме")
    @Test
    void whenFailToSaveCandidateThenReceiveError() {
        when(mockCandidateService.save(any(Candidate.class), any(FileDto.class)))
                .thenThrow(new RuntimeException());

        var model = new ConcurrentModel();
        var view = candidateController.create(mockCandidate, mockTestFile, model);

        assertThat(view).isEqualTo("errors/404");
    }

    @DisplayName("Ошибка 404 при поиске резюме по несущствующему id")
    @Test
    void whenLookUpForNonExistingResumeIdThenReceiveError() {
        when(mockCandidateService.findById(anyInt())).thenReturn(Optional.empty());
        var expectedException = new RuntimeException("Резюме с указанным идентификатором не найдено");

        var model = new ConcurrentModel();
        var view = candidateController.getById(model, 1);
        var actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

    @DisplayName("Ошибка 404 при обновлении резюме")
    @Test
    void whenUpdateResumeWithNonExistingIdThenReceiveError() {
        when(mockCandidateService.update(any(Candidate.class), any(FileDto.class)))
                .thenReturn(false);
        var expectedException = new RuntimeException("Резюме с указанным идентификатором не найдено");

        var model = new ConcurrentModel();
        var view = candidateController.update(mockCandidate, mockTestFile, model);
        var actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }
}