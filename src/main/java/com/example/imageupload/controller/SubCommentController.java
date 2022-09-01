package com.example.imageupload.controller;

import com.example.imageupload.dto.request.CommentRequestDto;
import com.example.imageupload.dto.response.ResponseDto;
import com.example.imageupload.service.SubCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Validated
@RequiredArgsConstructor
@RestController
public class SubCommentController {

  private final SubCommentService subCommentService;

  @RequestMapping(value = "/api/auth/subComment", method = RequestMethod.POST)
  public ResponseDto<?> createSubComment(@RequestBody CommentRequestDto requestDto, HttpServletRequest request) {  // subcomment를 만들때도 id랑 content만 있으면 되니 이미 만들어져있는 CommentRequestDto를 이용한다.HttpServletRequest 토큰은 헤더 부분에 있어서 헤더 부분을확인하는것
    return subCommentService.createSubComment(requestDto, request);                                                // subCommentService에 있는 createSubComment 메소드에 위에 값들을 넣어 반환한다.
  }

  @RequestMapping(value = "/api/subComment/{id}", method = RequestMethod.GET)
  public ResponseDto<?> getAllSubComments(@PathVariable Long id) {
    return subCommentService.getAllSubCommentsByComment(id);
  }

  @RequestMapping(value = "/api/auth/subComment/{id}", method = RequestMethod.PUT)
  public ResponseDto<?> updateSubComment(@PathVariable Long id, @RequestBody CommentRequestDto requestDto, HttpServletRequest request) {
    return subCommentService.updateSubComment(id, requestDto, request);
  }

  @RequestMapping(value = "/api/auth/subComment/{id}", method = RequestMethod.DELETE)
  public ResponseDto<?> deleteSubComment(@PathVariable Long id, HttpServletRequest request) {
    return subCommentService.deleteSubComment(id, request);
  }

  @RequestMapping(value = "/api/auth/subComment/{id}", method = RequestMethod.POST)
  public ResponseDto<?> likeSubComment(@PathVariable Long id, HttpServletRequest request) {
    return subCommentService.likeSubComment(id, request);
  }
}
