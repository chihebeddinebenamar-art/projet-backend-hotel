package com.Pfa.projectPfa_hotel.service;

import com.Pfa.projectPfa_hotel.exception.BadRequestException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryStorageService {

    private final ObjectProvider<Cloudinary> cloudinary;

    public CloudinaryStorageService(ObjectProvider<Cloudinary> cloudinary) {
        this.cloudinary = cloudinary;
    }

    private Cloudinary requireCloudinary() {
        Cloudinary c = cloudinary.getIfAvailable();
        if (c == null) {
            throw new BadRequestException(
                    "Cloudinary n’est pas configuré. Définissez CLOUDINARY_API_KEY et CLOUDINARY_API_SECRET "
                            + "(ou cloudinary.api-key et cloudinary.api-secret), puis redémarrez.");
        }
        return c;
    }

    /**
     * Envoie une image vers Cloudinary. Le dossier regroupe les fichiers par chambre.
     *
     * @return [0] = URL sécurisée (https), [1] = public_id Cloudinary (pour suppression)
     */
    public String[] uploadRoomImage(long roomId, MultipartFile file) throws IOException {
        Cloudinary c = requireCloudinary();
        String folder = "hotel_pfa/rooms/" + roomId;
        @SuppressWarnings("unchecked")
        Map<String, Object> result = c.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", folder,
                        "public_id", UUID.randomUUID().toString(),
                        "resource_type", "image",
                        "overwrite", false));
        String secureUrl = (String) result.get("secure_url");
        String returnedPublicId = (String) result.get("public_id");
        return new String[] { secureUrl, returnedPublicId };
    }

    public void deleteByPublicId(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }
        Cloudinary c = cloudinary.getIfAvailable();
        if (c == null) {
            return;
        }
        try {
            c.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new IllegalStateException("Échec suppression Cloudinary: " + e.getMessage(), e);
        }
    }
}
