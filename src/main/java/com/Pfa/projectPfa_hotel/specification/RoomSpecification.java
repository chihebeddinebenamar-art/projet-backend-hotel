package com.Pfa.projectPfa_hotel.specification;

import com.Pfa.projectPfa_hotel.model.Room;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public final class RoomSpecification {

    private RoomSpecification() {
    }

    public static Specification<Room> hasOptionalRoomType(String roomType) {
        return (root, query, cb) -> {
            if (roomType == null || roomType.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("roomType"), roomType);
        };
    }

    public static Specification<Room> priceAtLeast(BigDecimal minPrice) {
        return (root, query, cb) -> minPrice == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("roomPrice"), minPrice);
    }

    public static Specification<Room> priceAtMost(BigDecimal maxPrice) {
        return (root, query, cb) -> maxPrice == null
                ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("roomPrice"), maxPrice);
    }

    public static Specification<Room> combined(String roomType, BigDecimal minPrice, BigDecimal maxPrice) {
        return Specification.where(hasOptionalRoomType(roomType))
                .and(priceAtLeast(minPrice))
                .and(priceAtMost(maxPrice));
    }
}
