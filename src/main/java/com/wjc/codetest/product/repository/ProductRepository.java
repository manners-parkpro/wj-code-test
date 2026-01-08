package com.wjc.codetest.product.repository;

import com.wjc.codetest.product.model.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * @param name
     * @param pageable
     *
     * 문제 : findAllByCategory지만, 파라미터명이 불일치
     * 해결 :
     */
    Page<Product> findAllByCategory(String name, Pageable pageable);

    /**
     * 문제 : 중복에 대한 카테고리를 등록하게끔 하는것이 문제
     * 해결 : List<String> findAllCategories();
     */
    @Query("SELECT DISTINCT p.category FROM Product p")
    List<String> findDistinctCategories();
}
