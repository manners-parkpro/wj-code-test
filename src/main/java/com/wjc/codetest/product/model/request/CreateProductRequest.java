package com.wjc.codetest.product.model.request;

import lombok.Getter;
import lombok.Setter;

/**
 * 문제 : 가독성, 에러처리, DTO 설계에 대한 문제
 * 원인 : 코드
 * 개선안
 * <p>
 *     1. JAVA17에서는 Record type 을 사용하는것을 권장한다.
 *      1-1. Record Type은 불변 데이터 전달을 전제로 한 타입이라 불필요한 애노테이션 없이 명확한 DTO를 표현하기에 적합하다.
 *
 *     2. implementation 'org.springframework.boot:spring-boot-starter-validation' 의존성 추가
 *      2-1. 의존성 추가로 @NotBlank, @NotNull, @NotEmpty를 사용하여 필수여부 체크 및 데이터 누락방지에 대한 명확성을 확보한다.
 * </p>
 */
@Getter
@Setter
public class CreateProductRequest {
    private String category;
    private String name;

    /**
     * 문제 : DTO 설계에 대한 문제
     * 원인 : 코드
     * 개선안
     * <p>
     *     1. @AllArgsConstructor, @NoArgsConstructor 애노테이션 추가 또는 Record Type으로 수정이 필요하다.
     *      1-1. 불필요한 생성자나 사용하지 않는 생성자는 협업관계에서 혼동을 불러일으킬수 있기 때문에 제거하는것이 바람직 하다.
     * </p>
     */
    public CreateProductRequest(String category) {
        this.category = category;
    }

    public CreateProductRequest(String category, String name) {
        this.category = category;
        this.name = name;
    }
}

