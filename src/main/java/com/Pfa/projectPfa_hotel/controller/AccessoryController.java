package com.Pfa.projectPfa_hotel.controller;

import com.Pfa.projectPfa_hotel.dto.AccessoryDto;
import com.Pfa.projectPfa_hotel.dto.AccessoryRequest;
import com.Pfa.projectPfa_hotel.service.AccessoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accessories")
@RequiredArgsConstructor
public class AccessoryController {

    private final AccessoryService accessoryService;

    @GetMapping
    public List<AccessoryDto> list() {
        return accessoryService.findAll();
    }

    @PostMapping
    public ResponseEntity<AccessoryDto> create(@RequestBody AccessoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accessoryService.create(request));
    }

    @PutMapping("/{id}")
    public AccessoryDto update(@PathVariable Long id, @RequestBody AccessoryRequest request) {
        return accessoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        accessoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
