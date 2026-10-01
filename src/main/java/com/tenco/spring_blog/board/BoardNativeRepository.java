package com.tenco.spring_blog.board;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor    // final 필드에 대한 생성자를 .class 생성 시 자동으로 생성. DI 처리
@Repository // IoC. 스프링이 데이터 접근 계층으로 인식. 데이터베이스 예외를 스프링 예외로 변환해 준다.
public class BoardNativeRepository {
    // EntityManager는 JPA의 핵심 인터페이스
    // 데이터베이스와 모든 작업을 담당(PrepareStatment, ResultSet 등)
    private final EntityManager em;

    // 글쓰기
    @Transactional  // 트랜잭션 기능
    public void save(String username, String title, String content){
        Query query = em.createNativeQuery("insert into board_tb(username, title, content, created_at)"
        + "values(?,?,?,now())");

        query.setParameter(1, username);
        query.setParameter(2, title);
        query.setParameter(3, content);

        query.executeUpdate();
    }

    // 게시글 전체 조회
    public List<Board> findAll() {
        String sql= """
                select * from board_tb order by id desc
                """;
        // Board.class를 선언하지 않으면 Object 타입으로 반환됨
        Query query = em.createNativeQuery(sql, Board.class);
        // while(rs.next) 기능
        return query.getResultList();
    }

    // 게시글 상세보기
    public Board findById(Long id) {
        String sql = """
                select * from board_tb where id = ?
                """;
        Query query = em.createNativeQuery(sql, Board.class);
        query.setParameter(1, id);  // 값 바인딩
        try {
            // 형 변환 필요
            return (Board) query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    // 삭제
    @Transactional
    public void deleteById(Long id) {
        String sql = """
                delete from board_tb where id = ?
                """;
        Query query = em.createNativeQuery(sql);
        query.setParameter(1, id);
        query.executeUpdate();
    }

    // 수정하기
    @Transactional
    public boolean updateById(String title, String content, Long id) {
        String sql = """
                update board_tb set title = ?, content = ? where id = ?
                """;
        Query query = em.createNativeQuery(sql);
        query.setParameter(1, title);
        query.setParameter(2, content);
        query.setParameter(3, id);
        int rows = query.executeUpdate();
        if(rows > 0 ){
            return true;
        }
        else {
            return false;
        }
    }
}
