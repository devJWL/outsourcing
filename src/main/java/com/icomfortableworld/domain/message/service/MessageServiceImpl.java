package com.icomfortableworld.domain.message.service;

import com.icomfortableworld.domain.member.model.MemberModel;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.icomfortableworld.domain.member.repository.member.MemberRepository;
import com.icomfortableworld.domain.message.dto.request.MessageRequestDto;
import com.icomfortableworld.domain.message.entity.Message;
import com.icomfortableworld.domain.message.repository.MessageJpaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageServiceImpl implements MessageService {

    private final MessageJpaRepository messageRepository;
    private final MemberRepository memberRepository;

    @Override
    public void sendMessage(MessageRequestDto requestDto, Long senderId) {
        Long receiverId = requestDto.getReceiverId();

        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("자신에게 메시지를 보낼 수 없습니다.");
        }
        MemberModel receiver = memberRepository.findByIdOrElseThrow(receiverId);
        MemberModel sender = memberRepository.findByIdOrElseThrow(senderId);

        Message message = new Message();
        message.setFromName(sender.getUsername());
        message.setToName(receiver.getUsername());
        message.setContent(requestDto.getContent());

        messageRepository.save(message);
    }
}
