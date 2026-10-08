package com.tenco.spring_blog.board;

import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.error.Exception404;
import com.tenco.spring_blog.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Board 관련 비즈니스 로직을 처리하는 Service 계층

@Slf4j
@RequiredArgsConstructor
@Service // IoC (싱글톤 패턴으로 관리됨)
// 모든 메서드를 읽기 전용 트랜잭션으로 실행(finaAll, findById에 적합), 성능 최적화,
// 변경 감지 비활성화, 데이터 수정 감지, 데이터베이스 락을 최소화해 동시성 성능 개선
@Transactional(readOnly = true)
public class BoardService {
    private final BoardJpaRepository boardJpaRepository;

    /*
     * 게시글 저장
     * @param saveDto : 게시글 저장 정보
     * @param sessionUser : 작성자 정보
     * @return : 저장된 게시글 반환
     */

    // 데이터 수정이 필요하므로 트랜잭션 읽기 전용 설정을 해제하고 쓰기 가능한 트랜잭션으로 실행
    @Transactional
    public Board save(BoardRequest.saveDto saveDto, User sessionUser) {
        log.info("게시글 저장 시작 - 제목 : {}, 작성자 : {}", saveDto.getTitle(), sessionUser.getUsername());
        Board board = saveDto.toEntity(sessionUser);
        Board savedBoard = boardJpaRepository.save(board);
        log.info("게시글 저장 완료 - ID : {}, 제목 : {}", savedBoard.getId(), savedBoard.getTitle());
        return savedBoard;
    }

    /*
     * 게시글 상세 조회
     * @param id : 게시글 PK
     * @return : 게시글 정보 (작성자 정보 포함)
     */
    public Board findById(Long id) {
        log.info("게시글 상세 조회 서비스 - ID : {}", id);
        // boardJpaRepository.findById(id) 이 메서드 대신 JOIN FETCH를 사용함
        Board board = boardJpaRepository.findByIdJoinUser(id)   // 직접 정의한 쿼리
                .orElseThrow(() -> {
                    log.warn("게시글 조회 실패 - ID : {}", id);
                    return new Exception404("게시글을 찾을 수 없습니다.");
                });
        log.info("게시글 상세 조회 완료 - 제목 : {}, 작성자 : {}", board.getTitle(), board.getUser().getUsername());
        return board;
    }

    /*
     * 게시글 목록 조회(메인 페이지용)
     * @return : 게시글 목록(작성자 정보 포함)
     */
    public List<Board> findAll(){
        log.info("게시글 목록 조회 시작 ");
        List<Board> boardList = boardJpaRepository.findAllJoinUser();
        log.info("게시글 목록 조회 완료 ");
        return boardList;
    }

    /*
     * 게시글 수정 권한 체크
     * @param id : 게시글 PK
     * @param updateDto : 수정할 정보
     * @param sessionUser : 요청자 정보
     * @return : 수정된 게시글
     */
    @Transactional
    public Board updateById(Long id, BoardRequest.updateDto updateDto, User sessionUser){
        log.info("게시글 수정 시작");
        Board board = findById(id); // 1. 게시글 조회
        if(!board.isOwner(sessionUser.getId())){ // 2. 권한 체크
            throw new Exception403("본인이 작성한 게시글만 수정할 수 있습니다");
        }
        board.update(updateDto); // 3. 더티 채킹을 활용한 수정
        log.info("게시글 수정 완료");
        return board;
    }

    /*
     * 게시글 삭제 권한 체크
     * @param id : 게시글 PK
     * @param sessionUser : 요청자 정보
     */
    @Transactional
    public void deleteById(Long id, User sessionUser){
        log.info("게시글 삭제 시작");
        Board board = findById(id); // 1. 게시글 조회
        if(!board.isOwner(sessionUser.getId())){ // 2. 권한 체크
            log.warn("게시글 삭제 권한 없음 - 게시글 ID  : {}, 작성자 : {}, 요청자 : {}",
                    id, board.getUser().getUsername(), sessionUser.getUsername());
            throw new Exception403("본인이 작성한 게시글만 삭제할 수 있습니다");
        }
        boardJpaRepository.deleteById(id);
        log.info("게시글 삭제 완료");
    }
}
