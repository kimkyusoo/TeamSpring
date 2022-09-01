package com.example.imageupload.service;

import com.example.imageupload.dto.request.CommentRequestDto;
import com.example.imageupload.dto.response.CommentResponseDto;
import com.example.imageupload.dto.response.ResponseDto;
import com.example.imageupload.dto.response.SubCommentResponseDto;
import com.example.imageupload.jwt.TokenProvider;
import com.example.imageupload.model.*;
import com.example.imageupload.repository.CommentHeartRepository;
import com.example.imageupload.repository.CommentRepository;
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
public class CommentService {

  private final CommentRepository commentRepository;
  private final TokenProvider tokenProvider;
  private final PostService postService;
  private final CommentHeartRepository heartRepository;
  private final SubCommentRepository subCommentRepository;

  @Transactional
  public ResponseDto<?> createComment(CommentRequestDto requestDto, HttpServletRequest request) {
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

    Post post = postService.isPresentPost(requestDto.getRequestId());
    if (null == post) {
      return ResponseDto.fail("NOT_FOUND", "존재하지 않는 게시글 id 입니다.");
    }

    Comment comment = Comment.builder()
        .user(user)
        .post(post)
        .content(requestDto.getContent())
        .build();
    commentRepository.save(comment);

    return ResponseDto.success(
        CommentResponseDto.builder()
            .id(comment.getId())
            .author(comment.getUser().getNickname())
            .content(comment.getContent())
            .likes(comment.getLikes())
            .createdAt(comment.getCreatedAt())
            .modifiedAt(comment.getModifiedAt())
            .build()
    );
  }

  @Transactional(readOnly = true)
  public ResponseDto<?> getAllCommentsByPost(Long requestId) {
    Post post = postService.isPresentPost(requestId);
    if (null == post) {
      return ResponseDto.fail("NOT_FOUND", "존재하지 않는 게시글 id 입니다.");
    }

    List<Comment> commentList = commentRepository.findAllByPost(post);
    List<CommentResponseDto> commentResponseDtoList = new ArrayList<>();

    for (Comment comment : commentList) {

      List<SubComment> subCommentList = subCommentRepository.findAllByComment(comment);
      List<SubCommentResponseDto> subCommentResponseDtoList = new ArrayList<>();
      for (SubComment subComment : subCommentList) {
        subCommentResponseDtoList.add(
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

      commentResponseDtoList.add(
              CommentResponseDto.builder()
                      .id(comment.getId())
                      .author(comment.getUser().getNickname())
                      .content(comment.getContent())
                      .likes(comment.getLikes())
                      .createdAt(comment.getCreatedAt())
                      .modifiedAt(comment.getModifiedAt())
                      .SubCommentResponseDtoList(subCommentResponseDtoList)
                      .build()
      );
    }
    return ResponseDto.success(commentResponseDtoList);
  }

  @Transactional
  public ResponseDto<?> updateComment(Long id, CommentRequestDto requestDto, HttpServletRequest request) {
    if (null == request.getHeader("Access-Token")) {
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

    Post post = postService.isPresentPost(requestDto.getRequestId());
    if (null == post) {
      return ResponseDto.fail("NOT_FOUND", "존재하지 않는 게시글 id 입니다.");
    }

    Comment comment = isPresentComment(id);
    if (null == comment) {
      return ResponseDto.fail("NOT_FOUND", "존재하지 않는 댓글 id 입니다.");
    }

    if (comment.validateMember(user)) {
      return ResponseDto.fail("BAD_REQUEST", "작성자만 수정할 수 있습니다.");
    }

    comment.update(requestDto);
    return ResponseDto.success(
        CommentResponseDto.builder()
            .id(comment.getId())
            .author(comment.getUser().getNickname())
            .content(comment.getContent())
            .likes(comment.getLikes())
            .createdAt(comment.getCreatedAt())
            .modifiedAt(comment.getModifiedAt())
            .build()
    );
  }

  @Transactional
  public ResponseDto<?> deleteComment(Long id, HttpServletRequest request) {
    if (null == request.getHeader("Access-Token")) {
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

    Comment comment = isPresentComment(id);
    if (null == comment) {
      return ResponseDto.fail("NOT_FOUND", "존재하지 않는 댓글 id 입니다.");
    }

    if (comment.validateMember(user)) {
      return ResponseDto.fail("BAD_REQUEST", "작성자만 수정할 수 있습니다.");
    }

    commentRepository.delete(comment);
    return ResponseDto.success("success");
  }

  @Transactional(readOnly = true)
  public Comment isPresentComment(Long id) {
    Optional<Comment> optionalComment = commentRepository.findById(id);
    return optionalComment.orElse(null);
  }

  @Transactional
  public User validateMember(HttpServletRequest request) {
    if (!tokenProvider.validateToken(request.getHeader("Refresh-Token"))) {
      return null;
    }
    return tokenProvider.getUserFromAuthentication();
  }

  public CommentHeart isPresentHeart(Long commentId, String nickname) {
    Optional<CommentHeart> optionalHeart = heartRepository.findByRequestIdAndNickname(commentId,nickname);
    return optionalHeart.orElse(null);
  }

  @Transactional
  public ResponseDto<?> likeComment(Long id, HttpServletRequest request) {

    if (null == request.getHeader("Access-Token")) {
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

    Comment comment = isPresentComment(id);
    if (null == comment) {
      return ResponseDto.fail("NOT_FOUND", "존재하지 않는 게시글 id 입니다.");
    }

    CommentHeart commentHeart = isPresentHeart(comment.getId(), user.getNickname());
    if(null == commentHeart)
      heartRepository.save(CommentHeart.builder().requestId(comment.getId()).nickname(user.getNickname()).build());
    else
      heartRepository.delete(commentHeart);

    comment.updateLikes(heartRepository.findAllByRequestId(comment.getId()).size());

    return ResponseDto.success("like success");
  }

}
