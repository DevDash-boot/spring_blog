package com.tenco.spring_blog.board;

import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.error.Exception404;
import com.tenco.spring_blog.reply.ReplyJpaRepository;
import com.tenco.spring_blog.user.SessionUserDto;
import com.tenco.spring_blog.user.User;
import com.tenco.spring_blog.user.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final UserJpaRepository userJpaRepository;
    private final ReplyJpaRepository replyJpaRepository;

    /*
     * 게시글 저장
     * @param saveDto : 게시글 저장 정보
     * @param sessionUser : 작성자 정보
     * @return : 저장된 게시글 반환
     */

    // 데이터 수정이 필요하므로 트랜잭션 읽기 전용 설정을 해제하고 쓰기 가능한 트랜잭션으로 실행
    @Transactional
    public Board save(BoardRequest.saveDto saveDto, SessionUserDto sessionUser) {
        log.info("게시글 저장 시작 - 제목 : {}, 작성자 : {}", saveDto.getTitle(), sessionUser.getUsername());
        // Board board = saveDto.toEntity(sessionUser);
        User user = userJpaRepository.getReferenceById(sessionUser.getId());
        Board savedBoard = boardJpaRepository.save(saveDto.toEntity(user));
        log.info("게시글 저장 완료 - ID : {}, 제목 : {}", savedBoard.getId(), savedBoard.getTitle());
        return savedBoard;
    }

    /*
     * 게시글 상세 조회
     * @param id : 게시글 PK
     * @return : 게시글 정보 (작성자 정보 포함)
     */
    public BoardResponse.DetailDto findById(Long id) {
        log.info("게시글 상세 조회 서비스 - ID : {}", id);
        // boardJpaRepository.findById(id) 이 메서드 대신 JOIN FETCH를 사용함
        Board board = boardJpaRepository.findByIdJoinUser(id)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));
//        Board board = boardJpaRepository.findByIdJoinUser(id)   // 직접 정의한 쿼리
//                .orElseThrow(() -> {
//                    log.warn("게시글 조회 실패 - ID : {}", id);
//                    return new Exception404("게시글을 찾을 수 없습니다.");
//                });
        // 서비스 단에서 지연 로딩을 하기 위해 미리 호출한다면 Board 엔티티에 User 엔티티가 셋팅되어 Controller로 넘어가서 주석 처리
//        log.info("게시글 상세 조회 완료 - 제목 : {}, 작성자 : {}", board.getTitle(), board.getUser().getUsername());
        return new BoardResponse.DetailDto(board);
    }

    // 게시글 목록 조회 페이징 처리
    public BoardResponse.PageDto findAllPage(int page, int size) {
        log.info("게시글 목록 조회 시작 ");

        // 1부터 시작하는 번호를 0부터로 변환
        int pageIndex = Math.max(0, page - 1);
        // 페이지 크기는 1 ~ 50 정도로 제한(size = 100000 같은 요청 방어)
        int validSize = Math.clamp(size, 1, 50);

        // 최신글이 먼저 오도록 id를 내림차순으로 정렬. 주문서 생성
        Pageable pageable = PageRequest.of(pageIndex, validSize, Sort.by(Sort.Direction.DESC, "id"));
        // 페이징 처리가 된 게시글 목록 조회 요청
        Page<Board> boardPage = boardJpaRepository.findAllJoinUser(pageable);
        // 조회 후 트랜잭션 안에서 응답 DTO로 변환 처리
        return new BoardResponse.PageDto(boardPage);
    }

    // 게시글 수정 화면 요청 (인가 검사)
    public BoardResponse.updateFormDto findByIdAndUpdateForm(Long id, Long userId) {
        Board board = boardJpaRepository.findByIdJoinUser(id)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));
        // 인가 검사
        if (!board.isOwner(userId)) {
            throw new Exception403("게시글 수정 권한이 없습니다.");
        }
        return new BoardResponse.updateFormDto(board);
    }

    /*
     * 게시글 수정 권한 체크
     * @param id : 게시글 PK
     * @param updateDto : 수정할 정보
     * @param sessionUser : 요청자 정보
     * @return : 수정된 게시글
     */
    @Transactional
    public Board updateById(Long id, BoardRequest.updateDto updateDto, SessionUserDto sessionUser) {
        log.info("게시글 수정 시작");
        Board board = boardJpaRepository.findByIdJoinUser(id)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));
        if (!board.isOwner(sessionUser.getId())) { // 2. 권한 체크
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
    public void deleteById(Long id, SessionUserDto sessionUser) {
        log.info("게시글 삭제 시작");
        Board board = boardJpaRepository.findByIdJoinUser(id)
                .orElseThrow(() -> new Exception404("게시글을 찾을 수 없습니다."));
        if (!board.isOwner(sessionUser.getId())) { // 2. 권한 체크
            log.warn("게시글 삭제 권한 없음 - 게시글 ID  : {}, 작성자 : {}, 요청자 : {}",
                    id, board.getUser().getUsername(), sessionUser.getUsername());
            throw new Exception403("본인이 작성한 게시글만 삭제할 수 있습니다");
        }
        replyJpaRepository.deleteByBoardId(id); // 댓글 전체 삭제
        boardJpaRepository.deleteById(id);  // 게시글 삭제
        log.info("게시글 삭제 완료");
    }
}
