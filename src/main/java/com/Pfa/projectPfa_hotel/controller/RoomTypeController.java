package com.Pfa.projectPfa_hotel.controller;

import com.Pfa.projectPfa_hotel.dto.RoomTypeDto;
import com.Pfa.projectPfa_hotel.dto.RoomTypeRequest;
import com.Pfa.projectPfa_hotel.service.HotelRoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/room-types")
@RequiredArgsConstructor
public class RoomTypeController {

    private final HotelRoomTypeService hotelRoomTypeService;

    @GetMapping
    public List<RoomTypeDto> list() {
        return hotelRoomTypeService.findAll();
    }

    @PostMapping
    public ResponseEntity<RoomTypeDto> create(@RequestBody RoomTypeRequest request) {
        RoomTypeDto created = hotelRoomTypeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public RoomTypeDto update(@PathVariable Long id, @RequestBody RoomTypeRequest request) {
        return hotelRoomTypeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        hotelRoomTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
