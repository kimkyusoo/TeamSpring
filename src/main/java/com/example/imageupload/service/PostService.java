package com.example.imageupload.service;

import com.example.imageupload.dto.request.PostRequestDto;
import com.example.imageupload.dto.response.CommentResponseDto;
import com.example.imageupload.dto.response.PostResponseDto;
import com.example.imageupload.dto.response.ResponseDto;
import com.example.imageupload.dto.response.SubCommentResponseDto;
import com.example.imageupload.jwt.TokenProvider;
import com.example.imageupload.model.Comment;
import com.example.imageupload.model.Post;

import com.example.imageupload.model.PostHeart;
import com.example.imageupload.model.SubComment;
import com.example.imageupload.repository.CommentRepository;
import com.example.imageupload.repository.PostHeartRepository;
import com.example.imageupload.repository.PostRepository;
import com.example.imageupload.repository.SubCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;

  private final PostHeartRepository heartRepository;
  private final CommentRepository commentRepository;
  private final SubCommentRepository subCommentRepository;
  private final TokenProvider tokenProvider;

  @Transactional
  public ResponseDto<?> createPost(PostRequestDto requestDto, HttpServletRequest request) {
    if (null == request.getHeader("Refresh-Token")) {
      return ResponseDto.fail("MEMBER_NOT_FOUND",
              "로그인이 필요합니다.");
    }

    if (null == request.getHeader("Authorization")) {
      return ResponseDto.fail("MEMBER_NOT_FOUND",
              "로그인이 필요합니다.");
    }
    User user = validateUser(request);
    if (null == user) {
      return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
    }
    Post post = Post.builder()
            .title(requestDto.getTitle())
            .content(requestDto.getContent())
            .user(user)
            .build();
    postRepository.save(post);
    return ResponseDto.success(
            PostResponseDto.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .author(post.getUser().getNickname())
                    .likes(post.getLikes())
                    .createdAt(post.getCreatedAt())
                    .modifiedAt(post.getModifiedAt())
                    .build()
    );
  }

  @Transactional(readOnly = true)
  public ResponseDto<?> getPost(Long id) {
    Post post = isPresentPost(id);
    if (null == post) {
      return ResponseDto.fail("NOT_FOUND", "존재하지 않는 게시글 id 입니다.");
    }
  List<Comment> commentList = commentRepository.findAllByPost(post);
  List<CommentResponseDto> commentResponseDtoList = new ArrayList<>();

    for(Comment comment : commentList) {

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

    return ResponseDto.success(
            PostResponseDto.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .likes(post.getLikes())
                    .commentResponseDtoList(commentResponseDtoList)
                    .author(post.getUser().getNickname())
                    .createdAt(post.getCreatedAt())
                    .modifiedAt(post.getModifiedAt())
                    .build()
    );
  }

  @Transactional(readOnly = true)
  public ResponseDto<?> getAllPost() {
    return ResponseDto.success(postRepository.findAllByOrderByModifiedAtDesc());
  }


  @Transactional
  public ResponseDto<?> updatePost(Long id, PostRequestDto postRequestDto, HttpServletRequest request) {
    if (null == request.getHeader("Refresh-Token")) {
      return ResponseDto.fail("User_NOT_FOUND",
              "로그인이 필요합니다.");
    }

    if (null == request.getHeader("Authorization")) {
      return ResponseDto.fail("User_NOT_FOUND",
              "로그인이 필요합니다.");
    }
    User user = validateUser(request);
    if (null == user) {
      return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
    }

    Post post = isPresentPost(id);
    if (null == post) {
      return ResponseDto.fail("NOT_FOUND", "존재하지 않는 게시글 id 입니다.");
    }

    if (post.validateUser(user)) {
      return ResponseDto.fail("BAD_REQUEST", "작성자만 수정할 수 있습니다.");
    }

    post.update(new PostRequestDto());
    return ResponseDto.success(post);
  }

  @Transactional
  public ResponseDto<?> deletePost(Long id, HttpServletRequest request) {
    if (null == request.getHeader("Refresh-Token")) {
      return ResponseDto.fail("MEMBER_NOT_FOUND",
              "로그인이 필요합니다.");
    }

    if (null == request.getHeader("Authorization")) {
      return ResponseDto.fail("MEMBER_NOT_FOUND",
              "로그인이 필요합니다.");
    }

    User user = validateUser(request);
    if (null == user) {
      return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
    }

    Post post = isPresentPost(id);
    if (null == post) {
      return ResponseDto.fail("NOT_FOUND", "존재하지 않는 게시글 id 입니다.");
    }

    if (post.validateUser(user)) {
      return ResponseDto.fail("BAD_REQUEST", "작성자만 삭제할 수 있습니다.");
    }

    postRepository.delete(post);
    return ResponseDto.success("delete success");
  }

  @Transactional(readOnly = true)
  public Post isPresentPost(Long id) {
    Optional<Post> optionalPost = postRepository.findById(id);
    return optionalPost.orElse(null);
  }

  @Transactional
  public User validateUser(HttpServletRequest request) {
    if (!tokenProvider.validateToken(request.getHeader("Access-Token"))) {
      return null;
    }
    return tokenProvider.getUserFromAuthentication();
  }

  public PostHeart isPresentHeart(Long postId, String nickname) {
    Optional< PostHeart> optionalHeart = heartRepository.findByRequestIdAndNickname(postId,nickname);
    return optionalHeart.orElse(null);
  }

  @Transactional
  public ResponseDto<?> likePost(Long id, HttpServletRequest request) {

    if (null == request.getHeader("Refresh-Token")) {
      return ResponseDto.fail("MEMBER_NOT_FOUND",
              "로그인이 필요합니다.");
    }

    if (null == request.getHeader("Authorization")) {
      return ResponseDto.fail("MEMBER_NOT_FOUND",
              "로그인이 필요합니다.");
    }

    User user = validateUser(request);
    if (null == user) {
      return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
    }

    Post post = isPresentPost(id);
    if (null == post) {
      return ResponseDto.fail("NOT_FOUND", "존재하지 않는 게시글 id 입니다.");
    }

    PostHeart heart = isPresentHeart(post.getId(), user.getUsername());

    if(null == heart)
      heartRepository.save(PostHeart.builder().requestId(post.getId()).nickname(user.getUsername()).build());
    else
      heartRepository.delete(heart);

    post.updateLikes(heartRepository.findAllByRequestId(post.getId()).size());

    return ResponseDto.success("like success");
  }

  @Transactional // 메소드 동작이 SQL 쿼리문임을 선언합니다.
  public Long delete(Long id, PostRequestDto requestDto) {
    Post post = postRepository.findById(id).orElseThrow(
            () -> new NullPointerException("해당 게시글이 존재하지 않습니다.")
    );
    post.delete(requestDto);
    return id;
  }
}
