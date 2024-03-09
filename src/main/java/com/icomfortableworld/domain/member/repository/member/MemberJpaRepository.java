package com.icomfortableworld.domain.member.repository.member;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.icomfortableworld.domain.member.entity.Member;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {

	Optional<Member> findByUsername(String username);

	Optional<Member> findByEmail(String email);
}
