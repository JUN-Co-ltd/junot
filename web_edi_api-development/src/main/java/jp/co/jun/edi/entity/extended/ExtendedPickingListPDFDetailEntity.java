package jp.co.jun.edi.entity.extended;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.converter.AllocationTypeConverter;
import jp.co.jun.edi.type.AllocationType;
import lombok.Data;

/**
 * ピッキングリストPDFフォーマットの明細Entity.
 *
 * ※ 明細とフッタ(合計行)で利用
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class ExtendedPickingListPDFDetailEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 店舗コード. */
    private String shpcd;

    /** 店舗名全角. */
    private String name;

    /** 配分出荷日. */
    @Column(name = "allocation_cargo_at")
    private Date allocationCargoAt;

    /** 配分区分. */
    @Column(name = "allocation_type")
    @Convert(converter = AllocationTypeConverter.class)
    private AllocationType allocationType;

}
