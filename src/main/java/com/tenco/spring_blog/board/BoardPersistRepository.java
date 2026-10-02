package com.tenco.spring_blog.board;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * 영속성 컨텍스트를 활용한 Repository 클래스 만들기
 * 소프트웨어에서는 데이터를 저장하고 관리하는 곳을 추상화한 개념
 */

@Repository // IoC + 싱글톤
@RequiredArgsConstructor    // final 필드 초기화 처리
public class BoardPersistRepository {

    private final EntityManager em;

    @Transactional
    public void updateById(Long id, BoardRequest.updateDto reqDto) {
        // 1. 수정할 엔티티를 먼저 조회 후 영속 상태로 만듬
        Board boardEntity = em.find(Board.class, id);

        // 2. 엔티티 존재 여부 확인
        if(boardEntity == null){
            throw new IllegalArgumentException("수정할 게시글을 찾을 수 없습니다.");
        }

        // 3. 엔티티 객체 상태 변경 중
        // boardEntity.setTitle(reqDto.getTitle());
        // boardEntity.setContent(reqDto.getContent());

        // 4. 1차 캐시에 저장된 엔티티 객체의 내부 상태값이 변경되고 트랜잭션이 종료되면 더티 채킹(Dirty Checking)이 발생
        // 더티 채킹 : 현재와 DB가 다른 경우 업데이트를 자동으로 해줌

        boardEntity.update(reqDto);
    }


    // 게시글 삭제하기 (영속성 컨텍스트를 활용한 안전한 삭제)
    // 삭제 과정
    // board 엔티티가 영속 --> 삭제로 변경
    // 1차 캐시에서 해당 엔티티 제거
    // 트랜잭션 커밋 시점에 DELETE SQL이 자동 실행
    @Transactional
    public void deleteById(Long id){
        // 1. 삭제할 엔티티를 영속 상태로 조회
        Board boardEntity = em.find(Board.class, id);

        // 2. 엔티티 존재 여부 확인 (안전한 삭제)
        if(boardEntity == null){
            throw new IllegalArgumentException("삭제할 게시글을 찾을 수 없습니다.");
        }

        // 3. 영속 상태의 엔티티를 삭제 상태로 변경
        em.remove(boardEntity);
    }

    // 기본키로 게시글 단건 조회(1차 캐시 활용)
    public Board findById(Long id){
        // find() 특징
        // 1. 기본키로만 조회 가능
        // 2. 1차 캐시에 먼저 찾기를 시도
        // 3. 없으면 DB에서 조회 후 1차 캐시에 저장
        // 4. 영속 상태로 만든 후 반환
        Board board = em.find(Board.class, id);

        return board;
    }

    // JPQL을 사용한 방법 조회
    // JPQL 단점
    // 1. 1차 캐시를 우회하여 항상 DB에 접근
    // 2. 코드가 복잡할 수 있다.
    // 3. getSingleResult()에 예외 처리가 필요
    public Board findByIdwithJPQL(Long id){
        // :id = ? 같은 기능
        String jpql = """
                SELECT b FROM Board b WHERE b.id = :id
                """;
        try{
            // Qurey query = em.createQuery("select b from Board b where b.id = :id");
            // query.setParameter("id", id);
            // query.executeUpdate();
            return em.createQuery(jpql, Board.class)
                    .setParameter("id",id)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    // JPQL을 사용한 게시글 목록 조회
    // JPQL : 엔티티 객체를 대상으로 하는 객체지향 쿼리
    // Board는 엔티티 클래스명, b 별칭
    // 테이블명(board_tb)가 아닌 엔티티명(Board)를 사용
    public List<Board> findAll(){
        String jpql = """
                SELECT b FROM Board b ORDER BY b.createdAt DESC
                """;
        // createQuery() : JQPL 쿼리를 생성
        // 두 번째 매개변수 : 반환 타입을 지정(타입 안정성 확보)
        // getResultList() : List<Board>로 반환
        return em.createQuery(jpql, Board.class).getResultList();
    }

    // 게시글 저장 기능
    @Transactional
    // 1. 매개변수로 받은 board는 이 시점에서 비영속 상태다.
    // 아직 영속성 컨텍스트에 의해 관리되지 않는 상태다.
    // 데이터베이스와 연관 없는 순수 자바 객체 상태다.
    public Board save(Board board) {
        // 2. em.persist(board)를 호출하면 엔티티가 영속 상태가 된다.
        // 영속성 컨텍스트가 board 객체를 관리하기 시작한다.
        // 이때 1차 캐시에 저장된다.
        // 실제 INSERT SQL은 일반적으로 아직 DB에 실행되지 않는다.
        // INSERT SQL은 쓰기 지연 저장소에 등록된다.
        em.persist(board);

        // 3. 여기서는 아직 트랜잭션이 커밋되지 않았다.
        // 따라서 일반적인 경우 INSERT SQL이 아직 DB에 반영되지 않았다.
        // return board는 영속 상태의 board 객체를 반환한다.
        return board;

        // 4. save() 메서드가 끝난 뒤 트랜잭션 커밋 시점에
        // 영속성 컨텍스트의 변경 내용이 flush되고 INSERT SQL이 DB에 실행된다.
        // ID 생성 전략에 따라 board.id는 이미 persist() 과정에서 할당될 수도 있다.
    }

    // 엔티티의 영속 상태 4가지
    // 1. 비영속 상태 : 새로 생성된 객체, 영속성 컨텍스트와 무관
    // 2. 영속 상태 : 영속성 컨텍스트에 관리되는 상태
    // 3. 준영속 상태 : 영속성 컨텍스트에서 분리된 상태
    // 4. 삭제 상태 : 삭제 예정 상태(트랜잭션 커밋 시 DELETE 쿼리 실행할 때)
    private void entityLifecycleEX(){
        // 비영속 상태
        Board board = new Board("제목", "내용", "작성자");

        // 영속 상태
        em.persist(board);

        // 준영속 상태
        em.detach(board);

        // 삭제 상태
        em.remove(board);
    }
}
