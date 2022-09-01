package com.example.imageupload.repository;

import com.example.imageupload.model.SubCommentHeart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface SubCommentHeartRepository extends JpaRepository<SubCommentHeart, Long> {
  Optional<SubCommentHeart> findByRequestIdAndNickname(Long SubCommentId, String Nickname);
  List<SubCommentHeart> findAllByRequestId(Long RequestId);
}
