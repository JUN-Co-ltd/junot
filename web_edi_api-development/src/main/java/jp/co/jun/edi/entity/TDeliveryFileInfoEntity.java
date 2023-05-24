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
 * 納品依頼ファイル情報のEntity.
 */
@Entity
@Table(name = "t_delivery_file_info")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class TDeliveryFileInfoEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 納品ID. */
    @Column(name = "delivery_id")
    private BigInteger deliveryId;

    /** 納品依頼回数. */
    @Column(name = "delivery_count")
    private int deliveryCount;

    /** 発注ID. */
    @Column(name = "order_id")
    private BigInteger orderId;

    /** ファイルID. */
    @Column(name = "file_no_id")
    private BigInteger fileNoId;

    /** 公開日. */
    @Column(name = "published_at")
    private Date publishedAt;

    /** 公開終了日. */
    @Column(name = "published_end_at")
    private Date publishedEndAt;
}
