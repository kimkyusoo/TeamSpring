package com.example.imageupload.service;

import com.example.imageupload.model.*;
import com.example.imageupload.dto.response.*;
import com.example.imageupload.jwt.TokenProvider;
import com.example.imageupload.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;

    private final PostRepository postRepository;

    private final CommentRepository commentRepository;

    private final SubCommentRepository subCommentRepository;

    private final PostHeartRepository postHeartRepository;

    private final CommentHeartRepository commentHeartRepository;

    private final TokenProvider tokenProvider;


    @Transactional(readOnly = true)
    public ResponseDto<?> getMyPage(HttpServletRequest request){
        User user = validateUser(request);
        if (null == user){
            return ResponseDto.fail("INVALID_TOKEN","Token이 유효하지 않습니다");
        }

        // post list dto
        List<Post> postList = postRepository.findAllByUserId(user.getId());
        List<PostResponseDto> postResponseDtoList = new ArrayList<>();

        // comment List dto
        List<Comment> commentList = commentRepository.findAllByUserId(user.getId());
        List<CommentResponseDto> commentResponseDtoList = new ArrayList<>();

        // subComment List dto
        List<SubComment> subCommentList = subCommentRepository.findAllByUserId(user.getId());
        List<SubCommentResponseDto> subCommentResponseDtoList = new ArrayList<>();

        //heart post,comment List dto
        List<PostHeart> postHeart = postHeartRepository.findAllByNickname(user.getNickname());
        List<CommentHeart> commentHeart = commentHeartRepository.findAllByNickname(user.getNickname());

        List<Optional<Post>> postList2 = new ArrayList<>();
        List<Optional<Comment>> commentList2 = new ArrayList<>();

        for(int i = 0; i<postHeart.size(); i++) {
            postList2.add(postRepository.findById(postHeart.get(i).getRequestId()));
        }

        for(int i = 0; i<commentHeart.size(); i++) {
            commentList2.add(commentRepository.findById(commentHeart.get(i).getRequestId()));
        }

        List<PostResponseDto> heartPostResponseDtoList = new ArrayList<>();
        List<CommentResponseDto> heartCommentResponseDtoList = new ArrayList<>();

        for(Post post : postList){
            postResponseDtoList.add(
                    PostResponseDto.builder()
                            .id(post.getId())
                            .title(post.getTitle())
                            .author(post.getUser().getNickname())
                            .content(post.getContent())
                            .likes(post.getLikes())
                            .createdAt(post.getCreatedAt())
                            .modifiedAt(post.getModifiedAt())
                            .build()
            );
        }

        for (Comment comment : commentList) {
            commentResponseDtoList.add(
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

        for(SubComment subComment : subCommentList){
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

        for(Optional<Post> post : postList2){
            heartPostResponseDtoList.add(
                    PostResponseDto.builder()
                            .id(post.get().getId())
                            .title(post.get().getTitle())
                            .author(post.get().getUser().getNickname())
                            .content(post.get().getContent())
                            .likes(post.get().getLikes())
                            .createdAt(post.get().getCreatedAt())
                            .modifiedAt(post.get().getModifiedAt())
                            .build()
            );
        }

        for(Optional<Comment> comment : commentList2){
            heartCommentResponseDtoList.add(
                    CommentResponseDto.builder()
                            .id(comment.get().getId())
                            .author(comment.get().getUser().getNickname())
                            .content(comment.get().getContent())
                            .likes(comment.get().getLikes())
                            .createdAt(comment.get().getCreatedAt())
                            .modifiedAt(comment.get().getModifiedAt())
                            .build()
            );
        }

        return ResponseDto.success(
                MyPageResponseDto.builder()
                        .postResponseDtoList(postResponseDtoList)
                        .commentResponseDtoList(commentResponseDtoList)
                        .subCommentResponseDtoList(subCommentResponseDtoList)
                        .heartPostResponseDtoList(heartPostResponseDtoList)
                        .heartCommentResponseDto(heartCommentResponseDtoList)
                        .build()
        );

    }

    @Transactional
    public User validateUser(HttpServletRequest request) {
        if (!tokenProvider.validateToken(request.getHeader("Refresh-Token"))) {
            return null;
        }
        return tokenProvider.getUserFromAuthentication();
    }


}
