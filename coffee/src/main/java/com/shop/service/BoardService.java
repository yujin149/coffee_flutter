package com.shop.service;

import com.shop.constant.BoardStatus;
import com.shop.dto.BoardDto;
import com.shop.dto.BoardFormDto;
import com.shop.entity.Board;
import com.shop.repository.BoardRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service                     // 스프링의 서비스 계층임을 나타냄
@Transactional              // 모든 메서드를 트랜잭션으로 처리
@RequiredArgsConstructor    // final 필드에 대한 생성자 자동 생성
public class BoardService {

    private final BoardRepository boardRepository;

    // 새로운 게시글 저장
    public Long saveBoard(BoardFormDto boardFormDto, String writer) {
        Board board = boardFormDto.createBoard(writer);
        boardRepository.save(board);
        return board.getId();
    }

    // 게시글 목록 조회 (검색 기능 포함)
    @Transactional(readOnly = true)    // 읽기 전용 트랜잭션으로 성능 최적화
    public Page<Board> getBoardList(String searchBy, String searchQuery, Pageable pageable) {
        // 검색 조건이 없는 경우 일반 게시글 전체 조회
        if (!StringUtils.hasText(searchBy) || !StringUtils.hasText(searchQuery)) {
            return boardRepository.findByBoardStatus(BoardStatus.GENERAL, pageable);
        }

        // 검색 조건에 따른 게시글 검색
        switch (searchBy) {
            case "title":   // 제목으로 검색
                return boardRepository.findByBoardStatusAndTitleContaining(BoardStatus.GENERAL, searchQuery, pageable);
            case "content": // 내용으로 검색
                return boardRepository.findByBoardStatusAndContentContaining(BoardStatus.GENERAL, searchQuery, pageable);
            case "writer":  // 작성자로 검색
                return boardRepository.findByBoardStatusAndWriterContaining(BoardStatus.GENERAL, searchQuery, pageable);
            default:        // 기본적으로 일반 게시글 전체 조회
                return boardRepository.findByBoardStatus(BoardStatus.GENERAL, pageable);
        }
    }

    // 게시글 상세 조회 (조회수 증가 포함)
    @Transactional(readOnly = true)
    public BoardDto getBoardDetail(Long boardId) {
        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
        board.setHits(board.getHits() + 1);    // 조회수 증가
        return new BoardDto(board);
    }

    // 이전 게시글 조회
    public Board getPrevBoard(Long boardId) {
        return boardRepository.findFirstByIdLessThanOrderByIdDesc(boardId);
    }

    // 다음 게시글 조회
    public Board getNextBoard(Long boardId) {
        return boardRepository.findFirstByIdGreaterThanOrderByIdAsc(boardId);
    }

    // 게시글 삭제
    public void deleteBoard(Long boardId) {
        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
        boardRepository.delete(board);
    }

    // 게시글 수정
    @Transactional
    public Long updateBoard(Long boardId, BoardFormDto boardFormDto) {
        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
        board.updateBoard(boardFormDto);
        return boardId;
    }

    // 작성자 검증
    @Transactional(readOnly = true)
    public boolean validateWriter(Long boardId, String email) {
        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
        return board.getWriter().equals(email);
    }

    // 공지사항 목록 조회
    @Transactional(readOnly = true)
    public List<Board> getNoticeBoards() {
        return boardRepository.findByBoardStatusOrderByIdDesc(BoardStatus.NOTICE);
    }
}