package com.example.imageupload.repository;

import com.example.imageupload.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
  List<Post> findAllByOrderByModifiedAtDesc();

  List<Post> findAll();

  List<Post> findAllByUserId(Long id);
}
