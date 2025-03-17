package com.shop.controller;

import com.shop.dto.BoardDto;
import com.shop.entity.Board;
import com.shop.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class BoardRestController {

    private final BoardService boardService;

    @GetMapping("/board")
    public ResponseEntity<Map<String, Object>> getBoardList(
            @RequestParam(required = false, defaultValue = "") String searchBy,
            @RequestParam(required = false, defaultValue = "") String searchQuery,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        
        try {
            // 공지사항 목록 조회
            List<Board> noticeBoards = boardService.getNoticeBoards();
            List<BoardDto> noticeBoardDtos = noticeBoards.stream()
                .map(BoardDto::new)
                .collect(Collectors.toList());
            
            // 일반 게시글 목록 조회 (페이징)
            Page<Board> generalBoards = boardService.getBoardList(searchBy, searchQuery, pageable);
            Page<BoardDto> generalBoardDtos = generalBoards.map(BoardDto::new);

            Map<String, Object> response = new HashMap<>();
            response.put("noticeBoards", noticeBoardDtos);
            response.put("boards", generalBoardDtos);
            
            return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                .header("Access-Control-Allow-Headers", "*")
                .body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/board/{boardId}")
    public ResponseEntity<Map<String, Object>> getBoardDetail(@PathVariable("boardId") Long boardId) {
        try {
            BoardDto boardDto = boardService.getBoardDetail(boardId);
            Board prevBoard = boardService.getPrevBoard(boardId);
            Board nextBoard = boardService.getNextBoard(boardId);

            Map<String, Object> response = new HashMap<>();
            response.put("board", boardDto);
            response.put("prevBoard", prevBoard != null ? new BoardDto(prevBoard) : null);
            response.put("nextBoard", nextBoard != null ? new BoardDto(nextBoard) : null);

            return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                .header("Access-Control-Allow-Headers", "*")
                .body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
} 