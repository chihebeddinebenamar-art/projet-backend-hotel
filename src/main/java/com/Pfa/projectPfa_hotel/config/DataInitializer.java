package com.Pfa.projectPfa_hotel.config;

import com.Pfa.projectPfa_hotel.model.Accessory;
import com.Pfa.projectPfa_hotel.model.AppUser;
import com.Pfa.projectPfa_hotel.model.HotelRoomType;
import com.Pfa.projectPfa_hotel.repository.AccessoryRepository;
import com.Pfa.projectPfa_hotel.repository.AppUserRepository;
import com.Pfa.projectPfa_hotel.repository.HotelRoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final HotelRoomTypeRepository hotelRoomTypeRepository;
    private final AccessoryRepository accessoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (appUserRepository.count() == 0) {
            AppUser admin = new AppUser(
                    "admin@hotel.local",
                    passwordEncoder.encode("admin123"),
                    "ADMIN");
            admin.setNom("Administrateur");
            admin.setPrenom("Principal");
            admin.setTelephone("");
            appUserRepository.save(admin);
        }
        if (hotelRoomTypeRepository.count() == 0) {
            hotelRoomTypeRepository.save(HotelRoomType.builder().name("Standard").maxOccupancy(2).build());
            hotelRoomTypeRepository.save(HotelRoomType.builder().name("Deluxe").maxOccupancy(3).build());
            hotelRoomTypeRepository.save(HotelRoomType.builder().name("Suite").maxOccupancy(4).build());
        }
        if (accessoryRepository.count() == 0) {
            accessoryRepository.save(Accessory.builder()
                    .name("Wi-Fi")
                    .description("Internet haut débit gratuit")
                    .build());
            accessoryRepository.save(Accessory.builder()
                    .name("Climatisation")
                    .description("Air conditionné réglable")
                    .build());
            accessoryRepository.save(Accessory.builder()
                    .name("Minibar")
                    .description("Boissons et snacks")
                    .build());
            accessoryRepository.save(Accessory.builder()
                    .name("TV écran plat")
                    .description("Chaînes satellite")
                    .build());
        }
    }
}
