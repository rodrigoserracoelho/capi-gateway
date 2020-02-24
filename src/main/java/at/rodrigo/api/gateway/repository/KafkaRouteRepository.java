package at.rodrigo.api.gateway.repository;

import at.rodrigo.api.gateway.entity.KafkaRoute;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface KafkaRouteRepository extends MongoRepository<KafkaRoute, String> {
}
