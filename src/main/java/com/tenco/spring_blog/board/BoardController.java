package com.tenco.spring_blog.board;

import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.purchase.PurchaseService;
import com.tenco.spring_blog.reply.ReplyResponse;
import com.tenco.spring_blog.reply.ReplyService;
import com.tenco.spring_blog.user.SessionUserDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor // DI 처리
public class BoardController {

    private final BoardService boardService;
    private final ReplyService replyService;
    private final PurchaseService purchaseService;

    // 유료 게시글 구매 요청 URL
    @PostMapping("/board/{id}/purchase")
    public String purchase(@PathVariable(name = "id")Long id, HttpSession session){
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        purchaseService.purchase(id, sessionUserDto.getId());
        return "redirect:/board/" + id;
    }

    // 조회하기
    // GET - http://localhost:8080/ , http://localhost:8080/board/list
    @GetMapping({"/", "/board/list"})
    public String list(@RequestParam(name = "page", defaultValue = "1") int page,
                       @RequestParam(name = "size", defaultValue = "3") int size,
                       @RequestParam(name = "keyword", defaultValue = "") String keyword, Model model) {
        BoardResponse.PageDto pageDto = boardService.findAllPage(page, size, keyword);
        model.addAttribute("boardPage", pageDto);
        model.addAttribute("keyword", keyword);
        model.addAttribute("isSearch", !keyword.isBlank());
        // 페이지 링크는 직접 URL 인코딩이 필요하다.
        model.addAttribute("keywordParam", URLEncoder.encode(keyword, StandardCharsets.UTF_8));
        return "board/list";
    }

    // 상세보기
    // GET - http://localhost:8080/board/3
    @GetMapping("/board/{id}")
    public String detail(@PathVariable(name = "id") Long id, Model model, HttpSession session) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        Long sessionUserId = (sessionUserDto == null) ? null : sessionUserDto.getId();
        BoardResponse.DetailDto detailDto = boardService.findById(id, sessionUserId);
        boolean isOwner = sessionUserDto != null
                && detailDto.getUserId().equals(sessionUserDto.getId());
        List<ReplyResponse.ListDto> replyList = replyService.findAllByBoardId(id, sessionUserId);
        model.addAttribute("board", detailDto);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("replyList", replyList);
        model.addAttribute("sessionUserDto", sessionUserDto);

        return "board/detail";
    }

    // GET - http://localhost:8080/board/save (화면 요청)
    @GetMapping("/board/save")
    public String saveForm() {
        return "board/save-form";
    }

    // 게시글 작성하기
    // POST - http://localhost:8080/board/save
    @PostMapping("/board/save")
    public String save(BoardRequest.saveDto saveDto, HttpSession session) {
        saveDto.validate(); // 유효성 검사
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        boardService.save(saveDto, sessionUserDto);
        return "redirect:/";
    }

    // 게시글 수정 화면 요청
    // GET - http://localhost:8080/board/1/update
    @GetMapping("/board/{id}/update")
    public String updateForm(@PathVariable Long id, Model model, HttpSession session) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        BoardResponse.updateFormDto board = boardService.findByIdAndUpdateForm(id, sessionUserDto.getId());
        model.addAttribute("board", board);
        return "board/update-form";
    }

    // 게시글 수정 기능 요청
    // POST - http://localhost:8080/board/1/update
    @PostMapping("/board/{id}/update")
    public String update(@PathVariable Long id, BoardRequest.updateDto updateDto, HttpSession session) {
        updateDto.validate();   // 유효성 검사
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        boardService.updateById(id, updateDto, sessionUserDto);
        return "redirect:/board/" + id;
    }

    // 게시글 삭제
    // POST - http://localhost:8080/board/1/delete
    @PostMapping("/board/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        boardService.deleteById(id, sessionUserDto);
        return "redirect:/";
    }
}
