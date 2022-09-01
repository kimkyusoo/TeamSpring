package com.example.imageupload.repository;

import com.example.imageupload.model.CommentHeart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface CommentHeartRepository extends JpaRepository<CommentHeart, Long> {
  Optional<CommentHeart> findByRequestIdAndNickname(Long CommentId, String Nickname);
  List<CommentHeart> findAllByRequestId(Long RequestId);

  List<CommentHeart> findAllByNickname(String Nickname);
}
