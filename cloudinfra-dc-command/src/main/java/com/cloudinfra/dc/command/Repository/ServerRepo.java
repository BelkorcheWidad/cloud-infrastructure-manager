package com.cloudinfra.dc.command.Repository;

import com.cloudinfra.dc.command.Aggregate.ServerAggregate;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ServerRepo extends JpaRepository<ServerAggregate,Long> {
}
