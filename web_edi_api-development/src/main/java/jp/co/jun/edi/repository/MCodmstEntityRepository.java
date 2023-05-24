// PRD_0123 #7054 add JFE start
package jp.co.jun.edi.repository;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.MCodmstEntity2;

/**
 * MCodmstEntityRepository.
 */
@Repository
public interface MCodmstEntityRepository extends CrudRepository<MCodmstEntity2, BigInteger> {

	//PRD_0136 #10671 mod start
//	@Query(value = "SELECT"
//			+ " c.* "
//			+ " FROM"
//			+ " t_item t"
//			+ " INNER JOIN"
//			+ " m_codmst2 c"
//			+ " ON"
//			+ " t.brand_code = c.code1"
//            + " WHERE"
//            + " t.id = :id", nativeQuery = true)
//	MCodmstEntity2 selectOneByCode1(@Param("id")BigInteger id);
    //PRD_0175 #10671 mod start
//	@Query(value = "SELECT"
//			+ " c.* "
//			+ " FROM"
//			+ " t_item t"
//			+ " INNER JOIN"
//			+ " m_codmst c"
//			+ " ON"
//			+ " t.brand_code = c.code1"
//			+ " AND c.tblid = 'TC'"
//			+ " AND c.code2 = 1"
//            + " WHERE"
//            + " t.id = :id", nativeQuery = true)
//	MCodmstEntity2 selectOneByCode1(@Param("id")BigInteger id);
	@Query(value = "SELECT"
			+ " c.* "
			+ " FROM"
			+ " t_item t"
			+ " INNER JOIN"
			+ " m_codmst c"
			+ " ON"
			+ " t.brand_code = c.code1"
            + " AND c.tblid = 'DL'"
            + " WHERE"
            + " t.id = :id", nativeQuery = true)
	MCodmstEntity2 selectOneByCode1(@Param("id")BigInteger id);
	//PRD_0136 #10671 mod start
	//PRD_0175 #10671 mod end
}
// PRD_0123 #7054 add JFE end