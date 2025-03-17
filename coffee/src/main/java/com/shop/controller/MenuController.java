package com.shop.controller;

import com.shop.dto.CrawlingDto;
import com.shop.dto.ItemSearchDto;
import com.shop.dto.MainItemDto;
import com.shop.service.CrawlingService;
import com.shop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;


@Controller
@RequiredArgsConstructor
public class MenuController {

    private final ItemService itemService;
    private final CrawlingService crawlingService;

    @GetMapping(value = "/all")
    public String allItem(ItemSearchDto itemSearchDto, Model model, Optional<Integer> page)   {
        Pageable pageable = PageRequest.of(page.orElse(0), 8);
        Page<MainItemDto> items = itemService.getMainItemPage(itemSearchDto, pageable);
        List<CrawlingDto> crawlingItems = crawlingService.getCrawlingItems();

        model.addAttribute("items", items);
        model.addAttribute("itemSearchDto", itemSearchDto);
        model.addAttribute("crawlingItems", crawlingItems);
        model.addAttribute("maxPage", 5);
        model.addAttribute("currentUrl", "/all");

        return "menu/all";
    }

    @GetMapping(value = "coffee")
    public String coffeeItem(ItemSearchDto itemSearchDto, Model model, Optional<Integer> page)   {
        Pageable pageable = PageRequest.of(page.orElse(0), 8);
        Page<MainItemDto> items = itemService.getCoffeeItemPage(itemSearchDto, pageable);
        List<CrawlingDto> crawlingItems = crawlingService.getCrawlingItems();

        model.addAttribute("items", items);
        model.addAttribute("itemSearchDto", itemSearchDto);
        model.addAttribute("crawlingItems", crawlingItems);
        model.addAttribute("maxPage", 5);
        model.addAttribute("currentUrl", "/coffee");

        return "menu/coffee";
    }

    @GetMapping(value = "bean")
    public String beanItem(ItemSearchDto itemSearchDto, Model model, Optional<Integer> page)   {
        Pageable pageable = PageRequest.of(page.orElse(0), 8);
        Page<MainItemDto> items = itemService.getBeanItemPage(itemSearchDto, pageable);
        List<CrawlingDto> crawlingItems = crawlingService.getCrawlingItems();

        model.addAttribute("items", items);
        model.addAttribute("itemSearchDto", itemSearchDto);
        model.addAttribute("crawlingItems", crawlingItems);
        model.addAttribute("maxPage", 5);
        model.addAttribute("currentUrl", "/bean");

        return "menu/bean";
    }

    @GetMapping(value = "desert")
    public String desertItem(ItemSearchDto itemSearchDto, Model model, Optional<Integer> page)   {
        Pageable pageable = PageRequest.of(page.orElse(0), 8);
        Page<MainItemDto> items = itemService.getDesertItemPage(itemSearchDto, pageable);
        List<CrawlingDto> crawlingItems = crawlingService.getCrawlingItems();

        model.addAttribute("items", items);
        model.addAttribute("itemSearchDto", itemSearchDto);
        model.addAttribute("crawlingItems", crawlingItems);
        model.addAttribute("maxPage", 5);
        model.addAttribute("currentUrl", "/desert");

        return "menu/desert";
    }
}
