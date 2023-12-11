package com.vote.me.api.controller;

import com.vote.me.api.model.Voting;
import com.vote.me.api.repository.VotingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/voting")
public class VotingController {
    @Autowired
    private VotingRepository votingRepository;

    @GetMapping
    public List<Voting> getAllVoting() {
        return votingRepository.findAll();
    }

    @PostMapping
    public Voting createVoting(@RequestBody Voting voting) {
        if (voting.getDeadline() == null) {
            Instant currentInstant = Instant.now();
            Instant newDeadlineInstant = currentInstant.plusMillis(1000);

           voting.setDeadline(LocalDateTime.ofInstant(newDeadlineInstant, ZoneId.systemDefault()));
        }

        return votingRepository.save(voting);
    }

    @GetMapping("{id}")
    public ResponseEntity<Voting> getVotingById(@PathVariable long id) {
        Voting voting = votingRepository.findById(id).orElseThrow();
        return ResponseEntity.ok(voting);
    }

    @PutMapping("{id}")
    public ResponseEntity<String> updateVoting(@PathVariable long id, @RequestBody Voting newVoting) {
        if (votingRepository.existsById(id)) {
            Voting votingToUpdate = votingRepository.findById(id).orElseThrow();
            votingToUpdate.setTitle(newVoting.getTitle());
            votingToUpdate.setDescription(newVoting.getTitle());

            votingRepository.save(votingToUpdate);

            return ResponseEntity.ok("Voting updated");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voting not founded");
        }

    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteVotingById(@PathVariable long id) {
        if (votingRepository.existsById(id)) {
            votingRepository.deleteById(id);
            return ResponseEntity.ok("Voting excluded");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Voting not found");
        }
    }
}
