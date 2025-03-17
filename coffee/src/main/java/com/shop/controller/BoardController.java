package com.shop.controller;

import com.shop.constant.BoardStatus;
import com.shop.dto.BoardDto;
import com.shop.dto.BoardFormDto;
import com.shop.entity.Board;
import com.shop.service.BoardService;
import com.shop.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.shop.entity.Member;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final MemberService memberService;

    // 새 게시글 작성 폼을 보여줄때
    // 기본 값으로 일반 게시글로 설정하고
    // 게시글 종류(공지/일반) 선택 옵션을 모델에 추가
    @GetMapping(value = "/board/new")
    public String boardForm(Model model) {
        BoardFormDto boardFormDto = new BoardFormDto();
        boardFormDto.setBoardStatus(BoardStatus.GENERAL);
        model.addAttribute("boardFormDto", boardFormDto);
        model.addAttribute("boardStatuses", BoardStatus.values());
        return "board/boardWrite";
    }

    
    // 새 게시글을 저장할때
    // 입력값 검증 후 에러가 있으면 작성 폼으로 다시 이동
    // 현재는 작성자를 임시로 "admin"으로 설정.
    @PostMapping(value = "/board/new")
    public String boardNew(@Valid BoardFormDto boardFormDto,
                           BindingResult bindingResult,
                           Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("boardStatuses", BoardStatus.values());
            return "board/boardWrite";
        }

        // 현재 로그인한 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userid = authentication.getName();
        Member member = memberService.findMemberByUserid(userid);
        
        // 작성자를 현재 로그인한 사용자의 이름으로 설정
        boardService.saveBoard(boardFormDto, member.getName());
        return "redirect:/board";
    }


    @GetMapping(value = "/board")
    public String boardList(@RequestParam(required = false, defaultValue = "") String searchBy,
                            @RequestParam(required = false, defaultValue = "") String searchQuery,
                            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
                            Pageable pageable, Model model) {
        // 공지사항 목록 조회 (고정), 항상 상단에 고정되어 표시됨.
        List<Board> noticeBoards = boardService.getNoticeBoards();
        // 일반 게시글 목록 조회 (페이징)
        // searchBy: 검색 조건, searchQuery: 검색어, pageable: 페이징 정보(페이지 정보, 정렬 정보, 페이지 크기 / 한페이지당 10개, ID 기준 내림차순 정렬)
        Page<Board> generalBoards = boardService.getBoardList(searchBy, searchQuery, pageable);

        model.addAttribute("noticeBoards", noticeBoards);
        model.addAttribute("boards", generalBoards);
        model.addAttribute("searchBy", searchBy);
        model.addAttribute("searchQuery", searchQuery);
        return "board/board";
    }

    @GetMapping("/board/{boardId}")
    public String boardDetail(@PathVariable("boardId") Long boardId, Model model) {
        // 게시글 상세 내용 조회
        // 이전글, 다음글 정보도 함께 조회하여 모델에 추가
        BoardDto boardDto = boardService.getBoardDetail(boardId);
        Board prevBoard = boardService.getPrevBoard(boardId);
        Board nextBoard = boardService.getNextBoard(boardId);
        model.addAttribute("board", boardDto);
        model.addAttribute("prevBoard", prevBoard);
        model.addAttribute("nextBoard", nextBoard);
        return "board/boardView";
    }

    @DeleteMapping("/board/{boardId}")
    @ResponseBody
    public ResponseEntity<Long> deleteBoard(@PathVariable("boardId") Long boardId) {
        // REST API 형식으로 게시글 삭제 요청 처리
        // 삭제 요청을 받으면 게시글 삭제 서비스 호출
        // 삭제 성공 시 삭제된 게시글 ID를 응답 본문에 포함하여 200 OK 응답 반환
        boardService.deleteBoard(boardId);
        return new ResponseEntity<>(boardId, HttpStatus.OK);
    }

    // 게시글 수정 폼을 보여줄때
    // 게시글 상세 내용을 조회하여 모델에 추가
    @GetMapping(value = "/board/modify/{boardId}")
    public String boardModifyForm(@PathVariable("boardId") Long boardId, Model model) {
        BoardDto boardDto = boardService.getBoardDetail(boardId);
        model.addAttribute("boardFormDto", boardDto.toFormDto());
        model.addAttribute("boardStatuses", BoardStatus.values());
        return "board/boardModify";
    }

    
    // 게시글 수정 요청 처리
    // 입력값 검증 후 수정 처리
    @PostMapping(value = "/board/modify/{boardId}")
    public String boardModify(@Valid BoardFormDto boardFormDto,
                              BindingResult bindingResult,
                              @PathVariable("boardId") Long boardId) {
        if (bindingResult.hasErrors()) {
            return "board/boardModify";
        }
        boardService.updateBoard(boardId, boardFormDto);
        return "redirect:/board/" + boardId;
    }
}
