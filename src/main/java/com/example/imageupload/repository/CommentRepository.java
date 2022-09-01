package com.example.imageupload.repository;

import com.example.imageupload.model.Comment;
import com.example.imageupload.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
  List<Comment> findAllByPost(Post post);
  void deleteAllByPost(Post post);
  List<Comment> findAllByUserId(Long id);
}
