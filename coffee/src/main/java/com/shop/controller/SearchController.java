package com.shop.controller;

import com.shop.dto.ItemSearchDto;
import com.shop.dto.MainItemDto;
import com.shop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final ItemService itemService;

    @GetMapping("/search")
    public String searchItems(ItemSearchDto itemSearchDto,
                              @PageableDefault(size = 12) Pageable pageable,
                              Model model) {

        Page<MainItemDto> items = itemService.getMainItemPage(itemSearchDto, pageable);

        model.addAttribute("items", items);
        model.addAttribute("searchQuery", itemSearchDto.getSearchQuery());
        model.addAttribute("maxPage", 10); // 페이지 번호의 최대 개수

        return "search/search";
    }
}