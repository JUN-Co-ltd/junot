package jp.co.jun.edi.entity;

import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 売上情報明細のEntity.
 */
@Entity
@Table(name = "t_pos_order_detail")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class TPosOrderDetailEntity extends GenericEntity {
	private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 営業日付. */
    @Column(name = "sales_date")
    private Date salesDate;

    /** 店舗コード. */
    @Column(name = "store_code")
    private String storeCode;

    /** 品番. */
    @Column(name = "part_no")
    private String partNo;

    /** カラーコード. */
    @Column(name = "color_code")
    private String colorCode;

    /** サイズコード. */
    @Column(name = "size_code")
    private String sizeCode;

    /** 売上数. */
    @Column(name = "sales_score")
    private int salesScore;
}
