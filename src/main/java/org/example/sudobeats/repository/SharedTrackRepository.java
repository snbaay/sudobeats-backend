package org.example.sudobeats.repository;
import org.example.sudobeats.entity.SharedTrack;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SharedTrackRepository extends JpaRepository<SharedTrack, UUID> {
    Optional<SharedTrack> findByPublicSlug(UUID publicSlug);
}
