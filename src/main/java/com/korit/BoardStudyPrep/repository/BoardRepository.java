package com.korit.BoardStudyPrep.repository;

import com.korit.BoardStudyPrep.entity.Board;
import com.korit.BoardStudyPrep.mapper.BoardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BoardRepository {

    @Autowired
    private BoardMapper boardMapper;

    public Optional<Board> addBoard(Board board) {
        try {
            boardMapper.addBoard(board);
        } catch (DuplicateKeyException e) {
            return Optional.empty();
        }
        return Optional.of(board);
    }

    public Optional<Board> getBoardByBoardId(Integer boardId) {
        return boardMapper.getBoardByBoardId(boardId);
    }

    public List<Board> getBoardList() {
        return boardMapper.getBoardList();
    }

    public List<Board> getBoardListByUserId(Integer userId) {
        return boardMapper.getBoardListByUserId(userId);
    }
}
