package com.cloudinfra.vm.command.Repository;

import com.cloudinfra.vm.command.Aggregate.UserAggregate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<UserAggregate,Long> {
}
