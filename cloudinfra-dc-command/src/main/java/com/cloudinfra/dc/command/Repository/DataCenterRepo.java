package com.cloudinfra.dc.command.Repository;

import com.cloudinfra.dc.command.Aggregate.DataCenterAggregate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataCenterRepo extends JpaRepository<DataCenterAggregate,Long> {
}
