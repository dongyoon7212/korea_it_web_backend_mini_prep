package com.korit.BoardStudyPrep.controller;

import com.korit.BoardStudyPrep.dto.mail.MailSendReqDto;
import com.korit.BoardStudyPrep.security.model.PrincipalUser;
import com.korit.BoardStudyPrep.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/mail")
public class MailController {

    @Autowired
    private MailService mailService;

    @PostMapping("/send")
    public ResponseEntity<?> sendMail(@RequestBody MailSendReqDto mailSendReqDto, @AuthenticationPrincipal PrincipalUser principalUser) {
        return ResponseEntity.ok(mailService.sendMail(mailSendReqDto, principalUser));
    }

    @GetMapping("/verify")
    public String verify(Model model, @RequestParam String verifyToken) {
        Map<String, Object> resultMap = mailService.verify(verifyToken);
        model.addAllAttributes(resultMap);
        return "result_page";
    }
}
