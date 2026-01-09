package com.wjc.codetest.product.model.response;

import com.wjc.codetest.product.model.domain.Product;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * <p>
 *
 * </p>
 *
 * @author : 변영우 byw1666@wjcompass.com
 * @since : 2025-10-27
 */

/**
 * 문제 : 가독성 및 설계에 대한 문제
 *      - @Setter로 인해 응답 객체가 외부에서 자유롭게 변경 가능이 가능하다.
 *      - List<Product> 형태로 JPA Entity를 직접 노출하여 보안 이슈, Lazy Loading 문제, 순환 참조 및 도메인 변경 시 API Spec 변경 위험이 존재한다.
 * 원인 : 코드
 *      - Lombok을 편의적으로 사용하면서 DTO의 역할을 고려하지 않고 있다.
 * 개선안
 * <p>
 *      1. Response DTO에서 @Setter를 제거하여 불변 객체로 설계하거나, 불변성을 보장할 수 있는 record type을 사용한다.
 *      2. JPA Entity를 직접 반환하지 않고, 별도의 ProductResponse DTO로 변환하여 반환한다.
 *      이를 통해 Lazy Loading, 순환 참조, Jackson 직렬화 문제를 방지하고, API 안정성과 가독성, 유지보수성을 향상시킬수 있다고 판단된다.
 * </p>
 */
@Getter
@Setter
public class ProductListResponse {
    private List<Product> products;
    private int totalPages;
    private long totalElements;
    private int page;

    /**
     * 문제 : 가독성에 대한 문제
     *      - 필드명과 생성자 파라미터명이 일관되지 않고 있다.
     *          products <-> content
     *          page<-> number
     *
     * 원인 : 코드
     *      - 코드 자체가 문서 역할을 전혀 하지 못하고 있다.
     *      - 유지보수 시 혼란을 유발 시킬수 있는 문제점이 있다.
     *
     * 개선안
     * <p>
     *      from 메소드를 사용하여, Page 구조 변경 시 Controller에 영항이 없도록 해야 한다고 생각됩니다.
     *      추가로, 응답에 대한 책임을 명확하게 분리하여 확장성 및 유지보수성을 향상시킬수 있는 가장 좋은 방법이라고 판단 됩니다.
     * </p>
     */
    public ProductListResponse(List<Product> content, int totalPages, long totalElements, int number) {
        this.products = content;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.page = number;
    }

    /**
     * 추가
     * Page 구조 변경 시 Controller 영향 없음
     * 응답에 대한 규격 책임분리
     */
    public static ProductListResponse from(Page<Product> page) {
        return new ProductListResponse(
                page.getContent(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getNumber()
        );
    }
}
