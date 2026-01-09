package com.wjc.codetest.product.service;

import com.wjc.codetest.product.model.request.CreateProductRequest;
import com.wjc.codetest.product.model.request.GetProductListRequest;
import com.wjc.codetest.product.model.domain.Product;
import com.wjc.codetest.product.model.request.UpdateProductRequest;
import com.wjc.codetest.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    /**
     * [ Service ] 비지니스 로직에서 전체적인 문제점.
     *
     * 문제 : 가독성 및 성능에 대한 문제
     *      - 서비스 계층에 트랜잭션 경계가 명시되지 않아 다수의 Repository 호출 시 데이터 정합성 및 성능 저하 가능성이 존재한다.
     *
     * 원인 : 코드
     *      - 비즈니스 로직을 수행하는 Service 계층에서 @Transactional을 통한 트랜잭션 관리 책임이 명확히 정의되지 않고 있다.
     *
     * 개선안
     * <p>
     *     전체적으로, @Transactional에 대한 처리가 존재하지 않는다.
     *     @Transactional을 사용하지 않으면, 데이터 정합성, 영속성 컨텍스트, 지연 로딩, 예외 롤백 측면에서 의도하지 않은 동작이나 에러가 발생할 수 있다.
     *     때문에, DB Write는 @Transactional, DB Read는 @Transactional(readOnly = true) 애노테이션을 반드시 작성해야 한다.
     * </p>
     */

    private final ProductRepository productRepository;

    /**
     * 문제 : 가독성 및 성능에 대한 문제
     *      - 중복된 카테고리 또는 상품명에 대한 검증이 없어, 중복된 데이터가 발생될 수 있다.
     *
     * 원인 : 코드
     *      - 도메인 레벨 또는 애플리케이션 레벨에서 중복 검증 책임이 명확히 정의되지 않음
     *
     * 개선안:
     *      1. category + name 기준의 중복 검증 로직을 별도 validation 메서드로 분리
     *          1-1. DB unique 제약과 병행하여 비즈니스 규칙을 명확히 표현
     *      2. Product에 멤버변수가 많아 지는 케이스를 고려하여 @Builder를 사용하여 데이터 저장하는것을 지향한다.
     *          2-1. 유연성을 확보할 수 있다.
     *          2-2. 가독성을 높일 수 있다.
     *          2-3. 변경 가능성을 최소화할 수 있다.
     */
    public Product create(CreateProductRequest dto) {
        Product product = new Product(dto.getCategory(), dto.getName());
        return productRepository.save(product);
    }

    /**
     * 문제 : 가독성에 대한 문제
     *      - Optional을 명시적으로 분기 처리하여 코드 흐름이 복잡해지고 핵심 로직 파악에 불필요한 리소스가 발생한다.
     *      - 조회 결과가 없을 경우의 예외 처리 의도가 코드에서 즉시 드러나지 않고있다.
     *
     * 원인 : 코드
     *      - Optional이 제공하는 API를 활용하지 않고 isPresent / get 기반의 절차적 처리 방식으로 구현되어 있다.
     *
     * 개선안
     * <p>
     *     public Product findById(Long productId) {
     *         return productRepository.findById(productId).orElseThrow(() -> new RuntimeException("product not found"));
     *     }
     *
     *     아래코드를 위와같이 함수형 인터페이스를 람다 표현식으로 구현함으로써 가독성 향상을 얻을수 있다고 판단됩니다..
     *     추가로, 근본적으로 Product Entity를 그대로 반환하는것은 보안에 위험이 크고 설계를 노출시킬수 있다.
     *     때문에, 별도의 ResponseDTO로 변환하여 return 하는것이 가장 적합하다고 판단이 됩니다.
     * </p>
     */
    public Product getProductById(Long productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (!productOptional.isPresent()) {
            throw new RuntimeException("product not found");
        }
        return productOptional.get();
    }

    /**
     * 문제 : 가독성 및 코드에 대한 문제
     *      - Setter 기반 상태 변경으로 도메인의 불변식이 보장되지 않는다.
     *      - 변경 의도가 코드에 명확히 드러나지 않고 있다.
     *
     * 원인 : 코드
     *      - 도메인 상태 변경을 일반 Setter에 위임하여 책임이 서비스 계층으로 분산되어 있어, 유지보수 및 테스트에 대한 용이성이 어려워진다.
     *
     * 개선안
     * <p>
     *
     *     @Transactional
     *     public Product update(Long productId, UpdateProductRequest dto) {
     *         Product product = getProductById(productId);
     *         product.UpdateProductRequest(dto.getName(), dto.getCategory());
     *         return productRepository.save(product);
     *     }
     *
     *     1. Setter는 도메인의 상태를 무의미하게 변경할 수 있어 불변식을 깨뜨리고 책임을 서비스로 분산시키기 때문에,
     *        상태 변경이나 수정은 도메인 계층의 명시적인 행위로만 허용하는 것이 맞다고 판단된다.
     *        따라서, 위에 기재해둔 소스와 같이 수정이 필요하다.
     *
     *     2. update 이후 Entity를 반환하는건 보안상 문제가 발생할수 있고 연관관계가 많을시 예기치 못한 버그가 발생활 확률이 높다.
     *        따라서, 별도의 ResponseDTO 생성 후 반환하는게 맞다고 판단된다.
     * </p>
     */
    public Product update(UpdateProductRequest dto) {
        Product product = getProductById(dto.getId());
        product.setCategory(dto.getCategory());
        product.setName(dto.getName());
        Product updatedProduct = productRepository.save(product);
        return updatedProduct;

    }

    public void deleteById(Long productId) {
        Product product = getProductById(productId);
        productRepository.delete(product);
    }

    /**
     * 문제 : 가독성에 대한 문제
     *      - 서비스 계층에서 페이징 객체(PageRequest)를 직접 생성하여 조회 로직과 페이징 정책이 강하게 결합되어 있어,
     *        페이징/정렬 정책 변경 시 서비스 코드 수정이 반드시 되야 되고, 영향도 범위가 넓을거라 판단된다.
     *
     * 원인 : 코드
     *      - Pageable과 PageRequest의 역할을 구분하지 않고 서비스 계층이 페이징 생성 책임까지 담당하고 있기 때문에,
     *        결과적으로 재사용성 저하 및 테스트 시 Pageable 조합이 어려워진다.
     *
     * 개선안
     * <p>
     *     public Page<Product> getListByCategory(GetProductListRequest dto, Pageable pageable) {
     *         return productRepository.findAllByCategory(dto.getCategory(), pageable);
     *     }
     *
     *     1. Controller <-> Service 간 책임 경계가 명확하지 않기때문에, 명확히 구분 하여 재사용성, 테스트 용이성, 가독성을 함께 향상시킨다.
     *
     *     2. 페이징 조건의 생성 책임을 Controller 이동하여, Service와 Repository는 추상 타입인 Pageable만 의존하도록 변경하는 것이 맞다고 판단된다.
     *      2-1. 로직과 페이징/정렬 정책을 분리하고, 페이징 정책 변경 시 서비스 코드 수정 없이 확장 가능하도록 구조를 개선한다.
     * </p>
     */
    public Page<Product> getListByCategory(GetProductListRequest dto) {
        PageRequest pageRequest = PageRequest.of(dto.getPage(), dto.getSize(), Sort.by(Sort.Direction.ASC, "category"));
        return productRepository.findAllByCategory(dto.getCategory(), pageRequest);
    }

    public List<String> getUniqueCategories() {
        return productRepository.findDistinctCategories();
    }
}
