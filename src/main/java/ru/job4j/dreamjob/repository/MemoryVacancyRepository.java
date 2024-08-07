package ru.job4j.dreamjob.repository;

import net.jcip.annotations.ThreadSafe;
import org.springframework.stereotype.Repository;
import ru.job4j.dreamjob.model.Vacancy;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@ThreadSafe
@Repository
public class MemoryVacancyRepository implements VacancyRepository {

    private final AtomicInteger nextId = new AtomicInteger(0);

    private final Map<Integer, Vacancy> vacancies = new ConcurrentHashMap<>();
/*
    private MemoryVacancyRepository() {
        save(new Vacancy(0, "Intern Java Developer",
                "We pay nothing, you get experience",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), true, 1, 0));
        save(new Vacancy(0, "Junior Java Developer",
                "You work for some food and experience",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), true, 2, 0));
        save(new Vacancy(0, "Junior+ Java Developer",
                "You work for food, cookies and experience",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), true, 3, 0));
        save(new Vacancy(0, "Middle Java Developer",
                "You work for salary, food and experience",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), true, 1, 0));
        save(new Vacancy(0, "Middle+ Java Developer",
                "Description is not available here",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), false, 2, 0));
        save(new Vacancy(0, "Senior Java Developer",
                "Don't even call us",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), false, 1, 0));
    }
*/

    @Override
    public Vacancy save(Vacancy vacancy) {
        vacancy.setId(nextId.incrementAndGet());
        vacancies.put(vacancy.getId(), vacancy);
        return vacancy;
    }

    @Override
    public boolean deleteById(int id) {
        return vacancies.remove(id, vacancies.get(id));
    }

    @Override
    public boolean update(Vacancy vacancy) {
        return vacancies.computeIfPresent(vacancy.getId(),
                (id, oldVacancy) -> new Vacancy(
                        oldVacancy.getId(),
                        vacancy.getTitle(),
                        vacancy.getDescription(),
                        vacancy.getCreationDate(),
                        vacancy.getVisible(),
                        vacancy.getCityId(),
                        vacancy.getFileId())) != null; /* true if Updated */
    }

    @Override
    public Optional<Vacancy> findById(int id) {
        return Optional.ofNullable(vacancies.get(id));
    }

    @Override
    public Collection<Vacancy> findAll() {
        return vacancies.values();
    }
}