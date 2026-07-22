package com.hillingdon.parking.services;

import com.hillingdon.parking.dto.BadgeResponse;
import com.hillingdon.parking.models.Badge;
import com.hillingdon.parking.models.User;
import com.hillingdon.parking.models.Vehicle;
import com.hillingdon.parking.repositories.BadgeRepository;
import com.hillingdon.parking.repositories.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BadgeService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final BadgeRepository badgeRepository;
    private final VehicleRepository vehicleRepository;
    private final RestClient restClient = RestClient.create();

    @Value("${app.supabase.url}")
    private String supabaseUrl;

    @Value("${app.supabase.service-role-key}")
    private String supabaseServiceRoleKey;

    @Value("${app.supabase.storage-bucket}")
    private String storageBucket;

    public Badge submitBadge(User patient, String plate, String badgeNumber, LocalDate expiresAt, MultipartFile photo) {
        if (photo == null || photo.isEmpty()) {
            throw new IllegalArgumentException("A photo of the Blue Badge is required");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(photo.getContentType())) {
            throw new IllegalArgumentException("Photo must be a JPEG, PNG or WebP image");
        }
        if (expiresAt.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Badge has already expired");
        }

        Vehicle vehicle = findOrCreateVehicle(patient, plate);
        String photoPath = uploadPhoto(patient.getId(), photo);

        Badge badge = new Badge();
        badge.setUser(patient);
        badge.setVehicle(vehicle);
        badge.setBadgeNumber(badgeNumber);
        badge.setPhotoUrl(photoPath);
        badge.setExpiresAt(expiresAt);
        badge.setStatus(Badge.BadgeStatus.PENDING);

        return badgeRepository.save(badge);
    }

    public List<Badge> getPendingBadges() {
        return badgeRepository.findByStatus(Badge.BadgeStatus.PENDING);
    }

    public List<Badge> getBadgesForUser(User user) {
        return badgeRepository.findByUser(user);
    }

    /**
     * Badge.photoUrl stores the private storage object path, not a public URL —
     * the bucket holds sensitive documents, so callers get a short-lived signed URL instead.
     */
    public BadgeResponse toResponse(Badge badge) {
        BadgeResponse response = BadgeResponse.from(badge);
        response.setPhotoUrl(createSignedUrl(badge.getPhotoUrl()));
        return response;
    }

    private String createSignedUrl(String path) {
        try {
            SignedUrlResponse signed = restClient.post()
                    .uri("%s/storage/v1/object/sign/%s/%s".formatted(supabaseUrl, storageBucket, path))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseServiceRoleKey)
                    .header("apikey", supabaseServiceRoleKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("expiresIn", 3600))
                    .retrieve()
                    .body(SignedUrlResponse.class);
            return supabaseUrl + "/storage/v1" + signed.signedURL();
        } catch (RestClientException e) {
            log.error("Failed to create signed URL for badge photo: {}", e.getMessage());
            throw new IllegalStateException("Could not retrieve badge photo, please try again");
        }
    }

    private record SignedUrlResponse(String signedURL) {
    }

    private Vehicle findOrCreateVehicle(User patient, String plate) {
        String normalisedPlate = plate.toUpperCase().replaceAll("\\s+", "");
        return vehicleRepository.findByPlate(normalisedPlate)
                .orElseGet(() -> {
                    Vehicle vehicle = new Vehicle();
                    vehicle.setPlate(normalisedPlate);
                    vehicle.setUser(patient);
                    return vehicleRepository.save(vehicle);
                });
    }

    private String uploadPhoto(UUID patientId, MultipartFile photo) {
        String extension = switch (photo.getContentType()) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
        String path = "%s/%s.%s".formatted(patientId, UUID.randomUUID(), extension);

        byte[] bytes;
        try {
            bytes = photo.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read uploaded badge photo", e);
        }

        try {
            restClient.post()
                    .uri("%s/storage/v1/object/%s/%s".formatted(supabaseUrl, storageBucket, path))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseServiceRoleKey)
                    .header("apikey", supabaseServiceRoleKey)
                    .contentType(MediaType.parseMediaType(photo.getContentType()))
                    .body(bytes)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.error("Failed to upload badge photo to Supabase Storage: {}", e.getMessage());
            throw new IllegalStateException("Could not store badge photo, please try again");
        }

        return path;
    }
}
