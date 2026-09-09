package com.cloudinfra.vm.command.Repository;

import com.cloudinfra.vm.command.Aggregate.VmAggregate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VmRepo extends JpaRepository<VmAggregate,Long> {
}
