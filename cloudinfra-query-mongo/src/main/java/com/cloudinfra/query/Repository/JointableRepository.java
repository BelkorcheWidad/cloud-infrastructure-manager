package com.cloudinfra.query.Repository;

import com.cloudinfra.query.Document.JoinTable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface JointableRepository  extends MongoRepository<JoinTable,Long> {
}
