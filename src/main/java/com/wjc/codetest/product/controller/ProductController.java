package com.wjc.codetest.product.controller;

import com.wjc.codetest.product.model.request.CreateProductRequest;
import com.wjc.codetest.product.model.request.GetProductListRequest;
import com.wjc.codetest.product.model.domain.Product;
import com.wjc.codetest.product.model.request.UpdateProductRequest;
import com.wjc.codetest.product.model.response.ProductListResponse;
import com.wjc.codetest.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ProductController {
    /**
     * 문제 : 가독성 및 성능 이슈에 대한 문제
     *      - 단일 ProductService가 조회와 변경 책임을 동시에 가지며, 서비스 확장 시 비즈니스 로직의 응집도가 저하될 가능성이 있음
     *
     * 원인 : 코드
     *      - 조회(Query)와 변경(Command)에 대한 책임이 하나의 Service에 집중된 구조로 설계됨
     *
     * 개선안
     * <p>
     *     service가 확장되거나, 변경이 이루어질때 너무 많은 책임을 하나의 service에 두는것이 아니라.
     *     ProductQueryService : 조회만 실행하는 service
     *     ProductCommandService : 생성, 수정만 하는 service
     *
     *     위와 같이 분리하여 가독성 및 예상치 못한 side-effect를 방지한다.
     * </p>
     */
    private final ProductService productService;

    /**
     * 문제 : 성능 및 보안에 대한 문제
     *      - Entity를 직접 반환할 경우 연관관계 확장 시 N+1 문제가 컨트롤러 계층에서 노출될 가능성이 있다.
     *      - API 응답을 통해 내부 도메인 모델이 외부로 노출된다.
     *
     * 원인 : 코드
     *      - Controller 계층에서 Entity를 그대로 반환하도록 설계되어 표현 계층과 도메인 계층의 역할 분리가 명확하지 않다.
     *
     * 개선안
     * <p>
     *     Product Entity에서 연관관계가 복잡하지 않아 N + 1 발생이 일어날 확률은 적지만, 연관관계가 생기게 되면 N + 1 문제 발생
     *     추가로, Entity를 바로 반환하는것은 설계자체를 공개하는 것 이기때문에, 보안상 너무 위험하다고 판단한다.
     *
     *     때문에, ResponseType을 Entity가 아닌 공통적으로 사용할수 있는 ResponseDTO 생성 후 반환 하는것이 바람직하다.
     * </p>
     */
    @GetMapping(value = "/get/product/by/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable(name = "productId") Long productId){
        Product product = productService.getProductById(productId);
        return ResponseEntity.ok(product);
    }

    /**
     * 문제 : 성능 및 보안에 대한 문제
     *      - 생성 API에서도 Entity를 직접 반환하여 도메인 구조 변경 시 API 스펙이 함께 변경될 위험이 있다.
     *
     * 원인 : 코드
     *      - 응답 타입에 대한 명확한 책임 정의 없이 Entity를 그대로 외부에 노출하는 설계 선택이 잘못되었다고 판단한다.
     *
     * 개선안
     * <p>
     *     35 ~ 42번 Line 설명과 비슷한 문제로,
     *     위에 주석과 같이 Entity를 반환하지 않고, ResponseDTO 생성 후 반환 하는것이 바람직하다.
     * </p>
     */
    @PostMapping(value = "/create/product")
    public ResponseEntity<Product> createProduct(@RequestBody CreateProductRequest dto){
        Product product = productService.create(dto);
        return ResponseEntity.ok(product);
    }

    /**
     * 문제 : RestAPI 설계에 대한 문제
     *      - 삭제 API가 POST로 정의되어 HTTP Method의 의미가 불분명하다고 판단된다.
     *      - Boolean 응답은 삭제 결과에 대한 명확한 의미를 전달하지 못한다고 보인다
     *
     * 원인 : 설계 및 코드
     *      - REST API 설계 시 HTTP Method와 Resource 행위 간 매핑이 고려되지 않고 있다고 보여진다.
     *
     * 개선안
     * <p>
     *     1. @DeleteMapping 으로 수정하여 해당 Mapping이 무엇을 하는지에 대한 명확성 및 책임을 확보한다.
     *     2. 삭제 API에서 별도의 return 은 필요하지 않을거 같은 판단이 든다.
     *        또한, 비지니스로직에서 에러 처리를 이미 하고 있기 때문에 아래와 같이 수정하는것이 바람직 하다고 판단된다.
     *
     *     public void deleteProduct(@PathVariable(name = "productId") Long productId){
     *         productService.deleteById(productId);
     *     }
     * </p>
     */
    @PostMapping(value = "/delete/product/{productId}")
    public ResponseEntity<Boolean> deleteProduct(@PathVariable(name = "productId") Long productId){
        productService.deleteById(productId);
        return ResponseEntity.ok(true);
    }

    /**
     * 문제 : RestAPI 설계에 대한 문제
     *      - 수정 API가 POST로 정의되어 리소스 변경 의도가 명확하지 않다고 판단된다.
     *
     * 원인 : 설계 및 코드
     *      - REST API 설계 시 HTTP Method와 Resource 행위 간 매핑이 고려되지 않고 있다고 보여진다.
     *
     * 개선안
     * <p>
     *     1. @PatchMapping 으로 수정하여 해당 Mapping이 무엇을 하는지에 대한 명확성 및 책임을 확보한다.
     *     2. 조금 더 Rest 하게 처리를 할수 있도록 /update/product/{id} 형태로 값을 받아서 처리하는것이 바람직하다.
     *      2-1. productService.update(id, dto);
     *     3. ResponseType을 Entity가 아닌 공통적으로 사용할수 있는 ResponseDTO 생성 후 반환 하는것이 바람직하다.
     * </p>
     */
    @PostMapping(value = "/update/product")
    public ResponseEntity<Product> updateProduct(@RequestBody UpdateProductRequest dto){
        Product product = productService.update(dto);
        return ResponseEntity.ok(product);
    }

    /**
     * 문제 : RestAPI 설계에 대한 문제
     *      - 조회 API임에도 POST + RequestBody를 사용하여 API 의도가 직관적으로 드러나지 않고 있다.
     *      - Controller에서 페이징 및 응답 조립 책임이 과도하게 설계되어 있다.
     *
     * 원인 : 설계 및 코드
     *      - 조회 API에 대한 REST 설계 원칙(Get + QueryParam)이 일관되게 적용되지 않는 걸로 보여진다.
     *      - DTO 변환 책임이 Controller에 위치해 있다.
     *
     * 개선안
     * <p>
     *     1. @GetMapping 으로 변경 및 @RequestBody 삭제
     *     2. 아래와 같이 수정하는것이 바람직 하다.
     *      2-1. Controller 에서 너무 많은 비지니스 로직이 존재하는걸로 보인다. 때문에, service단에 pageable을 parameter로 넘겨 service에서 처리 하도록 수정하는것이 바람직 하다.
     *      2-2. ProductListResponse에 from 정적 메소드 추가하여 Controller 코드의 가독성, DTO 책임에 명확성, 변환 규칙 변경 시 영향 최소화를 확보한다.
     *
     *     @GetMapping("/product/list")
     *     public ResponseEntity<ProductListResponse> getProductListByCategory(
     *             @RequestParam String category,
     *             @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.DESC) Pageable pageable) {
     *         Page<Product> page = productService.getProductListByCategory(category, pageable);
     *         return ResponseEntity.ok(ProductListResponse.from(page));
     *     }
     *
     *     3. Method Name과 Service layout method Namaing 규칙 불일치하여 혼동을 최소화 하기 위해 동일하게 맞춰주는것이 바람직하다.
     * </p>
     */
    @PostMapping(value = "/product/list")
    public ResponseEntity<ProductListResponse> getProductListByCategory(@RequestBody GetProductListRequest dto){
        Page<Product> productList = productService.getListByCategory(dto);
        return ResponseEntity.ok(new ProductListResponse(productList.getContent(), productList.getTotalPages(), productList.getTotalElements(), productList.getNumber()));
    }

    /**
     * 문제 : 가독성에 대한 문제
     *      - Controller 메서드명과 Service 메서드명이 일관되지 않아 호출 관계를 한눈에 파악하기 어려움이 존재한다.
     *
     * 원인 : 코드
     *      - 계층 간 네이밍 규칙이 명확히 정의되지 않고있다.
     *
     * 개선안
     * <p>
     *     Controller Method Name과 Service layout method Namaing 규칙 불일치하여 혼동을 최소화 하기 위해 동일하게 맞춰주는것이 바람직하다.
     * </p>
     */
    @GetMapping(value = "/product/category/list")
    public ResponseEntity<List<String>> getProductListByCategory(){
        List<String> uniqueCategories = productService.getUniqueCategories();
        return ResponseEntity.ok(uniqueCategories);
    }
}
