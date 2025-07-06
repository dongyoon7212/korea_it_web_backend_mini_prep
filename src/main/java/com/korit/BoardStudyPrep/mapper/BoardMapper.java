package com.korit.BoardStudyPrep.mapper;

import com.korit.BoardStudyPrep.entity.Board;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface BoardMapper {
    int addBoard(Board board);
    Optional<Board> getBoardByBoardId(Integer boardId);
    List<Board> getBoardList();
    List<Board> getBoardListByUserId(Integer userId);
}
