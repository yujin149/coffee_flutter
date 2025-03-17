package com.shop.repository;

import com.shop.entity.Item;
import com.shop.entity.ItemImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemImgRepository extends JpaRepository<ItemImg,Long> {
    List<ItemImg> findByItemIdOrderByIdAsc(Long itemId);

    void deleteByItemId(Long itemId);
    ItemImg findByItemIdAndRepImgYn(Long itemId, String repImgYn);

    List<ItemImg> findByItemId(Long itemId);

    List<ItemImg> findByRepImgYn(String repImgYn);

}
