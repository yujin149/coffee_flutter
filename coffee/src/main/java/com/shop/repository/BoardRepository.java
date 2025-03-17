package com.shop.repository;

import com.shop.constant.BoardStatus;
import com.shop.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    // 제목에 특정 문자열이 포함된 게시글을 페이징하여 검색
    Page<Board> findByTitleContaining(String title, Pageable pageable);
    
    // 내용에 특정 문자열이 포함된 게시글을 페이징하여 검색
    Page<Board> findByContentContaining(String content, Pageable pageable);
    
    // 작성자명에 특정 문자열이 포함된 게시글을 페이징하여 검색
    Page<Board> findByWriterContaining(String writer, Pageable pageable);
    
    // 현재 게시글의 이전 게시글 찾기 (이전 글 기능)
    Board findFirstByIdLessThanOrderByIdDesc(Long id);
    
    // 현재 게시글의 다음 게시글 찾기 (다음 글 기능)
    Board findFirstByIdGreaterThanOrderByIdAsc(Long id);
    
    // 특정 게시글 종류(공지/일반)의 모든 게시글을 ID 내림차순으로 조회
    List<Board> findByBoardStatusOrderByIdDesc(BoardStatus boardStatus);
    
    // 특정 게시글 종류의 게시글을 페이징하여 조회
    Page<Board> findByBoardStatus(BoardStatus boardStatus, Pageable pageable);
    
    // 특정 게시글 종류이면서 제목에 특정 문자열이 포함된 게시글 검색
    Page<Board> findByBoardStatusAndTitleContaining(BoardStatus boardStatus, String title, Pageable pageable);
    
    // 특정 게시글 종류이면서 내용에 특정 문자열이 포함된 게시글 검색
    Page<Board> findByBoardStatusAndContentContaining(BoardStatus boardStatus, String content, Pageable pageable);
    
    // 특정 게시글 종류이면서 작성자명에 특정 문자열이 포함된 게시글 검색
    Page<Board> findByBoardStatusAndWriterContaining(BoardStatus boardStatus, String writer, Pageable pageable);
}