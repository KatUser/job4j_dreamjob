package ru.job4j.dreamjob.repository;

import org.springframework.stereotype.Repository;
import ru.job4j.dreamjob.model.Candidate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class MemoryCandidateRepository implements CandidateRepository {

    private int nextId = 1;

    private final Map<Integer, Candidate> candidates = new HashMap<>();

    private MemoryCandidateRepository() {
        save(new Candidate(0,
                "Tom",
                "smart, knows some SQL",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)));
        save(new Candidate(0,
                "Jerry",
                "not smart, likes cheese and Golang",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)));
        save(new Candidate(0,
                "Woodie",
                "nice, but too loud; uses C++",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)));
        save(new Candidate(0,
                "Piggie",
                "is a small piglet; enjoys Assembler",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)));
        save(new Candidate(0,
                "Minnie",
                "is a small mouse that codes in Java",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)));
    }

    @Override
    public Candidate save(Candidate candidate) {
        candidate.setId(nextId++);
        candidates.put(candidate.getId(), candidate);
        return candidate;
    }

    @Override
    public boolean deleteById(int id) {
        return candidates.remove(id, candidates.get(id));
    }

    @Override
    public boolean update(Candidate candidate) {
        return candidates.computeIfPresent(candidate.getId(),
                (id, oldCandidate) -> new Candidate(
                        oldCandidate.getId(),
                        candidate.getName(),
                        candidate.getDescription(),
                        candidate.getCreationDate())) != null;
    }

    @Override
    public Optional<Candidate> findById(int id) {
        return  Optional.ofNullable(candidates.get(id));
    }

    @Override
    public Collection<Candidate> findAll() {
        return candidates.values();
    }
}