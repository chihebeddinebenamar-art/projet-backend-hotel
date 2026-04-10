package com.Pfa.projectPfa_hotel.controller;

import com.Pfa.projectPfa_hotel.dto.ReceptionistRequest;
import com.Pfa.projectPfa_hotel.dto.ReceptionistResponse;
import com.Pfa.projectPfa_hotel.service.ReceptionistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/receptionists")
@RequiredArgsConstructor
public class ReceptionistController {

    private final ReceptionistService receptionistService;

    @GetMapping
    public ResponseEntity<List<ReceptionistResponse>> list() {
        return ResponseEntity.ok(receptionistService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReceptionistResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(receptionistService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ReceptionistResponse> create(@RequestBody ReceptionistRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(receptionistService.create(body));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReceptionistResponse> update(
            @PathVariable Long id,
            @RequestBody ReceptionistRequest body) {
        return ResponseEntity.ok(receptionistService.update(id, body));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        receptionistService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
