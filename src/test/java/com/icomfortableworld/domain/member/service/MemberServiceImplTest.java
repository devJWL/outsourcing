package com.icomfortableworld.domain.member.service;

import com.icomfortableworld.domain.member.dto.request.SignupRequestDto;
import com.icomfortableworld.domain.member.entity.Member;
import com.icomfortableworld.domain.member.entity.MemberRoleEnum;
import com.icomfortableworld.domain.member.entity.PasswordHistory;
import com.icomfortableworld.domain.member.exception.CustomMemberException;
import com.icomfortableworld.domain.member.exception.MemberErrorCode;
import com.icomfortableworld.domain.member.model.MemberModel;
import com.icomfortableworld.domain.member.repository.history.PasswordHistoryJpaRepository;
import com.icomfortableworld.domain.member.repository.member.MemberRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.icomfortableworld.domain.member.TestMember.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PasswordHistoryJpaRepository passwordHistoryJpaRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberServiceImpl memberService;

    @Nested
    @DisplayName("회원가입 테스트")
    class SignupTest {

        @Test
        @DisplayName("일반회원 가입 성공 테스트")
        void signupSuccessTest() {
            //given
            SignupRequestDto signupRequestDto = new SignupRequestDto(
                    TEST_MEMBER_USERNAME, TEST_MEMBER_EMAIL, TEST_MEMBER_NICKNAME,
                    TEST_MEMBER_PASSWORD, null, false, null);

            String encodedPassword = passwordEncoder.encode(TEST_MEMBER_PASSWORD);

            MemberModel memberModel = new MemberModel(TEST_MEMBER_ID, TEST_MEMBER_USERNAME, TEST_MEMBER_EMAIL, TEST_MEMBER_NICKNAME,
                    TEST_MEMBER_PASSWORD, null, MemberRoleEnum.USER, null);
            given(memberRepository.findByUsername(TEST_MEMBER_USERNAME)).willReturn(Optional.empty());
            given(memberRepository.findByEmail(TEST_MEMBER_EMAIL)).willReturn(Optional.empty());
            given(passwordEncoder.encode(TEST_MEMBER_PASSWORD)).willReturn(encodedPassword);
            given(memberRepository.save(any(Member.class))).willReturn(memberModel);


            //when
            memberService.signup(signupRequestDto);

            //then
            then(memberRepository).should(times(1)).save(any(Member.class));
            then(passwordHistoryJpaRepository).should(times(1)).save(any(PasswordHistory.class));
//            verify(memberRepository, times(1)).save(any(Member.class));
//            verify(passwordHistoryJpaRepository, times(1)).save(any(PasswordHistory.class));
        }

        @Test
        @DisplayName("일반회원 가입 실패 테스트 중복된 username")
        void signupFailDuplicatedUsernameTest() {
            //given
            SignupRequestDto signupRequestDto = new SignupRequestDto(
                    TEST_MEMBER_USERNAME, TEST_MEMBER_EMAIL, TEST_MEMBER_NICKNAME,
                    TEST_MEMBER_PASSWORD, null, false, null);
            MemberModel memberModel = new MemberModel(TEST_MEMBER_ID, TEST_MEMBER_USERNAME, TEST_MEMBER_EMAIL, TEST_MEMBER_NICKNAME,
                    TEST_MEMBER_PASSWORD, null, MemberRoleEnum.USER, null);
            given(memberRepository.findByUsername(TEST_MEMBER_USERNAME)).willReturn(Optional.of(memberModel));

            // when & then
            String message = Assertions.assertThrows(CustomMemberException.class, () -> memberService.signup(signupRequestDto)).getMessage();
            Assertions.assertSame(MemberErrorCode.MEMBER_ERROR_CODE_USERNAME_ALREADY_EXISTS.getMessage(),
                    message);
        }

        @Test
        @DisplayName("일반회원 가입 실패 테스트 중복된 email")
        void signupFailDuplicatedEmailTest() {
            //given
            SignupRequestDto signupRequestDto = new SignupRequestDto(
                    TEST_MEMBER_USERNAME, TEST_MEMBER_EMAIL, TEST_MEMBER_NICKNAME,
                    TEST_MEMBER_PASSWORD, null, false, null);

            MemberModel memberModel = new MemberModel(TEST_MEMBER_ID, TEST_MEMBER_USERNAME, TEST_MEMBER_EMAIL, TEST_MEMBER_NICKNAME,
                    TEST_MEMBER_PASSWORD, null, MemberRoleEnum.USER, null);
            given(memberRepository.findByEmail(TEST_MEMBER_EMAIL)).willReturn(Optional.of(memberModel));

            // when & then
            String message = Assertions.assertThrows(CustomMemberException.class, () -> memberService.signup(signupRequestDto)).getMessage();
            Assertions.assertSame(MemberErrorCode.MEMBER_ERROR_CODE_EMAIL_ALREADY_EXISTS.getMessage(),
                    message);
        }

        @Test
        @DisplayName("관리자회원 가입 실패 올바르지 않은 관리자 토큰")
        void signupFailInvalidTokenTest() {
            //given
            ReflectionTestUtils.setField(memberService, "adminToken", "valid admin token");
            String invalidAdminToken = "invalid admin Token";
            SignupRequestDto signupRequestDto = new SignupRequestDto(
                    TEST_MEMBER_USERNAME, TEST_MEMBER_EMAIL, TEST_MEMBER_NICKNAME,
                    TEST_MEMBER_PASSWORD, null, true, invalidAdminToken);

            // when & then
            String message = Assertions.assertThrows(CustomMemberException.class, () -> memberService.signup(signupRequestDto)).getMessage();
            Assertions.assertSame(MemberErrorCode.MEMBER_ERROR_CODE_ADMIN_TOKEN_MISMATCH.getMessage(),
                    message);
        }
    }
}