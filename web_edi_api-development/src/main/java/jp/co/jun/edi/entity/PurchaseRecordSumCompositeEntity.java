//PRD_0133 #10181 add JFE start
package jp.co.jun.edi.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

/**
 * 仕入実績一覧検索合計値結果格納用のEntity.
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class PurchaseRecordSumCompositeEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 件数合計. */
    @Id
    private int count;

    /** 数量合計. */
    //@Column(name = "fix_arrival_count_sum")
    private BigInteger fixArrivalCountSum;

    /** m級合計. */
    //@Column(name = "m_kyu_sum")
    private BigDecimal mKyuSum;

    /** 金額(単価*数量). */
    //@Column(name = "unit_price_sum_total")
    private BigInteger unitPriceSumTotal;
}
//PRD_0133 #10181 add JFE end