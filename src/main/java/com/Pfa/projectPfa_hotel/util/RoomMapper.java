package com.Pfa.projectPfa_hotel.util;

import com.Pfa.projectPfa_hotel.dto.AccessoryDto;
import com.Pfa.projectPfa_hotel.model.Accessory;
import com.Pfa.projectPfa_hotel.model.Photo;
import com.Pfa.projectPfa_hotel.model.Room;
import com.Pfa.projectPfa_hotel.response.PhotoResponse;
import com.Pfa.projectPfa_hotel.response.RoomResponse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class RoomMapper {

    private RoomMapper() {
    }

    public static RoomResponse toResponse(Room room) {
        if (room == null) {
            return null;
        }
        if (room.getPhotos() != null) {
            room.getPhotos().size();
        }
        List<Photo> photoList = room.getPhotos() != null
                ? new ArrayList<>(room.getPhotos())
                : new ArrayList<>();
        photoList.sort(Comparator
                .comparing(Photo::isPrimaryPhoto).reversed()
                .thenComparing(Photo::getSortOrder)
                .thenComparing(Photo::getId));

        String mainUrl = photoList.stream()
                .filter(Photo::isPrimaryPhoto)
                .map(Photo::getUrl)
                .findFirst()
                .orElse(photoList.isEmpty() ? null : photoList.get(0).getUrl());

        List<PhotoResponse> pr = photoList.stream()
                .map(p -> new PhotoResponse(p.getId(), p.getUrl(), p.getSortOrder(), p.isPrimaryPhoto()))
                .toList();

        RoomResponse response = new RoomResponse();
        response.setId(room.getId());
        response.setRoomNumber(room.getRoomNumber());
        response.setRoomType(room.getRoomType());
        response.setRoomPrice(room.getRoomPrice());
        response.setBooked(room.isBooked());
        response.setPhoto(mainUrl);
        response.setPhotos(pr);
        response.setBookings(null);

        if (room.getAccessories() != null && !room.getAccessories().isEmpty()) {
            List<AccessoryDto> acc = room.getAccessories().stream()
                    .map(RoomMapper::toAccessoryDto)
                    .sorted(Comparator.comparing(AccessoryDto::getName, String.CASE_INSENSITIVE_ORDER))
                    .toList();
            response.setAccessories(acc);
        } else {
            response.setAccessories(List.of());
        }
        return response;
    }

    private static AccessoryDto toAccessoryDto(Accessory a) {
        return new AccessoryDto(a.getId(), a.getName(), a.getDescription());
    }
}
