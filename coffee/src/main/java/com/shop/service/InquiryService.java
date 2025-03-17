package com.shop.service;


import com.shop.dto.InquiryFormDto;
import com.shop.dto.InquiryImgDto;
import com.shop.entity.Inquiry;
import com.shop.entity.InquiryImg;
import com.shop.entity.Member;
import com.shop.repository.InquiryImgRepository;
import com.shop.repository.InquiryRepository;
import com.shop.utill.SecurityUtil;
import com.shop.repository.MemberRepository;
import com.shop.entity.InquiryAnswer;
import com.shop.repository.InquiryAnswerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.shop.constant.InquiryStatus;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class InquiryService {
    private final MemberRepository memberRepository;
    private final InquiryRepository inquiryRepository;
    private final InquiryImgRepository inquiryImgRepository;
    private final InquiryImgService inquiryImgService;
    private final InquiryAnswerRepository inquiryAnswerRepository;

    @Transactional
    public Long saveInquiry(InquiryFormDto inquiryFormDto, List<MultipartFile> inquiryImgFileList) throws Exception {
        // 현재 로그인한 회원의 정보를 가져와서 작성자로 설정
        String userId = SecurityUtil.getCurrentMemberId();
        if (userId == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        Member member = memberRepository.findByUserid(userId);
        if (member == null) {
            throw new RuntimeException("회원 정보를 찾을 수 없습니다.");
        }

        // 상품 문의 등록
        Inquiry inquiry = inquiryFormDto.createInquiry();
        inquiry.setWriter(member.getName());
        inquiry.setWriterId(member.getUserid());
        inquiryRepository.save(inquiry);

        // 이미지 등록 (null 체크 추가)
        if(inquiryImgFileList != null) {
            for(int i = 0; i < inquiryImgFileList.size(); i++) {
                MultipartFile inquiryImgFile = inquiryImgFileList.get(i);
                if(!inquiryImgFile.isEmpty()) {  // 빈 파일이 아닐 때만 저장
                    InquiryImg inquiryImg = new InquiryImg();
                    inquiryImg.setInquiry(inquiry);
                    inquiryImgService.saveInquiryImg(inquiryImg, inquiryImgFile);
                }
            }
        }

        return inquiry.getId();
    }

    //수정하기
    @Transactional(readOnly = true)
    public InquiryFormDto getInquiryDtl(Long InquiryId){
        // 문의글 조회
        Inquiry inquiry = inquiryRepository.findById(InquiryId)
                .orElseThrow(() -> new EntityNotFoundException("문의글을 찾을 수 없습니다."));

        // 이미지 조회
        List<InquiryImg> inquiryImgList = inquiryImgRepository.findByInquiryIdOrderByIdAsc(InquiryId);
        List<InquiryImgDto> inquiryImgDtoList = new ArrayList<>();

        for(InquiryImg inquiryImg : inquiryImgList){
            InquiryImgDto inquiryImgDto = InquiryImgDto.of(inquiryImg);
            inquiryImgDtoList.add(inquiryImgDto);
        }

        // DTO 생성 및 설정
        InquiryFormDto inquiryFormDto = InquiryFormDto.of(inquiry);
        inquiryFormDto.setInquiryImgDtoList(inquiryImgDtoList);
        inquiryFormDto.setItemId(inquiry.getItem().getId());  // Item ID 설정

        // 이미지 ID 리스트 설정
        List<Long> inquiryImgIds = inquiryImgList.stream()
                .map(InquiryImg::getId)
                .collect(java.util.stream.Collectors.toList());
        inquiryFormDto.setInquiryImgIds(inquiryImgIds);

        return inquiryFormDto;
    }

    public void updateInquiry(Long inquiryId, InquiryFormDto inquiryFormDto, List<MultipartFile> inquiryImgFileList) throws Exception {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(EntityNotFoundException::new);
        inquiry.updateInquiry(inquiryFormDto);

        // 현재 로그인한 회원의 정보를 가져와서 작성자로 설정
        String userId = SecurityUtil.getCurrentMemberId();
        if (userId == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        Member member = memberRepository.findByUserid(userId);
        if (member == null) {
            throw new RuntimeException("회원 정보를 찾을 수 없습니다.");
        }

        // 기존 상태 유지
        inquiryFormDto.setInquiryStatus(inquiry.getInquiryStatus());
        inquiryFormDto.setWriter(member.getName());
        inquiryFormDto.setWriterId(member.getUserid());

        List<Long> inquiryImgIds = inquiryFormDto.getInquiryImgIds();
        if(inquiryImgIds == null) {
            inquiryImgIds = new ArrayList<>();
        }

        //이미지 등록
        for(int i = 0; i < inquiryImgFileList.size(); i++){
            MultipartFile inquiryImgFile = inquiryImgFileList.get(i);
            if(!inquiryImgFile.isEmpty()) {  // 빈 파일이 아닐 때만 처리
                if(i < inquiryImgIds.size()) {  // 기존 이미지가 있는 경우 수정
                    inquiryImgService.updateInquiryImg(inquiryImgIds.get(i), inquiryImgFile);
                } else {  // 새로운 이미지인 경우 추가
                    InquiryImg inquiryImg = new InquiryImg();
                    inquiryImg.setInquiry(inquiry);
                    inquiryImgService.saveInquiryImg(inquiryImg, inquiryImgFile);
                }
            }
        }
        inquiryRepository.save(inquiry);
    }

    public List<Inquiry> getAllInquiries() {
        return inquiryRepository.findAllByOrderByIdDesc();
    }

    public Page<Inquiry> getInquiryPage(int page, String searchBy, String searchQuery) {
        Pageable pageable = PageRequest.of(page, 10);

        if(searchBy == null || searchQuery == null || searchQuery.isEmpty()) {
            return inquiryRepository.findAllByOrderByIdDesc(pageable);
        }

        switch(searchBy) {
            case "title":
                return inquiryRepository.findByTitleContainingOrderByIdDesc(searchQuery, pageable);
            case "content":
                return inquiryRepository.findByContentContainingOrderByIdDesc(searchQuery, pageable);
            case "writer":
                return inquiryRepository.findByWriterContainingOrderByIdDesc(searchQuery, pageable);
            case "item":
                return inquiryRepository.findByItem_ItemNmContainingOrderByIdDesc(searchQuery, pageable);
            default:
                return inquiryRepository.findAllByOrderByIdDesc(pageable);
        }
    }

    public void saveAnswer(Long inquiryId, String content) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new EntityNotFoundException("문의글을 찾을 수 없습니다."));

        InquiryAnswer answer = inquiryAnswerRepository.findByInquiryId(inquiryId);
        if(answer == null) {
            answer = new InquiryAnswer();
            answer.setInquiry(inquiry);
        }
        answer.setContent(content);
        inquiryAnswerRepository.save(answer);

        // 답변 상태 변경
        inquiry.setInquiryStatus(InquiryStatus.ANSWER);
        inquiryRepository.save(inquiry);
    }

    public void deleteAnswer(Long answerId) {
        InquiryAnswer answer = inquiryAnswerRepository.findById(answerId)
                .orElseThrow(() -> new EntityNotFoundException("답변을 찾을 수 없습니다."));

        // 문의글 상태 변경
        Inquiry inquiry = answer.getInquiry();
        inquiry.setInquiryStatus(InquiryStatus.QUESTION);
        inquiryRepository.save(inquiry);

        inquiryAnswerRepository.delete(answer);
    }

}
