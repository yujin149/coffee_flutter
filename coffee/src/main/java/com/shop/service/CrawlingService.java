package com.shop.service;

import com.shop.constant.ItemMenu;
import com.shop.constant.ItemSellStatus;
import com.shop.dto.CrawlingDto;
import com.shop.entity.Item;
import com.shop.entity.ItemImg;
import com.shop.repository.CrawlingRepository;
import com.shop.repository.ItemImgRepository;
import com.shop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CrawlingService {
    private final CrawlingRepository crawlingRepository;
    private final ItemImgRepository itemImgRepository;
    private final ItemRepository itemRepository;
    // 크롤링할 URL 설정
    private static final Map<String, ItemMenu> URL_TO_ENUM_MAP = Map.of(
//            "https://www.coupang.com/np/search?component=&q=%EC%9B%90%EB%91%90&channel=user", ItemMenu.BEAN,
            "https://www.megacoffee.co.kr/goods/goods_list.php?cateCd=001014", ItemMenu.BEAN,
            "https://www.megacoffee.co.kr/goods/goods_list.php?cateCd=007", ItemMenu.DESERT
    );
    // 크롤링 데이터 가져오기
    public List<CrawlingDto> getCrawlingData() {
        List<CrawlingDto> allCrawlingData = new ArrayList<>();

        // ChromeDriver 경로 설정
        System.setProperty("webdriver.chrome.driver", "D:\\00_projects\\03_팀프로젝트\\작업자료\\백업자료\\00_압축\\coffee_21_완\\src\\main\\resources\\driver\\chromedriver-win64\\chromedriver.exe");

        // ChromeDriver 옵션 설정
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-gpu", "--no-sandbox", "--headless", "--disable-dev-shm-usage");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

        WebDriver driver = new ChromeDriver(options);

        try {
            for (Map.Entry<String, ItemMenu> entry : URL_TO_ENUM_MAP.entrySet()) {
                String url = entry.getKey();
                ItemMenu itemMenu = entry.getValue();

                System.out.println("Crawling URL: " + url);
                driver.get(url);

                // 페이지 로드 완료 대기
                new WebDriverWait(driver, Duration.ofSeconds(15)).until(
                        webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete")
                );

                boolean hasNextPage = true;
                int currentPage = 1; // 현재 페이지
                int maxPages = 5; // 최대 크롤링할 페이지 수 설정

                while (hasNextPage) {
                    System.out.println("Currently Crawling Page: " + currentPage);

                    // 현재 페이지의 상품 크롤링
                    List<WebElement> items = driver.findElements(By.cssSelector("#contents > div > center > div > div.content > div > div.goods_pick_list > div > div > div > ul > div > li > div"));

                    System.out.println("Number of items found on page " + currentPage + ": " + items.size());

                    for (WebElement item : items) {
                        try {
                            String image = item.findElement(By.cssSelector("#contents > div > center > div > div.content > div > div.goods_pick_list > div > div > div > ul > div > li > div > div > a > div > img")).getAttribute("src");
                            String subject = item.findElement(By.cssSelector("#contents > div > center > div > div.content > div > div.goods_pick_list > div > div > div > ul > div > li > div > div.item_info_cont > div.item_tit_box > a > span")).getText();
                            String price = item.findElement(By.cssSelector("#contents > div > center > div > div.content > div > div.goods_pick_list > div > div > div > ul > div > li > div > div.item_info_cont > div.item_money_box > strong > span")).getText();
                            String itemUrl = item.findElement(By.cssSelector("#contents > div > center > div > div.content > div > div.goods_pick_list > div > div > div > ul > div > li > div > div  a")).getAttribute("href");

                            CrawlingDto crawlingDto = CrawlingDto.builder()
                                    .image(image)
                                    .subject(subject)
                                    .price(price)
                                    .url(itemUrl)
                                    .itemMenu(itemMenu)
                                    .build();

                            allCrawlingData.add(crawlingDto);
                        } catch (Exception e) {
                            System.out.println("Error while processing item: " + e.getMessage());
                        }
                    }

                    // 다음 페이지로 이동
                    try {
                        if (currentPage >= maxPages) {
                            System.out.println("Reached maximum page count: " + maxPages);
                            hasNextPage = false; // 5페이지에 도달하면 크롤링 종료
                            break;
                        }

                        WebElement nextPageButton = driver.findElement(By.cssSelector("#contents > div > center > div > div.content > div > div.pagination > div > ul > li")); // 정확한 CSS Selector
                        if (nextPageButton.isDisplayed() && nextPageButton.isEnabled()) {
                            nextPageButton.click();
                            currentPage++; // 페이지 카운트 증가

                            // 페이지 로드 완료 대기
                            new WebDriverWait(driver, Duration.ofSeconds(15)).until(
                                    webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete")
                            );
                        } else {
                            hasNextPage = false; // 다음 페이지 버튼이 비활성화되면 루프 종료
                        }
                    } catch (Exception e) {
                        System.out.println("No more pages or error while navigating: " + e.getMessage());
                        hasNextPage = false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error during crawling: " + e.getMessage());
        } finally {
            driver.quit();
        }

        return allCrawlingData;
    }


    // 크롤링 데이터를 DB에 저장하는 메서드
    @Transactional
    public void saveCrawlingData(List<CrawlingDto> crawlingDtoList) {
        for (CrawlingDto dto : crawlingDtoList) {
            // 데이터 유효성 검사
            if (dto.getImage() == null || dto.getSubject() == null || dto.getPrice() == null) {
                System.out.println("Invalid data, skipping: " + dto);
                continue;
            }

            // 중복 데이터 확인 (상품명과 이미지 URL 기준)
            if (itemRepository.existsByItemNmAndImage(dto.getSubject(), dto.getImage())) {
                System.out.println("Duplicate data, skipping: " + dto);
                continue;
            }

            // Item 엔티티 생성 및 저장
            Item item = new Item();
            item.setItemNm(dto.getSubject());

            // 가격 변환 및 기본값 설정
            try {
                String numericPrice = dto.getPrice().replaceAll("[^0-9]", "");
                item.setPrice(!numericPrice.isEmpty() ? Integer.parseInt(numericPrice) : 0);
            } catch (NumberFormatException e) {
                item.setPrice(0);
            }

            item.setItemDetail("크롤링으로 생성된 상품입니다.");
            item.setStockNumber(100);
            item.setItemSellStatus(ItemSellStatus.SELL);
            // CrawlingDto의 itemMenu 값을 설정
            item.setItemMenu(dto.getItemMenu()); // 여기서 DTO의 itemMenu 값을 사용

            itemRepository.save(item);

            // ItemImg 엔티티 생성 및 저장
            ItemImg itemImg = new ItemImg();
            itemImg.setItem(item);
            itemImg.setImgName(dto.getImage().substring(dto.getImage().lastIndexOf("/") + 1)); // 파일 이름만 추출
            itemImg.setImgUrl(dto.getImage()); // 전체 이미지 URL 저장
            itemImg.setOriImgName("크롤링 이미지");
            itemImg.setRepImgYn("Y");
            itemImgRepository.save(itemImg);
        }
    }

    @Transactional(readOnly = true)
    public List<CrawlingDto> getCrawlingItems() {
        // 대표 이미지와 Item 데이터를 한 번에 가져오기
        List<ItemImg> itemImgs = itemImgRepository.findByRepImgYn("Y");
        List<CrawlingDto> crawlingDtoList = itemImgs.stream().map(itemImg -> {
            Item item = itemImg.getItem();
            return new CrawlingDto(
                    item.getId(),
                    itemImg.getImgUrl(), // 대표 이미지 URL
                    item.getItemNm(),
                    String.valueOf(item.getPrice()),
                    item.getUrl(), // URL 필드 추가
                    item.getItemMenu()
            );
        }).collect(Collectors.toList());

        return crawlingDtoList;
    }

    // itemId로 크롤링된 데이터를 조회하는 메서드
    public CrawlingDto getCrawlingItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .map(item -> CrawlingDto.builder()
                        .id(item.getId())
                        .image(item.getImage())
                        .subject(item.getItemNm())
                        .price(String.valueOf(item.getPrice()))
                        .url(item.getUrl())
                        .build())
                .orElse(null); // itemId에 해당하는 데이터가 없으면 null 반환
    }
    public List<CrawlingDto> getSavedData() {
        // 모든 엔티티 조회
        List<Item> entities = itemRepository.findAll(); // ItemRepository 사용
        List<CrawlingDto> dtoList = new ArrayList<>();
        for (Item entity : entities) {
            // 엔티티를 DTO로 변환
            CrawlingDto dto = CrawlingDto.builder()
                    .id(entity.getId())
                    .image(entity.getImage())
                    .subject(entity.getItemNm())
                    .price(String.valueOf(entity.getPrice()))
                    .url(entity.getUrl()) // Item에 URL 필드가 있어야 합니다
                    .build();
            dtoList.add(dto);
        }
        return dtoList;
    }


}
