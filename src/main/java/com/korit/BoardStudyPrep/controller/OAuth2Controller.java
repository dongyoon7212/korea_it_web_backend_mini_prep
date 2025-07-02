package com.korit.BoardStudyPrep.controller;


import com.korit.BoardStudyPrep.dto.oauth2.OAuth2MergeReqDto;
import com.korit.BoardStudyPrep.dto.oauth2.OAuth2SignupReqDto;
import com.korit.BoardStudyPrep.service.OAuth2AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/oauth2")
public class OAuth2Controller {

    @Autowired
    private OAuth2AuthService oAuth2AuthService;

    @PostMapping("/merge")
    public ResponseEntity<?> mergeAccount(@RequestBody OAuth2MergeReqDto oAuth2MergeReqDto) {
        return ResponseEntity.ok().body(oAuth2AuthService.mergeAccount(oAuth2MergeReqDto));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody OAuth2SignupReqDto oAuth2SignupReqDto) {
        return ResponseEntity.ok().body(oAuth2AuthService.signup(oAuth2SignupReqDto));
    }
}
