package com.korit.BoardStudyPrep.controller;

import com.korit.BoardStudyPrep.dto.board.AddBoardReqDto;
import com.korit.BoardStudyPrep.security.model.PrincipalUser;
import com.korit.BoardStudyPrep.service.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/board")
public class BoardController {

    @Autowired
    private BoardService boardService;

    @PostMapping("/add")
    public ResponseEntity<?> addBoard(@RequestBody AddBoardReqDto addBoardReqDto, @AuthenticationPrincipal PrincipalUser principalUser) {
        return ResponseEntity.ok(boardService.addBoard(addBoardReqDto, principalUser));
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<?> getBoardByBoardId(@PathVariable Integer boardId) {
        return ResponseEntity.ok(boardService.getBoardByBoardId(boardId));
    }

    @GetMapping("/list")
    public ResponseEntity<?> getBoardList() {
        return ResponseEntity.ok(boardService.getBoardList());
    }

    @GetMapping("/list/{userId}")
    public ResponseEntity<?> getBoardListByUserId(@PathVariable Integer userId) {
        return ResponseEntity.ok(boardService.getBoardListByUserId(userId));
    }
}
