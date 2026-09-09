package com.cloudinfra.query.Repository;

import com.cloudinfra.query.Document.ServerDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ServerRepository extends MongoRepository<ServerDocument, Long> {
}