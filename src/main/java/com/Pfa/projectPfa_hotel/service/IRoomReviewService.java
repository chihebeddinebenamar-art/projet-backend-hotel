package com.Pfa.projectPfa_hotel.service;

import com.Pfa.projectPfa_hotel.dto.CreateRoomReviewRequest;
import com.Pfa.projectPfa_hotel.dto.RoomReviewResponse;
import com.Pfa.projectPfa_hotel.dto.RoomReviewsPageResponse;

public interface IRoomReviewService {

    RoomReviewsPageResponse getForRoom(Long roomId, String viewerEmailOrNull);

    RoomReviewResponse create(Long roomId, String authorEmail, CreateRoomReviewRequest request);
}
