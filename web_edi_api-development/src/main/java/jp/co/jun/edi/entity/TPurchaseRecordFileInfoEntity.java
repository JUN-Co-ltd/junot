//PRD_0133 #10181 add JFE start
package jp.co.jun.edi.entity;

import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.converter.PurchaseRecordFileStatusTypeConverter;
import jp.co.jun.edi.type.PurchaseRecordFileStatusType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 仕入実績ファイル情報のEntity.
 */
@Entity
@Table(name = "t_purchase_record_file_info")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class TPurchaseRecordFileInfoEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** ファイルID. */
    @Column(name = "file_no_id")
    private BigInteger fileNoId;

    /** ステータス. */
    @Convert(converter = PurchaseRecordFileStatusTypeConverter.class)
    private PurchaseRecordFileStatusType status;

    /** 検索条件. */
    @Column(name = "search_conditions")
    private String searchConditions;
}
//PRD_0133 #10181 add JFE end
