// PRD_0123 #7054 add JFE start
package jp.co.jun.edi.repository;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.MDeliverylocationEntity;

/**
 * MDeliverylocationRepository.
 */
@Repository
public interface MDeliverylocationRepository extends CrudRepository<MDeliverylocationEntity, BigInteger>{

	@Query(value = "SELECT"
			+ " dl.company_name"
			+ " FROM"
			+ " m_delivery_destination dd"
			+ " INNER JOIN"
			+ " m_delivery_location dl"
			+ " ON"
			+ " dd.delivery_location_id = dl.id"
            + " WHERE"
            + " dd.logistics_code = :logisticsCode", nativeQuery = true)
	String selectCompanyNameByLogisticsCode (@Param("logisticsCode")String logisticsCode);
}
// PRD_0123 #7054 add JFE end