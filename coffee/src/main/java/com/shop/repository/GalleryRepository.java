package com.shop.repository;


import com.shop.entity.Gallery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface GalleryRepository extends JpaRepository<Gallery, Long> {

    // 갤러리 상태가 ON인 항목을 상단에 배치하고, 그 후 id로 내림차순 정렬
    @Query("SELECT g FROM Gallery g ORDER BY CASE WHEN g.galleryStatus = 'ON' THEN 1 ELSE 2 END, g.id DESC")
    Page<Gallery> findAllSortedByStatusAndId(Pageable pageable);

    // 제목이나 내용으로 검색하며, 갤러리 상태에 따라 정렬
    @Query("SELECT g FROM Gallery g WHERE (:searchType = 'all' AND (g.title LIKE %:keyword% OR g.content LIKE %:keyword%)) " +
           "OR (:searchType = 'title' AND g.title LIKE %:keyword%) " +
           "OR (:searchType = 'content' AND g.content LIKE %:keyword%) " +
           "ORDER BY CASE WHEN g.galleryStatus = 'ON' THEN 1 ELSE 2 END, g.id DESC")
    Page<Gallery> searchGalleryByKeyword(
        @Param("keyword") String keyword, 
        @Param("searchType") String searchType,
        Pageable pageable);

    // 특정 id를 기준으로 이전 게시글 찾기 (id가 작은 순서로)
    Gallery findFirstByIdLessThanOrderByIdDesc(Long id);

    // 특정 id를 기준으로 다음 게시글 찾기 (id가 큰 순서로)
    Gallery findFirstByIdGreaterThanOrderByIdAsc(Long id);

}
