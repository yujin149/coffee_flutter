package com.shop.controller;

import com.shop.dto.InquiryFormDto;
import com.shop.entity.Inquiry;
import com.shop.entity.InquiryImg;
import com.shop.entity.Member;
import com.shop.entity.InquiryAnswer;
import com.shop.service.InquiryService;
import com.shop.service.ItemService;
import com.shop.repository.MemberRepository;
import com.shop.repository.InquiryRepository;
import com.shop.repository.InquiryAnswerRepository;
import com.shop.repository.InquiryImgRepository;
import com.shop.repository.ItemImgRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Slf4j
@Controller
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;
    private final ItemService itemService;
    private final MemberRepository memberRepository;
    private final InquiryRepository inquiryRepository;
    private final InquiryAnswerRepository inquiryAnswerRepository;
    private final InquiryImgRepository inquiryImgRepository;
    private final ItemImgRepository itemImgRepository;

    //문의 작성
    @GetMapping(value = "/inquiry/write")
    public String inquiryForm(Model model){
        model.addAttribute("inquiryFormDto", new InquiryFormDto());
        model.addAttribute("items", itemService.getItems());
        return "inquiry/inquiryWrite";
    }

    @PostMapping(value = "/inquiry/write")
    public String inquiryWrite(@Valid InquiryFormDto inquiryFormDto,
                               BindingResult bindingResult,
                               Model model,
                               @RequestParam(value = "inquiryImgFile", required = false) List<MultipartFile> inquiryImgFileList){
        if (bindingResult.hasErrors()){
            model.addAttribute("items", itemService.getItems());
            return "inquiry/inquiryWrite";
        }

        try {
            // 빈 파일 리스트 처리
            if(inquiryImgFileList == null) {
                inquiryImgFileList = new ArrayList<>();
            }

            Long inquiryId = inquiryService.saveInquiry(inquiryFormDto, inquiryImgFileList);
            return "redirect:/inquiry";  // 성공 시 목록 페이지로 리다이렉트
        } catch (Exception e) {
            model.addAttribute("errorMessage", "문의글 등록 중 에러가 발생했습니다: " + e.getMessage());
            model.addAttribute("items", itemService.getItems());
            return "inquiry/inquiryWrite";
        }
    }

    //수정하기
    @GetMapping(value = "/inquiry/update/{inquiryId}")
    public String inquiryDtl(@PathVariable("inquiryId") Long inquiryId, Model model){
        try {
            InquiryFormDto inquiryFormDto = inquiryService.getInquiryDtl(inquiryId);
            model.addAttribute("inquiryFormDto",inquiryFormDto);
            model.addAttribute("items", itemService.getItems());
        }catch (EntityNotFoundException e){
            model.addAttribute("errorMessage","존재하지 않는 게시글입니다.");
            model.addAttribute("inquiryFormDto", new InquiryFormDto());
            model.addAttribute("items", itemService.getItems());
            return "inquiry/inquiryModify";
        }
        return "inquiry/inquiryModify";
    }

    @PostMapping("/inquiry/update/{inquiryId}")
    public String updateInquiry(@PathVariable("inquiryId") Long inquiryId,
                                @Valid InquiryFormDto inquiryFormDto,
                                BindingResult bindingResult,
                                @RequestParam("inquiryImgFile") List<MultipartFile> inquiryImgFileList,
                                Principal principal) {
        try {
            if (bindingResult.hasErrors()) {
                return "inquiry/inquiryModify";
            }

            Inquiry inquiry = inquiryRepository.findById(inquiryId)
                    .orElseThrow(() -> new EntityNotFoundException("문의글을 찾을 수 없습니다."));

            Member member = memberRepository.findByUserid(principal.getName());

            // 권한 체크
            if(!member.getRole().name().equals("ADMIN") &&
                    !inquiry.getWriterId().equals(member.getUserid())) {
                throw new RuntimeException("수정 권한이 없습니다.");
            }

            inquiryService.updateInquiry(inquiryId, inquiryFormDto, inquiryImgFileList);

            return "redirect:/inquiry/view/" + inquiryId;  // 수정된 문의글 상세 페이지로 리다이렉트
        } catch (Exception e) {
            return "redirect:/inquiry?error=update";  // 에러 발생 시 목록 페이지로 리다이렉트
        }
    }

    @GetMapping(value = "/inquiry")
    public String inquiryList(@RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "searchBy", required = false) String searchBy,
                              @RequestParam(value = "searchQuery", required = false) String searchQuery,
                              Principal principal,
                              Model model) {
        Page<Inquiry> inquiries = inquiryService.getInquiryPage(page, searchBy, searchQuery);

        model.addAttribute("inquiries", inquiries);
        model.addAttribute("searchBy", searchBy);
        model.addAttribute("searchQuery", searchQuery);

        // 페이징 계산
        int maxPage = 5;
        int start = Math.max(1, inquiries.getNumber() - (maxPage - 1) / 2);
        int end = Math.min(start + (maxPage - 1), inquiries.getTotalPages());

        model.addAttribute("start", start);
        model.addAttribute("end", end);

        if(principal != null) {
            Member member = memberRepository.findByUserid(principal.getName());
            model.addAttribute("member", member);
        }

        return "inquiry/inquiry";
    }

    @GetMapping("/inquiry/checkAccess/{id}")
    @ResponseBody
    public ResponseEntity<Boolean> checkAccess(@PathVariable("id") Long id, Principal principal) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("문의글을 찾을 수 없습니다."));

        // 비밀글이 아닌 경우 모든 사용자 접근 가능
        if (!inquiry.isSecret()) {
            return ResponseEntity.ok(true);
        }

        // 비밀글인 경우 로그인 체크
        if(principal == null) {
            return ResponseEntity.ok(false);  // 비밀글은 로그인이 필요
        }

        Member member = memberRepository.findByUserid(principal.getName());

        // ADMIN은 모든 게시물 접근 가능
        if(member.getRole().name().equals("ADMIN")) {
            return ResponseEntity.ok(true);
        }

        // 비밀글인 경우 작성자의 이름과 아이디가 모두 일치하는지 확인
        boolean hasAccess = inquiry.getWriterId().equals(member.getUserid()) &&
                inquiry.getWriter().equals(member.getName());

        return ResponseEntity.ok(hasAccess);
    }

    @GetMapping("/inquiry/view/{id}")
    public String inquiryView(@PathVariable("id") Long id, Model model, Principal principal) {
        try {
            Inquiry inquiry = inquiryRepository.findByIdWithItem(id)
                    .orElseThrow(() -> new EntityNotFoundException("문의글을 찾을 수 없습니다."));

            // 비밀글 접근 권한 체크를 먼저 수행
            if (inquiry.isSecret()) {
                if (principal == null) {
                    return "redirect:/members/login";
                }
                
                Member member = memberRepository.findByUserid(principal.getName());
                if (!member.getRole().name().equals("ADMIN") && 
                    (!inquiry.getWriterId().equals(member.getUserid()) || 
                     !inquiry.getWriter().equals(member.getName()))) {
                    return "redirect:/inquiry?error=unauthorized";
                }
            }

            model.addAttribute("inquiry", inquiry);
            model.addAttribute("answer", inquiryAnswerRepository.findByInquiryId(id));
            model.addAttribute("inquiryImages", inquiryImgRepository.findByInquiryIdOrderByIdAsc(id));

            // Item 관련 정보는 item이 있을 때만 조회
            if (inquiry.getItem() != null) {
                itemImgRepository.findByItemIdOrderByIdAsc(inquiry.getItem().getId())
                    .stream()
                    .findFirst()
                    .ifPresent(itemImg -> model.addAttribute("itemImg", itemImg));
            }

            if (principal != null) {
                model.addAttribute("member", memberRepository.findByUserid(principal.getName()));
            }

            return "inquiry/inquiryView";
        } catch (Exception e) {
            log.error("Error in inquiryView", e);
            return "redirect:/inquiry?error=view";
        }
    }

    @PostMapping("/inquiry/answer/{inquiryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveAnswer(@PathVariable("inquiryId") Long inquiryId,
                             @RequestParam("content") String content) {
        inquiryService.saveAnswer(inquiryId, content);
        return "redirect:/inquiry/view/" + inquiryId;
    }

    @PostMapping("/inquiry/answer/delete/{answerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteAnswer(@PathVariable("answerId") Long answerId) {
        InquiryAnswer answer = inquiryAnswerRepository.findById(answerId)
                .orElseThrow(() -> new EntityNotFoundException("답변을 찾을 수 없습니다."));
        Long inquiryId = answer.getInquiry().getId();
        inquiryService.deleteAnswer(answerId);
        return "redirect:/inquiry/view/" + inquiryId;
    }

    @PostMapping("/inquiry/delete/{inquiryId}")
    public String deleteInquiry(@PathVariable("inquiryId") Long inquiryId, Principal principal) {
        try {
            Inquiry inquiry = inquiryRepository.findById(inquiryId)
                    .orElseThrow(() -> new EntityNotFoundException("문의글을 찾을 수 없습니다."));

            Member member = memberRepository.findByUserid(principal.getName());

            // 권한 체크
            if(!member.getRole().name().equals("ADMIN") &&
                    !inquiry.getWriterId().equals(member.getUserid())) {
                throw new RuntimeException("삭제 권한이 없습니다.");
            }

            // 연관된 답변 삭제
            InquiryAnswer answer = inquiryAnswerRepository.findByInquiryId(inquiryId);
            if(answer != null) {
                inquiryAnswerRepository.delete(answer);
            }

            // 연관된 이미지 먼저 삭제
            List<InquiryImg> inquiryImgs = inquiryImgRepository.findByInquiryIdOrderByIdAsc(inquiryId);
            for(InquiryImg img : inquiryImgs) {
                inquiryImgRepository.delete(img);
            }

            // 문의글 삭제
            inquiryRepository.delete(inquiry);

            return "redirect:/inquiry";
        } catch (Exception e) {
            e.printStackTrace(); // 에러 로깅
            return "redirect:/inquiry?error=delete";
        }
    }

    @PostMapping("/inquiry/answer/update/{answerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateAnswer(@PathVariable("answerId") Long answerId,
                               @RequestParam("content") String content) {
        InquiryAnswer answer = inquiryAnswerRepository.findById(answerId)
                .orElseThrow(() -> new EntityNotFoundException("답변을 찾을 수 없습니다."));

        answer.setContent(content);
        inquiryAnswerRepository.save(answer);

        return "redirect:/inquiry/view/" + answer.getInquiry().getId();
    }

}
