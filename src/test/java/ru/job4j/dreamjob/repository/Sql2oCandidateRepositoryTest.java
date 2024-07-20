package ru.job4j.dreamjob.repository;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.job4j.dreamjob.configuration.DatasourceConfiguration;
import ru.job4j.dreamjob.model.Candidate;
import ru.job4j.dreamjob.model.File;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.*;

class Sql2oCandidateRepositoryTest {
    private static Sql2oCandidateRepository sql2oCandidateRepository;
    private static Sql2oFileRepository sql2oFileRepository;
    private static File file;

    @BeforeAll
    public static void initRepositories() throws Exception {
        var properties = new Properties();
        try (var inputStream = Sql2oCandidateRepositoryTest.class.getClassLoader()
                .getResourceAsStream("connection.properties")) {
            properties.load(inputStream);
        }

        var url = properties.getProperty("datasource.url");
        var username = properties.getProperty("datasource.username");
        var password = properties.getProperty("datasource.password");

        var configuration = new DatasourceConfiguration();
        var datasource = configuration.connectionPool(url, username, password);
        var sql2o = configuration.databaseClient(datasource);

        sql2oCandidateRepository = new Sql2oCandidateRepository(sql2o);
        sql2oFileRepository = new Sql2oFileRepository(sql2o);

        file = new File("test", "test");
        sql2oFileRepository.save(file);
    }

    @AfterAll
    public static void deleteFile() {
        sql2oFileRepository.deleteById(file.getId());
    }

    @AfterEach
    public void clearCandidates() {
        var allCandidates = sql2oCandidateRepository.findAll();
        allCandidates.forEach(candidate -> sql2oCandidateRepository.deleteById(candidate.getId()));
    }

    @Test
    void whenSaveThenGetTheSame() {
        var creationDate = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        var candidate = sql2oCandidateRepository.save(
                new Candidate(1, "name", "description", creationDate, 1, file.getId())
        );
        var savedCandidate = sql2oCandidateRepository.findById(candidate.getId()).get();
        assertThat(savedCandidate).usingRecursiveComparison().isEqualTo(candidate);
    }

    @Test
    public void whenSaveSeveralThenGetAll() {
        var creationDate = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        var candidate1 = sql2oCandidateRepository.save(
                new Candidate(0, "name", "description", creationDate, 1, file.getId())
        );
        var candidate2 = sql2oCandidateRepository.save(
                new Candidate(0, "name", "description", creationDate, 2, file.getId())
        );
        var candidate3 = sql2oCandidateRepository.save(
                new Candidate(0, "name", "description", creationDate, 3, file.getId())
        );

        assertThat(sql2oCandidateRepository.findAll()).isEqualTo(
                List.of(candidate1, candidate2, candidate3)
        );
    }

    @Test
    public void whenSaveNothingThenGetNothing() {
        assertThat(sql2oCandidateRepository.findAll()).isEqualTo(emptyList());
        assertThat(sql2oCandidateRepository.findById(0)).isEqualTo(empty());
    }

    @Test
    public void whenDeletedByInvalidIdThenGetFalse() {
        assertThat(sql2oCandidateRepository.deleteById(0)).isFalse();
    }

    @Test
    public void whenUpdateThenGetUpdated() {
        var creationDate = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        var candidate = sql2oCandidateRepository.save(
                new Candidate(0, "name", "description", creationDate, 1, file.getId())
        );
        var updatedCandidate = new Candidate(
                candidate.getId(), "updated name", "update description", creationDate.plusDays(1), 1, file.getId()
        );
        var isUpdated = sql2oCandidateRepository.update(updatedCandidate);
        var savedCandidate = sql2oCandidateRepository.findById(updatedCandidate.getId()).get();
        assertThat(isUpdated).isTrue();
        assertThat(savedCandidate).usingRecursiveComparison().isEqualTo(updatedCandidate);
    }

    @Test
    public void whenUpdateNonExistingCandidateThenGetFalse() {
        var creationDate = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        var candidate = new Candidate(0, "name", "description", creationDate, 1, file.getId());
        var isUpdated = sql2oCandidateRepository.update(candidate);
        assertThat(isUpdated).isFalse();
    }
}