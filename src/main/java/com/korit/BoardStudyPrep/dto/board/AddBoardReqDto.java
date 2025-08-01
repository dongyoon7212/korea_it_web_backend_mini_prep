package com.korit.BoardStudyPrep.dto.board;

import com.korit.BoardStudyPrep.entity.Board;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AddBoardReqDto {
    private String title;
    private String content;
    private Integer userId;

    public Board toEntity() {
        return Board.builder()
                .title(title)
                .content(content)
                .userId(userId)
                .build();
    }
}
