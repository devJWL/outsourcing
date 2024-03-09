package com.icomfortableworld.domain.follow.service;

import com.icomfortableworld.domain.follow.entity.Follow;
import com.icomfortableworld.domain.follow.repository.FollowRepository;
import com.icomfortableworld.domain.member.model.MemberModel;
import com.icomfortableworld.domain.member.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final MemberRepository memberRepository;

    @Override
    public void followMember(Long fromId, Long toId) {
        MemberModel follower = memberRepository.findByIdOrElseThrow(fromId);
        MemberModel following = memberRepository.findByIdOrElseThrow(toId);
        if (followRepository.existsByToIdAndFromId(toId, fromId)) {
            throw new IllegalArgumentException("이미 팔로우한 사용자입니다.");
        }
        Follow follow = new Follow();
        follow.setToId(toId);
        follow.setFromId(fromId);
        follow.setCreatedDate(LocalDateTime.now());
        followRepository.save(follow);
    }


    @Override
    public void unfollowMember(Long followId, Long fromId) {
        MemberModel follower = memberRepository.findByIdOrElseThrow(fromId);
        Follow follow = followRepository.findByFollowIdAndFromId(followId, fromId)
                .orElseThrow(() -> new IllegalArgumentException("해당 팔로워를 찾을 수 없습니다."));
        if (!follow.getFromId().equals(fromId)) {
            throw new IllegalArgumentException("자신의 팔로우만 취소할 수 있습니다.");
        }
        followRepository.delete(follow);
    }
}