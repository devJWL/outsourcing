package com.icomfortableworld.domain.member.service;

import com.icomfortableworld.domain.follow.entity.Follow;
import com.icomfortableworld.domain.follow.repository.FollowRepository;
import com.icomfortableworld.domain.member.dto.request.LoginRequestDto;
import com.icomfortableworld.domain.member.dto.request.MemberUpdateRequestDto;
import com.icomfortableworld.domain.member.dto.request.SignupRequestDto;
import com.icomfortableworld.domain.member.dto.response.LoginResponseDto;
import com.icomfortableworld.domain.member.dto.response.MemberResponseDto;
import com.icomfortableworld.domain.member.dto.response.MemberUpdateResponseDto;
import com.icomfortableworld.domain.member.entity.Member;
import com.icomfortableworld.domain.member.entity.MemberRoleEnum;
import com.icomfortableworld.domain.member.entity.PasswordHistory;
import com.icomfortableworld.domain.member.exception.CustomMemberException;
import com.icomfortableworld.domain.member.exception.MemberErrorCode;
import com.icomfortableworld.domain.member.model.MemberModel;
import com.icomfortableworld.domain.member.repository.history.PasswordHistoryJpaRepository;
import com.icomfortableworld.domain.member.repository.member.MemberRepository;
import com.icomfortableworld.domain.message.dto.request.MessageRequestDto;
import com.icomfortableworld.domain.message.entity.Message;
import com.icomfortableworld.domain.message.repository.MessageJpaRepository;
import com.icomfortableworld.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static com.icomfortableworld.domain.member.TestMember.*;
import static org.junit.jupiter.api.Assertions.*;
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
    private FollowRepository followRepository;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private MessageJpaRepository messageJpaRepository;
    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberServiceImpl memberService;

    @Nested
    @DisplayName("회원가입 테스트")
    class SignupTest {

        SignupRequestDto signupRequestDto;
        @BeforeEach
        void setUp() {
            signupRequestDto = new SignupRequestDto(
                    TEST_MEMBER_USERNAME, TEST_MEMBER_EMAIL, TEST_MEMBER_NICKNAME,
                    TEST_MEMBER_PASSWORD, null, false, null);
        }

        @Test
        @DisplayName("일반회원 가입 성공 테스트")
        void signupSuccessTest() {

            //given
            String encodedPassword = passwordEncoder.encode(TEST_MEMBER_PASSWORD);
            MemberModel memberModel = new MemberModel(TEST_MEMBER_ID, TEST_MEMBER_USERNAME, TEST_MEMBER_EMAIL, TEST_MEMBER_NICKNAME,
                    encodedPassword, null, MemberRoleEnum.USER, null);
            given(memberRepository.findByUsername(TEST_MEMBER_USERNAME)).willReturn(Optional.empty());
            given(memberRepository.findByEmail(TEST_MEMBER_EMAIL)).willReturn(Optional.empty());
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

            String encodedPassword = passwordEncoder.encode(TEST_MEMBER_PASSWORD);
            MemberModel memberModel = new MemberModel(TEST_MEMBER_ID, TEST_MEMBER_USERNAME, TEST_MEMBER_EMAIL, TEST_MEMBER_NICKNAME,
                    encodedPassword, null, MemberRoleEnum.USER, null);
            given(memberRepository.findByUsername(TEST_MEMBER_USERNAME)).willReturn(Optional.of(memberModel));

            // when
            String message = assertThrows(CustomMemberException.class,
                    () -> memberService.signup(signupRequestDto)).getMessage();

            // then
            assertEquals(MemberErrorCode.MEMBER_ERROR_CODE_USERNAME_ALREADY_EXISTS.getMessage(),
                    message);
        }

        @Test
        @DisplayName("일반회원 가입 실패 테스트 중복된 email")
        void signupFailDuplicatedEmailTest() {

            //given
            SignupRequestDto signupRequestDto = new SignupRequestDto(
                    TEST_MEMBER_USERNAME, TEST_MEMBER_EMAIL, TEST_MEMBER_NICKNAME,
                    TEST_MEMBER_PASSWORD, null, false, null);

            String encodedPassword = passwordEncoder.encode(TEST_MEMBER_PASSWORD);
            MemberModel memberModel = new MemberModel(TEST_MEMBER_ID, TEST_MEMBER_USERNAME, TEST_MEMBER_EMAIL, TEST_MEMBER_NICKNAME,
                    encodedPassword, null, MemberRoleEnum.USER, null);
            given(memberRepository.findByEmail(TEST_MEMBER_EMAIL)).willReturn(Optional.of(memberModel));

            // when
            String message = assertThrows(CustomMemberException.class, () -> memberService.signup(signupRequestDto)).getMessage();

            // then
            assertEquals(MemberErrorCode.MEMBER_ERROR_CODE_EMAIL_ALREADY_EXISTS.getMessage(),
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

            // when
            String message = assertThrows(CustomMemberException.class, () -> memberService.signup(signupRequestDto)).getMessage();

            // then
            assertEquals(MemberErrorCode.MEMBER_ERROR_CODE_ADMIN_TOKEN_MISMATCH.getMessage(),
                    message);
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTest {

        private LoginRequestDto loginRequestDto;

        @BeforeEach void setUp() {
            loginRequestDto = new LoginRequestDto(TEST_MEMBER_USERNAME, TEST_MEMBER_PASSWORD);
        }

        @Test
        @DisplayName("로그인 성공 테스트")
        void loginSuccessTest() {
            // given
            String encodedPassword = passwordEncoder.encode(TEST_MEMBER_PASSWORD);
            MemberModel memberModel = new MemberModel(TEST_MEMBER_ID, TEST_MEMBER_USERNAME, TEST_MEMBER_EMAIL, TEST_MEMBER_NICKNAME,
                    encodedPassword, null, MemberRoleEnum.USER, null);
            MessageRequestDto messageRequestDto = new MessageRequestDto();
            messageRequestDto.setReceiverId(TEST_MEMBER_ID);
            messageRequestDto.setContent(TEST_MESSAGE);

            given(memberRepository.findByUsernameOrElseThrow(loginRequestDto.getUsername())).willReturn(memberModel);
            given(jwtProvider.createToken(any(String.class), any(MemberRoleEnum.class))).willReturn("Bearer Token");
            given(messageJpaRepository.findByToNameAndIsReadFalse(TEST_MEMBER_USERNAME)).willReturn(List.of(
                    new Message(messageRequestDto, TEST_ANOTHOER_USERNAME)));
            given(passwordEncoder.matches(TEST_MEMBER_PASSWORD, encodedPassword)).willReturn(true);
            // when
            LoginResponseDto loginResponseDto = memberService.login(loginRequestDto);
            // then

            then(jwtProvider).should(times(1)).createToken(TEST_MEMBER_USERNAME, MemberRoleEnum.USER);
            then(messageJpaRepository).should(times(1)).findByToNameAndIsReadFalse(TEST_MEMBER_USERNAME);
            assertEquals(TEST_MEMBER_USERNAME, loginResponseDto.getUsername());
        }

        @Test
        @DisplayName("로그인 실패 테스트 비밀번호 불일치")
        void loginFailMismatchPasswordTest() {

            // given
            MemberModel memberModel = new MemberModel(TEST_MEMBER_ID, TEST_MEMBER_USERNAME, TEST_MEMBER_EMAIL, TEST_MEMBER_NICKNAME,
                    TEST_MEMBER_PASSWORD, null, MemberRoleEnum.USER, null);
            given(memberRepository.findByUsernameOrElseThrow(loginRequestDto.getUsername())).willReturn(memberModel);

            // when
            String message = assertThrows(CustomMemberException.class, () -> memberService.login(loginRequestDto)).getMessage();

            // then
            assertEquals(MemberErrorCode.MEMBER_ERROR_CODE_PASSWORD_MISMATCH.getMessage(), message);
        }

        @Test
        @DisplayName("로그인 실패 테스트 존재하지 않는 회원")
        void loginFailNotExistedMemberTest() {

            // given
            given(memberRepository.findByUsernameOrElseThrow(loginRequestDto.getUsername())).willThrow(
                    new CustomMemberException(MemberErrorCode.MEMBER_ERROR_CODE_NOT_FOUND)
            );

            // when
            String message = assertThrows(CustomMemberException.class, () -> memberService.login(loginRequestDto)).getMessage();

            // then
            assertEquals(MemberErrorCode.MEMBER_ERROR_CODE_NOT_FOUND.getMessage(), message);
        }
    }

    @Nested
    @DisplayName("회원 단건 조회 테스트")
    class inquiredMemberTest {
        @Test
        @DisplayName("회원 단건 조회 성공 테스트")
        void getMemberSuccessTest() {

            // given
            MemberModel memberModel = new MemberModel(TEST_MEMBER_ID, TEST_MEMBER_USERNAME, TEST_MEMBER_EMAIL, TEST_MEMBER_NICKNAME,
                    TEST_MEMBER_PASSWORD, null, MemberRoleEnum.USER, null);
            given(memberRepository.findByIdOrElseThrow(TEST_MEMBER_ID)).willReturn(memberModel);

            Follow follow = new Follow(1L, TEST_MEMBER_ID, TEST_ANOTHER_MEMBER_ID, null);
            given(followRepository.findByFromId(TEST_MEMBER_ID)).willReturn(
                    List.of(follow));

            // when
            MemberResponseDto memberResponseDto = memberService.getMemeber(TEST_MEMBER_ID);

            // then
            assertEquals(TEST_MEMBER_USERNAME, memberResponseDto.getUsername());
        }

        @Test
        @DisplayName("회원 단건 조회 실패 테스트 존재하지 않는 회원")
        void getMemberFailTest() {

            // given
            given(memberRepository.findByIdOrElseThrow(TEST_MEMBER_ID)).willThrow(
                    new CustomMemberException(MemberErrorCode.MEMBER_ERROR_CODE_NOT_FOUND)
            );

            // when
            String message = assertThrows(CustomMemberException.class,
                    () -> memberService.getMemeber(TEST_MEMBER_ID)).getMessage();

            // then
            assertEquals(MemberErrorCode.MEMBER_ERROR_CODE_NOT_FOUND.getMessage(), message);
        }
    }

    @Nested
    @DisplayName("회원 전체 조회 테스트")
    class inquiredAllMemberTest {
        @Test
        @DisplayName("회원 전체 조회 성공 테스트 관리자 권한")
        void getAllMemberSuccessTest() {

            // given
            MemberModel memberModel = new MemberModel(TEST_MEMBER_ID, TEST_MEMBER_USERNAME, TEST_MEMBER_EMAIL, TEST_MEMBER_NICKNAME,
                    TEST_MEMBER_PASSWORD, null, MemberRoleEnum.USER, null);
            given(memberRepository.findAll()).willReturn(List.of(memberModel));

            Follow follow = new Follow(1L, TEST_MEMBER_ID, TEST_ANOTHER_MEMBER_ID, null);
            given(followRepository.findByFromId(TEST_MEMBER_ID)).willReturn(
                    List.of(follow));

            MemberRoleEnum memberRoleEnum = MemberRoleEnum.ADMIN;

            // when
            List<MemberResponseDto> memberResponseDtoList = memberService.getMemebers(memberRoleEnum);

            // then
            assertEquals(TEST_MEMBER_USERNAME, memberResponseDtoList.get(0).getUsername());
            assertEquals(1L, memberResponseDtoList.get(0).getFollowingCount());
        }

        @Test
        @DisplayName("회원 전체 조회 실패 테스트 일반회원은 조회 불가")
        void getAllMemberFailTest() {

            // given
            MemberRoleEnum memberRoleEnum = MemberRoleEnum.USER;

            // when
            String message = assertThrows(CustomMemberException.class,
                    () -> memberService.getMemebers(memberRoleEnum)).getMessage();

            // then
            assertEquals(MemberErrorCode.MEMBER_ERROR_CODE_NOT_AUTH.getMessage(), message);
        }
    }

    @Nested
    @DisplayName("회원 정보 변경 테스트")
    class updatedMemberTest {
        @Test
        @DisplayName("회원 정보 변경 성공 테스트")
        void updateMemberSuccessTest() {
            // given
            MemberModel memberModel = new MemberModel(TEST_MEMBER_ID, TEST_MEMBER_USERNAME, TEST_MEMBER_EMAIL, TEST_MEMBER_NICKNAME,
                    TEST_ENCODED_PASSWORD, "null", MemberRoleEnum.USER, null);
            given(memberRepository.findByIdOrElseThrow(TEST_MEMBER_ID)).willReturn(memberModel);
            given(passwordEncoder.matches(TEST_MEMBER_PASSWORD, TEST_ENCODED_PASSWORD)).willReturn(true);

            final String newNickname = "newNickname";
            final String newIntroduction = "newIntroduction";
            final String newPassword = "newPassword";
            MemberModel updatedMemberModel = new MemberModel(TEST_MEMBER_ID, TEST_MEMBER_USERNAME, TEST_MEMBER_EMAIL,
                    newNickname, TEST_ENCODED_PASSWORD, newIntroduction, MemberRoleEnum.USER, null);
            given(memberRepository.updateMember(TEST_MEMBER_ID, newNickname, newIntroduction, newPassword))
                    .willReturn(updatedMemberModel);

            MemberUpdateRequestDto memberUpdateRequestDto = new MemberUpdateRequestDto(newNickname, newIntroduction,
                    TEST_MEMBER_PASSWORD, newPassword);
            // when
            memberService.updateMember(TEST_MEMBER_ID, memberUpdateRequestDto);
            MemberModel foundModel = memberRepository.findByIdOrElseThrow(TEST_MEMBER_ID);
            MemberUpdateResponseDto memberUpdateResponseDto = MemberUpdateResponseDto
                    .from(memberRepository.updateMember(foundModel.getMemberId(), newNickname, newIntroduction, newPassword));

            // then
            assertNotEquals(memberModel.getNickname(), newNickname);
            assertNotEquals(memberModel.getIntroduction(), newIntroduction);
            assertEquals(newNickname, memberUpdateResponseDto.getNickname());
            assertEquals(newIntroduction, memberUpdateResponseDto.getIntroduction());
        }

        @Test
        @DisplayName("회원 정보 변경 실패 테스트 비밀번호 불일치")
        void updateMemberFailTest() {
            // given
            MemberModel memberModel = new MemberModel(TEST_MEMBER_ID, TEST_MEMBER_USERNAME, TEST_MEMBER_EMAIL, TEST_MEMBER_NICKNAME,
                    TEST_ENCODED_PASSWORD, "null", MemberRoleEnum.USER, null);
            given(memberRepository.findByIdOrElseThrow(TEST_MEMBER_ID)).willReturn(memberModel);
            given(passwordEncoder.matches(TEST_MEMBER_PASSWORD, TEST_ENCODED_PASSWORD)).willReturn(false);
            final String newNickname = "newNickname";
            final String newIntroduction = "newIntroduction";
            final String newPassword = "newPassword";
            MemberUpdateRequestDto memberUpdateRequestDto = new MemberUpdateRequestDto(newNickname, newIntroduction,
                    TEST_MEMBER_PASSWORD, newPassword);
            // when

            String message = assertThrows(CustomMemberException.class,
                    () -> memberService.updateMember(TEST_MEMBER_ID, memberUpdateRequestDto)).getMessage();
            // then
            assertEquals(MemberErrorCode.MEMBER_ERROR_CODE_PASSWORD_MISMATCH.getMessage(), message);
        }
    }
}