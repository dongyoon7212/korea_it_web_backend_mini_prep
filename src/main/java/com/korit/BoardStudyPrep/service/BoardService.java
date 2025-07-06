package com.korit.BoardStudyPrep.service;

import com.korit.BoardStudyPrep.dto.ApiRespDto;
import com.korit.BoardStudyPrep.dto.board.AddBoardReqDto;
import com.korit.BoardStudyPrep.entity.Board;
import com.korit.BoardStudyPrep.repository.BoardRepository;
import com.korit.BoardStudyPrep.security.model.PrincipalUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    @Transactional(rollbackFor = Exception.class) // 예외 발생 시 롤백 처리
    public ApiRespDto<?> addBoard(AddBoardReqDto addBoardReqDto, PrincipalUser principalUser) {
        if (principalUser == null || !addBoardReqDto.getUserId().equals(principalUser.getUserId())) {
            // principalUser가 null인 경우도 포함하여 체크
            return new ApiRespDto<>("failed", "잘못된 접근입니다. 로그인 정보가 유효하지 않거나 권한이 없습니다.", null);
        }

        // 제목 검사
        if (addBoardReqDto.getTitle() == null || addBoardReqDto.getTitle().trim().isEmpty()) {
            return new ApiRespDto<>("failed", "제목은 필수 입력 사항입니다.", null);
        }

        // 내용 검사
        if (addBoardReqDto.getContent() == null || addBoardReqDto.getContent().trim().isEmpty()) {
            return new ApiRespDto<>("failed", "내용은 필수 입력 사항입니다.", null);
        }

        try {
            Optional<Board> optionalBoard = boardRepository.addBoard(addBoardReqDto.toEntity()); // Mapper에 saveBoard 메소드 정의 필요

            if (optionalBoard.isEmpty()) {
                // 삽입된 행이 0개라면 (예: userId가 유효하지 않거나 다른 DB 문제)
                return new ApiRespDto<>("failed", "게시물 추가에 실패했습니다. 데이터를 확인해주세요.", null);
            }

            return new ApiRespDto<>("success", "게시물이 성공적으로 추가되었습니다.", optionalBoard.get().getBoardId());
        } catch (Exception e) {
            // 데이터베이스 예외 처리 (예: SQLIntegrityConstraintViolationException 등)
            // 실제 운영에서는 e.getMessage()를 직접 노출하기보다 일반적인 오류 메시지 사용
            e.printStackTrace(); // 개발 중에는 스택 트레이스 출력
            return new ApiRespDto<>("error", "서버 오류로 게시물 추가에 실패했습니다. 다시 시도해주세요.", null);
        }
    }

    @Transactional(readOnly = true) // 읽기 전용 트랜잭션 설정
    public ApiRespDto<?> getBoardByBoardId(Integer boardId) {
        // 1. 유효성 검사: boardId가 null이거나 유효하지 않은 값인지 확인
        if (boardId == null || boardId <= 0) {
            return new ApiRespDto<>("failed", "유효하지 않은 게시물 ID입니다.", null);
        }

        // 2. Repository 호출
        Optional<Board> boardOptional = boardRepository.getBoardByBoardId(boardId);

        // 3. 결과 처리 및 응답 반환
        if (boardOptional.isPresent()) {
            return new ApiRespDto<>("success", "게시물을 성공적으로 조회했습니다.", boardOptional.get());
        } else {
            return new ApiRespDto<>("failed", "해당 ID의 게시물을 찾을 수 없습니다.", null);
        }
    }

    @Transactional(readOnly = true)
    public ApiRespDto<?> getBoardList() {
        // 1. Repository 호출
        List<Board> boardList = boardRepository.getBoardList();

        // 2. 결과 처리 및 응답 반환
        if (boardList.isEmpty()) {
            return new ApiRespDto<>("success", "조회된 게시물이 없습니다.", boardList);
        } else {
            return new ApiRespDto<>("success", "게시물 목록을 성공적으로 조회했습니다.", boardList);
        }
    }

    @Transactional(readOnly = true)
    public ApiRespDto<?> getBoardListByUserId(Integer userId) {
        // 2. 유효성 검사: userId가 null이거나 유효하지 않은 값인지 확인
        if (userId == null || userId <= 0) {
            return new ApiRespDto<>("failed", "유효하지 않은 사용자 ID입니다.", null);
        }

        // 3. Repository 호출
        List<Board> boardList = boardRepository.getBoardListByUserId(userId);

        // 4. 결과 처리 및 응답 반환
        if (boardList.isEmpty()) {
            return new ApiRespDto<>("success", "해당 사용자의 게시물이 없습니다.", boardList);
        } else {
            return new ApiRespDto<>("success", "사용자 게시물 목록을 성공적으로 조회했습니다.", boardList);
        }
    }
}
