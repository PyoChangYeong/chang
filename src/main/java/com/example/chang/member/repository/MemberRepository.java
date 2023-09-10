package com.example.chang.member.repository;

import com.example.chang.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member,Long> {
    @Query("select m from Member m where m.email =:email")
    Optional<Object> findByEmail(String email);
}
