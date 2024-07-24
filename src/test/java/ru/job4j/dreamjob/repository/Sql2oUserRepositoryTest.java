package ru.job4j.dreamjob.repository;

import static java.util.Optional.empty;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.job4j.dreamjob.configuration.DatasourceConfiguration;
import ru.job4j.dreamjob.model.User;

import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.*;

class Sql2oUserRepositoryTest {

    private static Sql2oUserRepository sql2oUserRepository;

    @BeforeAll
    public static void initRepository() throws Exception {
        var properties = new Properties();
        try (var inputStream = Sql2oUserRepository.class.getClassLoader()
                .getResourceAsStream("connection.properties")) {
            properties.load(inputStream);
        }
        var url = properties.getProperty("datasource.url");
        var username = properties.getProperty("datasource.username");
        var password = properties.getProperty("datasource.password");

        var configuration = new DatasourceConfiguration();
        var datasource = configuration.connectionPool(url, username, password);
        var sql2o = configuration.databaseClient(datasource);

        sql2oUserRepository = new Sql2oUserRepository(sql2o);
    }

    @AfterEach
    public void deleteUsers() {
        sql2oUserRepository.deleteAllUsers();
    }

    @Test
    public void whenSaveUserThenReceiveIt() {
        var user = new User(0, "u", "u", "u");
        sql2oUserRepository.save(user);
        var result = sql2oUserRepository.findByEmailAndPassword("u", "u");
        assertThat(result.get()).usingRecursiveComparison().isEqualTo(user);
    }

    @Test
    public void whenSaveUsersWithDifferentEmailsAndIdenticalNamesTheReceiveThem() {
        var user1 = new User(0, "u", "u", "p");
        var user2 = new User(0, "u2", "u", "r");
        sql2oUserRepository.save(user1);
        sql2oUserRepository.save(user2);
        var result1 = sql2oUserRepository.findByEmailAndPassword("u", "p").get();
        var result2 = sql2oUserRepository.findByEmailAndPassword("u2", "r").get();
        assertThat(List.of(user1, user2)).usingRecursiveComparison().isEqualTo(List.of(result1, result2));
    }

    @Test
    public void whenSaveUsersWithDifferentEmailsAndSamePasswordsTheReceiveThem() {
        var user1 = new User(0, "u", "u", "p");
        var user2 = new User(0, "u2", "u2", "p");
        sql2oUserRepository.save(user1);
        sql2oUserRepository.save(user2);
        var result1 = sql2oUserRepository.findByEmailAndPassword("u", "p").get();
        var result2 = sql2oUserRepository.findByEmailAndPassword("u2", "p").get();
        assertThat(List.of(user1, user2)).usingRecursiveComparison().isEqualTo(List.of(result1, result2));
    }

    @Test
    public void whenSaveUsersWithSameEmailsThenReceiveException() {
        var user1 = new User(0, "u", "u", "p");
        var user2 = new User(0, "u", "u2", "p2");
        sql2oUserRepository.save(user1);
        assertThatThrownBy(() -> {
            sql2oUserRepository.save(user2);
        }).isInstanceOf(Exception.class);

    }

    @Test
    public void whenSaveNothingThenReceiveNothing() {
        assertThat(sql2oUserRepository.findByEmailAndPassword("e", "p")).isEqualTo(empty());
    }
}