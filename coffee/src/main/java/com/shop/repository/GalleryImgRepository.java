package com.shop.repository;

import com.shop.entity.GalleryImg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GalleryImgRepository extends JpaRepository<GalleryImg, Long> {
    List<GalleryImg> findByGalleryIdOrderByIdAsc(Long galleryId);
}
