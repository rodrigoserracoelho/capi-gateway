package at.rodrigo.api.gateway.repository;

import at.rodrigo.api.gateway.entity.Api;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ApiRepository extends MongoRepository<Api, String> {

    List<Api> findAllBySwagger(boolean swagger);
    Api findByName(String apiName);


}
