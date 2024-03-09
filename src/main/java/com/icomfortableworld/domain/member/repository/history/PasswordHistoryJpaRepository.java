package com.icomfortableworld.domain.member.repository.history;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.icomfortableworld.domain.member.entity.PasswordHistory;

public interface PasswordHistoryJpaRepository extends JpaRepository<PasswordHistory, Long>, PasswordHistoryQuerydsl {

	List<PasswordHistory> findTop3ByMemberIdOrderByCreatedDateDesc(Long memberId);

}
