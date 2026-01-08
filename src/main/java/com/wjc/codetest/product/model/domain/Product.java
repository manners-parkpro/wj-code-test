package com.wjc.codetest.product.model.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 설계 전반에 대한 보완 여지는 존재하나, 본 과제에서는 기본 전제를 유지한 상태에서 식별 가능한 문제점과 개선 포인트 위주로 리뷰를 진행하도록 했습니다.
 */

/**
 * 문제 : 가독성 및 Entity 설계 문제
 * 원인 : 코드 및 설계
 * 개선안
 * <p>
 *     1. Entity에서 @Setter사용은 절대로 안된다.
 *      1-1. Setter를 통해 예상치 못하게 데이터가 변경이 될수 있는 위험도가 굉장히 크다
 *
 *     2. @NoArgsConstructor(access = AccessLevel.PROTECTED), @AllArgsConstructor(access = AccessLevel.PRIVATE) 애노테이션 추가
 *      2-1. access = AccessLevel.PROTECTED : JPA는 접근 가능, 외부에서는 생성 불가하도록 하기 위해 PROTECTED 지정한다.
 *      또한, @NoArgsConstructor PRIVATE 으로 하게 되면 JPA 프록시 객체를 생성하지 않기 떄문에 PROTECTED 지정한다.
 *
 *      2-2. access = AccessLevel.PRIVATE : 모든 필드를 받는 생성자를 외부에서 호출 못 하게 차단하기 위함이다.
 *
 *     3. getter 중복 코드 및 코드화된 생성자 코드는 삭제를 하는것이 혼동이 없을거 같다.
 *      3-1. 위에 기재한 애노테이션으로 해결이 가능하다.
 * </p>
 */
@Entity
@Getter
@Setter
public class Product {

    @Id
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    /**
     * 문제 : Entity 설계 문제
     *      - ID 생성 전략이 DB 환경에 따라 다르게 동작하여 테스트 환경과 운영 환경 간 동작 불일치가 발생할 수 있다.
     *
     * 원인 : 코드 및 설계
     *      - GenerationType.AUTO 사용으로 인해 JPA가 DB Dialect에 따라 서로 다른 ID 생성 전략을 선택한걸로 보여진다.
     *
     * 개선안
     * <p>
     *     GenerationType.AUTO
     *     AUTO는 DB 환경에 따라 JPA의 ID 생성과 flush 동작이 달라져 테스트와 운영 간 동작 불일치를 만들 수 있어 실무에서는 사용하지 않는것이 바람직 하다.
     *
     *     GenerationType.IDENTITY(auto-increment ID)로 코드로 수정하고 설계를 변경하는것이 맞다고 판단된다.
     * </p>
     */
    private Long id;

    /**
     * 문제 : 설계 확장성에 대한 문제
     *      - 카테고리를 단순 문자열로 관리하여 도메인 확장 시 무결성 및 관리 포인트가 분산될 수 있다.
     *
     * 원인 : 코드 및 설계
     *      - 카테고리에 대한 도메인 개념이 명확히 분리되지 않고 단일 필드(String)로만 모델링된다.
     *
     * 개선안
     * <p>
     *     1. 카테고리에 대한 확장성이 전혀 고려되지 않고 있다고 판단이 된다.
     *     2. Enum 또는 @OneToMany 형태로 테이블을 만들어 관리 하는게 바람직 하다고 생각한다.
     *     3. 3. Column name이 같을경우 name = "category"은 삭제해도 무방하다.
     * </p>
     */
    @Column(name = "category")
    private String category;

    /**
     * 문제 : 필드 설계 문제
     *      - 필드 길이에 대한 제약이 명확하지 않아 데이터 무결성 및 저장 공간 관리 측면에서 불리하다.
     *
     * 원인 : 설계
     *      - 컬럼 제약 조건(length 등)이 설계 단계에서 정의되지 않음
     *
     * 개선안
     * <p>
     *     1. name에 대한 글자수 제한이 필요하다.
     *     2. length를 작성하지 않으면 불필요한 자원을 낭비하게 된다. @Column(name = "name", length = 100)
     *     3. Column name이 같을경우 name = "name"은 삭제해도 무방하다.
     * </p>
     */
    @Column(name = "name", length = 100)
    private String name;

    /**
     * ===== 제거 시작점
     */
    protected Product() {
    }

    public Product(String category, String name) {
        this.category = category;
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }
    /**
     * ===== 제거
     */

    /**
     * 실제 UpdateProductRequest.java 내부에 존재하던 로직을 Entity 계층에 추가하였습니다.
     *
     * 문제 : Entity 설계 및 코드 문제
     *      - Entity의 상태 변경 로직이 명확히 드러나지 않으면 외부에서 예상치 못한 필드 단위 수정이 발생할 수 있다.
     *
     * 원인 : 코드 및 설계
     *      - 도메인 상태 변경을 책임지는 메서드가 명확한 의도를 가진 형태로 정의되지 않음
     *      - 불필요하게 UpdateProductRequest.java에 명시되어 있어 일관성이 없으며, 협업관계에 혼동을 줄수 있는 가능성이 크다.
     *
     * 개선안
     * <p>
     *      업데이트 로직이 여러 계층에 분산되어 있어 비즈니스 규칙의 일관성이 깨질 수 있다.
     *      따라서, 상태 변경 및 비즈니스 규칙 검증을 도메인 계층으로 한정함으로써, 도메인 모델의 책임을 명확히 하고 설계 일관성을 반드시 유지해야 한다.
     * </p>
     */
    public void UpdateProductRequest(String category, String name) {
        this.category = category;
        this.name = name;
    }
}
