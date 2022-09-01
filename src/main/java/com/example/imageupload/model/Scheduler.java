package com.example.imageupload.model;

import com.example.imageupload.dto.request.PostRequestDto;
import com.example.imageupload.repository.PostRepository;
import com.example.imageupload.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor // final 멤버 변수를 자동으로 생성합니다.
@Component // 스프링이 필요 시 자동으로 생성하는 클래스 목록에 추가합니다.
public class Scheduler {

    private final PostRepository postRepository;
    private final PostRequestDto postRequestDto;
    private  final PostService postService;


    // 초, 분, 시, 일, 월, 주 순서
    @Scheduled(cron = "0 0 1 * * *")
    public void deletePost() throws InterruptedException {
        System.out.println("게시글 삭제");
        // 저장된 모든 게시글을 조회합니다.
        List<Post> postList = postRepository.findAll();
        for (int i=0; i<postList.size(); i++) {
            // 1초에 한 게시글 씩 조회합니다
            TimeUnit.SECONDS.sleep(1);

            // i 번째 게시긓을 꺼냅니다. + 게시글이 0개일 경우 게시글을 삭제한다.
            Post p = postList.get(i);
            if (Post.comment_count == 0) {
               Long id = p.getId();
               postService.delete(id, postRequestDto );

             }
        }
    }
}
