package com.wjc.codetest.product.model.request;

import lombok.Getter;
import lombok.Setter;

/**
 * 문제 : 가독성, 에러처리, 보안, DTO 설계에 대한 문제
 * 원인 : 코드
 * 개선안
 * <p>
 *     1. JAVA17에서는 Record type 을 사용하는것을 권장한다.
 *      1-1. Record Type은 불변 데이터 전달을 전제로 한 타입이라 불필요한 애노테이션 없이 명확한 DTO를 표현하기에 적합하다.
 *
 *     2. implementation 'org.springframework.boot:spring-boot-starter-validation' 의존성 추가
 *      2-1. 의존성 추가로 @NotBlank, @NotNull, @NotEmpty를 사용하여 필수여부 체크 및 데이터 누락방지에 대한 명확성을 확보한다.
 *
 *    3. @AllArgsConstructor, @NoArgsConstructor 애노테이션 추가
 *     3-1. 불필요한 생성자로 인해 예상치 못한 데이터 수정이 발생할 가능성이 매우 높다.
 *     3-2. 모든 UpdateProductRequest에 parameter에서 id는 모두 제거 하고, Entity계층에서 제일 하단에 추가해둔 생성자만 있어도 충분하다고 판단된다.
 * </p>
 */

@Getter
@Setter
public class UpdateProductRequest {
    private Long id;
    private String category;
    private String name;

    /**
     * ===== 제거 시작점
     */
    public UpdateProductRequest(Long id) {
        this.id = id;
    }

    public UpdateProductRequest(Long id, String category) {
        this.id = id;
        this.category = category;
    }

    public UpdateProductRequest(Long id, String category, String name) {
        this.id = id;
        this.category = category;
        this.name = name;
    }
    /**
     * ===== 제거
     */
}

