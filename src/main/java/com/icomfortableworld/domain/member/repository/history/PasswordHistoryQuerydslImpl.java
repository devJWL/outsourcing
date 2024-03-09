package com.icomfortableworld.domain.member.repository.history;

import com.icomfortableworld.domain.member.entity.PasswordHistory;
import com.icomfortableworld.domain.member.entity.QPasswordHistory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PasswordHistoryQuerydslImpl implements PasswordHistoryQuerydsl {
    private final JPAQueryFactory jpaQueryFactory;

//    public PasswordHistoryQuerydslImpl(EntityManager em) {
//        this.jpaQueryFactory = new JPAQueryFactory(em);
//    }
    @Override
    public List<PasswordHistory> find3RecentlyPassword(Long memberId) {
        QPasswordHistory passwordHistory = QPasswordHistory.passwordHistory;

        return jpaQueryFactory.selectFrom(passwordHistory)
                .where(passwordHistory.memberId.eq(memberId))
                .orderBy(passwordHistory.createdDate.desc())
                .limit(3)
                .fetch();
    }
}
