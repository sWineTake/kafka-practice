package com.group.server.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OutBoxTableRepository extends JpaRepository<OutBoxTable, Long> {


}
