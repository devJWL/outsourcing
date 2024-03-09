package com.icomfortableworld.domain.feed.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.icomfortableworld.domain.comment.dto.CommentResponseDto;
import com.icomfortableworld.domain.comment.model.CommentModel;
import com.icomfortableworld.domain.comment.repository.CommentRepository;
import com.icomfortableworld.domain.feed.dto.requestDto.FeedRequestDto;
import com.icomfortableworld.domain.feed.dto.responseDto.CommentFeedResponseDto;
import com.icomfortableworld.domain.feed.dto.responseDto.FeedResponseDto;
import com.icomfortableworld.domain.feed.entity.Feed;
import com.icomfortableworld.domain.feed.model.FeedModel;
import com.icomfortableworld.domain.feed.repository.FeedRepository;
import com.icomfortableworld.domain.follow.entity.Follow;
import com.icomfortableworld.domain.follow.repository.FollowRepository;
import com.icomfortableworld.domain.member.entity.MemberRoleEnum;
import com.icomfortableworld.domain.member.model.MemberModel;
import com.icomfortableworld.domain.member.repository.member.MemberRepository;
import com.icomfortableworld.domain.tag.entity.Tag;
import com.icomfortableworld.domain.tag.entity.TagSet;
import com.icomfortableworld.domain.tag.repository.TagRepository;
import com.icomfortableworld.domain.tag.repository.TagSetRepository;
import com.icomfortableworld.domain.feed.exception.CustomFeedException;
import com.icomfortableworld.domain.feed.exception.FeedErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class FeedServiceImpl implements FeedService {

	private final FeedRepository feedRepository;
	private final MemberRepository memberRepository;
	private final CommentRepository commentRepository;
	private final TagRepository tagRepository;
	private final TagSetRepository tagSetRepository;
	private final FollowRepository followRepository;

	@Override
	public void createFeed(FeedRequestDto requestDto, Long memberId) {
		memberRepository.findByIdOrElseThrow(memberId);
		FeedModel feedModel = feedRepository.save(new Feed(requestDto, memberId)).toModel();

		for (String tagName : requestDto.getTagNameList()) {
			TagSet tagSet = tagSetRepository.findByTagName(tagName).orElse(null);
			if (tagSet == null) {
				tagSet = tagSetRepository.save(new TagSet(tagName));
			}
			Tag tag = new Tag(feedModel.getFeedId(), tagSet.getTagSetId());
			tagRepository.save(tag);
		}
	}

	@Override
	public FeedResponseDto updateFeed(Long feedId, FeedRequestDto requestDto, Long memberId,
		MemberRoleEnum memberRole) {
		memberRepository.findByIdOrElseThrow(memberId);
		String content = requestDto.getContent();

		FeedModel feedModel = feedRepository.update(feedId, memberId, content, memberRole);

		return new FeedResponseDto(feedModel.getContent(), new ArrayList<>());
	}

	//관리자 -> 전체조회, 일반 -> 팔로우한 사람만 조회
	@Override
	@Transactional(readOnly = true)
	public List<FeedResponseDto> getAllFeeds(Long memberId, MemberRoleEnum memberRoleEnum) {
		memberRepository.findByIdOrElseThrow(memberId);

		List<FeedResponseDto> response = new ArrayList<>();

		if(memberRoleEnum==MemberRoleEnum.ADMIN){
			List<FeedModel> feedModels = feedRepository.findAll();

			//여기부터 밑 함수
			response.addAll(getAllFeedsIncludingTags(feedModels));
		}else{
			List<Follow> followList = followRepository.findByFromId(memberId);
			for(Follow f : followList){
				Long toId = f.getToId();
				List<FeedModel> feedModels = feedRepository.findByMemberId(toId);
				//이렇게 해도 됨? 위에 코드랑 중복쓰
				response.addAll(getAllFeedsIncludingTags(feedModels));
			}
		}

		return response;
	}

	@Override
	@Transactional(readOnly = true)
	public CommentFeedResponseDto getFeed(Long feedId,Long memberId) {
		memberRepository.findByIdOrElseThrow(memberId);

		FeedModel feedModel = feedRepository.findByIdOrElseThrow(feedId);
		List<Tag> tagList = tagRepository.findAllByFeedId(feedId);
		List<String> tagNameList = new ArrayList<>();

		for (Tag tag : tagList) {
			tagNameList.add(tagSetRepository.findByTagSetId(tag.getTagSetId()).getTagName());
		}
		MemberModel memberModel = memberRepository.findByIdOrElseThrow(feedModel.getMemberId());

		List<CommentModel> commentModelList = commentRepository.findByFeedId(feedId);
		List<CommentResponseDto> commentContentList = new ArrayList<>();
		for(CommentModel commentModel : commentModelList){
			MemberModel mModel =  memberRepository.findByIdOrElseThrow(commentModel.getMemberId());
			commentContentList.add(new CommentResponseDto(commentModel.getContent(),
					mModel.getNickname(),commentModel.getCommentId()));
		}

		return new CommentFeedResponseDto(feedModel.getFeedId(), memberModel.getNickname(),
			feedModel.getContent(), tagNameList, commentContentList);
	}

	@Override
	@Transactional(readOnly = true)
	public List<FeedResponseDto> getSearchResultFeeds(String type, String q, Long memberId) {
		memberRepository.findByIdOrElseThrow(memberId);
		List<FeedResponseDto> responseDtoList = new ArrayList<>();

		if(type.equals("tag")) {
			//tag로 조회
			TagSet tagSet = tagSetRepository.findByTagName(q).orElse(null);
			if (tagSet != null) {
				List<Tag> tagList = tagRepository.findAllByTagSetId(tagSet.getTagSetId());

				for (Tag tag : tagList) {
					FeedModel feedModel = feedRepository.findById(tag.getFeedId()).orElse(null);
					if (feedModel == null) {
						continue;
					}

					MemberModel memberModel = memberRepository.findByIdOrElseThrow(feedModel.getMemberId());
					List<Tag> feedTagList = tagRepository.findAllByFeedId(feedModel.getFeedId());

					List<String> tagNameList = new ArrayList<>();

					for (Tag t : feedTagList) {
						TagSet tSet = tagSetRepository.findByTagSetId(t.getTagSetId());
						tagNameList.add(tSet.getTagName());
					}
					responseDtoList.add(new FeedResponseDto(feedModel.getFeedId(),
						memberModel.getNickname(), feedModel.getContent(), tagNameList));
				}
			}
		}else if(type.equals("content")){
			//단어로 내용 content에서 조회
			List<FeedModel> allFeeds = feedRepository.findAll();
			for (FeedModel feedModel : allFeeds) {
				if (feedModel.getContent().contains(q)) {
					List<Tag> tagList = tagRepository.findAllByFeedId(feedModel.getFeedId());
					List<String> tagNameList = new ArrayList<>();
					for (Tag t : tagList) {
						TagSet tSet = tagSetRepository.findByTagSetId(t.getTagSetId());
						tagNameList.add(tSet.getTagName());
					}
					responseDtoList.add(new FeedResponseDto(feedModel.getFeedId(),
						memberRepository.findByIdOrElseThrow(feedModel.getMemberId()).getNickname(),
						feedModel.getContent(), tagNameList));
				}
			}
		}

		if(responseDtoList.isEmpty()){
			throw new CustomFeedException(FeedErrorCode.FEED_ERROR_CODE_SEARCH_NOT_FOUND);
		}
		return responseDtoList;
	}

	@Override
	public void deleteFeed(Long feedId, Long memberId, MemberRoleEnum authority) {
		memberRepository.findByIdOrElseThrow(memberId);
		commentRepository.deleteByFeedId(feedId);
		feedRepository.deleteById(feedId, memberId, authority);
	}

	public List<FeedResponseDto> getAllFeedsIncludingTags(List<FeedModel> feedModels) {
		List<String> tagNameList = new ArrayList<>();
		List<FeedResponseDto> responseDtoList = new ArrayList<>();

		for (FeedModel feedModel : feedModels) {
			List<Tag> tagList = tagRepository.findAllByFeedId(feedModel.getFeedId());

			for (Tag tag : tagList) {
				tagNameList.add(tagSetRepository.findByTagSetId(tag.getTagSetId()).getTagName());
			}

			MemberModel memberModel = memberRepository.findByIdOrElseThrow(feedModel.getMemberId());

			FeedResponseDto responseDto = new FeedResponseDto(feedModel.getFeedId(), memberModel.getNickname(),
				feedModel.getContent(), tagNameList);
			responseDtoList.add(responseDto);
			tagNameList.clear();
		}
		return responseDtoList;
	}
}
