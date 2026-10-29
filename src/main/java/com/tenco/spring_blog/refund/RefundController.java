package com.tenco.spring_blog.refund;

import com.tenco.spring_blog._core.util.Define;
import com.tenco.spring_blog.payment.PaymentResponse;
import com.tenco.spring_blog.user.SessionUserDto;
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
@RequiredArgsConstructor
public class RefundController {
    private final RefundService refundService;

    // http://localhost:8080/refund/request/{paymentPk}
    @GetMapping("/refund/request/{paymentPk}")
    public String requestForm(@PathVariable Long paymentPk, HttpSession session, Model model) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        PaymentResponse.ListDto paymentListDto = refundService.findRequestForm(paymentPk, sessionUserDto.getId());
        model.addAttribute("payment", paymentListDto);
        return "refund/request-form";
    }

    @PostMapping("/refund/request")
    public String request(RefundRequest.SaveDto saveDto, HttpSession session) {
        saveDto.validate();
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        refundService.save(saveDto, sessionUserDto.getId());
        return "redirect:/refund/list";
    }

    @GetMapping("/refund/list")
    public String list(Model model, HttpSession session) {
        SessionUserDto sessionUserDto = (SessionUserDto) session.getAttribute(Define.SESSION_USER);
        model.addAttribute("refundList", refundService.findAllByUserId(sessionUserDto.getId()));
        return "refund/list";
    }
}
