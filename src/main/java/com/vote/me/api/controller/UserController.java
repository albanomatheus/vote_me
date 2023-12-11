package com.vote.me.api.controller;

import com.vote.me.api.model.Voting;
import com.vote.me.api.model.User;
import com.vote.me.api.repository.VotingRepository;
import com.vote.me.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VotingRepository votingRepository;

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping(consumes = {"application/json"})
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userRepository.save(user);
        return ResponseEntity.ok(createdUser);
    }

    @GetMapping("{id}")
    public ResponseEntity<User> getUserById(@PathVariable long id) {
        User user = userRepository.findById(id).orElseThrow();
        return ResponseEntity.ok(user);
    }

    @PutMapping("{id}")
    public ResponseEntity<String> updateUser(@PathVariable long id, @RequestBody User newUser) {
        if (userRepository.existsById(id)) {
            User userToUpdate = userRepository.findById(id).orElseThrow();
            userToUpdate.setName(newUser.getName());
            userToUpdate.setCpf(newUser.getCpf());
            userToUpdate.setUsername(newUser.getUsername());
            userToUpdate.setPassword(newUser.getPassword());

            userRepository.save(userToUpdate);

            return ResponseEntity.ok("User updated");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteUserById(@PathVariable long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.ok("User excluded");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not founded");
        }
    }

    @PostMapping("/vote/{userId}/{votingId}/{vote}")
    public ResponseEntity<String> vote(
            @PathVariable Long userId,
            @PathVariable Long votingId,
            @PathVariable String vote) {

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not founded"));

            Voting voting = votingRepository.findById(votingId)
                    .orElseThrow(() -> new RuntimeException("Voting not founded"));

            if (user.getVotings().contains(voting)) {
                return ResponseEntity.badRequest().body("User has already voted");
            }

            // não foi receber um valor valido pela API, portanto optei por fazer a requisição e não usar o valor
            String url = "https://run.mocky.io/v3/57f23672-c15f-48f8-90d3-d84ce00250b8/users/"+user.getCpf();

            URL apiUrl = new URL(url);

            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("GET");

            String responseCode = connection.getResponseMessage();

            if (!voting.isAfterDeadline()) {
                return ResponseEntity.badRequest().body("Voting already expired.");
            }

            if ("yes".equalsIgnoreCase(vote) || "no".equalsIgnoreCase(vote)) {
                int pro = "yes".equalsIgnoreCase(vote) ? 1 : -1;
                voting.setInFavor(voting.getInFavor() + pro);
                voting.getUsers().add(user);
                user.getVotings().add(voting);

                votingRepository.save(voting);
                userRepository.save(user);

                return ResponseEntity.ok("Vote registered.");
            } else {
                return ResponseEntity.badRequest().body("invalid vote");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("error");
        }
    }
}
