package com.icomfortableworld.domain.member.service;

import com.icomfortableworld.domain.member.entity.MemberRoleEnum;
import com.icomfortableworld.domain.member.repository.member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.icomfortableworld.domain.member.TestMember.TEST_MEMBER_EMAIL;
import static com.icomfortableworld.domain.member.TestMember.TEST_MEMBER_USERNAME;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @InjectMocks
    private MemberServiceImpl memberService;
    @Mock
    private MemberRepository memberRepository;

    @Nested
    @DisplayName("회원가입 테스트")
    class LoginTest {

        @Test
        @DisplayName("일반회원 가입 성공 테스트")
        void signupSuccessTest() {
            //given
            MemberRoleEnum memberRoleEnum = MemberRoleEnum.USER;
            given(memberRepository.findByUsername(TEST_MEMBER_USERNAME)).willReturn(Optional.empty());
            given(memberRepository.findByEmail(TEST_MEMBER_EMAIL)).willReturn(Optional.empty());

            //when

            //then
        }
    }
}