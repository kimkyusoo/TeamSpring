package com.example.imageupload.model;

import com.example.imageupload.dto.request.CommentRequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SubComment extends Timestamped {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)   //  자동생성되는 id가 기본키가 되어야 식별을 할수 있어서 이렇게 만듬
  private Long id;

  @JoinColumn(name = "user_id", nullable = false)    // user의 id를 가져와서 누가 작성했는지 알기 위해
  @ManyToOne(fetch = FetchType.LAZY)
  private User user;

  @JoinColumn(name = "comment_id", nullable = false)   // comment의 id를 가져와서 어떤 comment에 대한 subcomment인지 알기 위해
  @ManyToOne(fetch = FetchType.LAZY)
  private Comment comment;

  @Column(nullable = false)                           // subcomment의 내용
  private String content;

  @Column(nullable = false)                           // subcomment의 좋아요 갯수
  private int likes;


  public void update(CommentRequestDto commentRequestDto) {        // 클라이언트로 받은 requestdto의 내용으로 content의 내용을 바꿔줌
    this.content = commentRequestDto.getContent();
  }

  public boolean validateMember(User user) {
    return !this.user.equals(user);
  }

  public void updateLikes(int num){
    this.likes = num;
  }
}
