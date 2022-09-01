package com.example.imageupload.service;

import com.example.imageupload.dto.request.CommentRequestDto;
import com.example.imageupload.dto.response.ResponseDto;
import com.example.imageupload.dto.response.SubCommentResponseDto;
import com.example.imageupload.model.*;
import com.example.imageupload.jwt.TokenProvider;
import com.example.imageupload.repository.SubCommentHeartRepository;
import com.example.imageupload.repository.SubCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubCommentService {

  private final SubCommentRepository subCommentRepository;

  private final TokenProvider tokenProvider;
  private final CommentService commentService;
  private final SubCommentHeartRepository heartRepository;

  @Transactional
  public ResponseDto<?> createSubComment(CommentRequestDto requestDto, HttpServletRequest request) {   // subcomment를 만들때도 id랑 content만 있으면 되니 이미 만들어져있는 CommentRequestDto를 이용한다.
    if (null == request.getHeader("Refresh-Token")) {                                            // header부분에 토큰이 있는지 확인
      return ResponseDto.fail("MEMBER_NOT_FOUND",
          "로그인이 필요합니다.");
    }

    if (null == request.getHeader("Authorization")) {
      return ResponseDto.fail("MEMBER_NOT_FOUND",
          "로그인이 필요합니다.");
    }

    User user = validateMember(request);
    if (null == user) {
      return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
    }

    Comment comment = commentService.isPresentComment(requestDto.getRequestId());
    if (null == comment) {
      return ResponseDto.fail("NOT_FOUND", "존재하지 않는 게시글 id 입니다.");
    }

    SubComment subComment = SubComment.builder()
        .user(user)
        .comment(comment)
        .content(requestDto.getContent())
        .build();
    subCommentRepository.save(subComment);
    return ResponseDto.success(
        SubCommentResponseDto.builder()
            .id(subComment.getId())
            .author(subComment.getUser().getNickname())
            .content(subComment.getContent())
            .likes(subComment.getLikes())
            .createdAt(subComment.getCreatedAt())
            .modifiedAt(subComment.getModifiedAt())
            .build()
    );
  }

  @Transactional(readOnly = true)
  public ResponseDto<?> getAllSubCommentsByComment(Long subcommentId) {
    Comment comment = commentService.isPresentComment(subcommentId);
    if (null == comment) {
      return ResponseDto.fail("NOT_FOUND", "존재하지 않는 댓글 id 입니다.");
    }

    List<SubComment> subCommentList = subCommentRepository.findAllByComment(comment);
    List<SubCommentResponseDto> commentResponseDtoList = new ArrayList<>();

    for (SubComment subComment : subCommentList) {
      commentResponseDtoList.add(
          SubCommentResponseDto.builder()
              .id(subComment.getId())
              .author(subComment.getUser().getNickname())
              .content(subComment.getContent())
              .likes(subComment.getLikes())
              .createdAt(subComment.getCreatedAt())
              .modifiedAt(subComment.getModifiedAt())
              .build()
      );
    }
    return ResponseDto.success(commentResponseDtoList);
  }

  @Transactional
  public ResponseDto<?> updateSubComment(Long id, CommentRequestDto requestDto, HttpServletRequest request) {
    if (null == request.getHeader("Refresh-Token")) {
      return ResponseDto.fail("MEMBER_NOT_FOUND",
          "로그인이 필요합니다.");
    }

    if (null == request.getHeader("Authorization")) {
      return ResponseDto.fail("MEMBER_NOT_FOUND",
          "로그인이 필요합니다.");
    }

    User user = validateMember(request);
    if (null == user) {
      return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
    }

    Comment comment = commentService.isPresentComment(requestDto.getRequestId());
    if (null == comment) {
      return ResponseDto.fail("NOT_FOUND", "존재하지 않는 댓글 id 입니다.");
    }

    SubComment subComment = isPresentSubComment(id);
    if (null == subComment) {
      return ResponseDto.fail("NOT_FOUND", "존재하지 않는 대댓글 id 입니다.");
    }

    if (subComment.validateMember(user)) {
      return ResponseDto.fail("BAD_REQUEST", "작성자만 수정할 수 있습니다.");
    }

    subComment.update(requestDto);
    return ResponseDto.success(
        SubCommentResponseDto.builder()
            .id(subComment.getId())
            .author(subComment.getUser().getNickname())
            .content(subComment.getContent())
            .likes(subComment.getLikes())
            .createdAt(subComment.getCreatedAt())
            .modifiedAt(subComment.getModifiedAt())
            .build()
    );
  }

  @Transactional
  public ResponseDto<?> deleteSubComment(Long id, HttpServletRequest request) {
    if (null == request.getHeader("Refresh-Token")) {
      return ResponseDto.fail("MEMBER_NOT_FOUND",
          "로그인이 필요합니다.");
    }

    if (null == request.getHeader("Authorization")) {
      return ResponseDto.fail("MEMBER_NOT_FOUND",
          "로그인이 필요합니다.");
    }

    User user = validateMember(request);                                 // member에 토큰을 넣어줘서 확인함
    if (null == user) {
      return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");  // member에 해당하는 토큰이 없으면
    }

    SubComment subComment = isPresentSubComment(id);
    if (null == subComment) {
      return ResponseDto.fail("NOT_FOUND", "존재하지 않는 대댓글 id 입니다.");  // subcomment에 해당 subcommet id를 넣어서 존재하는지 확인
    }

    if (subComment.validateMember(user)) {
      return ResponseDto.fail("BAD_REQUEST", "작성자만 수정할 수 있습니다.");    // 작성자 본인인지 확인
    }

    subCommentRepository.delete(subComment);
    return ResponseDto.success("success");
  }

  @Transactional(readOnly = true)
  public SubComment isPresentSubComment(Long id) {
    Optional<SubComment> optionalSubComment = subCommentRepository.findById(id);
    return optionalSubComment.orElse(null);
  }

  @Transactional
  public User validateMember(HttpServletRequest request) {
    if (!tokenProvider.validateToken(request.getHeader("Refresh-Token"))) {
      return null;
    }
    return tokenProvider.getUserFromAuthentication();
  }


  public SubCommentHeart isPresentHeart(Long subCommentId, String nickname) {
    Optional<SubCommentHeart> optionalHeart = heartRepository.findByRequestIdAndNickname(subCommentId,nickname);
    return optionalHeart.orElse(null);
  }

  @Transactional
  public ResponseDto<?> likeSubComment(Long id, HttpServletRequest request) {

    if (null == request.getHeader("Refresh-Token")) {
      return ResponseDto.fail("MEMBER_NOT_FOUND",
              "로그인이 필요합니다.");
    }

    if (null == request.getHeader("Authorization")) {
      return ResponseDto.fail("MEMBER_NOT_FOUND",
              "로그인이 필요합니다.");
    }

    User user = validateMember(request);
    if (null == user) {
      return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
    }

    SubComment subComment = isPresentSubComment(id);
    if (null == subComment) {
      return ResponseDto.fail("NOT_FOUND", "존재하지 않는 대댓글 id 입니다.");
    }

    SubCommentHeart subCommentHeart = isPresentHeart(subComment.getId(), user.getNickname());
    if(null == subCommentHeart)
      heartRepository.save(SubCommentHeart.builder().requestId(subComment.getId()).nickname(user.getNickname()).build());
    else
      heartRepository.delete(subCommentHeart);

    subComment.updateLikes(heartRepository.findAllByRequestId(subComment.getId()).size());

    return ResponseDto.success("like success");
  }

}
