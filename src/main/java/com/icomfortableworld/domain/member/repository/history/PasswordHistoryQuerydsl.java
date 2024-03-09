package com.icomfortableworld.domain.member.repository.history;

import com.icomfortableworld.domain.member.entity.PasswordHistory;

import java.util.List;

public interface PasswordHistoryQuerydsl {
    List<PasswordHistory> find3RecentlyPassword(Long memberId);
}
