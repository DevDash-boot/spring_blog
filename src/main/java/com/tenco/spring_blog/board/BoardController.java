package com.tenco.spring_blog.board;

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
    //private final BoardNativeRepository boardNativeRepository;
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
    @GetMapping("/board/{id}")
    public String detail(@PathVariable(name = "id") Long id, Model model) {
       Board boardEntity = boardPersistRepository.findById(id);
       //Board boardEntity = boardPersistRepository.findByIdwithJPQL(id);
       if(boardEntity == null){
           // 추후 404 에러 페이지를 만들어서 처리할 예정
           throw new RuntimeException("게시글을 찾을 수 없습니다. " + id);
       }
       model.addAttribute("board", boardEntity);
        return "board/detail";
    }

    // GET - http://localhost:8080/board/save (화면 요청)
    @GetMapping("/board/save")
    public String saveForm() {
        return "board/save-form";
    }

    // TODO - 수정 예정
    // 게시글 작성하기
    // POST - http://localhost:8080/board/save
    // Spring이 폼 데이터를 객체로 변환하는 과정(데이터 바인딩 메커니즘)
    // 폼 데이터 바인딩 : Spring이 HTTP 요청 파라미터를 객체로 자동 변환
    @PostMapping("/board/save")
    public String save(BoardRequest.saveDto reqDto) {
        // DTO에서 Entity 클래스 타입으로 변환해줘야 함 - 비영속 상태
//        Board board = Board.builder()
//                .title(reqDto.getTitle())
//                .content(reqDto.getContent())
//                .user(reqDto.getUser())
//                .build();
//        Board boardEntity = boardPersistRepository.save(board); // 영속 상태

        return "redirect:/";
    }

    // 게시글 수정 화면 요청
    // form 태그는 get과 post밖에 못한다.
    // GET - http://localhost:8080/board/1/update
    @GetMapping("/board/{id}/update")
    public String updateForm(@PathVariable Long id, Model model) {
        // 수정하기 화면 요청 (조회 기능)
        Board board = boardPersistRepository.findById(id);
        model.addAttribute("board", board);

        return "board/update-form";
    }

    // 게시글 수정 기능 요청
    // POST - http://localhost:8080/board/1/update
    @PostMapping("/board/{id}/update")
    public String update(@PathVariable Long id, BoardRequest.updateDto reqDto) {
        reqDto.validate();  // 유효성 실패 시 throw
        boardPersistRepository.updateById(id, reqDto);

        return "redirect:/board/" + id; // 리다이렉트 수정된 게시글 상세보기 화면으로 이동
    }

    // 게시글 삭제
    // form 태그는 get과 post밖에 못한다.
    // POST - http://localhost:8080/board/1/delete
    @PostMapping("/board/{id}/delete")
    public String delete(@PathVariable Long id){
        boardPersistRepository.deleteById(id);
         // PRG 패턴 사용
        return "redirect:/";
    }
}
