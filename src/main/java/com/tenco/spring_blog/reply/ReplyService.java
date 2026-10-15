package com.tenco.spring_blog.reply;

import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.error.Exception404;
import com.tenco.spring_blog.board.Board;
import com.tenco.spring_blog.board.BoardJpaRepository;
import com.tenco.spring_blog.user.SessionUserDto;
import com.tenco.spring_blog.user.User;
import com.tenco.spring_blog.user.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReplyService {
    private final BoardJpaRepository boardJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final ReplyJpaRepository replyJpaRepository;

    // 게시글 댓글 목록(로그인 하지 않았으면 sessionUserId는 null 이다.)
    public List<ReplyResponse.ListDto> findAllByBoardId(Long boardId, Long sessionUserId) {
        List<Reply> replyList = replyJpaRepository.findAllByBoardIdJoinUser(boardId);
        return replyList.stream().map(reply -> new ReplyResponse.ListDto(reply, sessionUserId)).toList();
    }

    // 댓글 작성
    @Transactional
    public void save(ReplyRequest.saveDto saveDto, SessionUserDto sessionUserDto){
        // 게시글 먼저 조회(실제로 있는지 존재 확인)
        Board board = boardJpaRepository.findById(saveDto.getBoardId())
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));
        if(sessionUserDto == null){
            return;
        }
        // 작성자 정보 조회
        User user = userJpaRepository.findById(sessionUserDto.getId())
                .orElseThrow(() -> new Exception404("작성자 정보를 찾을 수 없습니다."));

        // 작성
        replyJpaRepository.save(saveDto.toEntity(board, user));
    }

    // 댓글 한 개 삭제
    @Transactional
    public void deleteById(Long replyId, SessionUserDto sessionUserDto) {
        Reply reply = replyJpaRepository.findById(replyId)
                .orElseThrow(() -> new Exception404("댓글을 찾을 수 없습니다."));
        if (!reply.isOwner(sessionUserDto.getId())) {
            throw new Exception403("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }
        replyJpaRepository.delete(reply);
    }
}
