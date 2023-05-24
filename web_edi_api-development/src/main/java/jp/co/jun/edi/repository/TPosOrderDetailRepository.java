package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jp.co.jun.edi.entity.TPosOrderDetailEntity;

/**
 * TPosOrderDetailRepository.
 */
@Repository
public interface TPosOrderDetailRepository extends JpaRepository<TPosOrderDetailEntity, BigInteger> {
    /**
     * 品番から直近1週間の売上数を取得.
     * @param partNo 品番
     * @param fromAt 営業日付(From)
     * @param toAt 営業日付(To)
     * @return 売上情報明細リスト
     */
    @Query(value="SELECT"
            + " t.id "
            + ",t.sales_date "
            + ",t.store_code "
            + ",t.part_no "
            + ",t.color_code "
            + ",t.size_code "
            + ",SUM(t.sales_score) sales_score "
            + ",t.created_at "
            + ",t.created_user_id "
            + ",updated_at "
            + ",updated_user_id "
            + ",deleted_at "
            + "FROM t_pos_order_detail t "
            + " WHERE t.part_no = :partNo "
            + " AND t.sales_date >= :fromAt "
            + " AND t.sales_date < :toAt "
            + " AND t.deleted_at is null "
            + "GROUP BY "
            + "  store_code "
            + ", part_no "
            + ", color_code "
            + ", size_code ", nativeQuery = true)
    List<TPosOrderDetailEntity> sumByPartNoDate(
            @Param("partNo") String partNo,
            @Param("fromAt") Date fromAt,
            @Param("toAt") Date toAt);
}
