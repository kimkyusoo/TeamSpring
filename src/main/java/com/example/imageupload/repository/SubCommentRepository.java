package com.example.imageupload.repository;

import com.example.imageupload.model.Comment;
import com.example.imageupload.model.SubComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubCommentRepository extends JpaRepository<SubComment, Long> {
  List<SubComment> findAllByComment(Comment comment);
  void deleteAllByComment(Comment comment);

  List<SubComment> findAllByUserId(Long id);
}
