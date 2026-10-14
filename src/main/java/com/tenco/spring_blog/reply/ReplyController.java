package com.tenco.spring_blog.reply;

import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.user.SessionUserDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ReplyController {
    private final ReplyService replyService;

    // 댓글 작성
    @PostMapping("/reply/save")
    public String save(ReplyRequest.saveDto saveDto, HttpSession session) {
        saveDto.validate();
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        replyService.save(saveDto, sessionUserDto);
        return "redirect:/board/" + saveDto.getBoardId();
    }

    // 댓글 삭제
    @PostMapping("/reply/{id}/delete")
    public String delete(@PathVariable Long id, @RequestParam(name = "boardId") Long BoardId, HttpSession session) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        replyService.deleteById(id, sessionUserDto);
        return "redirect:/board/" + BoardId;
    }
}
