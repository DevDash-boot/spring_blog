package com.tenco.spring_blog.board;

import com.tenco.spring_blog._core.error.Exception403;
import com.tenco.spring_blog._core.error.Exception404;
import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.user.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor // DI 처리
public class BoardController {
    // DI 처리
    private final BoardPersistRepository boardPersistRepository;

    // 조회하기
    // GET - http://localhost:8080/ , http://localhost:8080/board/list
    @GetMapping({"/", "/board/list"})
    public String list(Model model) {
        List<Board> boardList = boardPersistRepository.findAll();
        model.addAttribute("boardList", boardList);
        return "board/list";
    }

    // 상세보기
    // GET - http://localhost:8080/board/3
    // excludePathPatterns로 제외되어 로그인 없이 접근 가능
    @GetMapping("/board/{id}")
    public String detail(@PathVariable(name = "id") Long id, Model model) {
        Board boardEntity = boardPersistRepository.findById(id);
        if (boardEntity == null) {
            throw new Exception404("게시글을 찾을 수 없습니다. " + id);
        }
        model.addAttribute("board", boardEntity);
        return "board/detail";
    }

    // GET - http://localhost:8080/board/save (화면 요청)
    @GetMapping("/board/save")
    public String saveForm(HttpSession session) {
        // 인터셉터에서 인증검사 진행됨
        return "board/save-form";
    }

    // 게시글 작성하기
    // POST - http://localhost:8080/board/save
    // Spring이 폼 데이터를 객체로 변환하는 과정(데이터 바인딩 메커니즘)
    // 폼 데이터 바인딩 : Spring이 HTTP 요청 파라미터를 객체로 자동 변환
    @PostMapping("/board/save")
    public String save(BoardRequest.saveDto saveDto, HttpSession session) {
        // 1. 인증 검사
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);

        // 2. 유효성 검사
        // 입력 데이터 검증
        saveDto.validate();
        // DTO에서 Board 객체 생성
        Board board = saveDto.toEntity(sessionUser);
        // Board 저장
        Board savedBoard = boardPersistRepository.save(board);
        // 저장 성공시 메인 페이지로 이동
        return "redirect:/";

    }

    // 게시글 수정 화면 요청
    // form 태그는 get과 post밖에 못한다.
    // GET - http://localhost:8080/board/1/update
    @GetMapping("/board/{id}/update")
    public String updateForm(@PathVariable Long id, Model model, HttpSession session) {
        // 1. 인증 검사(로그인 여부 확인)
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);

        // 2. 권한 체크를 위한 게시글 조회
        Board board = boardPersistRepository.findById(id);

        // 3. 권한 체크 : 본인인 작성한 게시글만 수정 가능
        if (!board.isOwner(sessionUser.getId())) {
            throw new Exception403("수정 권한이 없습니다.");
        }
        model.addAttribute("board", board);
        // 수정하기 화면 요청 (조회 기능)
        return "board/update-form";
    }

    // 게시글 수정 기능 요청
    // POST - http://localhost:8080/board/1/update
    @PostMapping("/board/{id}/update")
    public String update(@PathVariable Long id, BoardRequest.updateDto updateDto, HttpSession session) {
        // 여기에서도 하는 이유 : 시간이 지나 로그인이 풀릴 경우를 대비, 다른 경로로 접근했을 때를 대비
        // 1. 인증 검사
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);

        // 2 권한 검사
        Board boardEntity = boardPersistRepository.findById(id);

        // 3. 권한 체크 : 본인인 작성한 게시글만 수정 가능
        if (!boardEntity.isOwner(sessionUser.getId())) {
            throw new Exception403("수정 권한이 없습니다.");
        }

        // 4. 유효성 검사
        updateDto.validate();

        // 5. 더티 체킹을 통한 수정 실행
        boardPersistRepository.updateById(id, updateDto);

        // 6. 수정 완료 후 해당 게시글 상세보기로 리다이렉트 처리
        return "redirect:/board/" + id;
    }

    // 게시글 삭제
    // form 태그는 get과 post밖에 못한다.
    // POST - http://localhost:8080/board/1/delete
    @PostMapping("/board/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) {
        // 1. 인증 검사(로그인 여부 확인)
        User sessionUser = (User) session.getAttribute(Define.SESSION_USER);

        // 2. 권한 확인(로그인 했지만 내가 작성한 글인지 여부 확인)
        // (1) 삭제할 게시글 조회(권한 체크)
        Board boardEntity = boardPersistRepository.findById(id);
        // 권한 체크 : 본인이 작성한 게시글만 삭제(세션 id와 board의 id 비교)
        if (!boardEntity.isOwner(sessionUser.getId())) {
            throw new Exception403("삭제 권한이 없습니다.");
        }
        // (2) 권한 확인 후 삭제 실행
        boardPersistRepository.deleteById(id);

        // (3) 삭제 성공 후 메인 페이지로 리다이렉트
        return "redirect:/";
        // 3. 관리자가 광고성 게시글이나 잘못된 내용에 대한 삭제 가능(권한) - 후추
    }
}
