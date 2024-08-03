package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.service.FileService;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    FileController fileController;

    @Mock
    FileService mockFileService;

    @BeforeEach
    public void initServices() {
        fileController = new FileController(mockFileService);
    }

    @DisplayName("Получение существующего файла по его id")
    @Test
    void whenGetFileByIdThenReceiveIt() {
        FileDto fileDto = new FileDto("name", new byte[] {1, 2, 3});
        when(mockFileService.getFileById(anyInt())).thenReturn(Optional.of(fileDto));

        var view = fileController.getById(1);

        assertThat(view).isEqualTo(ResponseEntity.ok(Optional.of(fileDto).get().getContent()));
    }

    @DisplayName("Поиск несуществующего файла")
    @Test
    void whenGetNonExistingFileByIdThenEmptyOptional() {
        when(mockFileService.getFileById(anyInt())).thenReturn(Optional.empty());

        var view = fileController.getById(1);

        assertThat(view).isEqualTo(ResponseEntity.notFound().build());
    }

}