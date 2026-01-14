package com.wjc.codetest.product.model.request;

import lombok.Getter;
import lombok.Setter;

/**
 * 문제 : 가독성 및 DTO 설계에 대한 문제
 *      - @Setter 사용으로 인해 요청 객체가 외부에서 자유롭게 변경 가능하여, 요청 값의 무결성을 보장하기 어렵다고 판단된다.
 *      - 생성자 또는 명시적인 생성 방식이 없고, 페이징 정보(page, size)가 조회 조건(category)과 강하게 결합되어 있어 요청 DTO의 책임이 모호해지고 재사용성이 떨어진다.
 *
 * 원인 : 코드
 *      - Lombok을 편의적으로 사용하면서 요청 DTO의 역할과 책임을 충분히 고려하지 않고 있다.
 *      - 페이징 처리(Pageable)를 Spring이 제공하는 추상화 대신 직접 page, size 필드로 노출하여 계층 간 의존도가 높아졌다.
 *
 * 개선안
 * <p>
 *     1. @Setter를 제거하고 생성자 기반 또는 record type으로 DTO를 설계하여 요청 객체의 불변성을 확보한다.
 *     2. 페이징 정보는 요청 DTO에서 제거하고, Controller 계층에서 Pageable로 분리하여 처리한다.
 *
 *      public class Page {
 *          private int page;
 *          private int size;
 *      }
 *
 *      public class GetProductListRequest extends Page {
 *          private String category;
 *      }
 *
 *      3. 2번과 같이 처리 하는 것 외에, 위와 같이 역할과 책임을 명학히 분리 하여 재사용성을 높이고, 페이징 정보 규격 수정시 유연하게 수정할수 있도록 처리 하는것이 바람직 하다고 판단된다.
 * </p>
 */
@Getter
@Setter
public class GetProductListRequest {
    private String category;
    private int page;
    private int size;
}
