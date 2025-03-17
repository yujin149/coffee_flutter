package com.shop.repository;

import com.shop.dto.GallerySearchDto;
import com.shop.entity.Gallery;

import java.util.List;

public interface GalleryRepositoryCustom {
    List<Gallery> getAdminGalleryList(GallerySearchDto gallerySearchDto);
}
