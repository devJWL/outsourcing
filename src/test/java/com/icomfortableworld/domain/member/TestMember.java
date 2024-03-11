package com.icomfortableworld.domain.member;

public interface TestMember {
    Long TEST_MEMBER_ID = 1L;
    Long TEST_ANOTHER_MEMBER_ID = 2L;
    Long TEST_EXISTING_MEMBER_ID = 1L;
    Long TEST_NOT_EXISTING_MEMBER_ID = -1L;
    String TEST_MEMBER_USERNAME = "un1234";
    String TEST_ANOTHOER_MEMBER_USERNAME = "anun1234";
    String TEST_EXISTING_MEMBER_USERNAME = "existing username";
    String TEST_NOT_EXISTING_MEMBER_USERNAME = "not existing username";
    String TEST_MEMBER_EMAIL = "email@email.com";
    String TEST_NOT_EXISTING_MEMBER_EMAIL = "not_existing@email.com";
    String TEST_MEMBER_NICKNAME = "nickname";
    String TEST_MEMBER_CHANGED_NICKNAME = "changedNickname";
    String TEST_MEMBER_PASSWORD = "password";
    String TEST_MEMBER_INVALID_PASSWORD = "invalidPassword";
    String TEST_MEMBER_ENCODED_PASSWORD = "encodedPassword";
    String TEST_MEMBER_CHANGED_PASSWORD = "changedPassword";
    String TEST_MEMBER_INIT_INTRODUCTION = "initIntroduction";
    String TEST_MEMBER_CHANGED_INTRODUCTION = "changedIntroduction";
    String TEST_ADMIN_TOKEN = "admin_token";
    String TEST_INVALID_ADMIN_TOKEN = "invalid_admin_token";
    String TEST_BAEARER_TOKEN = "Bearer Token";
    String TEST_MESSAGE = "message test";
}
