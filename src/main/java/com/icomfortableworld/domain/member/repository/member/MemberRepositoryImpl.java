package com.icomfortableworld.domain.member.repository.member;

import java.util.List;
import java.util.Optional;

import com.icomfortableworld.domain.member.repository.history.PasswordHistoryJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import com.icomfortableworld.domain.member.entity.Member;
import com.icomfortableworld.domain.member.entity.PasswordHistory;
import com.icomfortableworld.domain.member.exception.CustomMemberException;
import com.icomfortableworld.domain.member.exception.MemberErrorCode;
import com.icomfortableworld.domain.member.model.MemberModel;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MemberRepositoryImpl implements MemberRepository {

	private final MemberJpaRepository jpaRepository;
	private final PasswordHistoryJpaRepository passwordHistoryJpaRepository;
	private final PasswordEncoder passwordEncoder;
	@Override
	public Optional<MemberModel> findById(Long memberId) {
		return jpaRepository.findById(memberId).map(Member::toModel);
	}

	@Override
	public MemberModel findByIdOrElseThrow(Long memberId) {
		return findById(memberId).orElseThrow(
			() -> new CustomMemberException(MemberErrorCode.MEMBER_ERROR_CODE_NOT_FOUND)
		);
	}

	@Override
	public Optional<MemberModel> findByUsername(String username) {
		return jpaRepository.findByUsername(username).map(Member::toModel);
	}

	@Override
	public MemberModel findByUsernameOrElseThrow(String username) {
		return findByUsername(username).orElseThrow(
			() -> new CustomMemberException(MemberErrorCode.MEMBER_ERROR_CODE_NOT_FOUND)
		);
	}

	@Override
	public Optional<MemberModel> findByEmail(String email) {
		return jpaRepository.findByEmail(email).map(Member::toModel);
	}

	@Override
	public MemberModel save(Member member) {
		return jpaRepository.save(member).toModel();
	}

	@Override
	public List<MemberModel> findAll() {
		return jpaRepository.findAll().stream().map(Member::toModel).toList();
	}

	@Override
	public MemberModel updateMember(Long memberId, String newNickname, String newIntroduction, String newPassword) {
		Member member = jpaRepository.findById(memberId).orElse(null);
		if (member == null) {
			throw new CustomMemberException(MemberErrorCode.MEMBER_ERROR_CODE_NOT_FOUND);
		}
		if (newNickname != null) {
			member.updateNickname(newNickname);
		}
		if (newIntroduction != null) {
			member.updateIntroduction(newIntroduction);
		}
		if (newPassword != null) {
			checkRecent3Password(memberId, newPassword);
			String encryptedPassword = passwordEncoder.encode(newPassword);
			member.updatePassword(encryptedPassword);
			passwordHistoryJpaRepository.save(PasswordHistory.of(memberId, encryptedPassword));
		}
		return member.toModel();
	}

	private void checkRecent3Password(Long memberId, String newPassword) {
		List<PasswordHistory> recent3PasswordList = passwordHistoryJpaRepository.find3RecentlyPassword(memberId);
		if (recent3PasswordList.stream()
			.anyMatch(passwordHistory -> passwordEncoder.matches(newPassword, passwordHistory.getPassword()))) {
			throw new CustomMemberException(MemberErrorCode.MEMBER_ERROR_CODE_PASSWORD_RECENTLY_USED);
		}
	}
}
