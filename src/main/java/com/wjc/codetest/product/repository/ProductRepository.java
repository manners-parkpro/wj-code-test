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
     * 문제 : 성능 및 가독성에 대한 문제
     *      - 메서드명은 findAllByCategory 이지만, 파라미터명이 name으로 선언되어 있어 의미가 불일치하다.
     *      - 메서드명을 보았을 때 조회 조건이 명확하지 않아 가독성과 오해의 소지가 있다.
     *
     * 원인 : 코드
     *      - 도메인 필드명(category)과 파라미터 명명 규칙을 일관되게 적용하지 않고 있다.
     *      - 메서드 네이밍과 시그니처 간 의미 정합성에 대한 고려가 부족 하다고 판단된다.
     *
     * 개선안
     * <p>
     *     Page<Product> findAllByCategory(String category, Pageable pageable);
     *
     *     parameter를 도메인 필드명과 동일하게 category로 수정하여 메서드의 의도를 명확히 표현하는 것이 가장 바람직하다고 판단된다.
     * </p>
     */
    Page<Product> findAllByCategory(String name, Pageable pageable);

    @Query("SELECT DISTINCT p.category FROM Product p")
    List<String> findDistinctCategories();
}
