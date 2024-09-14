package com.mylogo.visitors.visitormgmt.repository;

import com.mylogo.visitors.visitormgmt.model.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitorRepository extends JpaRepository<Visitor,Long> {

}
